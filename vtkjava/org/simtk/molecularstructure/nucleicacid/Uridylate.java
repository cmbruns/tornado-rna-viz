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
public class Uridylate extends Pyrimidine {
    public Uridylate() {}
    public Uridylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "uridylate";}

    public char getOneLetterCode() {return 'U';}
}
