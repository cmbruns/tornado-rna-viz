/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Methionine extends AminoAcid {

    public Methionine() {
        super();
    }

    public Methionine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'M';
    }

    
    public String getThreeLetterCode() {
        return "Met";
    }

    
    public String getResidueName() {
        return "methionine";
    }

}
