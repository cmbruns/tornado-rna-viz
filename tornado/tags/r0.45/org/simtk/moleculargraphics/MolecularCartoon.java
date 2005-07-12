/*
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.Color;

import vtk.*;

import org.simtk.atomicstructure.*;
import org.simtk.molecularstructure.*;
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
abstract public class MolecularCartoon {

    public enum CartoonType {
        SPACE_FILLING,
        BALL_AND_STICK,
        ROPE_AND_CYLINDER,
        RESIDUE_SPHERE
    };

    /**
     * Create a yellow graphical object that highlights the selected residue
     * @param residue
     * @return
     */
    public vtkProp highlight(Residue residue) {
        return highlight(residue, Color.yellow);
    }
    abstract public vtkProp highlight(Residue residue, Color color);

    abstract public Residue getNearbyResidue(Vector3D v);
    
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
    
}