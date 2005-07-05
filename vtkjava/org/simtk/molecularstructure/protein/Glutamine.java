/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Glutamine extends AminoAcid {

    public Glutamine() {
        super();
    }

    public Glutamine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    public char getOneLetterCode() {
        return 'Q';
    }

    public String getThreeLetterCode() {
        return "Gln";
    }

    public String getResidueName() {
        return "glutamine";
    }

}
