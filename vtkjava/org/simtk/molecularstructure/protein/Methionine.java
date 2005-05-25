/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Methionine extends AminoAcid {

    public Methionine() {
        super();
    }

    public Methionine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'M';
    }

    @Override
    public String getThreeLetterCode() {
        return "Met";
    }

    @Override
    public String getResidueName() {
        return "methionine";
    }

}
