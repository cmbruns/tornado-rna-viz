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

/** 
 * @author Christopher Bruns
 * 
 * Represents a base-pair interaction between two residues in a nucleic acid structure
 */
public class BasePair 
implements Iterable
{
    Nucleotide residue1;
    Nucleotide residue2;
    
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
            return;
        }
        else if (r2Index > r1Index) {
            residue1 = r1;
            residue2 = r2;
            return;
        }
        // If number is the same, sort on insertion code
        else if (r1Code > r2Code) {
            residue1 = r2;
            residue2 = r1;
            return;            
        }
        else {
            residue1 = r1;
            residue2 = r2;
            return;            
        }
    }
    
    
    public Nucleotide getResidue1() {return residue1;}
    public Nucleotide getResidue2() {return residue2;}

    public Plane3D getBasePlane() {
        // 1) compute best plane containing base group atoms
        Vector planeAtoms = new Vector();
        if (residue1 != null) {
            Molecule base = residue1.get(Nucleotide.baseGroup);
            for (int a = 0; a < base.getAtomCount(); a++) {
                planeAtoms.addElement(base.getAtom(a).getCoordinates());
            }
        }
        if (residue2 != null) {
            Molecule base = residue2.get(Nucleotide.baseGroup);
            for (int a = 0; a < base.getAtomCount(); a++) {
                planeAtoms.addElement(base.getAtom(a).getCoordinates());
            }
        }
        Plane3D basePairPlane = Plane3D.bestPlane3D(planeAtoms);
        return basePairPlane;
    }
    
    /**
     * Estimate position at center of a double helix containing this base pair
     * @return
     */
    public Vector3D getHelixCenter() {
        // 1) compute best plane containing base group atoms
        Vector planeAtoms = new Vector();
        Molecule base = residue1.get(Nucleotide.baseGroup);
        for (int a = 0; a < base.getAtomCount(); a++) {
            planeAtoms.addElement(base.getAtom(a).getCoordinates());
        }
        base = residue2.get(Nucleotide.baseGroup);
        for (int a = 0; a < base.getAtomCount(); a++) {
            planeAtoms.addElement(base.getAtom(a).getCoordinates());
        }
        Plane3D basePairPlane = Plane3D.bestPlane3D(planeAtoms);
        
        // 2) compute minor-major axis by comparing C1*->C1* axis to base group centroid
        Vector3D basePairCentroid = BaseVector3D.centroid(planeAtoms);
        BaseVector3D c11 = residue1.getAtom(" C1*").getCoordinates();
        BaseVector3D c12 = residue2.getAtom(" C1*").getCoordinates();
        Vector3D centerC1 = c11.plus(c12).scale(0.5);
        Vector3D approximateMinorMajorDirection = basePairCentroid.minus(centerC1).unit();

        Vector3D basePairDirection = c12.minus(c11).unit();
        Vector3D minorMajorDirection = basePairDirection.cross(basePairPlane.getNormal()).unit();
        // Cross product might point in the exact opposite direction, depending upon base order
        if (minorMajorDirection.dot(approximateMinorMajorDirection) < 0)
            minorMajorDirection = minorMajorDirection.scale(-1.0);
        
        // 3) extend minor-major axis to estimate helix center
        // TODO - adjust this distance according to something
        Vector3D helixCenter = centerC1.plus(minorMajorDirection.scale(5.90));
        return helixCenter;
    }
    
    public String toString() {
        return "BasePair " + residue1.getResidueNumber() + ":" + residue2.getResidueNumber();
    }

    public Iterator iterator() {
        return new Iterator() {
            int residueIndex = 1;
            public boolean hasNext() {
                if (residueIndex <= 2) return true;
                return false;
            }
            public Object next() {
                Residue answer = null;
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
}
