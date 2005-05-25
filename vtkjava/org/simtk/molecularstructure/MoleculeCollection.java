/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure;

import java.io.*;
import java.util.*;

import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * A collection of one or more molecules, as might be found in a PDB file.
 */
public class MoleculeCollection {
    Vector<Atom> atoms = new Vector<Atom>();
    Vector<Molecule> molecules = new Vector<Molecule>();

    Vector3D centerOfMass = new Vector3D();
    double mass = 0;

    public double getMass() {
        return mass;
    }
    public Vector3D getCenterOfMass() {
        if (mass <= 0) return null;
        return centerOfMass;
    }
    
    public Vector<Molecule> molecules() {return molecules;}
    
    public int getMoleculeCount() {return molecules.size();}
    public Molecule getMolecule(int i) {return (Molecule) molecules.elementAt(i);}
    
    public int getAtomCount() {return atoms.size();}
    
    public void loadPDBFormat(String fileName) throws FileNotFoundException, IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
		loadPDBFormat(fileStream);
        fileStream.close();
	}

    public void loadPDBFormat(InputStream is) throws IOException {
        
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
		Molecule mol = Molecule.createFactoryPDBMolecule(reader);

		// TODO do something more proactive if there are no molecules (such as throw an exception)
		if (mol == null) {return;}
		
		while (mol.getAtomCount() > 0) {
		    molecules.addElement(mol);

		    double myMassProportion = getMass() / (getMass() + mol.getMass());
		    centerOfMass = centerOfMass.scale(myMassProportion).plus(
		            mol.getCenterOfMass().scale(1.0 - myMassProportion) );
		    
		    mass += mol.getMass();
		    
		    for (int a = 0; a < mol.getAtomCount(); a++)
		        atoms.addElement(mol.getAtom(a));
		    
		    mol = Molecule.createFactoryPDBMolecule(reader);
		    if (mol == null) break;
		}
	}
}
