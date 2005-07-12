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
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import vtk.*;

import java.awt.*;
import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

/** 
 * @author Christopher Bruns
 * 
 * Draw a simple colored line joining each pair of bonded atoms.
 * Plus a cross at each non-bonded atom
 */
public class WireFrameCartoon extends GlyphCartoon {

    static double crossSize = 1.0;
    static vtkLineSource lineSource;
    static {
        lineSource = new vtkLineSource();
        lineSource.SetPoint1(-0.5, 0.0, 0.0);
        lineSource.SetPoint2(0.5, 0.0, 0.0);
    }
        
    public WireFrameCartoon() {
        super();

        // Use lines as the glyph primitive
        setGlyphSource(lineSource.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleByNormal();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar
        
        glyphActor.GetProperty().SetLineWidth(2.0);
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
        if (molecule instanceof Residue) {
            Residue residue = (Residue) molecule;
            for (int a = 0; a < residue.getAtomCount(); a++) {
                Atom atom = residue.getAtom(a);
                addAtom(atom, currentObjects);                    
            }
        }
        else if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (Iterator iterResidue = biopolymer.residues().iterator(); iterResidue.hasNext(); ) {
                addMolecule((Residue) iterResidue.next(), currentObjects);
            }
        }
        else for (Iterator i1 = molecule.getAtoms().iterator(); i1.hasNext(); ) {
            Atom atom = (Atom) i1.next();
            addAtom(atom, currentObjects);
        }        
    }
    
    void addAtom(Atom atom, Vector parentObjects) {
        if (atom == null) return;
        
        // Don't add things that have already been added
        if (glyphColors.containsKey(atom)) return;

        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(atom);

        BaseVector3D c = atom.getCoordinates();

        int colorScalar = (int) (atom.getMass());

        Color col = atom.getDefaultColor();
        lut.SetTableValue(colorScalar, col.getRed()/255.0, col.getGreen()/255.0, col.getBlue()/255.0, 1.0);
        
        // For unbonded atoms, put a cross at atom position
        if (atom.getBonds().size() == 0) {
            // X
            linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
            lineNormals.InsertNextTuple3(crossSize, 0.0, 0.0);

            glyphColors.add(currentObjects, lineScalars, lineScalars.GetNumberOfTuples(), colorScalar);
            lineScalars.InsertNextValue(colorScalar);

            // Y
            linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
            lineNormals.InsertNextTuple3(0.0, crossSize, 0.0);

            glyphColors.add(currentObjects, lineScalars, lineScalars.GetNumberOfTuples(), colorScalar);
            lineScalars.InsertNextValue(colorScalar);

            // Z
            linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
            lineNormals.InsertNextTuple3(0.0, 0.0, crossSize);

            glyphColors.add(currentObjects, lineScalars, lineScalars.GetNumberOfTuples(), colorScalar);
            lineScalars.InsertNextValue(colorScalar);
        }
        // For bonded atoms, draw a line for each bond
        else for (Iterator i2 = atom.getBonds().iterator(); i2.hasNext(); ) {
            Atom atom2 = (Atom) i2.next();
            Vector3D midpoint = c.plus(atom2.getCoordinates()).scale(0.5); // middle of bond
            Vector3D b = c.plus(midpoint).scale(0.5); // middle of half-bond
            Vector3D n = midpoint.minus(c); // direction/length vector

            linePoints.InsertNextPoint(b.getX(), b.getY(), b.getZ());
            lineNormals.InsertNextTuple3(n.getX(), n.getY(), n.getZ());

            glyphColors.add(currentObjects, lineScalars, lineScalars.GetNumberOfTuples(), colorScalar);
            lineScalars.InsertNextValue(colorScalar);
        }
    }

    public void show(Molecule molecule) {
        addMolecule(molecule, null);
        glyphColors.show(molecule);
    }

    
}
