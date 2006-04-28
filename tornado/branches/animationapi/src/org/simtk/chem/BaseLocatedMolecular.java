/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Apr 24, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import org.simtk.hash3d.Hash3D;
import org.simtk.geometry3d.Vector3D;

public class BaseLocatedMolecular extends BaseMolecular implements LocatedMolecular {

    protected BaseLocatedMolecular() {}
    
    protected BaseLocatedMolecular(AtomCollection atoms) {
        for (Atom atom : atoms) this.atoms().add(atom);
        createBonds();
    }
    
    private void addBond(Atom atom1, Atom atom2) {
        Bond bond = new CovalentBond(atom1, atom2);
        atom1.bonds().add(bond);
        atom2.bonds().add(bond);
        bonds().add(bond);
    }
    
    // Create covalent bonds where it seems that they are needed
    protected void createBonds() {

        // Maybe iodine has the largest "ordinary" covalent radius of 1.33
        double maxCovalentRadius = 1.40;

        // 1) Create a hash for rapid access
        Hash3D<LocatedAtom> atomHash = new Hash3D<LocatedAtom>(maxCovalentRadius);
        // 2) Place each atom into the hash
        for (Atom atom : atoms()) {
            if (atom instanceof LocatedAtom)
                atomHash.put((Vector3D)atom, (LocatedAtom)atom);
        }
        // 3) Use hash to locate neighboring atoms
        for (Atom atom1 : atoms()) {
            if (atom1 instanceof Vector3D) {
                Vector3D v1 = (Vector3D) atom1;
                
                double cutoffDistance = (atom1.getCovalentRadius() + maxCovalentRadius) * 1.5;
                for (Atom atom2 : atomHash.neighborValues(v1, cutoffDistance)) {
                    
                    if (atom1.equals(atom2)) continue;
                    
                    if (! (atom2 instanceof Vector3D)) continue;
                    Vector3D v2 = (Vector3D) atom2;
                    
                    // Make sure the bond length is about right
                    double distance = v1.distance(v2);
                    double covalentDistance = atom1.getCovalentRadius() + atom2.getCovalentRadius();
                    double vanDerWaalsDistance = atom1.getVanDerWaalsRadius() + atom2.getVanDerWaalsRadius();
                    // Bond length must be at least 3/4 of that expected
                    double minDistance = 0.75 * (covalentDistance);
                    // Bond length must be closer to covalent than to van der Waals distance
                    if (covalentDistance >= vanDerWaalsDistance) continue;
                    double discriminantDistance = vanDerWaalsDistance - covalentDistance;
                    double maxDistance = covalentDistance + 0.25 * discriminantDistance;
                    if (maxDistance > 1.25 * covalentDistance) maxDistance = 1.25 * covalentDistance;
                    if (distance < minDistance) continue;
                    if (distance > maxDistance) continue;
                    
                    // Make sure it is in the same molecule or part
                    if ( (atom1 instanceof PDBAtom) && (atom2 instanceof PDBAtom) ) {
                        PDBAtom pdbAtom1 = (PDBAtom) atom1;
                        PDBAtom pdbAtom2 = (PDBAtom) atom2;
                        // Must be in the same chain
                        if (pdbAtom1.getChainIdentifier() != pdbAtom2.getChainIdentifier()) continue;
                        // Must be in the same alternate location group
                        if (pdbAtom1.getAlternateLocationIndicator() != pdbAtom2.getAlternateLocationIndicator()) continue;
                    }
                    
                    addBond(atom1, atom2);
                    // bonds.add(new Bond(atom1, atom2));
                    
                }
            }
        }
    }
}
