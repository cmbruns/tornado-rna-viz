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

import java.util.Iterator;
import org.simtk.geometry3d.*;

public class BasePointMass implements PointMass {
    private Vector3D coordinates;
    private double massInDaltons;
    
    public BasePointMass(Vector3D center, double massInDaltons) {
        this.coordinates = center;
        this.massInDaltons = massInDaltons;
    }

    protected Vector3D getCoordinates() {return coordinates;}
    public void setCoordinates(Vector3D v) {this.coordinates = v;}

    // Vector3D Interface - delegate to coordinates
    public double getX() {return getCoordinates().getX();}
    public double getY() {return getCoordinates().getY();}
    public double getZ() {return getCoordinates().getZ();}
    public Vector3D cross(Vector3D v2) {return getCoordinates().cross(v2);}
    public Vector3D rotate(Vector3D axis, double angle) {return getCoordinates().rotate(axis, angle);}
    public Vector3D plus(Vector3D v2) {return getCoordinates().plus(v2);}
    public Vector3D minus(Vector3D v2) {return getCoordinates().minus(v2);}

    // MathVector Interface - delegate to coordinates
    public double get(int i) {return getCoordinates().get(i);}
    public double getElement(int i) {return getCoordinates().getElement(i);}
    public int getDimension() {return getCoordinates().getDimension();}    
    public MathVector plus(MathVector v2) {return getCoordinates().plus(v2);}    
    public MathVector minus(MathVector v2) {return getCoordinates().minus(v2);}
    public double dot(MathVector v2) {return getCoordinates().dot(v2);}    
    public double length() {return getCoordinates().length();}
    public double lengthSquared() {return getCoordinates().lengthSquared();}
    public double distance(MathVector v2) {return getCoordinates().distance(v2);}
    public double distanceSquared(MathVector v2) {return getCoordinates().distanceSquared(v2);}
    public MathVector unit() {return getCoordinates().unit();}
    public MathVector times(double s) {return getCoordinates().times(s);}
    public Vector3D v3() {return getCoordinates().v3();}
    
    // Iterable interface - delegate to coordinates
    public Iterator iterator() {return coordinates.iterator();}    

    public Vector3D getCenterOfMass() {return getCoordinates();}

    public double getMassInDaltons() {return massInDaltons;}
}
