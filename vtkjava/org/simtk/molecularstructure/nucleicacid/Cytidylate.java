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
public class Cytidylate extends Pyrimidine {
    public Cytidylate() {}
    public Cytidylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "cytidylate";}

    public char getOneLetterCode() {return 'C';}
}
