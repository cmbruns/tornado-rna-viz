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

    @Override
    public char getOneLetterCode() {
        return 'Q';
    }

    @Override
    public String getThreeLetterCode() {
        return "Gln";
    }

    @Override
    public String getResidueName() {
        return "glutamine";
    }

}
