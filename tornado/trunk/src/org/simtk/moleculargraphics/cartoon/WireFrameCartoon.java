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
    // Key: glyph index number value: atoms at the other end of a bond
    Hashtable otherAtomIndices = new Hashtable();
    
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
            for (Iterator i = residue.getAtomIterator(); i.hasNext(); ) {
                PDBAtom atom = (PDBAtom) i.next();
                addAtom(atom, currentObjects);                    
            }
        }
        else if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (Iterator iterResidue = biopolymer.residues().iterator(); iterResidue.hasNext(); ) {
                addMolecule((Residue) iterResidue.next(), currentObjects);
            }
        }
        else for (Iterator i1 = molecule.getAtomIterator(); i1.hasNext(); ) {
            PDBAtom atom = (PDBAtom) i1.next();
            addAtom(atom, currentObjects);
        }        
    }
    
    void addAtom(PDBAtom atom, Vector parentObjects) {
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

        Vector3D c = atom.getCoordinates();

        int colorScalar = (int) (atom.getMass());

        Color col = atom.getDefaultAtomColor();
        lut.SetTableValue(colorScalar, col.getRed()/255.0, col.getGreen()/255.0, col.getBlue()/255.0, 1.0);
        
        // For unbonded atoms, put a cross at atom position
        if (atom.getBonds().size() == 0) {
            createSingleAtomGlyph(atom, currentObjects, colorScalar);
        }
        // For bonded atoms, draw a line for each bond
        else for (Iterator i2 = atom.getBonds().iterator(); i2.hasNext(); ) {
            LocatedAtom atom2 = (LocatedAtom) i2.next();
            
            createBondGlyph(atom, atom2, currentObjects, colorScalar);
        }
    }

    public void show(Molecule molecule) {
        addMolecule(molecule, null);
        glyphColors.show(molecule);
    }

    /** Change graphics primitives only for those objects that have moved
     * 
     * @param molecule
     */
    public void updateCoordinates(Molecule molecule) {
        // Each bond glyph depends on two atoms, asymmetrically
        
        boolean modified = false;

        for (Iterator i = molecule.getAtomIterator(); i.hasNext(); ) {
            PDBAtom atom = (PDBAtom) i.next();
            if (glyphColors.containsKey(atom)) {
                Vector glyphs = (Vector) glyphColors.objectGlyphs.get(atom); // TODO encapsulate this
                for (Iterator g = glyphs.iterator();g.hasNext();) {
                    GlyphPosition pos = (GlyphPosition) g.next();
                    if (atom.getBonds().size() < 1) {
                        // Single atom glyph, only need to change center
                        pos.setPosition(atom.getCoordinates());
                        modified = true;
                    }
                    else {
                        int glyphIndex = pos.arrayIndex;
                        if (otherAtomIndices.containsKey(new Integer(glyphIndex))) {
                            PDBAtom atom2 = (PDBAtom) otherAtomIndices.get(new Integer(glyphIndex));
                            DoubleVector3D normal = getBondNormal(atom, atom2);
                            DoubleVector3D middle = getBondMiddle(atom, atom2);
                            pos.setPosition(middle);
                            pos.setNormal(normal);

                            // Even if the other atom is not in the modified molecule, it should be redrawn anyway
                            if (! molecule.containsAtom(atom2)) {
                                normal = getBondNormal(atom2, atom);
                                middle = getBondMiddle(atom2, atom);
                                // TODO find glyph at the other side of the bond
                            }
                            
                            modified = true;
                        }
                    }
                }
            }
        }
        
        if (modified) {
            linePoints.Modified();
            lineNormals.Modified();
        }
        else {
            System.out.println("No modification");
        }
        
    }
    
    private DoubleVector3D getBondNormal(LocatedAtom a1, LocatedAtom a2) {
        Vector3D c = a1.getCoordinates();        
        MathVector midpoint = c.plus(a2.getCoordinates()).scale(0.5); // middle of bond
        MathVector b = c.plus(midpoint).scale(0.5); // middle of half-bond
        MathVector n = midpoint.minus(c); // direction/length vector
        return new DoubleVector3D(n);
    }
    private DoubleVector3D getBondMiddle(LocatedAtom a1, LocatedAtom a2) {
        Vector3D c = a1.getCoordinates();        
        MathVector midpoint = c.plus(a2.getCoordinates()).scale(0.5); // middle of bond
        MathVector b = c.plus(midpoint).scale(0.5); // middle of half-bond
        return new DoubleVector3D(b);
    }
    private void createBondGlyph(LocatedAtom atom, LocatedAtom atom2, Vector currentObjects, int colorScalar) {
        DoubleVector3D b = getBondMiddle(atom, atom2);
        DoubleVector3D n = getBondNormal(atom, atom2);

        linePoints.InsertNextPoint(b.getX(), b.getY(), b.getZ());
        lineNormals.InsertNextTuple3(n.getX(), n.getY(), n.getZ());

        int glyphIndex = lineScalars.GetNumberOfTuples();
        
        glyphColors.add(currentObjects, lineData, glyphIndex, colorScalar);
        otherAtomIndices.put(new Integer(glyphIndex), atom2);
        
        lineScalars.InsertNextValue(colorScalar);        
    }
    private void createSingleAtomGlyph(LocatedAtom atom, Vector currentObjects, int colorScalar) {
        Vector3D c = atom.getCoordinates();
        
        // X
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        lineNormals.InsertNextTuple3(crossSize, 0.0, 0.0);

        glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
        lineScalars.InsertNextValue(colorScalar);

        // Y
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        lineNormals.InsertNextTuple3(0.0, crossSize, 0.0);

        glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
        lineScalars.InsertNextValue(colorScalar);

        // Z
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        lineNormals.InsertNextTuple3(0.0, 0.0, crossSize);

        glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
        lineScalars.InsertNextValue(colorScalar);
    }
}
