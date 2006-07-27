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

import java.util.*;
import vtk.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class AtomSphereActor extends GlyphCartoon {

    vtkSphereSource sphereSource = new vtkSphereSource();
    double sizeScale = 1.0;
    public boolean scaleByAtom = true;

    public AtomSphereActor() {
        this(1.0);
    }

    public AtomSphereActor(double scale) {
        super();

        sizeScale = scale;
        
        sphereSource.SetRadius(1.0);
        sphereSource.SetThetaResolution(8);
        sphereSource.SetPhiResolution(8);
        
        // Use lines as the glyph primitive
        setGlyphSource(sphereSource.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleByNormal(); // Take sphere size from glyph normal
        colorByScalar(); // Take color from glyph scalar
    }
    
    public void setScale(double s) {sizeScale = s;}
    
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
                Atom atom = (Atom) i.next();
                addAtom(atom, currentObjects);                    
            }
        }
        
        else if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (Residue residue : biopolymer.residues())
                addResidue(residue);
        }
        
        else for (Atom atom : molecule.atoms()) {
        // else for (Iterator i1 = molecule.getAtomIterator(); i1.hasNext(); ) {
            // Atom atom = (Atom) i1.next();
            addAtom(atom, currentObjects);
        }        
    }
    
    public void addResidue(Residue residue) {
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

        // Draw a sphere for each atom
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        
        double radius = sizeScale;
        if (scaleByAtom) radius *= atom.getVanDerWaalsRadius();
        lineNormals.InsertNextTuple3(radius, 0.0, 0.0);

        // glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
        colorScalars.InsertNextValue(colorScalar);
        
        isPopulated = true;
    }
}