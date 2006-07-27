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
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure;

import java.util.*; // Vector
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.atom.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule structure.
 */
public class PDBMoleculeClass 
extends MoleculeClass
implements Molecule, PDBMolecule {
    // private Collection<Atom> atoms = new LinkedHashSet<Atom>();
    // protected Vector atoms = new Vector();
    // protected Vector<Bond> bonds = new Vector<Bond>();
	// Vector bonds = new Vector();

    Vector3DClass centerOfMass = new Vector3DClass();
    // double mass = 0;
    private String chainID = " ";

    public Vector3D[] getCoordinates() {
        Vector3D[] answer = new Vector3DClass[atoms().size()];
        int i = 0;
        for (Atom a : atoms()) {
            i++;
            if (!(a instanceof Atom)) {
                answer[i] = null;
            }
            else {
                Atom atom =  a;
                answer[i] = atom.getCoordinates();
            }
        }
        return answer;
    }
    
    public void setChainID(String chainID) {this.chainID = chainID;}
    public String getPdbChainId() {return this.chainID;}
    
    public void setCoordinates(Vector3D[] coordinates) {
        if (coordinates.length != atoms().size())
            throw new RuntimeException("Coordinate array mismatch");

        int atomIndex = 0;
        for (Atom a : atoms()) {
            atomIndex ++;
            Atom atom =  a;
            atom.setCoordinates(coordinates[atomIndex]);
        }
    }
    
    public Vector3D getCenterOfMass() {
        if (getMass() <= 0) return null;
        return centerOfMass;
    }

    public PDBMoleculeClass(char chainId) {
        super(chainId);
    } // Empty molecule

    //	public PDBMoleculeClass(PDBAtomSet atomSet) {
//        // for (Atom atom : atomSet) {
//        for (Iterator i = atomSet.iterator(); i.hasNext();) {
//            Atom atom = (Atom) i.next();
//            atoms().add(atom);
//		}
//        createBonds();
//	}

    /**
     * Change the position of the molecule by the specified amount
     * @param t amount to translate
     */
    public Plane3D bestPlane3D() 
    throws InsufficientPointsException
    {
        Vector3D[] coordinates = new Vector3DClass[atoms().size()];
        double[] masses = new double[atoms().size()];

        int a = 0;
        for (Iterator i = atoms().iterator(); i.hasNext();) {
            Atom atom = (Atom) i.next();
            coordinates[a] = atom.getCoordinates();
            masses[a] = atom.getMass();

            a++;
        }
        return Plane3D.bestPlane3D(coordinates, masses);
    }
    
}
