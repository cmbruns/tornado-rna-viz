/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Tryptophan extends AminoAcid {

    public Tryptophan() {
        super();
    }

    public Tryptophan(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'W';
    }

    @Override
    public String getThreeLetterCode() {
        return "Trp";
    }

    @Override
    public String getResidueName() {
        return "tryptophan";
    }

}
