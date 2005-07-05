/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Tryptophan extends AminoAcid {

    public Tryptophan() {
        super();
    }

    public Tryptophan(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'W';
    }

    
    public String getThreeLetterCode() {
        return "Trp";
    }

    
    public String getResidueName() {
        return "tryptophan";
    }

}
