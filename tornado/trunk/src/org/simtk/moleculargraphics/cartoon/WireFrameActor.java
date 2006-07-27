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
public class WireFrameActor extends GlyphCartoon {
    // Key: glyph index number value: atoms at the other end of a bond
    Hashtable otherAtomIndices = new Hashtable();
    
    static double crossSize = 1.0;
    static vtkLineSource lineSource;
    static {
        lineSource = new vtkLineSource();
        lineSource.SetPoint1(-0.5, 0.0, 0.0);
        lineSource.SetPoint2(0.5, 0.0, 0.0);
    }
        
    public WireFrameActor() {
        super();

        // Use lines as the glyph primitive
        setGlyphSource(lineSource.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleByNormal();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar
        
        actor.GetProperty().SetLineWidth(2.0);
    }
    
    public void addMolecule(Molecule molecule) {
        if (molecule == null) return;

        // Don't add things that have already been added
        // if (glyphColors.containsKey(molecule)) return;
        
        Set<Object> parentObjects = null;
        
        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector<Object>();
        if (parentObjects != null)
            for (Object o : parentObjects) 
                currentObjects.add(o);

        currentObjects.add(molecule);
        
        // If it's a biopolymer, index the glyphs by residue
        if (molecule instanceof Residue) {
            Residue residue = (Residue) molecule;
            for (Iterator i = residue.atoms().iterator(); i.hasNext(); ) {
                Atom atom = (Atom) i.next();
                addAtom(atom, currentObjects);                    
            }
        }
        else if (molecule instanceof BiopolymerClass) {
            BiopolymerClass biopolymer = (BiopolymerClass) molecule;
            for (Iterator<Residue> iterResidue = biopolymer.residues().iterator(); iterResidue.hasNext(); ) {
                addResidue(iterResidue.next());
            }
        }
        else for (Iterator i1 = molecule.atoms().iterator(); i1.hasNext(); ) {
            Atom atom = (Atom) i1.next();
            addAtom(atom, currentObjects);
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

        // For unbonded atoms, put a cross at atom position
        if (atom.bonds().size() == 0) {
            createSingleAtomGlyph(atom, currentObjects, colorScalar);
        }
        // For bonded atoms, draw a line for each bond
        else for (Iterator<Atom> i2 = atom.bonds().iterator(); i2.hasNext(); ) {
            Atom atom2 =  i2.next();
            
            createBondGlyph(atom, atom2, currentObjects, colorScalar);
        }
    }

    /** Change graphics primitives only for those objects that have moved
     * 
     * @param molecule
     */
    private Vector3DClass getBondNormal(Atom a1, Atom a2) {
        Vector3D c = a1.getCoordinates();        
        Vector3D midpoint = c.plus(a2.getCoordinates()).times(0.5); // middle of bond
        Vector3D b = c.plus(midpoint).times(0.5); // middle of half-bond
        Vector3D n = midpoint.minus(c); // direction/length vector
        return new Vector3DClass(n);
    }
    private Vector3DClass getBondMiddle(Atom a1, Atom a2) {
        Vector3D c = a1.getCoordinates();        
        Vector3D midpoint = c.plus(a2.getCoordinates()).times(0.5); // middle of bond
        Vector3D b = c.plus(midpoint).times(0.5); // middle of half-bond
        return new Vector3DClass(b);
    }
    private void createBondGlyph(Atom atom, Atom atom2, Vector currentObjects, double colorScalar) {
        Vector3DClass b = getBondMiddle(atom, atom2);
        Vector3DClass n = getBondNormal(atom, atom2);

        linePoints.InsertNextPoint(b.getX(), b.getY(), b.getZ());
        lineNormals.InsertNextTuple3(n.getX(), n.getY(), n.getZ());

        int glyphIndex = colorScalars.GetNumberOfTuples();
        
        // glyphColors.add(currentObjects, lineData, glyphIndex, colorScalar);
        otherAtomIndices.put(new Integer(glyphIndex), atom2);
        
        colorScalars.InsertNextValue(colorScalar);  
        
        isPopulated = true;
    }
    private void createSingleAtomGlyph(Atom atom, Vector currentObjects, double colorScalar) {
        Vector3D c = atom.getCoordinates();
        
        // X
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        lineNormals.InsertNextTuple3(crossSize, 0.0, 0.0);

        // glyphColors.add(currentObjects, lineData, colorScalars.GetNumberOfTuples(), colorScalar);
        colorScalars.InsertNextValue(colorScalar);

        // Y
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        lineNormals.InsertNextTuple3(0.0, crossSize, 0.0);

        // glyphColors.add(currentObjects, lineData, colorScalars.GetNumberOfTuples(), colorScalar);
        colorScalars.InsertNextValue(colorScalar);

        // Z
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        lineNormals.InsertNextTuple3(0.0, 0.0, crossSize);

        // glyphColors.add(currentObjects, lineData, colorScalars.GetNumberOfTuples(), colorScalar);
        colorScalars.InsertNextValue(colorScalar);
        
        isPopulated = true;
    }
}
