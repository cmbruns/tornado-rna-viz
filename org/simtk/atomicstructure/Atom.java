/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.atomicstructure;

import java.awt.*;
import java.util.*;
import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * Abstract base class for chemical atom, such a a particular nitrogen atom in a molecule
 */
public abstract class Atom {
	Vector3D coordinates = null;
	String localName = null; // name should be unique within a residue

	HashSet<Atom> bonds = new HashSet<Atom>();
	
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
	public HashSet<Atom> getBonds() {return bonds;}
	
	/**
	 * 
	 * @return The position in space of this PDBAtom
	 */
	public Vector3D getCoordinates() {return coordinates;}

    /**
     * @param coordinates The coordinates to set.
     */
    public void setCoordinates(Vector3D coordinates) {
        this.coordinates = coordinates;
    }
    
    public double distance(Atom atom2) {
        return coordinates.distance(atom2.coordinates);
    }
}
