/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Phenylalanine extends AminoAcid {

    public Phenylalanine() {
        super();
    }

    public Phenylalanine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'F';
    }

    
    public String getThreeLetterCode() {
        return "Phe";
    }

    
    public String getResidueName() {
        return "phenylalanine";
    }

}
