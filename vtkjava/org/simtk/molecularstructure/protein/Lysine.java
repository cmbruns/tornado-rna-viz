/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Lysine extends AminoAcid {

    public Lysine() {
        super();
    }

    public Lysine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'K';
    }

    @Override
    public String getThreeLetterCode() {
        return "Lys";
    }

    @Override
    public String getResidueName() {
        return "lysine";
    }

}
