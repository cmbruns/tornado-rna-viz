/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.atomicstructure;

import org.simtk.geometry3d.*;
import java.text.ParseException;

/**
 * @author Christopher Bruns
 *
 * \brief A chemical atom including members found in Protein Data Bank flat structure files.
 * 
 */
public abstract class PDBAtom extends Atom {

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

	public PDBAtom(String PDBLine) {
	    try {
	        readPDBLine(PDBLine);
	    } catch (ParseException exc) {}
	}
	
    /**
     * @return Returns the alternateLocationIndicator.
     */
    public char getAlternateLocationIndicator() {
        return alternateLocationIndicator;
    }
    /**
     * @param alternateLocationIndicator The alternateLocationIndicator to set.
     */
    public void setAlternateLocationIndicator(char alternateLocationIndicator) {
        this.alternateLocationIndicator = alternateLocationIndicator;
    }
    /**
     * @return Returns the atomName.
     */
    public String getAtomName() {
        return getName();
    }
    /**
     * @param atomName The atomName to set.
     */
    public void setAtomName(String atomName) {
        this.setName(atomName);
    }
    /**
     * @return Returns the chainIdentifier.
     */
    public char getChainIdentifier() {
        return chainIdentifier;
    }
    /**
     * @param chainIdentifier The chainIdentifier to set.
     */
    public void setChainIdentifier(char chainIdentifier) {
        this.chainIdentifier = chainIdentifier;
    }
    /**
     * @return Returns the charge.
     */
    public String getCharge() {
        return charge;
    }
    /**
     * @param charge The charge to set.
     */
    public void setCharge(String charge) {
        this.charge = charge;
    }
    /**
     * @return Returns the elementName.
     */
    public String getElementName() {
        return elementName;
    }
    /**
     * @param elementName The elementName to set.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
    /**
     * @return Returns the insertionCode.
     */
    public char getInsertionCode() {
        return insertionCode;
    }
    /**
     * @param insertionCode The insertionCode to set.
     */
    public void setInsertionCode(char insertionCode) {
        this.insertionCode = insertionCode;
    }
    /**
     * @return Returns the occupancy.
     */
    public double getOccupancy() {
        return occupancy;
    }
    /**
     * @param occupancy The occupancy to set.
     */
    public void setOccupancy(double occupancy) {
        this.occupancy = occupancy;
    }
    /**
     * @return Returns the recordName.
     */
    public String getRecordName() {
        return recordName;
    }
    /**
     * @param recordName The recordName to set.
     */
    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }
    /**
     * @return Returns the residueIndex.
     */
    public int getResidueIndex() {
        return residueIndex;
    }
    /**
     * @param residueIndex The residueIndex to set.
     */
    public void setResidueIndex(int residueIndex) {
        this.residueIndex = residueIndex;
    }
    /**
     * @return Returns the residueName.
     */
    public String getResidueName() {
        return residueName;
    }
    /**
     * @param residueName The residueName to set.
     */
    public void setResidueName(String residueName) {
        this.residueName = residueName;
    }
    /**
     * @return Returns the segmentIdentifier.
     */
    public String getSegmentIdentifier() {
        return segmentIdentifier;
    }
    /**
     * @param segmentIdentifier The segmentIdentifier to set.
     */
    public void setSegmentIdentifier(String segmentIdentifier) {
        this.segmentIdentifier = segmentIdentifier;
    }
    /**
     * @return Returns the serialNumber.
     */
    public int getSerialNumber() {
        return serialNumber;
    }
    /**
     * @param serialNumber The serialNumber to set.
     */
    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
    /**
     * @return Returns the temperatureFactor.
     */
    public double getTemperatureFactor() {
        return temperatureFactor;
    }
    /**
     * @param temperatureFactor The temperatureFactor to set.
     */
    public void setTemperatureFactor(double temperatureFactor) {
        this.temperatureFactor = temperatureFactor;
    }
	/** 
	 * \brief Populate an PDBAtom's attributes from one line of a PDB structure file.
	 * @param PDBLine A string containing one ATOM or HETATM record from a PDB structure file.
	 * @throws ParseException
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
		coordinates = new Vector3D(
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
