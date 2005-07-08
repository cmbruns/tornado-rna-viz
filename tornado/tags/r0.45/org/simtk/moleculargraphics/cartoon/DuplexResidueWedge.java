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
import org.simtk.molecularstructure.atom.*;

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
        // TODO If this doesn't work, try putting normal along Y-axis
        clipPlane.SetNormal(0.0, 1.0, 0.0);                
        clipPlane.SetOrigin(0.0, 0.0, 0.0);

        vtkClipPolyData clipper = new vtkClipPolyData();
        clipper.SetInput(cylinderFilter.GetOutput());
        clipper.SetClipFunction(clipPlane); 
        clipper.GenerateClipScalarsOn();
        clipper.GenerateClippedOutputOn();
        clipper.SetValue(0.5);
        
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
        
        // Use lines as the glyph primitive
        setGlyphSource(appendFilter.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleNone();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar
        // orientByNormal(); // not supported for tensorGlyph

        glyphActor.GetProperty().BackfaceCullingOff();
    }

    public void show(Molecule molecule) {
        addMolecule(molecule);
    }

    void addMolecule(Molecule molecule) {
        if (! (molecule instanceof NucleicAcid)) return;
        NucleicAcid nucleicAcid = (NucleicAcid) molecule;
        Vector duplexen = nucleicAcid.identifyHairpins();
        for (Iterator iterDuplex = duplexen.iterator(); iterDuplex.hasNext(); ) {
            Duplex duplex = (Duplex) iterDuplex.next();
            Cylinder duplexCylinder = DuplexCylinderCartoon.doubleHelixCylinder(duplex);
            Vector3D duplexDirection = duplexCylinder.getHead().minus(duplexCylinder.getTail()).unit();
            Line3D duplexAxis = new Line3D(duplexDirection, duplexCylinder.getTail());
            for (Iterator iterResidue = duplex.residues().iterator(); iterResidue.hasNext(); ) {
                Nucleotide residue = (Nucleotide) iterResidue.next();

                // Put residue position on cylinder axis
                BaseVector3D baseCentroid = residue.get(Nucleotide.baseGroup).getCenterOfMass();
                BaseVector3D helixCenter = duplexAxis.getClosestPoint(baseCentroid);

                // Put residue normal perpendicular to helix axis, along base-pair direction
                BaseVector3D backbonePosition = residue.getBackbonePosition();
                Atom watsonCrickAtom;
                if (residue instanceof Purine)
                    watsonCrickAtom = residue.getAtom(" N1 ");
                else 
                    watsonCrickAtom = residue.getAtom(" N3 ");
                if (watsonCrickAtom == null) continue;                
                BaseVector3D watsonCrickPosition = watsonCrickAtom.getCoordinates();
                BaseVector3D residueDirection = watsonCrickPosition.minus(backbonePosition);

                // Make it perpendicular to the helix axis
                // TODO residueDirection = residueDirection.minus(duplexAxis.getDirection().scale(duplexAxis.getDirection().dot(residueDirection))).unit();
                // residueDirection = residueDirection.cross(duplexAxis.getDirection()).unit();
                residueDirection = duplexDirection;

                Vector currentObjects = new Vector(); // assign molecular object on which to index this wedge
                currentObjects.add(molecule);
                currentObjects.add(duplex);
                currentObjects.add(residue);
                
                Color color = residue.getDefaultColor();
                if (! (colorIndices.containsKey(color))) {
                    colorIndices.put(color, new Integer(baseColorIndex));
                    lut.SetTableValue(baseColorIndex, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
                    baseColorIndex ++;
                }
                int colorScalar = ((Integer) colorIndices.get(color)).intValue();        

                linePoints.InsertNextPoint(helixCenter.getX(), helixCenter.getY(), helixCenter.getZ());
                lineNormals.InsertNextTuple3(residueDirection.getX(), residueDirection.getY(), residueDirection.getZ());
    
                glyphColors.add(currentObjects, lineScalars, lineScalars.GetNumberOfTuples(), colorScalar);
                lineScalars.InsertNextValue(colorScalar);

            }
        }
    }
}
