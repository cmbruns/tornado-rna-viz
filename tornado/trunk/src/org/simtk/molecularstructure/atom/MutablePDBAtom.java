/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Nov 14, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure.atom;

import java.text.ParseException;

public interface MutablePDBAtom extends PDBAtom {
    public char getAlternateLocationIndicator();
    public void setAlternateLocationIndicator(char alternateLocationIndicator);

    public String getPDBAtomName();
    public void setPDBAtomName(String atomName);

    public char getChainIdentifier();
    public void setChainIdentifier(char chainIdentifier);

    public String getPDBCharge();
    public void setPDBCharge(String charge);

    public String getPDBElementName();
    public void setPDBElementName(String elementName);

    public char getInsertionCode();
    public void setInsertionCode(char insertionCode);

    public double getOccupancy();
    public void setOccupancy(double occupancy);

    public String getPDBRecordName();
    public void setPDBRecordName(String recordName);

    public int getResidueNumber();
    public void setResidueNumber(int residueIndex);

    public String getPDBResidueName();
    public void setPDBResidueName(String residueName);

    public String getSegmentIdentifier();
    public void setSegmentIdentifier(String segmentIdentifier);

    public int getPDBAtomSerialNumber();
    public void setPDBAtomSerialNumber(int serialNumber);

    public double getTemperatureFactor();
    public void setTemperatureFactor(double temperatureFactor);

    /** 
     * \brief Populate an PDBAtom's attributes from one line of a PDB structure file.
     * @param PDBLine A string containing one ATOM or HETATM record from a PDB structure file.
     * @throws ParseException
     */
    public void readPDBLine(String PDBLine) throws ParseException;
}