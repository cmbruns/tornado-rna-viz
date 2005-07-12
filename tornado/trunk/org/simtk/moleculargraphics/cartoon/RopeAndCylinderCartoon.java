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
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.moleculargraphics.GraphicsCylinder;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;

import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * Graphical representation of an RNA molecule that shows cylinders where the double
 * helices are, and a backbone trace where other residues are.
 */
public class RopeAndCylinderCartoon extends MolecularCartoon {

    // Size of ropes connecting the barrels
    double ropeRadius = 0.6;
    double residueSphereRadius = ropeRadius;
    double ropeCylinderRadius = ropeRadius;
    
    // Size of the cylinders surrounding the double helices
    double barrelRadius = 8.0; // 11.5 to consume most atoms
    
    Color defaultRopeColor = new Color(0, 0, 255);
    Color defaultCylinderColor = new Color(0, 255, 255);
    int defaultCylinderResolution = 10;
    int defaultRopeResolution = 5;
    double defaultCylinderOpacity = 0.5;
    
    // Location of residue representation
    Hashtable residueCenters = new Hashtable();
    // Location of midpoint of link between this residue and the previous one
    Hashtable residuePreBonds = new Hashtable();
    // Location of midpoint of link between this residue and the subsequent one
    Hashtable residuePostBonds = new Hashtable();
    // Section of double-helix cylinder assigned to a residue
    Hashtable residueWedges = new Hashtable();

    Hashtable residueSphereSources = new Hashtable();
    
    // Note which residues are internal to duplexes, at the ends of duplexes, or in loops
    HashSet duplexResidues = new HashSet();
    HashSet endResidues = new HashSet();
    HashSet loopResidues = new HashSet();
    
    /**
     * Update graphical primitives to reflect a change in atomic positions
     *
     */
    public void updateCoordinates() {
        // TODO
    }

    /**
     * Keep just one copy of the sphere source for residue objects
     * @return
     */
    vtkSphereSource getResidueSphereSource(double scale) {
        Double scaleObject = new Double(scale);
        // Only intialize it the first time
        if (!residueSphereSources.containsKey(scaleObject)) {
            vtkSphereSource source = new vtkSphereSource();
            source.SetRadius(residueSphereRadius * scale);
            source.SetPhiResolution(defaultRopeResolution);
            source.SetThetaResolution(defaultRopeResolution);
            residueSphereSources.put(scaleObject, source);
        }
        return (vtkSphereSource) residueSphereSources.get(scaleObject);
    }
    
    public vtkProp highlight(Residue residue, Color color) {
        return represent(residue, 1.05, color);
    }

