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

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.Residue;
import java.util.*;
import java.text.ParseException;

/**
 * @author Christopher Bruns
 *
 * \brief A chemical atom including members found in Protein Data Bank flat structure files.
 * 
 */
public class AtomClass implements Atom {
    // Atom fields
    private ChemicalElement m_element;
    private String m_atomName = null;

    private AtomPosition position = null;
    private Map<String, AtomPosition> atomPositions = new HashMap<String, AtomPosition>();
    
    private Residue residue = null;
    
    // Located atom fields
    // private Vector3D m_coordinates = null;
    
    // Molecule Atom fields
    private Set<Atom> m_bonds = new HashSet<Atom>();
    
    // private double temperatureFactor = Double.NaN;
    // private double occupancy = Double.NaN;
    
	public AtomClass(String pdbLine) throws ParseException {
	    try {
	        readPdbLine(pdbLine);
	    } catch (ParseException exc) {}
	}
	
    public void addPosition(AtomPosition pos) {
        char altLoc = pos.getPdbAltLoc();
        atomPositions.put(""+altLoc, pos);

        // Update primary position
        if (position == null) position = pos;
        // Choose the primary position with the highest occupancy
        else {
            if (pos.getOccupancy() > position.getOccupancy())
                position = pos;
        }
    }
    
    public Vector3D getCenterOfMass() {return getCoordinates();}
    
    // LocatedAtom interface methods
    // public Vector3D coordinates();
    // public void setCoordinates(Vector3D coordinates);
    public Vector3D getCoordinates() {
        if (position == null) return null;
        return position.getCoordinates();
    }

    public void setCoordinates(Vector3D v) {
        if (position == null) position = new AtomPosition(v);
        else position.setCoordinates(v);
    }

    public double distance(Atom atom2) {return getCoordinates().distance(atom2.getCoordinates());}

    public Residue getResidue() {return residue;}
    public void setResidue(Residue r) {this.residue = r;}
    
    // MoleculeAtom interface methods
    public Set<Atom> bonds() {return m_bonds;}
    
    protected void setElement(ChemicalElement element) {
        m_element = element;
    }
    
    // ChemicalElement interface methods
    public double getCovalentRadius() {return m_element.getCovalentRadius();}
    public String getElementSymbol() {return m_element.getElementSymbol();}
    public String getElementName() {return m_element.getElementName();}
    public double getMass() {return m_element.getMass();}
    public double getVanDerWaalsRadius() {return m_element.getVanDerWaalsRadius();}

    
    public String getAtomName() {
        return m_atomName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setAtomName(java.lang.String)
     */
    
    public double getOccupancy() {
        if (position == null) return Double.NaN;
        return position.getOccupancy();
    }
    
    // public void setOccupancy(double o) {this.occupancy = o;}
    
    public double getTemperatureFactor() {
        if (position == null) return Double.NaN;
        return position.getTemperatureFactor();
    }
    // public void setTemperatureFactor(double b) {this.temperatureFactor = b;}
    
    // public void setCoordinates(Vector3D v) {
    //     m_coordinates = v;
    // }
    
    public void setAtomName(String atomName) {
        m_atomName = atomName;
    }

	public void readPdbLine(String pdbLine) 
		throws ParseException
	{	
		// PDB Fields: A applies to Atom, R to Residue, M to molecule
		//A	   1 -  6        Record name     "HETATM" or "ATOM  "
		//
		//A    7 - 11        Integer         serial         Atom serial number.
		//
		//A   13 - 16        Atom            name           Atom name.
		//
		//A   17             Character       altLoc         Alternate location indicator.
		//
		//R   18 - 20        Residue name    resName        Residue name.
		//
		//M   22             Character       chainID        Chain identifier.
		//
		//R   23 - 26        Integer         resSeq         Residue sequence number.
		//
		//R   27             AChar           iCode          Code for insertion of residues.
		//
		//A   31 - 38        Real(8.3)       x              Orthogonal coordinates for X.
		//
		//A   39 - 46        Real(8.3)       y              Orthogonal coordinates for Y.
		//
		//A   47 - 54        Real(8.3)       z              Orthogonal coordinates for Z.
		//
		//A   55 - 60        Real(6.2)       occupancy      Occupancy.
		//
		//A   61 - 66        Real(6.2)       tempFactor     Temperature factor.
		//
		//?   73 - 76        LString(4)      segID          Segment identifier;
		//                                                  left-justified.
		//
		//A   77 - 78        LString(2)      element        Element symbol; right-justified.
		//
		//A   79 - 80        LString(2)      charge         Charge on the atom.
	
		setAtomName(pdbLine.substring(12,16));

        addPosition(new AtomPosition(pdbLine));

        // Rectify the two possible element containing fields
        String elementName = pdbLine.substring(76,78).toUpperCase().replaceAll("[^A-Z]", "").trim();
        String atomElement = getAtomName().substring(0,2).toUpperCase().replaceAll("[^A-Z]", "").trim();

        ChemicalElement element = ChemicalElementClass.getElementByName(elementName);
        if (element.equals(ChemicalElementClass.UNKNOWN_ELEMENT))
            element = ChemicalElementClass.getElementByName(atomElement);
        
        setElement(element);
	}

}
