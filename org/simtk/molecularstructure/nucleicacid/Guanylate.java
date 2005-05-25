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
public class Guanylate extends Purine {
    public Guanylate() {}
    public Guanylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "guanylate";}

    public char getOneLetterCode() {return 'G';}
}
