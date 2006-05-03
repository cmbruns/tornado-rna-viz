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

    static public AminoAcid ALANINE =       new AminoAcid("alanine",           'A', "ALA");
    static public AminoAcid ASPARXXX =      new AminoAcid("(apartate or asparagine)",     'B', "ASX");
    static public AminoAcid CYSTEINE =      new AminoAcid("cysteine",          'C', "CYS");
    static public AminoAcid APARTATE =      new AminoAcid("aspartate",         'D', "ASP");
    static public AminoAcid GLUTAMATE =     new AminoAcid("glutamate",         'E', "GLU");
    static public AminoAcid PHENYLALANINE = new AminoAcid("phenylalanine",     'F', "PHE");
    static public AminoAcid GLYCINE =       new AminoAcid("glycine",           'G', "GLY");
    static public AminoAcid HISTIDINE =     new AminoAcid("histidine",         'H', "HIS");
    static public AminoAcid ISOLEUCINE =    new AminoAcid("isoleucine",        'I', "ILE");
    static public AminoAcid LYSINE =        new AminoAcid("lysine",            'K', "LYS");
    static public AminoAcid LEUCINE =       new AminoAcid("leucine",           'L', "LEU");
    static public AminoAcid METHIONINE =    new AminoAcid("methionine",        'M', "MET");
    static public AminoAcid ASPARAGINE =    new AminoAcid("asparagine",        'N', "ASN");
    static public AminoAcid PROLINE =       new AminoAcid("proline",           'P', "PRO");
    static public AminoAcid GLUTAMINE =     new AminoAcid("glutamine",         'Q', "GLN");
    static public AminoAcid ARGININE =      new AminoAcid("arginine",          'R', "ARG");
    static public AminoAcid SERINE =        new AminoAcid("serine",            'S', "SER");
    static public AminoAcid THREONINE =     new AminoAcid("threonine",         'T', "THR");
    static public AminoAcid SELENOCYSTEINE =new AminoAcid("selenocysteine",    'U', "SEC");
    static public AminoAcid VALINE =        new AminoAcid("valine",            'V', "VAL");
    static public AminoAcid TRYPTOPHAN =    new AminoAcid("tryptophan",        'W', "TRP");
    static public AminoAcid TYROSINE =      new AminoAcid("tyrosine",          'Y', "TYR");
    static public AminoAcid GLUTAMXXX =     new AminoAcid("(glutamate or glutamine)",     'Z', "GLX");

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
