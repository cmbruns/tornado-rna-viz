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

import java.util.Collection;

import org.simtk.geometry3d.*;

public class BaseLocatedAtom extends Vector3DClass implements LocatedAtom {
    // Composition rather than inheritance for BaseAtom
    private Atom baseAtom;
    
    static public LocatedAtom createAtom(Vector3D center, ChemicalElement element, String name) {
        BaseLocatedAtom answer = new BaseLocatedAtom();
        answer.initialize(center, element, name);
        return answer;
    }
    
    protected BaseLocatedAtom() {}
    
    protected void initialize(Vector3D center, ChemicalElement element, String name) {
        super.initialize(center);
        baseAtom = BaseAtom.createAtom(element, name);
    }
    
    // Atom interface
    public String getAtomName() {return baseAtom.getAtomName();}
    
    // ChemicalElement interface - delegate to element
    public String getElementSymbol() {return baseAtom.getElementSymbol();}
    public String getElementName() {return baseAtom.getElementName();}
    public double getCovalentRadius() {return baseAtom.getCovalentRadius();}
    public double getVanDerWaalsRadius() {return baseAtom.getVanDerWaalsRadius();}
    public double getMassInDaltons() {return baseAtom.getMassInDaltons();}
    
    // Molecular Interface delegates to baseAtom
    public Collection<Bond> bonds() {return baseAtom.bonds();} // covalent bonds
}
