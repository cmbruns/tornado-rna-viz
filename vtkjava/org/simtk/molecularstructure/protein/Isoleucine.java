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

    
    public char getOneLetterCode() {
        return 'I';
    }

    
    public String getThreeLetterCode() {
        return "Ile";
    }

    
    public String getResidueName() {
        return "isoleucine";
    }

}
