/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Apr 20, 2005
 *
 */
package org.simtk.geometry3d;

import java.util.Iterator;
import java.util.Vector;

import org.simtk.geometry3d.Vector3D.VectorIterator;

/**
 * @author Christopher Bruns
 *
 * A point or direction vector in three dimensions.
 */
public class Vector3DClass implements Vector3D {

    private double[] privateCoordinates = new double[3];
    public Vector3DClass() {
        setX(0);
        setY(0);
        setZ(0);
        }
    public Vector3DClass(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    public Vector3DClass(MathVectorClass template) {
        copy(template);
    }
    
    public void setX(double d) {privateCoordinates[0] = d;}
    public void setY(double d) {privateCoordinates[1] = d;}
    public void setZ(double d) {privateCoordinates[2] = d;}

    public double getX() {return privateCoordinates[0];}
    public double getY() {return privateCoordinates[1];}
    public double getZ() {return privateCoordinates[2];}
    

    abstract public void setX(double d);
    abstract public void setY(double d);
    abstract public void setZ(double d);
    
    abstract public double getX();
    abstract public double getY();
    abstract public double getZ();
    
    public Vector3D() {super(3);}
    
    public void set(int i, double d) {
        if (i == 0) setX(d);
        else if (i == 1) setY(d);
        else if (i == 2) setZ(d);
        else throw new ArrayIndexOutOfBoundsException();
    }
    
    /**
     * Overload plus operator to return Vector3D
     */
    Vector3D plus(Vector3D v2) {
        Vector3DClass answer = new Vector3DClass(this);
        answer.plusEquals(v2);
        return answer;
    }
    
    /**
     * 
     * Compute the centroid or mean point
     */
    public static Vector3D centroid(Vector bagOfPoints) {
        Vector3D[] coordinates = new Vector3D[0];
        return centroid((Vector3D[]) (bagOfPoints.toArray(coordinates)), null);
    }
    
    public static Vector3D centroid(Vector3D[] coordinates, double[] weights)   {
        if (coordinates == null) throw new NullPointerException();
        if (coordinates.length < 1) return null;
        
        Vector3DClass centroid = new Vector3DClass(0,0,0);
        double totalWeight = 0;
        double weight = 1.0;
        for (int i = 0; i < coordinates.length; i++) {
            if (weights != null) weight = weights[i];
            centroid.plusEquals(coordinates[i].scale(weight));
            totalWeight += weight;
        }
        centroid.selfScale(1.0/totalWeight);
        return centroid;
    }
    
    public double getElement(int i) {
        if (i == 0) return getX();
        else if (i == 1) return getY();
        else if (i == 2) return getZ();
        else throw new ArrayIndexOutOfBoundsException();
    }
    
    public double get(int i) {return getElement(i);}
    
    public Vector3DClass cross(Vector3D v2) {
        double x = getY()*v2.getZ() - getZ()*v2.getY();
        double y = getZ()*v2.getX() - getX()*v2.getZ();
        double z = getX()*v2.getY() - getY()*v2.getX();
        return new Vector3DClass(x,y,z);
    }
    
    public String toString() {
        String answer = "" + getX() + ", " + getY() + ", " + getZ();
        return answer;
    }
    
    
    // So we can hash on BaseVector3D
    public boolean equals(Object v) {
        if (this == v) return true;
        if (v == null) return false;
        if (v instanceof Vector3D) {
            Vector3D v2 = (Vector3D) v;
            if (getX() != v2.getX()) return false;
            if (getY() != v2.getY()) return false;
            if (getZ() != v2.getZ()) return false;
            return true;
        }
        else return false;
    }
    public int hashCode() {
        return 
        (new Double(getX())).hashCode() +
        (new Double(getY())).hashCode() +
        (new Double(getZ())).hashCode();          
    }
    
    public Vector3D rotate(Vector3D axis, double angle) {
        double cosAngle = Math.cos(angle);
        Vector3DClass answer = new Vector3DClass(this);
        answer.selfScale(cosAngle);
        answer.plusEquals( (axis.scale(axis.dot(this) * (1.0 - cosAngle))).plus
        (this.cross(axis).scale(Math.sin(angle))) );
        return answer;
    }
    
    public Iterator iterator() {
        return new VectorIterator(this);        
    }
    class VectorIterator implements Iterator {
        int coordinateIndex;
        Vector3D vector3d;
        VectorIterator(Vector3D v) {
            coordinateIndex = -1;
            vector3d = v;
        }
        public Object next() {
            coordinateIndex ++;
            if (coordinateIndex < 0) return null;
            if (coordinateIndex > 2) return null;
            return new Double(vector3d.get(coordinateIndex));
        }
        public boolean hasNext() {
            if (coordinateIndex < -1) return false;
            if (coordinateIndex > 1) return false;
            return true;
        }
        public void remove() {throw new UnsupportedOperationException();}
    }
    
    public Vector3D unit3() {
        return new Vector3DClass(unit());
    }
    public Vector3D plus3(Vector3D v2) {
        return new Vector3DClass(this.plus(v2));
    }
    public Vector3D minus3(Vector3D v2) {
        return new Vector3DClass(this.minus(v2));
    }
    public Vector3D scale3(double d) {
        return new Vector3DClass(this.scale(d));
    }
}
