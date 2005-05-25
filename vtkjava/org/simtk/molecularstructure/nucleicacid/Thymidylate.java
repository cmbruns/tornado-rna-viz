/*
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import org.simtk.atomicstructure.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 */
public class Thymidylate extends Pyrimidine {
    public Thymidylate() {}
    public Thymidylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "thymidylate";}

    public char getOneLetterCode() {return 'T';}
}
