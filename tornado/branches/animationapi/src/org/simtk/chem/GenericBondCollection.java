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
 * Created on Apr 27, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import java.util.*;

public class GenericBondCollection implements Collection<GenericBond> {
    private Collection<GenericBond> bonds = new HashSet<GenericBond>();
    private Map<String, Collection<String> > partners = 
        new HashMap<String, Collection<String> >();
    
    public Collection<String> getPartners(String atomName) {
        if (partners.containsKey(atomName))
            return partners.get(atomName);
        else return null;
    }
    
    // Collection interface
    public boolean     add(GenericBond o) {
        addPartner(o);
        return bonds.add(o);
    }

    public boolean     addAll(Collection<? extends GenericBond> c) {
        for (GenericBond bond : c) {
            addPartner(bond);
        }
        return bonds.addAll(c);
    }
    
    public void        clear() {
        partners.clear();
        bonds.clear();
    }
    
    public boolean     contains(Object o) {return bonds.contains(o);}
    public boolean     containsAll(Collection<?> c) {return bonds.containsAll(c);}
    public boolean     equals(Object o) {return bonds.equals(o);}
    public int         hashCode() {return bonds.hashCode();}
    public boolean     isEmpty() {return bonds.isEmpty();}
    public Iterator<GenericBond>     iterator() {return bonds.iterator();}
    
    public boolean     remove(Object o) {
        if (o instanceof GenericBond) removePartner((GenericBond)o);
        return bonds.remove(o);
    }
    
    public boolean     removeAll(Collection<?> c) {
        for (Object o : c) {
            if (o instanceof GenericBond) {
                GenericBond bond = (GenericBond) o;
                removePartner(bond);
            }
        }
        return bonds.removeAll(c);
    }
    
    public boolean     retainAll(Collection<?> c) {
        partners.clear();
        for (Object o : c) {
            if (o instanceof GenericBond) {
                GenericBond bond = (GenericBond) o;
                addPartner(bond);
            }
        }
        return bonds.retainAll(c);
    }

    public int         size() {return bonds.size();}
    public Object[]    toArray() {return bonds.toArray();}
    public <T> T[]     toArray(T[] a) {return bonds.toArray(a);}
    
    private void addPartner(GenericBond bond) {
        String name1 = bond.getAtomName1();
        String name2 = bond.getAtomName2();
        if (!(partners.containsKey(name1))) partners.put(name1, new HashSet<String>());
        if (!(partners.containsKey(name2))) partners.put(name2, new HashSet<String>());
        partners.get(name1).add(name2);
        partners.get(name2).add(name1);
    }
    
    private void removePartner(GenericBond bond) {
        String name1 = bond.getAtomName1();
        String name2 = bond.getAtomName2();
        if (partners.containsKey(name1)) partners.get(name1).remove(name2);
        if (partners.containsKey(name2)) partners.get(name2).remove(name1);
    }
}
