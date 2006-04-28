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

import java.util.*;

public class AtomCollection implements Collection<Atom> {
    private Collection<Atom> atoms = new Vector<Atom>();

    // Overload Container<Atom> methods that might change the mass
    public boolean add(Atom atom) { // append
        return this.atoms.add(atom);
    }    
    public boolean addAll(Collection<? extends Atom> atoms) {
        return this.atoms.addAll(atoms);
    }
    public void clear() {
        this.atoms.clear();
    }
    public boolean remove(Object o) {
        return this.atoms.remove(o);
    }
    public boolean removeAll(Collection<?> c) {
        return this.atoms.removeAll(c);
    }
    public boolean retainAll(Collection<?> c) {
        return this.atoms.retainAll(c);
    }
    
    // ConstCollection<Atom> methods delegate to this.atoms
    public boolean     contains(Object o) {return this.atoms.contains(o);}
    public boolean     containsAll(Collection<?> c) {return this.atoms.containsAll(c);}
    public boolean     isEmpty() {return this.atoms.isEmpty();}
    public Iterator<Atom> iterator() {return this.atoms.iterator();}
    public int         size() {return this.atoms.size();}
    public Object[]    toArray() {return this.atoms.toArray();}
    public <T> T[]     toArray(T[] a) {return this.atoms.toArray(a);}
}
