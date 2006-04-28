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
 * Created on Apr 24, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

public class BaseAtom extends BaseMolecular implements Atom {
    private ChemicalElement element;
    private String name;

    static public Atom createAtom(ChemicalElement element, String name) {
        BaseAtom answer = new BaseAtom();
        answer.setElement(element);
        answer.setAtomName(name);
        return answer;
    }
    
    private BaseAtom() {}
    
    protected void setElement(ChemicalElement element) {this.element = element;}
    protected void setAtomName(String name) {this.name = name;}

    // Atom interface
    public String getAtomName() {return name;}
    
    // ChemicalElement interface - delegate to element
    public String getElementSymbol() {return element.getElementSymbol();}
    public String getElementName() {return element.getElementName();}
    public double getCovalentRadius() {return element.getCovalentRadius();}
    public double getVanDerWaalsRadius() {return element.getVanDerWaalsRadius();}
    public double getMassInDaltons() {return element.getMassInDaltons();}
    
    // Molecular interface (inherits from BaseMolecular)
    
}
