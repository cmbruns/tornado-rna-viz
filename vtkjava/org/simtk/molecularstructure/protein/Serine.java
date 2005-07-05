/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Serine extends AminoAcid {

    public Serine() {
        super();
    }

    public Serine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'S';
    }

    
    public String getThreeLetterCode() {
        return "Ser";
    }

    
    public String getResidueName() {
        return "serine";
    }

}
