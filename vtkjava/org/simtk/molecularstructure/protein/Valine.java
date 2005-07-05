/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Valine extends AminoAcid {

    public Valine() {
        super();
    }

    public Valine(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
    }

    
    public char getOneLetterCode() {
        return 'V';
    }

    
    public String getThreeLetterCode() {
        return "Val";
    }

    
    public String getResidueName() {
        return "valine";
    }

}
