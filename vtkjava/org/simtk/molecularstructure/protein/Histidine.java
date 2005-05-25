/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Histidine extends AminoAcid {

    public Histidine() {
        super();
    }

    public Histidine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'H';
    }

    @Override
    public String getThreeLetterCode() {
        return "His";
    }

    @Override
    public String getResidueName() {
        return "histidine";
    }

}
