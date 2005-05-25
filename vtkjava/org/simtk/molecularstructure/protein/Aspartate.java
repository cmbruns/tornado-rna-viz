/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Aspartate extends AminoAcid {

    public Aspartate() {
        super();
    }

    public Aspartate(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'D';
    }

    @Override
    public String getThreeLetterCode() {
        return "Asp";
    }

    @Override
    public String getResidueName() {
        return "aspartate";
    }

}
