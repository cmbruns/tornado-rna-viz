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
package org.simtk.chem.mass;

import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Collection of masses that maintains cached center of mass
 */
public class LocatedMasses extends Vector<LocatedMass> implements LocatedMass, Collection<LocatedMass> {

    private double totalMass = 0;
    // private Vector3D weightedPositionSum = new Vector3DClass(0,0,0);
    private Collection<LocatedMass> subMasses = new Vector<LocatedMass>();
    
    public LocatedMasses() {
        clearMasses();
    }
    
    public double getMassInDaltons() {return totalMass;}

    
    // Delegate Collection<LocatedMass> methods to subMasses collection
    public boolean add(LocatedMass mass) { // append
        if (subMasses.add(mass)) {
            addMass(mass);
            return true;
        } else
            return false;
    }    
    public boolean addAll(Collection<? extends LocatedMass> masses) {
        if (subMasses.addAll(masses)) {
            for (LocatedMass mass : masses) {addMass(mass);}
            return true;
        }
        else return false;
    }
    public void clear() {
        subMasses.clear();
        clearMasses();
    }
    public boolean  contains(Object o) {
        return subMasses.contains(o);
    }
    public boolean containsAll(Collection<?> c) {
        return subMasses.containsAll(c);
    }
    public boolean equals(Object o) {
        return subMasses.equals(o);
    }
    public int hashCode() {return subMasses.hashCode();}
    public boolean isEmpty() {return subMasses.isEmpty();}
    public Iterator<LocatedMass> iterator() {
        return subMasses.iterator();
    }
    public boolean remove(Object o) {
        if (subMasses.remove(o)) {
            if (o instanceof LocatedMass)
                subtractMass((LocatedMass)o);
            return true;
        }
        else return false;
    }
    public boolean removeAll(Collection<?> c) {
        if (subMasses.removeAll(c)) {
            recomputeMass(); // Brute force for this tricky one
            return true;
        }
        else return false;
    }
    public boolean retainAll(Collection<?> c) {
        if (subMasses.retainAll(c)) {
            recomputeMass(); // Brute force for this tricky one
            return true;
        }
        else return false;
    }
    public int size() {return subMasses.size();}
    public Object[] toArray() {return subMasses.toArray();}
    public <T> T[] toArray(T[] a) {
        return subMasses.toArray(a);
    }
    
    
    
    private void clearMasses() {
        totalMass = 0.0;
    }
    
    private void addMass(LocatedMass mass) {
        double m = mass.getMassInDaltons();
        totalMass += m;
    }

    private void subtractMass(LocatedMass mass) {
        double m = mass.getMassInDaltons();
        totalMass -= m;
    }
    
    private void recomputeMass() {
        clearMasses();
        for (LocatedMass mass : subMasses) {
            addMass(mass);
        }
    }

    
}
