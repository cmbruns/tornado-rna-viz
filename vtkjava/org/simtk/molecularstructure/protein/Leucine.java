/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Leucine extends AminoAcid {

    public Leucine() {
        super();
    }

    public Leucine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'L';
    }

    
    public String getThreeLetterCode() {
        return "Leu";
    }

    
    public String getResidueName() {
        return "leucine";
    }

}
