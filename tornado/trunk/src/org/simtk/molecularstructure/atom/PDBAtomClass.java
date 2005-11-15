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

import java.util.*;
import java.awt.Color;
import java.text.ParseException;

/**
 * @author Christopher Bruns
 *
 * \brief A chemical atom including members found in Protein Data Bank flat structure files.
 * 
 */
public class PDBAtomClass implements MutablePDBAtom {
    // Atom fields
    private ChemicalElement m_element;
    
    // Located atom fields
    private Vector3D m_coordinates;
    
    // Molecule Atom fields
    private HashSet m_bonds = new HashSet();
    
    // PDB fields
    private double m_temperatureFactor;
    private double m_occupancy;	
    private String m_recordName;
    private int m_serialNumber;
	private String m_atomName;
    private char m_alternateLocationIndicator;
    private String m_pdbElementName;
    private String m_charge;
	// PDB Residue information
    private String m_residueName;
    private int m_residueIndex;
    private char m_insertionCode;
	// PDB Molecule information
    private char m_chainIdentifier;
    private String m_segmentIdentifier;

	public PDBAtomClass(String PDBLine) throws ParseException {
	    try {
	        readPDBLine(PDBLine);
	    } catch (ParseException exc) {}
	}
	
    // LocatedAtom interface methods
    // public Vector3D getCoordinates();
    // public void setCoordinates(Vector3D coordinates);
    public Vector3D getCoordinates() {return m_coordinates;}
    public void setCoordinates(Vector3D v) {m_coordinates = v;}
    public double distance(LocatedAtom atom2) {return getCoordinates().distance(atom2.getCoordinates());}
    public void translate(Vector3D v) {setCoordinates(getCoordinates().plus3(v));}

    // MoleculeAtom interface methods
    public void addBond(ChemicalElement atom2) {m_bonds.add(atom2);}
    public Collection getBonds() {return m_bonds;}
    
    protected void setElement(ChemicalElement element) {
        m_element = element;
    }
    
    // ChemicalElement interface methods
    public double getCovalentRadius() {return m_element.getCovalentRadius();}
    public Color getDefaultAtomColor() {return m_element.getDefaultAtomColor();}
    public String getElementSymbol() {return m_element.getElementSymbol();}
    public String getElementName() {return m_element.getElementName();}
    public double getMass() {return m_element.getMass();}
    public double getVanDerWaalsRadius() {return m_element.getVanDerWaalsRadius();}

    
    // PDBAtom interface methods
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getAlternateLocationIndicator()
     */
    public char getAlternateLocationIndicator() {
        return m_alternateLocationIndicator;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setAlternateLocationIndicator(char)
     */
    public void setAlternateLocationIndicator(char alternateLocationIndicator) {
        m_alternateLocationIndicator = alternateLocationIndicator;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getAtomName()
     */
    public String getPDBAtomName() {
        return m_atomName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setAtomName(java.lang.String)
     */
    public void setPDBAtomName(String atomName) {
        m_atomName = atomName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getChainIdentifier()
     */
    public char getChainIdentifier() {
        return m_chainIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setChainIdentifier(char)
     */
    public void setChainIdentifier(char chainIdentifier) {
        m_chainIdentifier = chainIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getCharge()
     */
    public String getPDBCharge() {
        return m_charge;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setCharge(java.lang.String)
     */
    public void setPDBCharge(String charge) {
        m_charge = charge;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getElementName()
     */
    public String getPDBElementName() {
        return m_pdbElementName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setElementName(java.lang.String)
     */
    public void setPDBElementName(String elementName) {
        m_pdbElementName = elementName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getInsertionCode()
     */
    public char getInsertionCode() {
        return m_insertionCode;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setInsertionCode(char)
     */
    public void setInsertionCode(char insertionCode) {
        m_insertionCode = insertionCode;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getOccupancy()
     */
    public double getOccupancy() {
        return m_occupancy;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setOccupancy(double)
     */
    public void setOccupancy(double occupancy) {
        m_occupancy = occupancy;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getRecordName()
     */
    public String getPDBRecordName() {
        return m_recordName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setRecordName(java.lang.String)
     */
    public void setPDBRecordName(String recordName) {
        m_recordName = recordName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getResidueIndex()
     */
    public int getResidueNumber() {
        return m_residueIndex;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setResidueIndex(int)
     */
    public void setResidueNumber(int residueIndex) {
        m_residueIndex = residueIndex;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getResidueName()
     */
    public String getPDBResidueName() {
        return m_residueName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setResidueName(java.lang.String)
     */
    public void setPDBResidueName(String residueName) {
        m_residueName = residueName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getSegmentIdentifier()
     */
    public String getSegmentIdentifier() {
        return m_segmentIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setSegmentIdentifier(java.lang.String)
     */
    public void setSegmentIdentifier(String segmentIdentifier) {
        m_segmentIdentifier = segmentIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getSerialNumber()
     */
    public int getPDBAtomSerialNumber() {
        return m_serialNumber;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setSerialNumber(int)
     */
    public void setPDBAtomSerialNumber(int serialNumber) {
        m_serialNumber = serialNumber;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getTemperatureFactor()
     */
    public double getTemperatureFactor() {
        return m_temperatureFactor;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setTemperatureFactor(double)
     */
    public void setTemperatureFactor(double temperatureFactor) {
        m_temperatureFactor = temperatureFactor;
    }
	/* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#readPDBLine(java.lang.String)
     */
	public void readPDBLine(String PDBLine) 
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
	
		setPDBRecordName(PDBLine.substring(0, 6));
		if ((! getPDBRecordName().equals("ATOM  ")) && (! getPDBRecordName().equals("HETATM"))) 
			throw new ParseException("ATOM record field not found in line: " + PDBLine, 0);

		setPDBAtomSerialNumber((new Integer(PDBLine.substring(6,11).trim())).intValue());
		setPDBAtomName(PDBLine.substring(12,16));
		setAlternateLocationIndicator(PDBLine.charAt(16));
		setPDBResidueName(PDBLine.substring(17,20));
        setChainIdentifier( PDBLine.charAt(21) );
        setResidueNumber( (new Integer(PDBLine.substring(22,26).trim())).intValue());
        setInsertionCode( PDBLine.charAt(26));
        setCoordinates( new DoubleVector3D(
				(new Double(PDBLine.substring(30,38).trim())).doubleValue(),
				(new Double(PDBLine.substring(38,46).trim())).doubleValue(),
				(new Double(PDBLine.substring(46,54).trim())).doubleValue() ));
        setOccupancy( (new Double(PDBLine.substring(54,60).trim())).doubleValue());
        setTemperatureFactor( (new Double(PDBLine.substring(60,66).trim())).doubleValue());
        setSegmentIdentifier( PDBLine.substring(73,77));
        setPDBElementName( PDBLine.substring(76,78));
        setPDBCharge( PDBLine.substring(78,80));
        
        // Rectify the two possible element containing fields
        String elementName = getPDBElementName().toUpperCase().replaceAll("[^A-Z]", "").trim();
        String atomElement = getPDBAtomName().substring(0,2).toUpperCase().replaceAll("[^A-Z]", "").trim();

        ChemicalElement element = ChemicalElementClass.getElementByName(elementName);
        if (element.equals(ChemicalElementClass.UNKNOWN_ELEMENT))
            element = ChemicalElementClass.getElementByName(atomElement);
        
        setElement(element);
	}
}
