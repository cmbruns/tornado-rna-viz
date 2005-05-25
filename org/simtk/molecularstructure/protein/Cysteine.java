/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Cysteine extends AminoAcid {
    public Cysteine() {}
    public Cysteine(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "cysteine";}
    public char getOneLetterCode() {return 'C';}
    public String getThreeLetterCode() {return "Cys";}
}
