/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Glutamate extends AminoAcid {

    public Glutamate() {
        super();
    }

    public Glutamate(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    public char getOneLetterCode() {
        return 'E';
    }

    public String getThreeLetterCode() {
        return "Glu";
    }

    public String getResidueName() {
        return "glutamate";
    }

}
