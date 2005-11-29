/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Jun 28, 2005
 *
 */
package org.simtk.molecularstructure;

import java.util.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.atom.*;

public class LocatedMoleculeBondClass implements LocatedMoleculeBond {

    private LocatedMoleculeAtom m_atom1;
    private LocatedMoleculeAtom m_atom2;
    private Vector m_atomCollection = new Vector();
    
    public LocatedMoleculeBondClass(LocatedMoleculeAtom a1, LocatedMoleculeAtom a2) {
        m_atom1 = a1; 
        m_atom2 = a2;
        m_atomCollection.add(m_atom1);
        m_atomCollection.add(m_atom2);
    }

    public Iterator iterator() {return m_atomCollection.iterator();}
    
    public Atom getAtom1() {return m_atom1;}
    public Atom getAtom2() {return m_atom1;}
    
    public Collection atoms() {return m_atomCollection;}
    
    public MoleculeAtom getOtherAtom(MoleculeAtom firstAtom) {
        if (firstAtom.equals(getAtom1())) return (MoleculeAtom) getAtom2();
        if (firstAtom.equals(getAtom2())) return (MoleculeAtom) getAtom1();
        return null;
    }
    
    public boolean equals(Object o) {
        if (! (o instanceof MolecularBond)) return false;
        MolecularBond bond2 = (MolecularBond) o;
        
        if ( (getAtom1().equals(bond2.getAtom1())) &&
             (getAtom2().equals(bond2.getAtom2())) ) return true;
        // Swapping atom1 and atom2 is still the same bond
        if ( (getAtom2().equals(bond2.getAtom1())) &&
                (getAtom1().equals(bond2.getAtom2())) ) return true;

        return false;
    }
    public int hashCode() {
        // Must be symmetric with respect to atom1 vs. atom2
        return getAtom1().hashCode() + getAtom2().hashCode();
    }

    /**
     * Compute a point between the atoms, in proportion to the atoms' covalent radii
     * @return
     */
    public Vector3D getMidpoint() {
        LocatedAtom atom1 = (LocatedAtom) getAtom1();
        LocatedAtom atom2 = (LocatedAtom) getAtom2();
        double covalentRatio = atom1.getCovalentRadius()/(atom2.getCovalentRadius() + atom1.getCovalentRadius());
        Vector3D fullBondVector = atom2.getCoordinates().minus(atom1.getCoordinates());
        Vector3D midBond = atom1.getCoordinates().plus(fullBondVector.times(covalentRatio)).v3();
        return new Vector3DClass(midBond);
    }    
}
