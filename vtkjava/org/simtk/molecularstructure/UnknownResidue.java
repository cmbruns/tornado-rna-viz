/*
 * Created on May 4, 2005
 *
 */
package org.simtk.molecularstructure;

import org.simtk.atomicstructure.*;

public class UnknownResidue extends Residue {

    UnknownResidue(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    
    public char getOneLetterCode() {return '?';}
    public String getResidueName() {return "(unknown residue type)";}
}
