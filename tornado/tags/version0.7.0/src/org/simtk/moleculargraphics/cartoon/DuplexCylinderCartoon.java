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
 * Created on Jul 7, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.*;

import vtk.*;

import org.simtk.geometry3d.*;
import org.simtk.moleculargraphics.GraphicsCylinder;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.PDBAtom;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.AminoAcid;
import org.simtk.molecularstructure.protein.Helix;
import org.simtk.util.*;

/** 
 *  
  * @author Christopher Bruns
  * 
  * A transparent blue cylinder around each duplex
 */
public class DuplexCylinderCartoon extends MolecularCartoonClass 
{
    // Size of the cylinders surrounding the double helices
    static double defaultbarrelRadius = 8.0; // 11.5 to consume most atoms
    Color defaultCylinderColor = new Color(0, 255, 255);
    int defaultCylinderResolution = 10;
    double defaultCylinderOpacity = 1.0;

    vtkAssembly assembly = new vtkAssembly();
    
    public DuplexCylinderCartoon() {
    }

    public void updateCoordinates() {
        // TODO
    }
    
    public vtkAssembly getAssembly() {return assembly;}
    
    public void add(LocatedMolecule molecule) {
        if (! (molecule instanceof NucleicAcid)) return;
        addNucleicAcid((NucleicAcid) molecule);
        super.add(molecule);
    }
    public void hide(LocatedMolecule molecule) {} // TODO
    public void hide() {} // TODO
    public void show(LocatedMolecule molecule) {} // TODO
    public void show() {} // TODO
    public void clear() {} // TODO

    public void addNucleicAcid(NucleicAcid nucleicAcid) {
//        Collection<Duplex> hairpins = nucleicAcid.identifyHairpins();
//        for (Iterator iterHairpin = hairpins.iterator(); iterHairpin.hasNext(); ) {
//            Duplex duplex = (Duplex) iterHairpin.next();
//            addDuplex(duplex);
//        }
        
        // Add duplexes (perhaps should restrict to source = rnaml only?
        for (SecondaryStructure structure : nucleicAcid.secondaryStructures()) {
            if (structure instanceof Duplex)  {
            	Duplex dup = (Duplex) structure;
            	List<BasePair> dupBPs = dup.basePairs();
            	BasePair firstBP = dupBPs.get(0);
            	BasePair lastBP = dupBPs.get(dupBPs.size()-1);
            	Nucleotide res5 = firstBP.getResidue1();
            	if (res5.getChainID().equals(nucleicAcid.getChainID())){
	                // System.out.println("Duplex found: "+dup.helixString());
	                try {addDuplex(dup);}
	                catch (InsufficientPointsException exc) {}
            	}
            }
        }
        

    }
    public void addDuplex(Duplex duplex) throws InsufficientPointsException {
        if (duplex == null) return;
        
        Cylinder helixCylinder = doubleHelixCylinder(duplex);
        vtkActor cylinderActor =  GraphicsCylinder.getVtkCylinder(helixCylinder, defaultCylinderColor, defaultCylinderResolution);
        cylinderActor.GetProperty().SetOpacity(defaultCylinderOpacity);
        assembly.AddPart(cylinderActor);
    }

