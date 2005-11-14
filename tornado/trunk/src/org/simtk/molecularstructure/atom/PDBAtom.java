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

public interface PDBAtom {

    /**
     * @return Returns the alternateLocationIndicator.
     */
    public abstract char getAlternateLocationIndicator();

    /**
     * @param alternateLocationIndicator The alternateLocationIndicator to set.
     */
    public abstract void setAlternateLocationIndicator(
            char alternateLocationIndicator);

    /**
     * @return Returns the atomName.
     */
    public abstract String getAtomName();

    /**
     * @param atomName The atomName to set.
     */
    public abstract void setAtomName(String atomName);

    /**
     * @return Returns the chainIdentifier.
     */
    public abstract char getChainIdentifier();

    /**
     * @param chainIdentifier The chainIdentifier to set.
     */
    public abstract void setChainIdentifier(char chainIdentifier);

    /**
     * @return Returns the charge.
     */
    public abstract String getCharge();

    /**
     * @param charge The charge to set.
     */
    public abstract void setCharge(String charge);

    /**
     * @return Returns the elementName.
     */
    public abstract String getElementName();

    /**
     * @param elementName The elementName to set.
     */
    public abstract void setElementName(String elementName);

    /**
     * @return Returns the insertionCode.
     */
    public abstract char getInsertionCode();

    /**
     * @param insertionCode The insertionCode to set.
     */
    public abstract void setInsertionCode(char insertionCode);

    /**
     * @return Returns the occupancy.
     */
    public abstract double getOccupancy();

    /**
     * @param occupancy The occupancy to set.
     */
    public abstract void setOccupancy(double occupancy);

    /**
     * @return Returns the recordName.
     */
    public abstract String getRecordName();

    /**
     * @param recordName The recordName to set.
     */
    public abstract void setRecordName(String recordName);

    /**
     * @return Returns the residueIndex.
     */
    public abstract int getResidueIndex();

    /**
     * @param residueIndex The residueIndex to set.
     */
    public abstract void setResidueIndex(int residueIndex);

    /**
     * @return Returns the residueName.
     */
    public abstract String getResidueName();

    /**
     * @param residueName The residueName to set.
     */
    public abstract void setResidueName(String residueName);

    /**
     * @return Returns the segmentIdentifier.
     */
    public abstract String getSegmentIdentifier();

    /**
     * @param segmentIdentifier The segmentIdentifier to set.
     */
    public abstract void setSegmentIdentifier(String segmentIdentifier);

    /**
     * @return Returns the serialNumber.
     */
    public abstract int getSerialNumber();

    /**
     * @param serialNumber The serialNumber to set.
     */
    public abstract void setSerialNumber(int serialNumber);

    /**
     * @return Returns the temperatureFactor.
     */
    public abstract double getTemperatureFactor();

    /**
     * @param temperatureFactor The temperatureFactor to set.
     */
    public abstract void setTemperatureFactor(double temperatureFactor);

    /** 
     * \brief Populate an PDBAtom's attributes from one line of a PDB structure file.
     * @param PDBLine A string containing one ATOM or HETATM record from a PDB structure file.
     * @throws ParseException
     */
    public abstract void readPDBLine(String PDBLine) throws ParseException;

}