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
public class Vector2DClass extends MathVectorClass implements MutableVector2D {

    public Vector2DClass() {
        super(2);
        setX(0);
        setY(0);
    }
    
    public Vector2DClass(double x, double y) {
        super(2);
        setX(x);
        setY(y);
    }

    public Vector2DClass(double[] vec) {
        super(2);
        setX(vec[0]);
        setY(vec[1]);
    }

    public Vector2DClass(MathVector template) {
        super(2);
        copy(template);
    }

    public void copy(MathVector v2) {
        if (v2.getDimension() != 2) throw new VectorSizeException();
        super.copy(v2);
    }

    protected void initialize(Vector2D v2) {
        setX(v2.getX());
        setY(v2.getY());
    }
    
    public void setX(double d) {set(0, d);}
    public void setY(double d) {set(1, d);}

    public double getX() {return get(0);}
    public double getY() {return get(1);}

    public double x() {return get(0);}
    public double y() {return get(1);}

    // Try to return Vector2D objects for methods that return vectors
    public Vector2D unit() {
        MutableVector2D answer = new Vector2DClass(this);
        answer.selfUnit();
        return answer;
    }
    
    public Vector2D minus(MathVector v2) {
        MutableVector2D answer = new Vector2DClass(this);
        answer.minusEquals(v2);
        return answer;
    }

    public Vector2D plus(MathVector v2) {
        MutableVector2D answer = new Vector2DClass(this);
        answer.plusEquals(v2);
        return answer;
    }
    
    public Vector2D times(double s) {
        MutableVector2D answer = new Vector2DClass(this);
        answer.timesEquals(s);
        return answer;
    }
    
    /**
     * 
     * Compute the centroid or mean point
     */
    public static Vector2D centroid(Collection<Vector2D> bagOfPoints) 
    throws InsufficientPointsException
    {
        Vector2D[] coordinates = new Vector2D[0];
        return centroid((Vector2D[]) (bagOfPoints.toArray(coordinates)), null);
    }
    
    public static Vector2D centroid(Vector2D[] coordinates, double[] weights) 
    throws InsufficientPointsException
    {
        if (coordinates == null) throw new InsufficientPointsException();
        if (coordinates.length < 1) throw new InsufficientPointsException();
        
        MutableVector2D centroid = new Vector2DClass(0,0);
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
    
    public Vector3D cross(Vector2D v2) {
        double x = 0.0;
        double y = 0.0;
        double z = getX()*v2.getY() - getY()*v2.getX();
        return new Vector3DClass(x,y,z);
    }
    
    public String toString() {
        String answer = "" + getX() + ", " + getY();
        return answer;
    }    
    
    // So we can hash on BaseVector2D
    public boolean equals(Object v) {
        if (this == v) return true;
        if (v == null) return false;
        if (v instanceof Vector2D) {
            Vector2D v2 = (Vector2D) v;
            if (getX() != v2.getX()) return false;
            if (getY() != v2.getY()) return false;
            return true;
        }
        else return false;
    }
    
    public int hashCode() {
        return 
        (new Double(getX())).hashCode() +
        2 * (new Double(getY())).hashCode();
    }
    
    public Vector2D rotate(double angle) {
        MutableVector2D answer = new Vector2DClass(this);
        
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        
        answer.setX(c*getX() - s*getY());
        answer.setY(c*getY() + s*getX());
        
        return answer;
    }

    public double[] toArray() {
        double[] answer = {x(),y()};
        return answer;
    }
}
