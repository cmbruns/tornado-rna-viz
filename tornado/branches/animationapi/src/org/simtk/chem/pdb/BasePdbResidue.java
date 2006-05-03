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
 * Created on Apr 22, 2005
 *
 */
package org.simtk.chem.pdb;

import java.text.ParseException;
import java.util.*;

import org.simtk.chem.Atom;
import org.simtk.chem.BaseResidue;
import org.simtk.chem.CanonicalResidue;

/** 
 * @author Christopher Bruns
 * 
 * \brief One monomer residue of a Biopolymer
 *
 */
public class BasePdbResidue extends BaseResidue implements PdbResidue {

    private char insertionCode = ' ';
    private char chainId = ' ';
    private Map<String, PdbAtom> keyAtoms = new HashMap<String, PdbAtom>();
    
    /**
     * Creates a new residue object.
     * 
     * This method does NOT create objects of derived classes such as AminoAcid.
     * Use createResidue() for that purpose.
     * 
     * @param bagOfAtoms
     */
    
    public static PdbResidue createResidue(String resName, char chainId, int resNum, char iCode) {
        BasePdbResidue answer = new BasePdbResidue();
        answer.setResidueName(resName);
        answer.setChainId(chainId);
        answer.setResidueNumber(resNum);
        answer.setInsertionCode(iCode);
        return answer;
    }
    
    public void addAtom(Atom atom) {
        super.addAtom(atom);
        if (atom instanceof PdbAtom) {
            PdbAtom pdbAtom = (PdbAtom) atom;
            keyAtoms.put(atomKey(pdbAtom.getAtomName(), pdbAtom.getAlternateLocationIndicator()), pdbAtom);
        }
    }
    private String atomKey(String atomName, char altLoc) {
        return atomName + ":" + altLoc;
    }
    public PdbAtom getAtomFromPdbLine(String pdbLine) throws ParseException {
        String atomName = BasePdbAtom.getAtomName(pdbLine);
        char altLoc = BasePdbAtom.getAltLoc(pdbLine);
        return keyAtoms.get(atomKey(atomName, altLoc));
    }
    public PdbAtom creativeGetAtomFromPdbLine(String pdbLine) throws ParseException {
        PdbAtom answer = getAtomFromPdbLine(pdbLine);
        if (answer == null) {
            answer = BasePdbAtom.createAtom(pdbLine);
            addAtom(answer);
        }
        return answer;
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
    protected void setInsertionCode(char insertionCode) {
        this.insertionCode = insertionCode;
    }
    /**
     * @return Returns the residueNumber.
     */

    protected void setChainId(char chainId) {this.chainId = chainId;}
    public char getChainId() {return chainId;}
    
    public String toString() {
        String answer = "";
        if (this instanceof CanonicalResidue) {
            answer = answer + ((CanonicalResidue) this).getOneLetterCode();
        } else {
            answer = answer + getThreeLetterCode();            
        }
        answer = answer + " " + getResidueNumber();
        return answer;
    }
}
