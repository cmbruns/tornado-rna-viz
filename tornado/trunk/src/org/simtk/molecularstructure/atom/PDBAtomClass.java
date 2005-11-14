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
import java.text.ParseException;

/**
 * @author Christopher Bruns
 *
 * \brief A chemical atom including members found in Protein Data Bank flat structure files.
 * 
 */
public abstract class PDBAtomClass extends LocatedAtomClass implements PDBAtom {

    double temperatureFactor;
	double occupancy;
	
	String recordName;
	int serialNumber;
	// String atomName;
	char alternateLocationIndicator;
	String elementName;
	String charge;
	
	// Residue information
	String residueName;
	int residueIndex;
	char insertionCode;
	
	// Molecule information
	char chainIdentifier;
	String segmentIdentifier;

	public PDBAtomClass(String PDBLine) {
	    try {
	        readPDBLine(PDBLine);
	    } catch (ParseException exc) {}
	}
	
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getAlternateLocationIndicator()
     */
    public char getAlternateLocationIndicator() {
        return alternateLocationIndicator;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setAlternateLocationIndicator(char)
     */
    public void setAlternateLocationIndicator(char alternateLocationIndicator) {
        this.alternateLocationIndicator = alternateLocationIndicator;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getAtomName()
     */
    public String getAtomName() {
        return getName();
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setAtomName(java.lang.String)
     */
    public void setAtomName(String atomName) {
        this.setName(atomName);
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getChainIdentifier()
     */
    public char getChainIdentifier() {
        return chainIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setChainIdentifier(char)
     */
    public void setChainIdentifier(char chainIdentifier) {
        this.chainIdentifier = chainIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getCharge()
     */
    public String getCharge() {
        return charge;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setCharge(java.lang.String)
     */
    public void setCharge(String charge) {
        this.charge = charge;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getElementName()
     */
    public String getElementName() {
        return elementName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setElementName(java.lang.String)
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getInsertionCode()
     */
    public char getInsertionCode() {
        return insertionCode;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setInsertionCode(char)
     */
    public void setInsertionCode(char insertionCode) {
        this.insertionCode = insertionCode;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getOccupancy()
     */
    public double getOccupancy() {
        return occupancy;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setOccupancy(double)
     */
    public void setOccupancy(double occupancy) {
        this.occupancy = occupancy;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getRecordName()
     */
    public String getRecordName() {
        return recordName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setRecordName(java.lang.String)
     */
    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getResidueIndex()
     */
    public int getResidueIndex() {
        return residueIndex;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setResidueIndex(int)
     */
    public void setResidueIndex(int residueIndex) {
        this.residueIndex = residueIndex;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getResidueName()
     */
    public String getResidueName() {
        return residueName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setResidueName(java.lang.String)
     */
    public void setResidueName(String residueName) {
        this.residueName = residueName;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getSegmentIdentifier()
     */
    public String getSegmentIdentifier() {
        return segmentIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setSegmentIdentifier(java.lang.String)
     */
    public void setSegmentIdentifier(String segmentIdentifier) {
        this.segmentIdentifier = segmentIdentifier;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getSerialNumber()
     */
    public int getSerialNumber() {
        return serialNumber;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setSerialNumber(int)
     */
    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#getTemperatureFactor()
     */
    public double getTemperatureFactor() {
        return temperatureFactor;
    }
    /* (non-Javadoc)
     * @see org.simtk.molecularstructure.atom.PDBAtom#setTemperatureFactor(double)
     */
    public void setTemperatureFactor(double temperatureFactor) {
        this.temperatureFactor = temperatureFactor;
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
	
		recordName = PDBLine.substring(0, 6);
		if ((! recordName.equals("ATOM  ")) && (! recordName.equals("HETATM"))) 
			throw new ParseException("ATOM record field not found in line: " + PDBLine, 0);

		serialNumber = (new Integer(PDBLine.substring(6,11).trim())).intValue();
		setName(PDBLine.substring(12,16));
		alternateLocationIndicator = PDBLine.charAt(16);
		residueName = PDBLine.substring(17,20);
		chainIdentifier = PDBLine.charAt(21);
		residueIndex = (new Integer(PDBLine.substring(22,26).trim())).intValue();
		insertionCode = PDBLine.charAt(26);
		coordinates = new DoubleVector3D(
				(new Double(PDBLine.substring(30,38).trim())).doubleValue(),
				(new Double(PDBLine.substring(38,46).trim())).doubleValue(),
				(new Double(PDBLine.substring(46,54).trim())).doubleValue() );
		occupancy = (new Double(PDBLine.substring(54,60).trim())).doubleValue();
		temperatureFactor = (new Double(PDBLine.substring(60,66).trim())).doubleValue();
		segmentIdentifier = PDBLine.substring(73,77);
		elementName = PDBLine.substring(76,78);
		charge = PDBLine.substring(78,80);
	}
	
	static public PDBAtom createFactoryPDBAtom(String PDBLine) {
	    // Deduce the element type
	    String elementName = PDBLine.substring(76,78); // Might be spelled explicitly
	    String atomName = PDBLine.substring(12,16);
	    String atomElement = PDBLine.substring(12,14); // First two characters of name should be element

	    // Rectify the two possible element containing fields
	    elementName = elementName.toUpperCase().replaceAll("[^A-Z]", "").trim();
	    atomElement = atomElement.toUpperCase().replaceAll("[^A-Z]", "").trim();

	    if (elementName.equals("C"))   return new PDBCarbon(PDBLine);
	    if (elementName.equals("H"))   return new PDBHydrogen(PDBLine);
	    if (elementName.equals("MG"))  return new PDBMagnesium(PDBLine);
	    if (elementName.equals("N"))   return new PDBNitrogen(PDBLine);
	    if (elementName.equals("O"))   return new PDBOxygen(PDBLine);
	    if (elementName.equals("P"))   return new PDBPhosphorus(PDBLine);
	    if (elementName.equals("S"))   return new PDBSulfur(PDBLine);
	    
	    if (atomElement.equals("C"))   return new PDBCarbon(PDBLine);
	    if (atomElement.equals("H"))   return new PDBHydrogen(PDBLine);
	    if (atomElement.equals("MG"))  return new PDBMagnesium(PDBLine);
	    if (atomElement.equals("N"))   return new PDBNitrogen(PDBLine);
	    if (atomElement.equals("O"))   return new PDBOxygen(PDBLine);
	    if (atomElement.equals("P"))   return new PDBPhosphorus(PDBLine);
	    if (atomElement.equals("S"))   return new PDBSulfur(PDBLine);

	    return new UnknownPDBAtom(PDBLine);
	}

}
