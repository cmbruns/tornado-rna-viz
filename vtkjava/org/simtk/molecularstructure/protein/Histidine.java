/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Histidine extends AminoAcid {

    public Histidine() {
        super();
    }

    public Histidine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'H';
    }

    
    public String getThreeLetterCode() {
        return "His";
    }

    
    public String getResidueName() {
        return "histidine";
    }

}
