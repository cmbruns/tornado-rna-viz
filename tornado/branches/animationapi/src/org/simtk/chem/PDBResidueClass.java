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
package org.simtk.chem;

import java.util.*;

/** 
 * @author Christopher Bruns
 * 
 * \brief One monomer residue of a Biopolymer
 *
 */
public class PDBResidueClass extends BaseResidue implements PDBResidue {

    private char insertionCode = ' ';
    private char chainId = ' ';
    
    /**
     * Creates a new residue object.
     * 
     * This method does NOT create objects of derived classes such as AminoAcid.
     * Use createResidue() for that purpose.
     * 
     * @param bagOfAtoms
     */
    
    protected void initializeFromAtoms(Collection<Atom> atoms) {
        super.initializeFromAtoms(atoms);

        PDBAtom atom = getOnePDBAtom(atoms);
        if (atom != null) {
            setInsertionCode(atom.getInsertionCode());
            chainId = atom.getChainIdentifier();
        }
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
    
    public static boolean isSolvent(String residueName) {
        String trimmedName = residueName.trim().toUpperCase(); // remove spaces

        // Water
        if (trimmedName.equals("HOH")) return true;
        if (trimmedName.equals("WAT")) return true;
        if (trimmedName.equals("H2O")) return true;
        if (trimmedName.equals("SOL")) return true;
        if (trimmedName.equals("TIP")) return true;

        // Deuterated water
        if (trimmedName.equals("DOD")) return true;
        if (trimmedName.equals("D2O")) return true;

        // Sulfate
        if (trimmedName.equals("SO4")) return true;
        if (trimmedName.equals("SUL")) return true;

        // Phosphate
        if (trimmedName.equals("PO4")) return true;

        return false;
    }
    
}
