/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Alanine extends AminoAcid {
    public Alanine() {}
    public Alanine(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "alanine";}
    public char getOneLetterCode() {return 'A';}
    public String getThreeLetterCode() {return "Ala";}
}