    public vtkAssembly representRope(Nucleotide residue, double scaleFactor, Color clr) {
        // Don't draw ropes for strictly internal duplex residues
        if ( duplexResidues.contains(residue) &&
             (! endResidues.contains(residue)) ) {
            return null;
        }

        // Assuming that the residues have already been processed at the molecule level
        if (!residueCenters.containsKey(residue)) {
            // Perhaps this residue is the entire molecule, so residue centers is not populated yet
            BaseVector3D center = createResidueCenter(residue);
            if (center == null) return null;
        }
        vtkAssembly assembly = new vtkAssembly();
        
        // Put one sphere at the residue's position;
        vtkPolyDataMapper sphereMapper = new vtkPolyDataMapper();
        sphereMapper.SetInput(getResidueSphereSource(scaleFactor).GetOutput());           
        vtkActor sphereActor = new vtkActor();
        sphereActor.SetMapper(sphereMapper);
        Color c;
        if (clr != null) c = clr;
        else c = defaultRopeColor;
        sphereActor.GetProperty().SetColor(c.getRed()/255.0, c.getGreen()/255.0, c.getBlue()/255.0);
        
        BaseVector3D center = (BaseVector3D) residueCenters.get(residue);
        sphereActor.SetPosition(center.getX(), center.getY(), center.getZ());
        assembly.AddPart(sphereActor);
        
        // Put one bond linking toward the previous residue
        if (residuePreBonds.containsKey(residue)) {
            BaseVector3D prePoint = (BaseVector3D) residuePreBonds.get(residue);
            
            // Construct cylinder from center to midway to the previous residue
            BaseVector3D midPoint = prePoint;
            Cylinder cylinder = new Cylinder(center, midPoint, ropeCylinderRadius * scaleFactor);
            vtkActor cylinderActor = GraphicsCylinder.getVtkCylinder(cylinder, c, defaultRopeResolution);
            cylinderActor.GetProperty().SetColor(c.getRed()/255.0, c.getGreen()/255.0, c.getBlue()/255.0);
            cylinderActor.GetProperty().SetOpacity(1.0);
            
            assembly.AddPart(cylinderActor);
        }
        
        // Put one bond linking toward the subsequent residue
        if (residuePostBonds.containsKey(residue)) {
            BaseVector3D postPoint = (BaseVector3D) residuePostBonds.get(residue);
            
            // Construct cylinder from center to midway to the previous residue
            BaseVector3D midPoint = postPoint;
            Cylinder cylinder = new Cylinder(postPoint, center, ropeCylinderRadius * scaleFactor);
            vtkActor cylinderActor = GraphicsCylinder.getVtkCylinder(cylinder, c, defaultRopeResolution);
            cylinderActor.GetProperty().SetColor(c.getRed()/255.0, c.getGreen()/255.0, c.getBlue()/255.0);
            cylinderActor.GetProperty().SetOpacity(1.0);
            
            assembly.AddPart(cylinderActor);
        }
        return assembly;
    }

