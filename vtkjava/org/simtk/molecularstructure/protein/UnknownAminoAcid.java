/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class UnknownAminoAcid extends AminoAcid {
    public UnknownAminoAcid() {}
    public UnknownAminoAcid(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public char getOneLetterCode() {return 'X';}
    public String getThreeLetterCode() {return "Xxx";}
    public String getResidueName() {return "(unknown amino acid type)";}
}
