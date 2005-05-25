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
public class Pyrimidine extends Nucleotide {
    public Pyrimidine() {}
    public Pyrimidine(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public char getOneLetterCode() {return 'Y';}
    
    protected void addGenericBonds() {
        super.addGenericBonds();
        // Note - sugar-base linkage depends upon nucleotide type
        // addGenericBond(" C1*", " N9 ");
        addGenericBond(" C1*", " N1 ");
    }
}
