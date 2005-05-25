/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Glycine extends AminoAcid {

    public Glycine() {
        super();
    }

    public Glycine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'G';
    }

    @Override
    public String getThreeLetterCode() {
        return "Gly";
    }

    @Override
    public String getResidueName() {
        return "glycine";
    }

}
