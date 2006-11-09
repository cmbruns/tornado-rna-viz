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
import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.mol.color.ColorScheme;
import org.simtk.mol.color.ConstantColor;
import org.simtk.mol.color.UnknownObjectColorException;
import org.simtk.molecularstructure.*;

import vtk.*;

public class BackboneStick extends GlyphCartoon {
    double sphereFudge = 1.05; // spheres aren't quit flush with cylinder for some reason
    int stickResolution = 7;
    double stickLength = 2.0;
    double stickRadius = 0.50;
    protected ColorScheme colorScheme = new ConstantColor(Color.blue);

    public BackboneStick() {
        this(0.50);
    }
    
    public BackboneStick(double r) {
        super();
        stickRadius = r;        
        initialize();
    }

    public BackboneStick(double r, int res) {
        super();
        stickRadius = r;
        stickResolution = res;
        initialize();
    }

    private void initialize() {
        // Make a cylinder to use as the basis of all bonds
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(stickResolution);
        cylinderSource.SetRadius(stickRadius);
        cylinderSource.SetHeight(stickLength);
        cylinderSource.SetCapping(0);
        
        // Cap the ends of the cylinder with spheres
        vtkSphereSource sphereSource1 = new vtkSphereSource();
        sphereSource1.SetRadius(stickRadius * sphereFudge);
        sphereSource1.SetPhiResolution(6);
        sphereSource1.SetThetaResolution(stickResolution);

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
        sphereSource2.SetThetaResolution(stickResolution);

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
        append.AddInput(sphere2Filter.GetOutput());
        
        // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
        vtkTransform cylinderTransform = new vtkTransform();
        cylinderTransform.Identity();
        cylinderTransform.RotateZ(90);
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        // cylinderFilter.SetInput(cylinderSource.GetOutput());
        cylinderFilter.SetInput(append.GetOutput());
        cylinderFilter.SetTransform(cylinderTransform);
        
        // Use lines as the glyph primitive
        setGlyphSource(cylinderFilter.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleNone();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar
        orientByNormal();
    }

    public void addMolecule(Molecule molecule) {
        if (molecule == null) return;

        // Don't add things that have already been added
        // if (glyphColors.containsKey(molecule)) return;
        
        Vector parentObjects = null;
        
        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(molecule);
        
        // If it's a biopolymer, index the glyphs by residue
        if (molecule instanceof Residue) {
            Residue residue = (Residue) molecule;
            currentObjects.remove(currentObjects.size() - 1); 
            addResidue(residue, currentObjects);
        }
        else if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (Residue residue : biopolymer.residues()) {
                addResidue(residue, null);
            }
        }
    }
    
    void addResidue(Residue residue, List parentObjects) {
        if (residue == null) return;
        
        // Don't add things that have already been added
        // if (glyphColors.containsKey(residue)) return;

        Vector3D backbonePosition;        
        try {backbonePosition = residue.getBackbonePosition();}
        catch (InsufficientAtomsException exc) {return;}
        if (backbonePosition == null) return;

        Vector3D nextPosition = null;
        if (residue.getNextResidue() != null)
            if (residue.getNextResidue() instanceof Residue) {
                try {
                    nextPosition = ((Residue)residue.getNextResidue()).getBackbonePosition();
                } catch (InsufficientAtomsException exc) {}
            }

        Vector3D previousPosition = null;
        if (residue.getPreviousResidue() != null) 
            if (residue.getPreviousResidue() instanceof Residue) {
                try {
                    previousPosition = ((Residue)residue.getPreviousResidue()).getBackbonePosition();
                } catch (InsufficientAtomsException exc) {}
            }
        
        // Collect molecular objects on which to index the glyphs
        Set<Object> currentObjects = new HashSet<Object>();
        if (parentObjects != null)
            for (Object o : parentObjects) currentObjects.add(o);
        currentObjects.add(residue);

        // Color color = residue.getDefaultColor();
        Color color;
        try {color = colorScheme.colorOf(residue);}
        catch (UnknownObjectColorException exc) {color = Color.white;}
        
        double colorScalar = getColorIndex(residue);

        if (previousPosition != null) {
            // Point midway between two residues
            Vector3DClass midPosition = new Vector3DClass( backbonePosition.plus(previousPosition).times(0.5) );

            // Direction of this half bond
            // To make the two half-bonds line up flush, choose a deterministic direction between the two atoms
            if (residue.getResidueNumber() > residue.getPreviousResidue().getResidueNumber())
                tileSticks(backbonePosition, midPosition, currentObjects, colorScalar);
            else
                tileSticks(midPosition, backbonePosition, currentObjects, colorScalar);
        }

        if (nextPosition != null) {
            // Point midway between two residues
            Vector3DClass midPosition = new Vector3DClass( backbonePosition.plus(nextPosition).times(0.5) );

            // Direction of this half bond
            // To make the two half-bonds line up flush, choose a deterministic direction between the two atoms
            if (residue.getResidueNumber() > residue.getNextResidue().getResidueNumber())
                tileSticks(backbonePosition, midPosition, currentObjects, colorScalar);
            else
                tileSticks(midPosition, backbonePosition, currentObjects, colorScalar);
        }
    }
    
    private void tileSticks(Vector3D segmentStart, Vector3D segmentEnd, Set<Object> currentObjects, double colorScalar) {
        Vector3D segmentDirection = segmentEnd.minus(segmentStart);

        // Use sticks to tile path from atom center, c, to bond midpoint
        int numberOfSticks = (int) Math.ceil(segmentDirection.length() / stickLength);

        // Scale to unit length.  NOTE - effect of this on calculations above and below
        segmentDirection = segmentDirection.unit();
        
        Vector3D startStickCenter = new Vector3DClass( segmentStart.plus(segmentDirection.times(stickLength * 0.5)) );
        Vector3D endStickCenter = new Vector3DClass( segmentEnd.minus(segmentDirection.times(stickLength * 0.5)) );

        Vector3D stickCenterVector = new Vector3DClass( endStickCenter.minus(startStickCenter) );
        for (int s = 0; s < numberOfSticks; s++) {
            double alpha = 0.0;
            if (numberOfSticks > 1)
                alpha = s / (numberOfSticks - 1.0);
            Vector3DClass stickCenter = new Vector3DClass( startStickCenter.plus(stickCenterVector.times(alpha)) );
        
            linePoints.InsertNextPoint(stickCenter.getX(), stickCenter.getY(), stickCenter.getZ());
            lineNormals.InsertNextTuple3(segmentDirection.getX(), segmentDirection.getY(), segmentDirection.getZ());

            // glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
            colorScalars.InsertNextValue(colorScalar);   
            
            isPopulated = true;
        }        
    }
}
