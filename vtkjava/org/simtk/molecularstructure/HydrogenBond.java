/*
 * Created on Jun 28, 2005
 *
 */
package org.simtk.molecularstructure;

import org.simtk.atomicstructure.*;

public class HydrogenBond extends Bond {
    public HydrogenBond(Atom donor, Atom acceptor) {super(donor,acceptor);}
    
    // Hydrogen bonds are not symmetric, so swapping the atoms is not equal
    public boolean equals(Object o) {
        if (! (o instanceof HydrogenBond)) return false;
        HydrogenBond bond2 = (HydrogenBond) o;
        if ( (atom1.equals(bond2.atom2)) &&
             (atom2.equals(bond2.atom2)) ) return true;
        return false;
    }
    public int hashCode() {
        // Must be symmetric with respect to atom1 vs. atom2
        return atom1.hashCode() + atom2.hashCode();
    }
}
