/*
 * Created on Jun 27, 2005
 *
 */
package org.simtk.atomicstructure;

public class Bond {
    protected Atom atom1;
    protected Atom atom2;
    
    public Bond(Atom a1, Atom a2) {atom1 = a1; atom2 = a2;}

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Bond)) return false;
        Bond bond2 = (Bond) o;
        if ( (atom1.equals(bond2.atom2)) &&
             (atom2.equals(bond2.atom2)) ) return true;
        // Swapping atom1 and atom2 is still the same bond
        if ( (atom2.equals(bond2.atom2)) &&
                (atom1.equals(bond2.atom2)) ) return true;
        return false;
    }
    @Override
    public int hashCode() {
        // Must be symmetric with respect to atom1 vs. atom2
        return atom1.hashCode() + atom2.hashCode();
    }
}
