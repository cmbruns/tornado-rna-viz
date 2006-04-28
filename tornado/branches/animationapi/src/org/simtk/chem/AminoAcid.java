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
 * Created on Apr 26, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import java.util.HashMap;
import java.util.Map;

public class AminoAcid extends BaseCanonicalResidueType {

    static public AminoAcid ALANINE =    new AminoAcid("alanine",     'A', "ALA");
    // TODO
    static public AminoAcid UNKNOWN =      new AminoAcid("(unknown amino acid)", 'X', "UNK");
    
    private static Map<String, CanonicalResidueType> nameTypes = new HashMap<String, CanonicalResidueType>();

    static public CanonicalResidueType get(String threeLetterCode) {
        if (nameTypes.containsKey(threeLetterCode))
            return nameTypes.get(threeLetterCode);
        else if (nameTypes.containsKey(threeLetterCode.toUpperCase()))
            return nameTypes.get(threeLetterCode.toUpperCase());
        else return UNKNOWN;
    }
    
    static public boolean isAminoAcidCode(String code) {
        String trimmedName = code.trim().toUpperCase(); // remove spaces

        if (trimmedName.equals("ALA")) return true;
        if (trimmedName.equals("CYS")) return true;
        if (trimmedName.equals("ASP")) return true;
        if (trimmedName.equals("GLU")) return true;
        if (trimmedName.equals("PHE")) return true;
        if (trimmedName.equals("GLY")) return true;
        if (trimmedName.equals("HIS")) return true;
        if (trimmedName.equals("ILE")) return true;
        if (trimmedName.equals("LYS")) return true;
        if (trimmedName.equals("LEU")) return true;
        if (trimmedName.equals("MET")) return true;
        if (trimmedName.equals("ASN")) return true;
        if (trimmedName.equals("PRO")) return true;
        if (trimmedName.equals("GLN")) return true;
        if (trimmedName.equals("ARG")) return true;
        if (trimmedName.equals("SER")) return true;
        if (trimmedName.equals("THR")) return true;
        if (trimmedName.equals("VAL")) return true;
        if (trimmedName.equals("TRP")) return true;
        if (trimmedName.equals("TYR")) return true;

        if (trimmedName.equals("ASX")) return true;
        if (trimmedName.equals("GLX")) return true;
                
        return false;
    }
    
    protected void initialize(String name, char oneLetterCode, String threeLetterCode) {
        super.initialize(name, oneLetterCode, threeLetterCode);
        indexResidueCode(threeLetterCode);
        indexResidueCode(""+oneLetterCode);
        indexResidueCode(name);        
    }    
    private void indexResidueCode(String code) {
        nameTypes.put(code, this);
        nameTypes.put(code.toUpperCase(), this);
    }

    private AminoAcid(String name, char oneLetterCode, String threeLetterCode) {
        initialize(name, oneLetterCode, threeLetterCode);
    }    
}
