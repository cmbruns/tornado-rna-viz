/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;
import org.simtk.molecularstructure.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule of protein.
 */
public class Protein extends Biopolymer {
    public Protein() {} // Empty molecule
    public Protein(PDBAtomSet atomSet) {super(atomSet);}

    protected void addGenericResidueBonds() {
        super.addGenericResidueBonds();
        // TODO this is not working
        addGenericResidueBond(" C  ", " N  ");
    }
}
