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
package org.simtk.chem;

import org.simtk.geometry3d.*;

import java.text.ParseException;

/**
 * @author Christopher Bruns
 *
 * \brief A chemical atom including members found in Protein Data Bank flat structure files.
 * 
 */
public class PDBAtomClass extends BaseLocatedAtom implements PDBAtom {

    // PDB fields
    private double temperatureFactor;
    private double occupancy;	
    private String recordName;
    private int serialNumber;
	// private String atomName; // use similar field in Atom
    private char alternateLocationIndicator;
    private String pdbElementName;
    private String charge;
	// PDB Residue information
    private String residueName;
    private int residueIndex;
    private char insertionCode;
	// PDB Molecule information
    private char chainIdentifier;
    private String segmentIdentifier;

    static PDBAtom createAtom(String PdbLine) throws ParseException {
        PDBAtomClass answer = new PDBAtomClass();
        
        String atomName = getAtomName(PdbLine);
        Vector3D coordinates = getCoordinates(PdbLine);
        ChemicalElement element = getElement(PdbLine);
        
        // Use superclass initializer
        answer.initialize(coordinates, element, atomName);

        answer.setPDBRecordName(getRecordName(PdbLine));
        if ((! answer.getPDBRecordName().equals("ATOM  ")) && (! answer.getPDBRecordName().equals("HETATM"))) 
            throw new ParseException("ATOM record field not found in line: " + PdbLine, 0);
    
        answer.setPDBAtomSerialNumber(getSerialNumber(PdbLine));
        // answer.setPDBAtomName(getAtomName(PdbLine)); // redundant
        answer.setAlternateLocationIndicator(getAltLoc(PdbLine));
        answer.setPDBResidueName(getResidueName(PdbLine));
        answer.setChainIdentifier(getChainId(PdbLine));
        answer.setResidueNumber(getResidueNum(PdbLine));
        answer.setInsertionCode(getInsertionCode(PdbLine));
        // answer.setCoordinates(getCoordinates(PdbLine)); // redundant
        answer.setOccupancy(getOccupancy(PdbLine));
        answer.setTemperatureFactor(getBValue(PdbLine));
        answer.setSegmentIdentifier(getSegId(PdbLine));
        answer.setPDBElementName(getElementName(PdbLine));
        answer.setPDBCharge(getChargeString(PdbLine));

        return answer;
    }
    
    protected PDBAtomClass() {}
    // PDB Fields: A applies to Atom, R to Residue, M to molecule
    //A    1 -  6        Record name     "HETATM" or "ATOM  "
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

    public static String getRecordName(String pdbLine)  {return pdbLine.substring(0, 6);}
    public static int getSerialNumber(String pdbLine)   {return new Integer(pdbLine.substring(6,11).trim());}
    public static String getAtomName(String pdbLine)    {return pdbLine.substring(12,16);}
    public static char getAltLoc(String pdbLine)        {return pdbLine.charAt(16);}
    public static String getResidueName(String pdbLine) {return pdbLine.substring(17,20);}
    public static char getChainId(String pdbLine)       {return pdbLine.charAt(21);}
    public static int getResidueNum(String pdbLine)     {return new Integer(pdbLine.substring(22,26).trim());}
    public static char getInsertionCode(String pdbLine) {return pdbLine.charAt(26);}

    public static double getX(String pdbLine)           {return new Double(pdbLine.substring(30,38).trim());}
    public static double getY(String pdbLine)           {return new Double(pdbLine.substring(38,46).trim());}
    public static double getZ(String pdbLine)           {return new Double(pdbLine.substring(46,54).trim());}
    public static Vector3D getCoordinates(String pdbLine) {
        return new Vector3DClass(getX(pdbLine), getY(pdbLine), getZ(pdbLine));
    }

    public static double getOccupancy(String pdbLine)   {return new Double(pdbLine.substring(54,60).trim());}
    public static double getBValue(String pdbLine)      {return new Double(pdbLine.substring(60,66).trim());}
    public static String getSegId(String pdbLine)       {return pdbLine.substring(73,77);}
    public static String getElementName(String pdbLine) {return pdbLine.substring(76,78);}
    public static String getChargeString(String pdbLine){return pdbLine.substring(78,80);}
    
    public static ChemicalElement getElement(String pdbLine) {
        // Rectify the two possible element containing fields
        String elementName = getElementName(pdbLine).toUpperCase().replaceAll("[^A-Z]", "").trim();
        String atomElement = getAtomName(pdbLine).substring(0,2).toUpperCase().replaceAll("[^A-Z]", "").trim();
    
        ChemicalElement element = ChemicalElementClass.getElementByName(elementName);
        if (element.equals(ChemicalElementClass.UNKNOWN_ELEMENT))
            element = ChemicalElementClass.getElementByName(atomElement);
        
        return element;        
    }

    // PDBAtom interface methods
    public char getAlternateLocationIndicator() {
        return alternateLocationIndicator;
    }
    protected void setAlternateLocationIndicator(char alternateLocationIndicator) {
        this.alternateLocationIndicator = alternateLocationIndicator;
    }
//    public String getPDBAtomName() {
//        return atomName;
//    }
//    protected void setPDBAtomName(String atomName) {
//        this.atomName = atomName;
//    }
    public char getChainIdentifier() {
        return chainIdentifier;
    }
    protected void setChainIdentifier(char chainIdentifier) {
        this.chainIdentifier = chainIdentifier;
    }
    public String getPDBCharge() {
        return charge;
    }
    protected void setPDBCharge(String charge) {
        this.charge = charge;
    }
    public String getPDBElementName() {
        return pdbElementName;
    }
    protected void setPDBElementName(String elementName) {
        this.pdbElementName = elementName;
    }
    public char getInsertionCode() {
        return insertionCode;
    }
    protected void setInsertionCode(char insertionCode) {
        this.insertionCode = insertionCode;
    }
    public double getOccupancy() {
        return occupancy;
    }
    protected void setOccupancy(double occupancy) {
        this.occupancy = occupancy;
    }
    public String getPDBRecordName() {
        return recordName;
    }
    protected void setPDBRecordName(String recordName) {
        this.recordName = recordName;
    }
    public int getResidueNumber() {
        return residueIndex;
    }
    protected void setResidueNumber(int residueIndex) {
        this.residueIndex = residueIndex;
    }
    public String getPDBResidueName() {
        return residueName;
    }
    protected void setPDBResidueName(String residueName) {
        this.residueName = residueName;
    }
    public String getSegmentIdentifier() {
        return segmentIdentifier;
    }
    protected void setSegmentIdentifier(String segmentIdentifier) {
        this.segmentIdentifier = segmentIdentifier;
    }
    public int getPDBAtomSerialNumber() {
        return serialNumber;
    }
    protected void setPDBAtomSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
    public double getTemperatureFactor() {
        return temperatureFactor;
    }
    protected void setTemperatureFactor(double temperatureFactor) {
        this.temperatureFactor = temperatureFactor;
    }
    
    protected void setCoordinates(Vector3D v) {
        initialize(v);
    }
}
