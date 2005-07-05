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

    public char getOneLetterCode() {
        return 'G';
    }

    public String getThreeLetterCode() {
        return "Gly";
    }

    public String getResidueName() {
        return "glycine";
    }

}
