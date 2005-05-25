/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Serine extends AminoAcid {

    public Serine() {
        super();
    }

    public Serine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'S';
    }

    @Override
    public String getThreeLetterCode() {
        return "Ser";
    }

    @Override
    public String getResidueName() {
        return "serine";
    }

}
