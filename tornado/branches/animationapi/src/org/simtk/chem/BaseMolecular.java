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

import java.util.*;

import org.simtk.chem.pdb.PdbAtom;

public class BaseMolecular implements Molecular {
    private Collection<Bond> bonds = new Vector<Bond>();
    private AtomCollection atoms = new AtomCollection();

    protected static PdbAtom getOnePDBAtom(Collection<Atom> atoms) {
        PdbAtom answer = null;
        for (Atom atom : atoms) {
            if (atom instanceof PdbAtom) {
                answer = (PdbAtom) atom;
            }
        } 
        return answer;
    }
    
    public Iterable<Atom> atoms() {return atoms;}
    public Iterable<Bond> bonds() {return bonds;}
    public void addBond(Bond bond) {bonds.add(bond);}
    public void addAtom(Atom atom) {atoms.add(atom);}
}
