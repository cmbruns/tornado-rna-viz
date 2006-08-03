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

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;

import java.awt.Color;
import java.util.*;

import vtk.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * These shapes do not work for vtkGlyph3D because they are too asymmetric
  * and their orientations cannot be fully specified.  Sucks.
 */
public class DuplexResidueWedge extends TensorGlyphCartoon {
    double wedgeThickness = 2.0;
    double wedgeRadius = 8.0;

    private int baseColorIndex = 150;
    private Hashtable colorIndices = new Hashtable();

    public DuplexResidueWedge() {
        this(8.0);
    }
            
    public DuplexResidueWedge(double r) {
        super();

        wedgeRadius = r;
        
        // Make a cylinder
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(15);
        cylinderSource.SetRadius(wedgeRadius);
        cylinderSource.SetHeight(wedgeThickness);
        cylinderSource.SetCapping(1);

        // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
        vtkTransform cylinderTransform = new vtkTransform();
        cylinderTransform.Identity();
        cylinderTransform.RotateZ(90);
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        cylinderFilter.SetInput(cylinderSource.GetOutput());
        cylinderFilter.SetTransform(cylinderTransform);

        // Cut the cylinder in half
        // Set the plane to use in cutting the cylinder in half
        vtkPlane clipPlane = new vtkPlane();
        clipPlane.SetNormal(0.0, 1.0, 0.0);                
        clipPlane.SetOrigin(0.0, 0.0, 0.0);

        vtkClipPolyData clipper = new vtkClipPolyData();
        clipper.SetInput(cylinderFilter.GetOutput());
        clipper.SetClipFunction(clipPlane); 
        clipper.GenerateClipScalarsOn();
        clipper.GenerateClippedOutputOn();
        // clipper.SetValue(0.5); // causes plane to be placed a bit wrong
        
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

        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        appendFilter.AddInput(clipper.GetClippedOutput());
        appendFilter.AddInput(cutTriangles.GetOutput());
        
        // Resmooth the surface shading after the append filter chunked it
        vtkPolyDataNormals dataNormals = new vtkPolyDataNormals();
        dataNormals.SetFeatureAngle(80.0); // Angles smaller than this are smoothed
        dataNormals.SetInput(appendFilter.GetOutput());

        
        // Use lines as the glyph primitive
        setGlyphSource(dataNormals.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleNone();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar
        // orientByNormal(); // not supported for tensorGlyph
    }

    public void addMolecule(Molecule molecule) {
        if (! (molecule instanceof NucleicAcid)) return;
        NucleicAcid nucleicAcid = (NucleicAcid) molecule;
        Collection<Duplex> duplexen = nucleicAcid.identifyHairpins();
        DUPLEX: for (Iterator iterDuplex = duplexen.iterator(); iterDuplex.hasNext(); ) {
            Duplex duplex = (Duplex) iterDuplex.next();

            Cylinder duplexCylinder;
            try {duplexCylinder = DuplexCylinderActor.doubleHelixCylinder(duplex);}
            catch (InsufficientPointsException exc) {continue DUPLEX;}
       
            MutableVector3D duplexDirection = new Vector3DClass( duplexCylinder.getHead().minus(duplexCylinder.getTail()).unit() );
            Line3D duplexAxis = new Line3D(duplexDirection, duplexCylinder.getTail());

            BASEPAIR: for (Iterator iterBasePair = duplex.basePairs().iterator(); iterBasePair.hasNext(); ) {
                BasePair basePair = (BasePair) iterBasePair.next();
                // Nucleotide residue = (Nucleotide) iterResidue.next();

                Residue residue1 = basePair.getResidue1();
                Residue residue2 = basePair.getResidue2();

                // Put residue position on cylinder axis                
                // Ignore residues lacking base atoms
                Vector3D baseCentroid1;
                Vector3D baseCentroid2;
                try {
                    baseCentroid1 = residue1.get(Nucleotide.baseGroup).getCenterOfMass();
                    baseCentroid2 = residue2.get(Nucleotide.baseGroup).getCenterOfMass();
                } catch (InsufficientAtomsException exc) {
                    continue BASEPAIR;
                }

                Vector3D baseCentroid = new Vector3DClass( baseCentroid1.plus(baseCentroid2).times(0.5) );
                Vector3D helixCenter = duplexAxis.getClosestPoint(baseCentroid);

                // Put residue normal perpendicular to helix axis, along base-pair direction
                Vector3D backbonePosition1;
                Vector3D backbonePosition2;
                try {
                    backbonePosition1 = residue1.getBackbonePosition();
                    backbonePosition2 = residue2.getBackbonePosition();
                } catch (InsufficientAtomsException exc) {
                    continue BASEPAIR; // Skip pairs lacking backbone positions
                }
                
                MutableVector3D residueDirection = new Vector3DClass( backbonePosition2.minus(backbonePosition1) );

                Vector3D thirdDirection = new Vector3DClass( duplexDirection.cross(residueDirection).unit() );
                // Make it all neatly orthogonal
                residueDirection = (MutableVector3D) thirdDirection.cross(duplexDirection);                

                Residue residues[] = {residue1, residue2};
                
                for (int i = 0; i < 2; i++) {
                    Residue residue = residues[i];
                    
                    Vector currentObjects = new Vector(); // assign molecular object on which to index this wedge
                    currentObjects.add(molecule);
                    currentObjects.add(duplex);
                    currentObjects.add(residue);
                    
                    double colorScalar = getColorIndex(residue);
    
                    linePoints.InsertNextPoint(helixCenter.getX(), helixCenter.getY(), helixCenter.getZ());
                    lineNormals.InsertNextTuple3(residueDirection.getX(), residueDirection.getY(), residueDirection.getZ());
                    
                    // Tensors for full orientation
                    // Vector3D otherDirection = duplexDirection.cross(residueDirection).unit();
                    tensors.InsertNextTuple9(duplexDirection.getX(),duplexDirection.getY(),duplexDirection.getZ(),
                            residueDirection.getX(),residueDirection.getY(),residueDirection.getZ(),
                            thirdDirection.getX(),thirdDirection.getY(),thirdDirection.getZ()); // TODO
        
                    // glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
                    colorScalars.InsertNextValue(colorScalar);
                    
                    // Reverse the orientation for the other residue
                    residueDirection.timesEquals(-1.0);
                    duplexDirection.timesEquals(-1.0);
                }
            }
        }
    }
}
