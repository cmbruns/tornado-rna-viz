/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Isoleucine extends AminoAcid {

    public Isoleucine() {
        super();
    }

    public Isoleucine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'I';
    }

    @Override
    public String getThreeLetterCode() {
        return "Ile";
    }

    @Override
    public String getResidueName() {
        return "isoleucine";
    }

}
