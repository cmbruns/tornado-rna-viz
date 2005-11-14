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
package org.simtk.molecularstructure.atom;

import java.awt.*;
import java.util.*;
import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * Abstract base class for chemical atom, such a a particular nitrogen atom in a molecule
 */
public abstract class LocatedAtomClass implements LocatedAtom, MoleculeAtom {
	Vector3D coordinates = null;
	String localName = null; // name should be unique within a residue
	HashSet bonds = new HashSet();
	
	// Force derived classes to set values
	public abstract double getVanDerWaalsRadius();
	public abstract double getCovalentRadius();
	public abstract double getMass();
	public abstract Color getDefaultColor();

	public double getRadius() {return getVanDerWaalsRadius();}
	public String getName() {return localName;}
	public void setName(String name) {localName = name;}
	
	// TODO add bond valence information to bonds
	/* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.MoleculeAtom#addBond(org.simtk.molecularstructure.atom.LocatedAtom)
     */
	public void addBond(LocatedAtom atom2) {
	    bonds.add(atom2);
	}
	/* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.MoleculeAtom#getBonds()
     */
	public HashSet getBonds() {return bonds;}
	
	/* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.LocatedAtom#getCoordinates()
     */
	public Vector3D getCoordinates() {return coordinates;}

    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.LocatedAtom#setCoordinates(org.simtk.geometry3d.Vector3D)
     */
    public void setCoordinates(Vector3D coordinates) {
        this.coordinates = coordinates;
    }
    
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.LocatedAtom#distance(org.simtk.molecularstructure.atom.LocatedAtomClass)
     */
    public double distance(LocatedAtomClass atom2) {
        return coordinates.distance(atom2.coordinates);
    }
    
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.LocatedAtom#translate(org.simtk.geometry3d.Vector3D)
     */
    public void translate(Vector3D v) {
        getCoordinates().plusEquals(v);
    }
}
