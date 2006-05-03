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

import junit.framework.TestCase;
import org.simtk.geometry3d.*;
import java.util.*;

public class TestLocatedMasses extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestLocatedMasses.class);
    }

    public TestLocatedMasses(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.hashCode()'
     */
    public void testHashCode() {
        LocatedMass m1 = new BasePointMass(new Vector3DClass(0, 0, 0), 10);
        LocatedMass m2 = new BasePointMass(new Vector3DClass(0, 0, 0), 10);
        LocatedMass m3 = new BasePointMass(new Vector3DClass(0, 0, 0), 10);

        // Use Vector class as positive control
        Vector v1 = new Vector();
        Vector v2 = new Vector();

        assertTrue(v1.hashCode() == v1.hashCode());
        assertTrue(v1.hashCode() == v2.hashCode());
        
        v1.add(m1);
        assertFalse(v1.hashCode() == v2.hashCode());
        
        v2.add(m1);
        assertTrue(v1.hashCode() == v2.hashCode());
        
        LocatedMasses l1 = new LocatedMasses();
        LocatedMasses l2 = new LocatedMasses();

        assertTrue(l1.hashCode() == l1.hashCode());
        assertTrue(l1.hashCode() == l2.hashCode());
        
        l2.add(m2);
        l1.add(m1);
        assertFalse(l1.hashCode() == l2.hashCode());
        
        l2.clear();
        assertFalse(l1.hashCode() == l2.hashCode());
        
        l2.add(m1);
        assertTrue(l1.hashCode() == l2.hashCode());
        
        l2.add(m2);
        assertFalse(l1.hashCode() == l2.hashCode());
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.LocatedMasses()'
     */
    public void testLocatedMasses() {
        LocatedMasses l1 = new LocatedMasses();
        assertNotNull(l1);
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.getMassInDaltons()'
     */
    public void testGetMassInDaltons() {
        LocatedMass m1 = new BasePointMass(new Vector3DClass(0, 1, 0), 10);
        LocatedMass m2 = new BasePointMass(new Vector3DClass(0, 3, 0), 10);
        LocatedMasses l1 = new LocatedMasses();

        l1.add(m1);
        l1.add(m2);
        
        assertTrue(l1.getMassInDaltons() == 20);
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.add(LocatedMass)'
     */
    public void testAdd() {
        LocatedMass m1 = new BasePointMass(new Vector3DClass(0, 1, 0), 10);
        LocatedMass m2 = new BasePointMass(new Vector3DClass(0, 3, 0), 10);
        LocatedMasses l1 = new LocatedMasses();
        assertTrue(l1.size() == 0);
        l1.add(m1);
        assertTrue(l1.size() == 1);
        l1.add(m2);
        assertTrue(l1.size() == 2);
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.addAll(Collection<? extends LocatedMass>)'
     */
    public void testAddAll() {
        LocatedMasses l1 = new LocatedMasses();
        LocatedMass m1 = new BasePointMass(new Vector3DClass(0, 1, 0), 10);
        LocatedMass m2 = new BasePointMass(new Vector3DClass(0, 3, 0), 10);
        Vector v = new Vector();
        v.add(m1);
        v.add(m2);
        l1.addAll(v);
        assertTrue(l1.size() == 2);
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.clear()'
     */
    public void testClear() {
        LocatedMass m1 = new BasePointMass(new Vector3DClass(0, 1, 0), 10);
        LocatedMass m2 = new BasePointMass(new Vector3DClass(0, 3, 0), 10);
        LocatedMasses l1 = new LocatedMasses();

        l1.add(m1);
        l1.add(m2);
        
        assertTrue(l1.getMassInDaltons() == 20);
        
        l1.clear();
        assertTrue(l1.size() == 0);
        assertTrue(l1.getMassInDaltons() == 0);        
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.contains(Object)'
     */
    public void testContains() {
        LocatedMass m1 = new BasePointMass(new Vector3DClass(0, 1, 0), 10);
        LocatedMass m2 = new BasePointMass(new Vector3DClass(0, 3, 0), 10);
        LocatedMasses l1 = new LocatedMasses();

        l1.add(m1);

        assertTrue(l1.contains(m1));
        assertFalse(l1.contains(m2));
        
        l1.clear();
        l1.add(m2);
        assertTrue(l1.contains(m2));
        assertFalse(l1.contains(m1));
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.containsAll(Collection<?>)'
     */
    public void testContainsAll() {
        LocatedMass m1 = new BasePointMass(new Vector3DClass(0, 1, 0), 10);
        LocatedMass m2 = new BasePointMass(new Vector3DClass(0, 3, 0), 10);
        LocatedMasses l1 = new LocatedMasses();

        l1.add(m1);
        
        Vector v1 = new Vector();
        Vector v2 = new Vector();
        
        assertTrue(l1.containsAll(v1));
        assertTrue(l1.containsAll(v2));

        v1.add(m1);
        v2.add(m1);
        v2.add(m2);

        assertTrue(l1.containsAll(v1));
        assertFalse(l1.containsAll(v2));
        
        l1.clear();
        l1.add(m2);
        assertFalse(l1.containsAll(v1));
        assertFalse(l1.containsAll(v2));
    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.equals(Object)'
     */
    public void testEqualsObject() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.isEmpty()'
     */
    public void testIsEmpty() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.iterator()'
     */
    public void testIterator() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.remove(Object)'
     */
    public void testRemove() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.removeAll(Collection<?>)'
     */
    public void testRemoveAll() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.retainAll(Collection<?>)'
     */
    public void testRetainAll() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.size()'
     */
    public void testSize() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.toArray()'
     */
    public void testToArray() {

    }

    /*
     * Test method for 'org.simtk.chem.mass.LocatedMasses.toArray(T[]) <T>'
     */
    public void testToArrayTArray() {

    }

    /*
     * Test method for 'java.lang.Object.Object()'
     */
    public void testObject() {

    }

    /*
     * Test method for 'java.lang.Object.getClass()'
     */
    public void testGetClass() {

    }

    /*
     * Test method for 'java.lang.Object.equals(Object)'
     */
    public void testEqualsObject1() {

    }

    /*
     * Test method for 'java.lang.Object.clone()'
     */
    public void testClone() {

    }

    /*
     * Test method for 'java.lang.Object.toString()'
     */
    public void testToString() {

    }

    /*
     * Test method for 'java.lang.Object.notify()'
     */
    public void testNotify() {

    }

    /*
     * Test method for 'java.lang.Object.notifyAll()'
     */
    public void testNotifyAll() {

    }

    /*
     * Test method for 'java.lang.Object.wait(long)'
     */
    public void testWaitLong() {

    }

    /*
     * Test method for 'java.lang.Object.wait(long, int)'
     */
    public void testWaitLongInt() {

    }

    /*
     * Test method for 'java.lang.Object.wait()'
     */
    public void testWait() {

    }

    /*
     * Test method for 'java.lang.Object.finalize()'
     */
    public void testFinalize() {

    }

}
