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

import java.awt.Color;
import java.util.*;

import vtk.*;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.geometry3d.*;

/** 
 * @author Christopher Bruns
 * 
 * Base class for graphical representation of a molecule.
 *
 * MolecularCartoon does not actually generate a representation.
 * At least one of the represent functions must be overloaded to 
 * generate actual 3D graphics primitives.
 */
abstract public class MolecularCartoonFirstTry extends MolecularCartoon {
    protected Hashtable atomPositions = new Hashtable();


    /**
     * Update graphics primitives related to atom objects
     * @param mol
     */
    public void updateAtomCoordinates() {
        HashSet vtkObjects = new HashSet();
        
        for (Iterator ai = atomPositions.keySet().iterator(); ai.hasNext();) {
            Atom a = (Atom) ai.next();
            Vector atomPrimitives = (Vector) atomPositions.get(a);
            for (Iterator pi = atomPrimitives.iterator(); pi.hasNext();) {
                GraphicsPrimitivePosition p = (GraphicsPrimitivePosition) pi.next();
                vtkObject o = p.update(a.getCoordinates());
                if (o != null)
                    vtkObjects.add(o);
                p.update(a.getCoordinates());
            }
        }
        
        for (Iterator i = vtkObjects.iterator(); i.hasNext();) {
            vtkObject object = (vtkObject) i.next();
            object.Modified();
        }
    }
    /**
     * Update graphics primitives related to atom objects
     * @param mol
     */
    public void updateAtomCoordinates(Molecule mol) {
        HashSet vtkObjects = new HashSet();
        
        for (Iterator ai = mol.getAtoms().iterator(); ai.hasNext();) {
            Atom a = (Atom) ai.next();
            if (! atomPositions.containsKey(a) ) continue;
            Vector atomPrimitives = (Vector) atomPositions.get(a);
            for (Iterator pi = atomPrimitives.iterator(); pi.hasNext();) {
                GraphicsPrimitivePosition p = (GraphicsPrimitivePosition) pi.next();
                vtkObject o = p.update(a.getCoordinates());
                if (o != null)
                    vtkObjects.add(o);
                p.update(a.getCoordinates());
            }
        }
        
        for (Iterator i = vtkObjects.iterator(); i.hasNext();) {
            vtkObject object = (vtkObject) i.next();
            object.Modified();
        }
    }

    /**
     * Update graphical primitives to reflect a change in atomic positions
     *
     */
    abstract public void updateCoordinates();
    public void updateCoordinates(Molecule mol) {updateCoordinates();}
    
    /**
     * Create a yellow graphical object that highlights the selected residue
     * @param residue
     * @return
     */
    public vtkProp highlight(Residue residue) {
        return highlight(residue, Color.yellow);
    }
    abstract public vtkProp highlight(Residue residue, Color color);

        /**
     * 
     * Default behavior is for a MoleculeCollection to be the sum of its molecules
     * @param molecules
     * @return returns null if there are no graphics primitives to render
     */
    public vtkAssembly represent(MoleculeCollection molecules) {
        boolean hasContents = false;
        vtkAssembly assembly = new vtkAssembly();        
        for (int m = 0; m < molecules.getMoleculeCount(); m++) {
            vtkAssembly moleculeAssembly = represent(molecules.getMolecule(m));
            if (moleculeAssembly != null) {
                assembly.AddPart(moleculeAssembly);
                hasContents = true;
            }
        }        
        if (hasContents) return assembly;
        else return null;
    }
    
    public vtkAssembly represent(Molecule molecule) {
        boolean hasContents = false;
        vtkAssembly assembly = new vtkAssembly();
        
        // Figure out if its a Biopolymer
        // if so, do residues
        if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (int r = 0; r < biopolymer.getResidueCount(); r++) {
	            vtkAssembly residueAssembly = represent(biopolymer.getResidue(r));
	            if (residueAssembly != null) {
	                assembly.AddPart(residueAssembly);
	                hasContents = true;
	            }                
            }
        }
        else { // Do atoms instead of residues, especially if this "molecule" is a residue itself
	        for (int a = 0; a < molecule.getAtomCount(); a++) {
	            vtkAssembly atomAssembly = represent(molecule.getAtom(a));
	            if (atomAssembly != null) {
	                assembly.AddPart(atomAssembly);
	                hasContents = true;
	            }
	        }    
        }
        
        if (hasContents) return assembly;
        else return null;
    }

    public vtkAssembly represent(Atom atom) {
        boolean hasContents = false;
        vtkAssembly assembly = new vtkAssembly();        

        // Render something here
        
        if (hasContents) return assembly;
        else return null;
    }

    abstract class GraphicsPrimitivePosition {
        /**
         * Modify the position of an existing graphics primitive
         * @param v
         */
        abstract public vtkObject update(BaseVector3D v);
    }
    class VTKPointPosition extends GraphicsPrimitivePosition {
        vtkPoints points;
        int pointID;
        VTKPointPosition(vtkPoints p, int id) {points = p; pointID = id;}
        public vtkObject update(BaseVector3D v) {
            points.SetPoint(pointID, v.getX(), v.getY(), v.getZ());
            return points;
        }
    }
    class VTKSpherePosition extends GraphicsPrimitivePosition {
        vtkSphereSource sphere;
        VTKSpherePosition(vtkSphereSource s) {sphere = s;}
        public vtkObject update(BaseVector3D v) {
            sphere.SetCenter(v.getX(), v.getY(), v.getZ());
            return sphere;
        }
    }
}
