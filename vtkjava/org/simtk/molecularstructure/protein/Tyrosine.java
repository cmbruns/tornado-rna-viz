/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Tyrosine extends AminoAcid {

    public Tyrosine() {
        super();
    }

    public Tyrosine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'Y';
    }

    
    public String getThreeLetterCode() {
        return "Tyr";
    }

    
    public String getResidueName() {
        return "tyrosine";
    }

}
