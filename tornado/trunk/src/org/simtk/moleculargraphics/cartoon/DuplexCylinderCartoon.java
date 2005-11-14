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
import org.simtk.molecularstructure.atom.LocatedAtom;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.util.*;

/** 
 *  
  * @author Christopher Bruns
  * 
  * A transparent blue cylinder around each duplex
 */
public class DuplexCylinderCartoon extends MolecularCartoonNewWay 
{
    // Size of the cylinders surrounding the double helices
    static double defaultbarrelRadius = 8.0; // 11.5 to consume most atoms
    Color defaultCylinderColor = new Color(0, 255, 255);
    int defaultCylinderResolution = 10;
    double defaultCylinderOpacity = 0.5;

    vtkAssembly assembly = new vtkAssembly();
    
    public DuplexCylinderCartoon() {
    }

    public vtkAssembly getAssembly() {return assembly;}
    
    public void select(Selectable s) {} // TODO
    public void unSelect(Selectable s) {} // TODO
    public void unSelect() {} // TODO
    public void highlight(Molecule molecule) {} // TODO
    public void show(Molecule molecule) {
        if (! (molecule instanceof NucleicAcid)) return;
        addNucleicAcid((NucleicAcid) molecule);
    }
    public void hide(Molecule molecule) {} // TODO
    public void clear() {} // TODO

    public void addNucleicAcid(NucleicAcid nucleicAcid) {
        Vector hairpins = nucleicAcid.identifyHairpins();
        for (Iterator iterHairpin = hairpins.iterator(); iterHairpin.hasNext(); ) {
            Duplex duplex = (Duplex) iterHairpin.next();
            addDuplex(duplex);
        }
    }
    public void addDuplex(Duplex duplex) {
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
    static public Cylinder doubleHelixCylinder(Duplex h) {
        if (h.basePairs().size() < 1) return null;

        // Remember where each residue goes in the cylinder
        Hashtable basePairCentroids = new Hashtable();
        
        // Average the direction of the base plane normals
        // Make the helix axis pass through the centroid of the base pair helix center guesses
        MathVector helixDirection = new DoubleVector3D(0,0,0);
        MathVector helixCentroid = new DoubleVector3D(0,0,0);
        // Vector<Vector3D> cylinderPoints = new Vector<Vector3D>();
        for (Iterator iterBasePair = h.basePairs().iterator(); iterBasePair.hasNext(); ) {
        // for (int p = 0; p < h.basePairs().size(); p++) {
            BasePair bp = (BasePair) iterBasePair.next();
            
            // Accumulate normals
            DoubleVector3D normal = bp.getBasePlane().getNormal();
            if (normal.dot(helixDirection) < 0) normal.selfScale(-1.0);
            helixDirection = helixDirection.plus(normal);
            
            // Accumulate centroid
            DoubleVector3D helixCenter = bp.getHelixCenter();
            basePairCentroids.put(bp, helixCenter);
            helixCentroid = helixCentroid.plus(helixCenter);

            // cylinderPoints.addElement(helixCenter);
        }
        helixDirection = helixDirection.unit();
        helixCentroid = helixCentroid.scale(1.0/h.basePairs().size());
        MathVector helixOffset = helixCentroid.minus(helixDirection.scale(helixDirection.dot(helixCentroid)));
        
        Line3D helixAxis = new Line3D( new DoubleVector3D(helixDirection), new DoubleVector3D(helixOffset) );
                
        // Find ends of helix
        TreeMap alphaBasePairs = new TreeMap();
        DoubleVector3D somePoint = (DoubleVector3D) basePairCentroids.values().iterator().next();
        double minAlpha = somePoint.dot(helixAxis.getDirection());
        double maxAlpha = minAlpha;
        for (Iterator i = basePairCentroids.keySet().iterator(); i.hasNext(); ) {
            BasePair bp = (BasePair) i.next();
            DoubleVector3D cylinderPoint = (DoubleVector3D) basePairCentroids.get(bp);
            double alpha = cylinderPoint.dot(helixAxis.getDirection());
            alphaBasePairs.put(new Double(alpha), bp);
            if (alpha > maxAlpha) maxAlpha = alpha;
            if (alpha < minAlpha) minAlpha = alpha;
        }
        // Extend helix to enclose end base pairs
        minAlpha -= 1.6;
        maxAlpha += 1.6;
        MathVector cylinderHead = helixAxis.getDirection().scale(maxAlpha).plus(helixAxis.getOrigin());
        MathVector cylinderTail = helixAxis.getDirection().scale(minAlpha).plus(helixAxis.getOrigin());
        
        double cylinderRadius = defaultbarrelRadius;
        Cylinder cylinder = new Cylinder( new DoubleVector3D(cylinderHead), new DoubleVector3D(cylinderTail), cylinderRadius);
        
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
            LocatedAtom atom1 = basePair.getResidue1().getAtom(" C1*");
            LocatedAtom atom2 = basePair.getResidue2().getAtom(" C1*");
            MathVector direction = atom2.getCoordinates().minus(atom1.getCoordinates()).unit();
            // Make sure direction is perpendicular to the helix axis
            direction = direction.minus(helixDirection.scale(helixDirection.dot(direction))).unit();
            residueNormals.put(basePair.getResidue1(), direction);
            residueNormals.put(basePair.getResidue2(), direction.scale(-1));
            
            previousAlpha = new Double(alpha);
            previousBasePair = basePair;
        }
        basePairEndAlphas.put(previousBasePair, new Double(maxAlpha));  

        return cylinder;
    }
}
