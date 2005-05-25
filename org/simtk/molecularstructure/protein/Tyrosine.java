/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Tyrosine extends AminoAcid {

    public Tyrosine() {
        super();
    }

    public Tyrosine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'Y';
    }

    @Override
    public String getThreeLetterCode() {
        return "Tyr";
    }

    @Override
    public String getResidueName() {
        return "tyrosine";
    }

}
