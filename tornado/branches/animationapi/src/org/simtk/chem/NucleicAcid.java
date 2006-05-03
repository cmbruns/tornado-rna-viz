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

public class NucleicAcid extends PolymerType {
    static public NucleicAcid DNA = new NucleicAcid();
    static public NucleicAcid RNA = new NucleicAcid();
    static public NucleicAcid NUCLEIC_ACID = new NucleicAcid();
    
    static {
        String[] dnaCodes = {"  A","  C","  G","  T"," +A", " +C", " +G"," +T"};
        DNA.addResidueCodes(dnaCodes);

        String[] rnaCodes = {"  A","  C","  G","  U"," +A", " +C", " +G"," +U"};
        RNA.addResidueCodes(rnaCodes);
        
        NUCLEIC_ACID.addResidueCodes(dnaCodes);
        NUCLEIC_ACID.addResidueCodes(rnaCodes);        
    }
    
    protected NucleicAcid() {}
}