    /**
     * Given a set of base paired residues, construct cylinder geometry to represent them.
     * 
     * One side effect of this routine is to populate the private residueWedges data structure.
     * 
     * @param v a list of base pairs to be used to define the helix.
     * @return a cylinder that approximates the location of the base stack.
     */
    static public Cylinder doubleHelixCylinder(Duplex h) 
    throws InsufficientPointsException
    {
        if (h.basePairs().size() < 1) return null;

        // Remember where each residue goes in the cylinder
        Map<BasePair, Vector3D> basePairCentroids = new HashMap<BasePair, Vector3D>();
        
        // Average the direction of the base plane normals
        // Make the helix axis pass through the centroid of the base pair helix center guesses
        Vector3D helixDirection = new Vector3DClass(0,0,0);
        Vector3D helixCentroid = new Vector3DClass(0,0,0);
        // Vector<Vector3D> cylinderPoints = new Vector<Vector3D>();
        BASEPAIR: for (Iterator iterBasePair = h.basePairs().iterator(); iterBasePair.hasNext(); ) {
        // for (int p = 0; p < h.basePairs().size(); p++) {
            BasePair bp = (BasePair) iterBasePair.next();
            
            // Accumulate normals
            try {
                Plane3D basePlane = bp.getBasePlane();
                MutableVector3D normal = new Vector3DClass(bp.getBasePlane().getNormal());
                if (normal.dot(helixDirection) < 0) normal.timesEquals(-1.0);
                helixDirection = helixDirection.plus(normal);
            
                // Accumulate centroid
                Vector3DClass helixCenter = bp.getHelixCenter();
                basePairCentroids.put(bp, helixCenter);
                helixCentroid = helixCentroid.plus(helixCenter);

            } catch (InsufficientAtomsException exc) {
                continue BASEPAIR; // skip pairs with ill defined planes
            }

            // cylinderPoints.addElement(helixCenter);
        }
        helixDirection = helixDirection.unit();
        helixCentroid = helixCentroid.times(1.0/h.basePairs().size());
        Vector3D helixOffset = helixCentroid.minus(helixDirection.times(helixDirection.dot(helixCentroid)));
        
        Line3D helixAxis = new Line3D( new Vector3DClass(helixDirection), new Vector3DClass(helixOffset) );
                
        // Find ends of helix
        TreeMap alphaBasePairs = new TreeMap();
        
        if (basePairCentroids.size() < 1) throw new InsufficientPointsException();

        Vector3D somePoint = (Vector3DClass) basePairCentroids.values().iterator().next();

        double minAlpha = somePoint.dot(helixAxis.getDirection());
        double maxAlpha = minAlpha;
        for (Iterator i = basePairCentroids.keySet().iterator(); i.hasNext(); ) {
            BasePair bp = (BasePair) i.next();
            Vector3DClass cylinderPoint = (Vector3DClass) basePairCentroids.get(bp);
            double alpha = cylinderPoint.dot(helixAxis.getDirection());
            alphaBasePairs.put(new Double(alpha), bp);
            if (alpha > maxAlpha) maxAlpha = alpha;
            if (alpha < minAlpha) minAlpha = alpha;
        }
        // Extend helix to enclose end base pairs
        minAlpha -= 1.6;
        maxAlpha += 1.6;
        Vector3D cylinderHead = helixAxis.getDirection().times(maxAlpha).plus(helixAxis.getOrigin());
        Vector3D cylinderTail = helixAxis.getDirection().times(minAlpha).plus(helixAxis.getOrigin());
        
        double cylinderRadius = defaultbarrelRadius;
        Cylinder cylinder = new Cylinder( new Vector3DClass(cylinderHead), new Vector3DClass(cylinderTail), cylinderRadius);
        
        // Populate half cylinders for each residue
        // Using TreeMap data structure to ensure that the alpha values are in increasing order
        // Determine range of alpha for each base pair
        Double previousAlpha = null;
        BasePair previousBasePair = null;
        Hashtable basePairStartAlphas = new Hashtable();
        Hashtable basePairEndAlphas = new Hashtable();
        Hashtable residueNormals = new Hashtable();
        for (Iterator i = alphaBasePairs.keySet().iterator(); i.hasNext(); ) {
            double alpha = ((Double) i.next()).doubleValue();
            BasePair basePair = (BasePair) alphaBasePairs.get(new Double(alpha));
            
            double startAlpha;
            if (previousAlpha == null) startAlpha = minAlpha;
            else startAlpha = (alpha + previousAlpha.doubleValue()) / 2;
            basePairStartAlphas.put(basePair, new Double(startAlpha));
            
            if (previousBasePair != null)
                basePairEndAlphas.put(previousBasePair, new Double(startAlpha));
            
            // Create cylinder slicing plane using vector between residue atoms
            PDBAtom atom1 = basePair.getResidue1().getAtom(" C1*");
            PDBAtom atom2 = basePair.getResidue2().getAtom(" C1*");
            Vector3D direction = atom2.getCoordinates().minus(atom1.getCoordinates()).unit();
            // Make sure direction is perpendicular to the helix axis
            direction = direction.minus(helixDirection.times(helixDirection.dot(direction))).unit();
            residueNormals.put(basePair.getResidue1(), direction);
            residueNormals.put(basePair.getResidue2(), direction.times(-1));
            
            previousAlpha = new Double(alpha);
            previousBasePair = basePair;
        }
        basePairEndAlphas.put(previousBasePair, new Double(maxAlpha));  

        return cylinder;
    }
}
