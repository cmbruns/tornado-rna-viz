/*
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import org.simtk.atomicstructure.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 * 
 */
public class Adenylate extends Purine {
    public Adenylate() {}
    public Adenylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "adenylate";}
    public char getOneLetterCode() {return 'A';}
}
