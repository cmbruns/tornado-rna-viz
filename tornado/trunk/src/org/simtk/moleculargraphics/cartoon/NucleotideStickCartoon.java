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
 * Created on Jul 6, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.atom.*;

import vtk.*;

public class NucleotideStickCartoon extends GlyphCartoon {
    double stickLength = 6.0;
    double stickRadius = 0.50;
    double sphereFudge = 1.05; // spheres aren't quit flush with cylinder for some reason
    int cylinderResolution = 8;

    private int baseColorIndex = 150;
    private Hashtable colorIndices = new Hashtable();

    public NucleotideStickCartoon() {
        this(0.50);
    }
    
    public NucleotideStickCartoon(double r) {
        super();

        stickRadius = r;
        
        // Make a cylinder to use as the basis of all bonds
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(cylinderResolution);
        cylinderSource.SetRadius(stickRadius);
        cylinderSource.SetHeight(stickLength);
        cylinderSource.SetCapping(0);

        // Cap the ends of the cylinder with spheres
        vtkSphereSource sphereSource1 = new vtkSphereSource();
        sphereSource1.SetRadius(stickRadius * sphereFudge);
        sphereSource1.SetPhiResolution(6);
        sphereSource1.SetThetaResolution(cylinderResolution);

        // Rotate the spheres so it lines up just right with the cylinder
        vtkTransform sphere1Transform = new vtkTransform();
        sphere1Transform.Identity();
        sphere1Transform.Translate(0.0, -stickLength/2.0, 0.0);
        sphere1Transform.RotateX(90);

        vtkTransformPolyDataFilter sphere1Filter = new vtkTransformPolyDataFilter();
        sphere1Filter.SetInput(sphereSource1.GetOutput());
        sphere1Filter.SetTransform(sphere1Transform);


        vtkSphereSource sphereSource2 = new vtkSphereSource();
        sphereSource2.SetRadius(stickRadius * sphereFudge);
        sphereSource2.SetPhiResolution(6);
        sphereSource2.SetThetaResolution(cylinderResolution);

        // Rotate the spheres so it lines up just right with the cylinder
        vtkTransform sphere2Transform = new vtkTransform();
        sphere2Transform.Identity();
        sphere2Transform.Translate(0.0, stickLength/2.0, 0.0);
        sphere2Transform.RotateX(90);

        vtkTransformPolyDataFilter sphere2Filter = new vtkTransformPolyDataFilter();
        sphere2Filter.SetInput(sphereSource2.GetOutput());
        sphere2Filter.SetTransform(sphere2Transform);


        vtkAppendPolyData append = new vtkAppendPolyData();
        append.AddInput(cylinderSource.GetOutput());
        append.AddInput(sphere1Filter.GetOutput());
        // append.AddInput(sphereSource2.GetOutput()); // not needed for backbone rep.
        
        // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
        vtkTransform cylinderTransform = new vtkTransform();
        cylinderTransform.Identity();
        cylinderTransform.RotateZ(90);
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        // cylinderFilter.SetInput(cylinderSource.GetOutput());
        cylinderFilter.SetInput(append.GetOutput());
        cylinderFilter.SetTransform(cylinderTransform);
        
        setGlyphSource(cylinderFilter.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleNone();  // Do not adjust size
        orientByNormal();
        colorByScalar(); // Take color from glyph scalar

        glyphActor.GetProperty().BackfaceCullingOn();

    }

    public void show(Molecule molecule) {
        addMolecule(molecule, null);
        glyphColors.show(molecule);
    }

    void addMolecule(Molecule molecule, Vector parentObjects) {
        if (molecule == null) return;

        // Don't add things that have already been added
        if (glyphColors.containsKey(molecule)) return;
        
        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(molecule);
        
        // If it's a biopolymer, index the glyphs by residue
        if (molecule instanceof Nucleotide) {
            Nucleotide nucleotide = (Nucleotide) molecule;
            currentObjects.remove(currentObjects.size() - 1); 
            addNucleotide(nucleotide, currentObjects);
        }
        else if (molecule instanceof NucleicAcid) {
            NucleicAcid nucleicAcid = (NucleicAcid) molecule;
            for (Iterator iterResidue = nucleicAcid.residues().iterator(); iterResidue.hasNext(); ) {
                addMolecule((Nucleotide) iterResidue.next(), currentObjects);
            }
        }
    }
    
    void addNucleotide(Nucleotide nucleotide, Vector parentObjects) {
        if (nucleotide == null) return;
        
        // Don't add things that have already been added
        if (glyphColors.containsKey(nucleotide)) return;

        // Put end of rod in the middle of the Watson-Crick face
        LocatedAtom sideChainAtom;
        if (nucleotide instanceof Purine)
            sideChainAtom = nucleotide.getAtom(" N1 ");
        else sideChainAtom = nucleotide.getAtom(" N3 ");

        LocatedAtom backboneAtom = nucleotide.getAtom(" C5*");
        
        if ( (sideChainAtom == null) || (backboneAtom == null) ) return;

        
        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(nucleotide);

        // Extend rod one Angstrom past the Watson-Crick face atom
        Vector3D rodStart = backboneAtom.getCoordinates();
        MathVector rodDirection = sideChainAtom.getCoordinates().minus(rodStart);
        double rodLength = rodDirection.length() + 1.0;
        rodDirection = rodDirection.unit();
        MathVector rodEnd = rodStart.plus(rodDirection.scale(rodLength));
        
        Vector3D c = rodStart;

        Vector3D n = new DoubleVector3D( rodDirection ); // direction vector

        // Use sticks to tile path from atom center, c, to bond rodEnd
        int numberOfSticks = (int) Math.ceil(rodLength / stickLength);

        MathVector startStickCenter = c.plus(n.scale(stickLength * 0.5));
        MathVector endStickCenter = rodEnd.minus(n.scale(stickLength * 0.5));

        Color color = nucleotide.getDefaultColor();
        if (! (colorIndices.containsKey(color))) {
            colorIndices.put(color, new Integer(baseColorIndex));
            lut.SetTableValue(baseColorIndex, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
            baseColorIndex ++;
        }
        int colorScalar = ((Integer) colorIndices.get(color)).intValue();        

        MathVector stickCenterVector = endStickCenter.minus(startStickCenter);
        for (int s = 0; s < numberOfSticks; s++) {
            double alpha = 0.0;
            if (numberOfSticks > 1)
                alpha = s / (numberOfSticks - 1.0);
            Vector3D stickCenter = new DoubleVector3D( startStickCenter.plus(stickCenterVector.scale(alpha)) );
        
            linePoints.InsertNextPoint(stickCenter.getX(), stickCenter.getY(), stickCenter.getZ());
            lineNormals.InsertNextTuple3(n.getX(), n.getY(), n.getZ());

            glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
            lineScalars.InsertNextValue(colorScalar);
        }
                
    }
    
}