    public vtkAssembly represent(Molecule molecule) {
        return represent(molecule, 1.00, null);
    }
    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr) {
        // Render one residue
        if (molecule instanceof Nucleotide) {
            Nucleotide residue = (Nucleotide) molecule;
            vtkAssembly assembly = new vtkAssembly();

            Color color = clr;
            if (clr == null)
                color = defaultCylinderColor;            
            // Create a wedge shape for each helical residue
            if (residueWedges.containsKey(residue)) {
                SemiCylinder startWedge = (SemiCylinder) residueWedges.get(residue); // not yet scaled
                // Scale size of wedge by scale factor
                Vector3D direction = startWedge.getHead().minus(startWedge.getTail()).scale(scaleFactor);
                SemiCylinder wedge = new SemiCylinder(
                        startWedge.getTail().plus(direction),
                        startWedge.getHead().minus(direction),
                        startWedge.getRadius() * scaleFactor,
                        startWedge.getNormal());                        

                // Set the plane to use in cutting the cylinder in half
                vtkPlane clipPlane = new vtkPlane();
                Vector3D normal = wedge.getNormal();
                clipPlane.SetNormal(normal.getX(), normal.getY(), normal.getZ());                
                BaseVector3D origin = wedge.getHead();
                clipPlane.SetOrigin(origin.getX(), origin.getY(), origin.getZ());
                
                vtkTransformPolyDataFilter cylinderFilter = GraphicsCylinder.getVtkCylinderFilter(wedge, defaultCylinderResolution);

                vtkClipPolyData clipper = new vtkClipPolyData();
                clipper.SetInput(cylinderFilter.GetOutput());
                clipper.SetClipFunction(clipPlane); 
                clipper.GenerateClipScalarsOn();
                clipper.GenerateClippedOutputOn();
                clipper.SetValue(0.5);

                vtkPolyDataMapper wedgeMapper = new vtkPolyDataMapper();
                wedgeMapper.SetInput(clipper.GetClippedOutput());
                wedgeMapper.ScalarVisibilityOff();

                vtkActor wedgeActor = new vtkActor();
                wedgeActor.SetMapper(wedgeMapper);
                
                
                wedgeActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
                wedgeActor.GetProperty().SetOpacity(1.0);
                assembly.AddPart(wedgeActor);
                
                // Close up place where clip was
                vtkCutter cutEdges = new vtkCutter();
                cutEdges.SetInput(cylinderFilter.GetOutput());
                cutEdges.SetCutFunction(clipPlane);
                cutEdges.GenerateCutScalarsOn();
                cutEdges.SetValue(0, 0.5);
                vtkStripper cutStrips = new vtkStripper();
                cutStrips.SetInput(cutEdges.GetOutput());
                cutStrips.Update();
                vtkPolyData cutPoly = new vtkPolyData();
                cutPoly.SetPoints(cutStrips.GetOutput().GetPoints());
                cutPoly.SetPolys(cutStrips.GetOutput().GetLines());

                // Triangle filter is robust enough to ignore the duplicate point at
                // the beginning and end of the polygons and triangulate them.
                vtkTriangleFilter cutTriangles = new vtkTriangleFilter();
                cutTriangles.SetInput(cutPoly);
                
                vtkPolyDataMapper cutMapper = new vtkPolyDataMapper();
                cutMapper.SetInput(cutPoly);
                cutMapper.SetInput(cutTriangles.GetOutput());
                vtkActor cutActor = new vtkActor();
                cutActor.SetMapper(cutMapper);
                cutActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
                cutActor.GetProperty().SetOpacity(1.0);
                assembly.AddPart(cutActor);
            }

            // Draw the rope part after the wedge part, so that duplex end residues have updated centers
            vtkProp3D ropePart = representRope(residue, scaleFactor, clr);
            if (ropePart != null) assembly.AddPart(ropePart);
            
            return assembly;
        }
        
        // Molecule containing multiple residues
        else if (molecule instanceof NucleicAcid) {
            residueCenters.clear();
            residuePreBonds.clear();
            residuePostBonds.clear();
            
            NucleicAcid rna = (NucleicAcid) molecule;
            vtkAssembly assembly = new vtkAssembly();

            // Create cylinders
            Color color = clr;
            if (clr == null)
                color = defaultCylinderColor;            
            Vector hairpins = rna.identifyHairpins();
            for (Iterator i = hairpins.iterator(); i.hasNext(); ) {
                Duplex pin = (Duplex) i.next();
                vtkActor cylinderActor = doubleHelixCylinderActor(pin, color, defaultCylinderResolution);
                cylinderActor.GetProperty().SetOpacity(defaultCylinderOpacity);
                assembly.AddPart(cylinderActor);
            }
            
            // Divide residues into 3 classes
            for (Iterator i = hairpins.iterator(); i.hasNext(); ) {
                Duplex pin = (Duplex) i.next();
                for (Iterator i2 = pin.basePairs().iterator(); i2.hasNext(); ) {
                    BasePair bp = (BasePair) i2.next();
                    for (Iterator i3 = bp.iterator(); i3.hasNext(); ) {
                        Residue residue = (Residue) i3.next();
                        // First put cylinder residues into one class
                        duplexResidues.add(residue);
                    }
                }
            }
            // Now distinguish end and loop residues
            for (Iterator i = rna.residues().iterator(); i.hasNext(); ) {
                Residue residue = (Residue) i.next();
                if (! duplexResidues.contains(residue)) {
                    loopResidues.add(residue);
                }
                else { // residue is in a duplex
                    Residue next = residue.getNextResidue();
                    Residue previous = residue.getPreviousResidue();
                    if ( (next != null) &&
                         (previous != null) &&
                         duplexResidues.contains(next) &&
                         duplexResidues.contains(previous) ) 
                    {
                        // This is a strictly internal residue
                    }
                    else {
                        // This residue is at the end of a duplex
                        endResidues.add(residue);
                        // Update residue center information
                        createResidueCenter(residue);
                    }
                }
            }
            
            // Create ropes
            // First populate residue positions structure
            Residue previousResidue = null;
            BaseVector3D previousCenter = null;
            for (Iterator i = rna.residues().iterator(); i.hasNext(); ) {
                Residue residue = (Residue) i.next();

                // Don't draw ropes for strictly internal duplex residues
                if ( duplexResidues.contains(residue) &&
                     (! endResidues.contains(residue)) ) {
                    previousResidue = null;
                    continue;
                }
                
                BaseVector3D center = createResidueCenter(residue);
                
                if (previousResidue != null) {
                    Vector3D midPoint = center.plus(previousCenter).scale(0.5);
                    residuePreBonds.put(residue, midPoint);
                    residuePostBonds.put(previousResidue, midPoint);
                }
                
                previousResidue = residue;
                previousCenter = center;
            }                        
            // Recursively use residue level rendering routine
            for (Iterator i = rna.residues().iterator(); i.hasNext(); ) {
                Residue residue = (Residue) i.next();
                if (residue instanceof Nucleotide) {
                    vtkAssembly rope = representRope((Nucleotide)residue, scaleFactor, clr);
                    if (rope != null) {
                        assembly.AddPart(rope);
                    }
                }
            }
            return assembly;
        }
        else return null;
    }

    // Where to center a sphere for a particular residue
    BaseVector3D createResidueCenter(Residue residue) {
        if (residue == null) return null;
        BaseVector3D center = residue.getAtom(" C1*").getCoordinates();

        // Update residue center to be on edge of cylinder
        if (endResidues.contains(residue)) {
            if (residueWedges.containsKey(residue)) {
                SemiCylinder wedge = (SemiCylinder) residueWedges.get(residue);
                Vector3D cylinderCenter = wedge.getHead().plus(wedge.getTail()).scale(0.5);
                Vector3D cylinderAxis = wedge.getHead().minus(wedge.getTail()).unit();
                Vector3D normal = wedge.getNormal();
                Vector3D residueCenter = normal.rotate(cylinderAxis, 110 * Math.PI/180.0).plus
                    (cylinderCenter).scale(wedge.getRadius());
                residueCenters.put(residue, residueCenter);
            }
        }
        
        residueCenters.put(residue, center);
        return center;
    }
    
    public vtkActor doubleHelixCylinderActor(Duplex h, Color color, int resolution) {
        Cylinder helixCylinder = doubleHelixCylinder(h);
        vtkActor cylinderActor =  GraphicsCylinder.getVtkCylinder(helixCylinder, color, resolution);
        return cylinderActor;
    }
    
    /**
     * Given a set of base paired residues, construct cylinder geometry to represent them.
     * 
     * One side effect of this routine is to populate the private residueWedges data structure.
     * 
     * @param v a list of base pairs to be used to define the helix.
     * @return a cylinder that approximates the location of the base stack.
     */
    public Cylinder doubleHelixCylinder(Duplex h) {
        if (h.basePairs().size() < 1) return null;

        // Remember where each residue goes in the cylinder
        Hashtable basePairCentroids = new Hashtable();
        
        // Average the direction of the base plane normals
        // Make the helix axis pass through the centroid of the base pair helix center guesses
        Vector3D helixDirection = new Vector3D(0,0,0);
        Vector3D helixCentroid = new Vector3D(0,0,0);
        // Vector<Vector3D> cylinderPoints = new Vector<Vector3D>();
        for (Iterator iterBasePair = h.basePairs().iterator(); iterBasePair.hasNext(); ) {
        // for (int p = 0; p < h.basePairs().size(); p++) {
            BasePair bp = (BasePair) iterBasePair.next();
            
            // Accumulate normals
            Vector3D normal = bp.getBasePlane().getNormal();
            if (normal.dot(helixDirection) < 0) normal = normal.scale(-1.0);
            helixDirection = helixDirection.plus(normal);
            
            // Accumulate centroid
            Vector3D helixCenter = bp.getHelixCenter();
            basePairCentroids.put(bp, helixCenter);
            helixCentroid = helixCentroid.plus(helixCenter);

            // cylinderPoints.addElement(helixCenter);
        }
        helixDirection = helixDirection.unit();
        helixCentroid = helixCentroid.scale(1.0/h.basePairs().size());
        Vector3D helixOffset = helixCentroid.minus(helixDirection.scale(helixDirection.dot(helixCentroid)));
        
        Line3D helixAxis = new Line3D(helixDirection, helixOffset);
                
        // Find ends of helix
        TreeMap alphaBasePairs = new TreeMap();
        Vector3D somePoint = (Vector3D) basePairCentroids.values().iterator().next();
        double minAlpha = somePoint.dot(helixAxis.getDirection());
        double maxAlpha = minAlpha;
        for (Iterator i = basePairCentroids.keySet().iterator(); i.hasNext(); ) {
            BasePair bp = (BasePair) i.next();
            Vector3D cylinderPoint = (Vector3D) basePairCentroids.get(bp);
            double alpha = cylinderPoint.dot(helixAxis.getDirection());
            alphaBasePairs.put(new Double(alpha), bp);
            if (alpha > maxAlpha) maxAlpha = alpha;
            if (alpha < minAlpha) minAlpha = alpha;
        }
        // Extend helix to enclose end base pairs
        minAlpha -= 1.6;
        maxAlpha += 1.6;
        Vector3D cylinderHead = helixAxis.getDirection().scale(maxAlpha).plus(helixAxis.getOrigin());
        Vector3D cylinderTail = helixAxis.getDirection().scale(minAlpha).plus(helixAxis.getOrigin());
        
        double cylinderRadius = barrelRadius;
        Cylinder cylinder = new Cylinder(cylinderHead, cylinderTail, cylinderRadius);
        
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
            Atom atom1 = basePair.getResidue1().getAtom(" C1*");
            Atom atom2 = basePair.getResidue2().getAtom(" C1*");
            Vector3D direction = atom2.getCoordinates().minus(atom1.getCoordinates()).unit();
            // Make sure direction is perpendicular to the helix axis
            direction = direction.minus(helixDirection.scale(helixDirection.dot(direction))).unit();
            residueNormals.put(basePair.getResidue1(), direction);
            residueNormals.put(basePair.getResidue2(), direction.scale(-1));
            
            previousAlpha = new Double(alpha);
            previousBasePair = basePair;
        }
        basePairEndAlphas.put(previousBasePair, new Double(maxAlpha));  
        // Actually create semicylinders for each residue
        for (Iterator i = basePairStartAlphas.keySet().iterator(); i.hasNext(); ) {
            BasePair basePair = (BasePair) i.next();
            double startAlpha = ((Double)basePairStartAlphas.get(basePair)).doubleValue();
            double endAlpha = ((Double)basePairEndAlphas.get(basePair)).doubleValue();

            Vector3D head = helixAxis.getDirection().scale(endAlpha).plus(helixAxis.getOrigin());
            Vector3D tail = helixAxis.getDirection().scale(startAlpha).plus(helixAxis.getOrigin());

            Residue residue1 = basePair.getResidue1();
            Residue residue2 = basePair.getResidue2();
            Vector3D normal1 = (Vector3D) residueNormals.get(residue1);
            Vector3D normal2 = (Vector3D) residueNormals.get(residue2);
            SemiCylinder semiCylinder1 = new SemiCylinder(head, tail, cylinderRadius, normal1);
            SemiCylinder semiCylinder2 = new SemiCylinder(head, tail, cylinderRadius, normal2);
            residueWedges.put(residue1, semiCylinder1);
            residueWedges.put(residue2, semiCylinder2);
            // Update residue center information
            createResidueCenter(residue1);
            createResidueCenter(residue2);
        }
        
        return cylinder;
    }
}
