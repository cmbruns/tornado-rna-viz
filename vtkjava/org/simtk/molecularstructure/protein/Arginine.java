/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Arginine extends AminoAcid {

    public Arginine() {
        super();
    }

    public Arginine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    public char getOneLetterCode() {
        return 'R';
    }

    public String getThreeLetterCode() {
        return "Arg";
    }

    public String getResidueName() {
        return "arginine";
    }

}
