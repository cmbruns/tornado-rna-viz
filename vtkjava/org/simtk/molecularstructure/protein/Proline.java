/*
 * Created on May 11, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtomSet;

public class Proline extends AminoAcid {

    public Proline() {}
    public Proline(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "proline";}
    public char getOneLetterCode() {return 'P';}
    public String getThreeLetterCode() {return "Pro";}

    protected void addGenericBonds() {
        super.addGenericBonds();
        // Only Proline links back to the main chain
        addGenericBond(" CD ", " N  ");
    }
}
