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

public class Nucleotide extends BaseCanonicalResidueType {

    static public Nucleotide ADENYLATE =    new Nucleotide("adenylate",     'A', "  A");
    static public Nucleotide CYTIDYLATE =   new Nucleotide("cytidylate",    'C', "  C");
    static public Nucleotide GUANYLATE =    new Nucleotide("guanylate",     'G', "  G");
    static public Nucleotide INOSITATE =    new Nucleotide("inositate",     'I', "  I");
    static public Nucleotide THYMIDYLATE =  new Nucleotide("thymidylate",   'T', "  T");
    static public Nucleotide URIDYLATE =    new Nucleotide("uridylate",     'U', "  U");
    static public Nucleotide MODIFIED_ADENYLATE =    new Nucleotide("modified adenylate",     'A', " +A");
    static public Nucleotide MODIFIED_CYTIDYLATE =   new Nucleotide("modified cytidylate",    'C', " +C");
    static public Nucleotide MODIFIED_GUANYLATE =    new Nucleotide("modified guanylate",     'G', " +G");
    static public Nucleotide MODIFIED_INOSITATE =    new Nucleotide("modified inositate",     'I', " +I");
    static public Nucleotide MODIFIED_THYMIDYLATE =  new Nucleotide("modified thymidylate",   'T', " +T");
    static public Nucleotide MODIFIED_URIDYLATE =    new Nucleotide("modified uridylate",     'U', " +U");
    static public Nucleotide UNKNOWN =      new Nucleotide("(unknown nucleotide)", 'N', "UNK");
    
    private static Map<String, CanonicalResidueType> nameTypes = new HashMap<String, CanonicalResidueType>();

    static public CanonicalResidueType get(String threeLetterCode) {
        if (nameTypes.containsKey(threeLetterCode))
            return nameTypes.get(threeLetterCode);
        else if (nameTypes.containsKey(threeLetterCode.toUpperCase()))
            return nameTypes.get(threeLetterCode.toUpperCase());
        else return UNKNOWN;
    }
    
    static public boolean isNucleotideCode(String code) {
        String uCode = code.toUpperCase().trim();

        if (uCode.equals("A")) return true;
        if (uCode.equals("C")) return true;
        if (uCode.equals("G")) return true;
        if (uCode.equals("I")) return true;
        if (uCode.equals("T")) return true;
        if (uCode.equals("U")) return true;

        if (uCode.equals("+A")) return true;
        if (uCode.equals("+C")) return true;
        if (uCode.equals("+G")) return true;
        if (uCode.equals("+I")) return true;
        if (uCode.equals("+T")) return true;
        if (uCode.equals("+U")) return true;

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

    private Nucleotide(String name, char oneLetterCode, String threeLetterCode) {
        initialize(name, oneLetterCode, threeLetterCode);
    }    
}
