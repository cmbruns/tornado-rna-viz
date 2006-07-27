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

import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

import vtk.vtkCylinderSource;
import vtk.vtkTransform;
import vtk.vtkTransformPolyDataFilter;

public class BondStickCartoon extends GlyphCartoon {
    double stickLength = 0.45;
    double stickRadius = 0.15;

    public BondStickCartoon() {
        this(0.15);
    }
        
    public BondStickCartoon(double r) {
        super();

        stickRadius = r;
        
        // Make a cylinder to use as the basis of all bonds
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(5);
        cylinderSource.SetRadius(stickRadius);
        cylinderSource.SetHeight(stickLength);
        cylinderSource.SetCapping(0);
        // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
        vtkTransform cylinderTransform = new vtkTransform();
        cylinderTransform.Identity();
        cylinderTransform.RotateZ(90);
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        cylinderFilter.SetInput(cylinderSource.GetOutput());
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

        Vector parentObjects = null;
        
        // Don't add things that have already been added
        // if (glyphColors.containsKey(molecule)) return;
        
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
            for (Iterator i = residue.atoms().iterator(); i.hasNext(); ) {
                addAtom((Atom)i.next(), currentObjects);
            }
        }
        else if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (Iterator iterResidue = biopolymer.residues().iterator(); iterResidue.hasNext(); ) {
                Residue residue = (Residue) iterResidue.next();
                    addResidue(residue);
            }
        }
        else for (Iterator<Atom> i1 = molecule.atoms().iterator(); i1.hasNext(); ) {
            addAtom(i1.next(), currentObjects);
        }        
    }
    
    void addResidue(Residue residue) {
        for (Atom atom : residue.atoms())
            addAtom(atom, null);
    }
    
    void addAtom(Atom atom, Vector parentObjects) {
        if (atom == null) return;
        
        // Don't add things that have already been added
        // if (glyphColors.containsKey(atom)) return;

        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(atom);

        Vector3D c = atom.getCoordinates();

        double colorScalar = toonColors.getColorIndex(atom);

        // For bonded atoms, draw a line for each bond
        for (Atom atom2 : atom.bonds()) {
            Vector3D midpoint = new Vector3DClass( c.plus(atom2.getCoordinates()).times(0.5) ); // middle of bond
            Vector3D b = new Vector3DClass( c.plus(midpoint).times(0.5) ); // middle of half-bond
            MutableVector3D n = new Vector3DClass( midpoint.minus(c).unit() ); // direction vector

            // Use sticks to tile path from atom center, c, to bond midpoint
            int numberOfSticks = (int) Math.ceil(c.distance(midpoint) / stickLength);

            Vector3D startStickCenter = new Vector3DClass( c.plus(n.times(stickLength * 0.5)) );
            Vector3D endStickCenter = new Vector3DClass( midpoint.minus(n.times(stickLength * 0.5)) );

            // Direction of this half bond
            // To make the two half-bonds line up flush, choose a deterministic direction between the two atoms
            if ( (atom.getAtomName().compareTo(atom2.getAtomName())) > 0 ) {
                n.timesEquals(-1.0);
            }
            
            Vector3D stickCenterVector = new Vector3DClass( endStickCenter.minus(startStickCenter) );
            for (int s = 0; s < numberOfSticks; s++) {
                double alpha = 0.0;
                if (numberOfSticks > 1)
                    alpha = s / (numberOfSticks - 1.0);
                Vector3D stickCenter = new Vector3DClass( startStickCenter.plus(stickCenterVector.times(alpha)) );
            
                linePoints.InsertNextPoint(stickCenter.getX(), stickCenter.getY(), stickCenter.getZ());
                lineNormals.InsertNextTuple3(n.getX(), n.getY(), n.getZ());
    
                // glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
                colorScalars.InsertNextValue(colorScalar);
                
                isPopulated = true;
            }
        }
    }
    
}
