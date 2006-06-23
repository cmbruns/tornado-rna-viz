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

import java.util.*;

/**
 * @author Christopher Bruns
 *
 * A point or direction vector in three dimensions.
 */
public class Vector3DClass extends MathVectorClass implements MutableVector3D {

    public Vector3DClass() {
        super(3);
        setX(0);
        setY(0);
        setZ(0);
    }
    
    public Vector3DClass(double x, double y, double z) {
        super(3);
        setX(x);
        setY(y);
        setZ(z);
    }

    public Vector3DClass(MathVector template) {
        super(3);
        copy(template);
    }

    public void copy(MathVector v2) {
        if (v2.getDimension() != 3) throw new VectorSizeException();
        super.copy(v2);
    }

    protected void initialize(Vector3D v2) {
        setX(v2.getX());
        setY(v2.getY());
        setZ(v2.getZ());
    }
    
    public void setX(double d) {set(0, d);}
    public void setY(double d) {set(1, d);}
    public void setZ(double d) {set(2, d);}

    public double getX() {return get(0);}
    public double getY() {return get(1);}
    public double getZ() {return get(2);}

    public double x() {return get(0);}
    public double y() {return get(1);}
    public double z() {return get(2);}

    // Try to return Vector3D objects for methods that return vectors
    public Vector3D unit() {
        MutableVector3D answer = new Vector3DClass(this);
        answer.selfUnit();
        return answer;
    }
    
    public Vector3D minus(MathVector v2) {
        MutableVector3D answer = new Vector3DClass(this);
        answer.minusEquals(v2);
        return answer;
    }

    public Vector3D plus(MathVector v2) {
        MutableVector3D answer = new Vector3DClass(this);
        answer.plusEquals(v2);
        return answer;
    }
    
    public Vector3D times(double s) {
        MutableVector3D answer = new Vector3DClass(this);
        answer.timesEquals(s);
        return answer;
    }
    
    /**
     * 
     * Compute the centroid or mean point
     */
    public static Vector3D centroid(Collection<Vector3D> bagOfPoints) 
    throws InsufficientPointsException
    {
        Vector3D[] coordinates = new Vector3D[0];
        return centroid((Vector3D[]) (bagOfPoints.toArray(coordinates)), null);
    }
    
    public static Vector3D centroid(Vector3D[] coordinates, double[] weights) 
    throws InsufficientPointsException
    {
        if (coordinates == null) throw new InsufficientPointsException();
        if (coordinates.length < 1) throw new InsufficientPointsException();
        
        MutableVector3D centroid = new Vector3DClass(0,0,0);
        double totalWeight = 0;
        double weight = 1.0;
        for (int i = 0; i < coordinates.length; i++) {
            if (weights != null) weight = weights[i];
            centroid.plusEquals(coordinates[i].times(weight));
            totalWeight += weight;
        }
        centroid.timesEquals(1.0/totalWeight);
        return centroid;
    }
    
    public Vector3D cross(Vector3D v2) {
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
        2 * (new Double(getY())).hashCode() +
        3 * (new Double(getZ())).hashCode();          
    }
    
    public Vector3D rotate(Vector3D axis, double angle) {
        double cosAngle = Math.cos(angle);
        MutableVector3D answer = new Vector3DClass(this);
        answer.timesEquals(cosAngle);
        answer.plusEquals( (axis.times(axis.dot(this) * (1.0 - cosAngle))).plus
        (this.cross(axis).times(Math.sin(angle))) );
        return answer;
    }
}
