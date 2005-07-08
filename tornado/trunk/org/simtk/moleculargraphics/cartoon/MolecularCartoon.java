/*
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.*;

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
    protected Hashtable atomPositions = new Hashtable();

    // replaced Java 1.5 enum with Java 1.4 compliant
    public static class CartoonType {
        static public CartoonType SPACE_FILLING = new CartoonType();
        static public CartoonType BALL_AND_STICK = new CartoonType();
        static public CartoonType ROPE_AND_CYLINDER = new CartoonType();
        static public CartoonType RESIDUE_SPHERE = new CartoonType();
        static public CartoonType BACKBONE_TRACE = new CartoonType();
        static public CartoonType WIRE_FRAME = new CartoonType();
    };

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
