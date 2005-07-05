/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Asparagine extends AminoAcid {

    public Asparagine() {
        super();
    }

    public Asparagine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    public char getOneLetterCode() {
        return 'N';
    }

    public String getThreeLetterCode() {
        return "Asn";
    }

    public String getResidueName() {
        return "asparagine";
    }

}
