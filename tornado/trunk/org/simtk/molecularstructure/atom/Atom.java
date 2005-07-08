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
public abstract class Atom {
	BaseVector3D coordinates = null;
	String localName = null; // name should be unique within a residue
	HashSet bonds = new HashSet();
	
	// Force derived classes to set values
	public abstract double getVanDerWaalsRadius();
	public abstract double getCovalentRadius();
	public abstract String getElementSymbol();
	public abstract double getMass();
	public abstract Color getDefaultColor();

	public double getRadius() {return getVanDerWaalsRadius();}
	public String getName() {return localName;}
	public void setName(String name) {localName = name;}
	
	// TODO add bond valence information to bonds
	public void addBond(Atom atom2) {
	    bonds.add(atom2);
	}
	public HashSet getBonds() {return bonds;}
	
	/**
	 * 
	 * @return The position in space of this PDBAtom
	 */
	public BaseVector3D getCoordinates() {return coordinates;}

    /**
     * @param coordinates The coordinates to set.
     */
    public void setCoordinates(BaseVector3D coordinates) {
        this.coordinates = coordinates;
    }
    
    public double distance(Atom atom2) {
        return coordinates.distance(atom2.coordinates);
    }
    
    public void translate(BaseVector3D v) {
        setCoordinates(getCoordinates().plus(v));
    }
}
