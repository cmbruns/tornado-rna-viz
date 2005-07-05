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

    
    public char getOneLetterCode() {
        return 'T';
    }

    
    public String getThreeLetterCode() {
        return "Thr";
    }

    
    public String getResidueName() {
        return "threonine";
    }

}
