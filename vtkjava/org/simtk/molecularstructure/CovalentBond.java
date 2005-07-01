/*
 * Created on Jun 28, 2005
 *
 */
package org.simtk.molecularstructure;

import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.Vector3D;

public class CovalentBond extends Bond {
    public CovalentBond(Atom a1, Atom a2) {super(a1,a2);}

    /**
     * Compute a point between the atoms, in proportion to the atoms' covalent radii
     * @return
     */
    public Vector3D getMidpoint() {
        double covalentRatio = atom1.getCovalentRadius()/(atom2.getCovalentRadius() + atom1.getCovalentRadius());
        Vector3D fullBondVector = atom2.getCoordinates().minus(atom1.getCoordinates());
        Vector3D midBond = atom1.getCoordinates().plus(fullBondVector.scale(covalentRatio));
        return midBond;
    }    
}
