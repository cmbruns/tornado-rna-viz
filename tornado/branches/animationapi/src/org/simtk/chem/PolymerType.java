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

public class PolymerType extends MoleculeType {
    static public PolymerType PROTEIN = new PolymerType();
    static public NucleicAcid NUCLEIC_ACID = NucleicAcid.NUCLEIC_ACID;
    static public NucleicAcid DNA = NucleicAcid.DNA;
    static public NucleicAcid RNA = NucleicAcid.RNA;
    
    static {
        String[] proteinCodes = {"ALA","CYS","ASP","GLU","PHE","GLY",
                "HIS","ILE","LYS","LEU","MET","ASN","PRO","GLN","ARG",
                "SER","THR","VAL","TRP","TYR",
                "ASX","GLX"};
        PROTEIN.addResidueCodes(proteinCodes);
        
        // (nucleic acid codes are set in NucleicAcid class)
    }

}
