/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Threonine extends AminoAcid {

    public Threonine() {
        super();
    }

    public Threonine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    @Override
    public char getOneLetterCode() {
        return 'T';
    }

    @Override
    public String getThreeLetterCode() {
        return "Thr";
    }

    @Override
    public String getResidueName() {
        return "threonine";
    }

}
