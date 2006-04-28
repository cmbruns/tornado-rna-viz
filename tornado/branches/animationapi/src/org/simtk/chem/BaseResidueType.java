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
 * Created on Apr 27, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import java.util.*;

public class BaseResidueType implements ResidueType {
    static public ResidueType UNKNOWN = new BaseResidueType();
    
    private String threeLetterCode = "UNK";
    private String residueName = "(unknown residue)";
    private Collection<GenericBond> genericBonds = new HashSet<GenericBond>();

    static public ResidueType getType(PDBAtom atom) {
        ResidueType answer = UNKNOWN;

        String residueCode = atom.getPDBResidueName();

        if (Nucleotide.isNucleotideCode(residueCode))
            answer = Nucleotide.get(residueCode);

        return answer;
    }
    
    public String getThreeLetterCode() {return threeLetterCode;}
    public String getResidueName() {return residueName;}
    public Collection<GenericBond> genericBonds() {return genericBonds;}
    
    protected void initialize(String residueName, String threeLetterCode) {
        this.residueName = residueName;
        this.threeLetterCode = threeLetterCode;
    }
}
