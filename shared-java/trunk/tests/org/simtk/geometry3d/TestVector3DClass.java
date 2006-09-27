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
 * Created on Jun 23, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.geometry3d;

import junit.framework.TestCase;

public class TestVector3DClass extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestVector3DClass.class);
    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.hashCode()'
     */
    public void testHashCode() {
        Vector3D v1 = new Vector3DClass(1,2,3);
        Vector3D v2 = new Vector3DClass(1,2,3);
        Vector3D v3 = new Vector3DClass(3,2,1);
        assertEquals(v1.hashCode(), v2.hashCode());
        assertTrue(v1.hashCode() != v3.hashCode());
    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.copy(MathVector)'
     */
    public void testCopy() {
        MutableVector3D v1 = new Vector3DClass(1,2,3);
        Vector3D v2 = new Vector3DClass(3,2,1);
        assertTrue(!(v1.equals(v2)));
        v1.copy(v2);
        assertTrue(v1.equals(v2));
    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.toString()'
     */
    public void testToString() {
        Vector3D v1 = new Vector3DClass(1,2,3);
        assertEquals("1.0, 2.0, 3.0", v1.toString());
    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.Vector3DClass()'
     */
    public void testVector3DClass() {
        Vector3D v1 = new Vector3DClass(0,0,0);
        Vector3D v2 = new Vector3DClass();
        assertEquals(v1,v2);
    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.setX(double)'
     */
    public void testSetX() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.setY(double)'
     */
    public void testSetY() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.setZ(double)'
     */
    public void testSetZ() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.getX()'
     */
    public void testGetX() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.getY()'
     */
    public void testGetY() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.getZ()'
     */
    public void testGetZ() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.x()'
     */
    public void testX() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.y()'
     */
    public void testY() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.z()'
     */
    public void testZ() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.unit()'
     */
    public void testUnit() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.minus(MathVector)'
     */
    public void testMinusMathVector() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.plus(MathVector)'
     */
    public void testPlusMathVector() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.times(double)'
     */
    public void testTimesDouble() {
        Vector3D v1 = new Vector3DClass(1,2,3);
        Vector3D v2 = v1.times(0.5);
        Vector3D v3 = new Vector3DClass(0.5, 1.0, 1.5);
        assertEquals(v3, v2);
    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.centroid(Collection<Vector3D>)'
     */
    public void testCentroidCollectionOfVector3D() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.centroid(Vector3D[], double[])'
     */
    public void testCentroidVector3DArrayDoubleArray() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.cross(Vector3D)'
     */
    public void testCross() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.equals(Object)'
     */
    public void testEqualsObject() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.Vector3DClass.rotate(Vector3D, double)'
     */
    public void testRotate() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.MathVectorClass(int)'
     */
    public void testMathVectorClass() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.get(int)'
     */
    public void testGet() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.getElement(int)'
     */
    public void testGetElement() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.set(int, double)'
     */
    public void testSet() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.getDimension()'
     */
    public void testGetDimension() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.plus(MathVector)'
     */
    public void testPlusMathVector1() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.plusEquals(MathVector)'
     */
    public void testPlusEquals() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.minusEquals(MathVector)'
     */
    public void testMinusEquals() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.minus(MathVector)'
     */
    public void testMinusMathVector1() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.dot(MathVector)'
     */
    public void testDot() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.lengthSquared()'
     */
    public void testLengthSquared() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.length()'
     */
    public void testLength() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.distance(MathVector)'
     */
    public void testDistance() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.distanceSquared(MathVector)'
     */
    public void testDistanceSquared() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.unit()'
     */
    public void testUnit1() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.selfUnit()'
     */
    public void testSelfUnit() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.times(double)'
     */
    public void testTimesDouble1() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.timesEquals(double)'
     */
    public void testTimesEquals() {

    }

    /*
     * Test method for 'org.simtk.geometry3d.MathVectorClass.iterator()'
     */
    public void testIterator() {

    }

}
