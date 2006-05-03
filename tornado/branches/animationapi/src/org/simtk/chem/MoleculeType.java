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
 * Created on Apr 28, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import java.util.*;

public class MoleculeType {

    static public MoleculeType OTHER = new MoleculeType();
    static public MoleculeType SOLVENT = new MoleculeType();

    static public PolymerType POLYMER = PolymerType.POLYMER;
    static public PolymerType PROTEIN = PolymerType.PROTEIN;
    static public NucleicAcid NUCLEIC_ACID = PolymerType.NUCLEIC_ACID;
    static public NucleicAcid DNA = PolymerType.DNA;
    static public NucleicAcid RNA = PolymerType.RNA;

    static {
        String[] solventCodes = {"HOH","WAT","H2O","SOL","TIP",
                "DOD","D2O",
                "SO4","SUL",
                "PO4"};
        SOLVENT.addResidueCodes(solventCodes);
    }
    
    private Set<String> knownResidueCodes = new HashSet<String>();

    public boolean usesResidueCode(String threeLetterCode) {
        return knownResidueCodes.contains(threeLetterCode);
    }
    
    protected MoleculeType() {}
    
    protected void addResidueCode(String code) {
        knownResidueCodes.add(code);
    }
    
    protected void addResidueCodes(String[] codes) {
        for (String code : codes)
            addResidueCode(code);
    }
}
