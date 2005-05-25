/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Leucine extends AminoAcid {

    public Leucine() {
        super();
    }

    public Leucine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'L';
    }

    @Override
    public String getThreeLetterCode() {
        return "Leu";
    }

    @Override
    public String getResidueName() {
        return "leucine";
    }

}
