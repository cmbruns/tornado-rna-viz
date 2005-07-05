/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Lysine extends AminoAcid {

    public Lysine() {
        super();
    }

    public Lysine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'K';
    }

    
    public String getThreeLetterCode() {
        return "Lys";
    }

    
    public String getResidueName() {
        return "lysine";
    }

}
