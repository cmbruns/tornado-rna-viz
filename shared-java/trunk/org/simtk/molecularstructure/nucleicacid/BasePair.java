/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on May 1, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.util.*;

/** 
 * @author Christopher Bruns
 * 
 * Represents a base-pair interaction between two residues in a nucleic acid structure
 */
public class BasePair 
implements Iterable<Residue>, SecondaryStructure
{
    Nucleotide residue1;
    Nucleotide residue2;
    protected String source;
    protected Biopolymer parentMolecule;
    protected EdgeType edge_1;
    protected EdgeType edge_2;
    protected BondOrientation bond_orient;
    protected String strand_orient;
    
    public enum EdgeType {
    	WATSONCRICK(Arrays.asList("w","+","-")), 
    	HOOGSTEEN(Arrays.asList("h")),
    	SUGAR(Arrays.asList("s")),
    	UNKNOWN(Arrays.asList("!"));
    	
    	public final List<String> designations;
    	EdgeType(List<String> des){ this.designations = des; }

        public static EdgeType getEdgeType(String str){
        	for (EdgeType et: EdgeType.values()){
        		if (et.designations.contains(str.toLowerCase())){
        			return et;
        		}
        	}
        	throw new IllegalArgumentException();
        }
    };

    public enum BondOrientation {
    	CIS(Arrays.asList("cis","c")), 
    	TRAN(Arrays.asList("tran","t")),
    	UNKNOWN(Arrays.asList("!"));
    	
    	public final List<String> designations;
    	BondOrientation(List<String> des){ this.designations = des; }

        public static BondOrientation getBondOrienation(String str){
        	for (BondOrientation bo: BondOrientation.values()){
        		if (bo.designations.contains(str.toLowerCase())){
        			return bo;
        		}
        	}
        	throw new IllegalArgumentException();
        }
    };


    public static BasePair getBasePair(Residue r1, Residue r2, String source){
    	if ((r1!=null)&&(r2!=null)){
    		for (SecondaryStructure struc: r1.secondaryStructures()){
    			if (struc instanceof BasePair){
    				BasePair strucBP = (BasePair) struc;
    				if ((source=="*" || source==strucBP.getSource()) && (strucBP.isBetween(r1, r2))){
    					return strucBP;
    				}
    			}
    		}
    	}
    	return null;
    }
    
    public static BasePair makeBasePair(Residue r1, Residue r2, String source){
        if ((r1!=null)&&(r2!=null)){
            if ((r1 instanceof Nucleotide)&& (r2 instanceof Nucleotide)){
            	BasePair thisBP = getBasePair(r1, r2, source);
            	if (thisBP==null) {
            		thisBP = new BasePair((Nucleotide)r1,(Nucleotide)r2);
                	thisBP.setSource(source);
            	}
                return thisBP;
            }
            else {
                if (!(r1 instanceof Nucleotide)){
                    System.out.println("unrecognized residue reported by "+source+" as basepaired: "+ r1);
                }
                if (!(r2 instanceof Nucleotide)){
                    System.out.println("unrecognized residue reported by "+source+" as basepaired: "+ r2 );
                }
            }
        }
        return null;
    }
    
    public BasePair(Nucleotide r1, Nucleotide r2) {
        if (r1 == null) throw new NullPointerException();
        if (r2 == null) throw new NullPointerException();
        
        // Put the two bases in a deterministic order
        int r1Index = r1.getResidueNumber();
        int r2Index = r2.getResidueNumber();
        char r1Code = r1.getInsertionCode();
        char r2Code = r2.getInsertionCode();

        // Put lowest number first
        if (r1Index > r2Index) {
            residue1 = r2;
            residue2 = r1;
        }
        else if (r2Index > r1Index) {
            residue1 = r1;
            residue2 = r2;
        }
        // If number is the same, sort on insertion code
        else if (r1Code > r2Code) {
            residue1 = r2;
            residue2 = r1;
        }
        else {
            residue1 = r1;
            residue2 = r2;
        }
        r1.addSecondaryStructure(this);
        r2.addSecondaryStructure(this);
        return;            
    }
    
    public String getSource() {return this.source;}
    public void setSource(String source) {this.source = source;}
    
    public BondOrientation getBond_orient() { return bond_orient; }
	public void setBond_orient(BondOrientation bond_orient) { this.bond_orient = bond_orient; }
	public void setBond_orient(String bond) { this.bond_orient = BondOrientation.getBondOrienation(bond); }

	public EdgeType getEdge_1() { return edge_1; }
	public EdgeType getEdge_2() { return edge_2; }
	public void setEdge(Residue res, EdgeType edgeT) { 
		if (res.equals(this.residue1)) {
			this.edge_1 = edgeT; 
		}
		else if (res.equals(this.residue2)) {
			this.edge_2 = edgeT; 
		}
		else throw new IllegalArgumentException();
	}
	public void setEdge(Residue res, String edge) { setEdge(res, EdgeType.getEdgeType(edge)); }
	
//	public void setEdge_1(EdgeType edge_1) { this.edge_1 = edge_1; }
//	public void setEdge_1(String edge) { this.edge_1 = EdgeType.getEdgeType(edge); }

//	public void setEdge_2(EdgeType edge_2) { this.edge_2 = edge_2; }
//	public void setEdge_2(String edge) { this.edge_2 = EdgeType.getEdgeType(edge); }

	public String getStrand_orient() { return strand_orient; }
	public void setStrand_orient(String strand_orient) { this.strand_orient = strand_orient; }

	public void addResidue(Residue r) {
        throw new UnsupportedOperationException();
    }
    public Iterator<Residue> getResidueIterator() {
        return iterator();
    }
    public Collection<Residue> residues() {
        Vector<Residue> answer = new Vector<Residue>();
        answer.add(residue1);
        answer.add(residue2);
        return answer;
    }
    
    public void setMolecule(Biopolymer molecule) {this.parentMolecule = molecule;}
    
    public Nucleotide getResidue1() {return residue1;}
    public Nucleotide getResidue2() {return residue2;}

    public Plane3D getBasePlane() 
    throws InsufficientAtomsException
    {
        // 1) compute best plane containing base group atoms
        Set<Vector3D> planeAtoms = new HashSet<Vector3D>();
        if (residue1 != null) {
            LocatedMolecule base = residue1.get(Nucleotide.baseGroup);
            if (base != null) {
                for (LocatedAtom a : base.atoms()) planeAtoms.add(a.getCoordinates());
            }
        }
        
        if (residue2 != null) {
            LocatedMolecule base = residue2.get(Nucleotide.baseGroup);
            if (base != null) {
                for (LocatedAtom a : base.atoms()) planeAtoms.add(a.getCoordinates());
            }
        }
        
        if (planeAtoms.size() < 1) return null;

        Plane3D basePairPlane;
        try {basePairPlane = Plane3D.bestPlane3D(planeAtoms);}
        catch (InsufficientPointsException exc) {
            throw new InsufficientAtomsException(exc);
        }        
        
        return basePairPlane;
    }
    
    /**
     * Estimate position at center of a double helix containing this base pair
     * @return
     */
    public Vector3DClass getHelixCenter() 
    throws InsufficientAtomsException
    {
        
        // 1) compute best plane containing base group atoms
        Collection<Vector3D> planeAtoms = new Vector<Vector3D>();
        LocatedMolecule base = residue1.get(Nucleotide.baseGroup);
        for (Iterator i = base.getAtomIterator(); i.hasNext();) {
            LocatedAtom a = (LocatedAtom) i.next();
            planeAtoms.add(a.getCoordinates());
        }
        base = residue2.get(Nucleotide.baseGroup);
        for (Iterator i = base.getAtomIterator(); i.hasNext();) {
            LocatedAtom a = (LocatedAtom) i.next();
            planeAtoms.add(a.getCoordinates());
        }
        Plane3D basePairPlane;
        try {basePairPlane = Plane3D.bestPlane3D(planeAtoms);}
        catch (InsufficientPointsException exc) {
            throw new InsufficientAtomsException(exc);
        }
        
        // 2) compute minor-major axis by comparing C1*->C1* axis to base group centroid
        Vector3D basePairCentroid;
        try {basePairCentroid = Vector3DClass.centroid(planeAtoms);}
        catch (InsufficientPointsException exc) {
            throw new InsufficientAtomsException(exc);
        }
        Vector3D c11 = residue1.getAtom(" C1*").getCoordinates();
        Vector3D c12 = residue2.getAtom(" C1*").getCoordinates();
        Vector3D centerC1 = c11.plus(c12).times(0.5);
        Vector3D approximateMinorMajorDirection = basePairCentroid.minus(centerC1).unit();

        Vector3DClass basePairDirection = new Vector3DClass( c12.minus(c11).unit() );
        Vector3D minorMajorDirection = basePairDirection.cross(basePairPlane.getNormal()).unit();
        // Cross product might point in the exact opposite direction, depending upon base order
        if (minorMajorDirection.dot(approximateMinorMajorDirection) < 0)
            minorMajorDirection = minorMajorDirection.times(-1.0);
        
        // 3) extend minor-major axis to estimate helix center
        // TODO - adjust this distance according to something
        Vector3D helixCenter = centerC1.plus(minorMajorDirection.times(5.90));
        return new Vector3DClass (helixCenter);
    }
    
    public String toString() {
        return "BasePair " + residue1 + " paired to " + residue2;
    }

    public Iterator<Residue> iterator() {
        return new Iterator<Residue>() {
            int residueIndex = 1;
            public boolean hasNext() {
                if (residueIndex <= 2) return true;
                return false;
            }
            public Residue next() {
                PDBResidue answer = null;
                if (residueIndex == 1) answer = residue1;
                else if (residueIndex == 2) answer = residue2;
                else { 
                    throw new NoSuchElementException();
                }
                residueIndex ++;
                return answer;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    // Make BasePairs nicely hashable by overriding equals and hashCode
    public boolean equals(Object o) {
        if (! (o instanceof BasePair)) return false;
        BasePair bp2 = (BasePair) o;
        
        // Residues must be exactly the same objects in both base pairs
        if ( (residue1.equals(bp2.residue1)) && (residue2.equals(bp2.residue2)) ) return true;
        if ( (residue2.equals(bp2.residue1)) && (residue1.equals(bp2.residue2)) ) return true;
        return false;
    }
    public int hashCode() {
        return residue1.hashCode() + residue2.hashCode();
    }

    public Boolean isBetween(Residue r1, Residue r2){
    	return ((r1==this.getResidue1()&& r2==this.getResidue2()) ||
    			(r1==this.getResidue2()&& r2==this.getResidue1()));
    }

}
