/*
 * Created on May 1, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import org.simtk.atomicstructure.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 * 
 */
public class Purine extends Nucleotide {
    public Purine() {}
    public Purine(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public char getOneLetterCode() {return 'R';}
    
    protected void addGenericBonds() {
        super.addGenericBonds();
        // Note - sugar-base linkage depends upon nucleotide type
        addGenericBond(" C1*", " N9 ");
        // addGenericBond(" C1*", " N1 ");
    }
}
