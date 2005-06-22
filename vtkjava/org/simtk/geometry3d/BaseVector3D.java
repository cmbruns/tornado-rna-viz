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
abstract public class BaseVector3D implements Iterable<Double> {
    abstract public void setX(double d);
    abstract public void setY(double d);
    abstract public void setZ(double d);
    
    abstract public double getX();
    abstract public double getY();
    abstract public double getZ();
    
    /**
     * 
     * Compute the centroid or mean point
     */
    public static Vector3D centroid(Vector<BaseVector3D> bagOfPoints) {
        BaseVector3D[] coordinates = new BaseVector3D[0];
        return centroid(bagOfPoints.toArray(coordinates), null);
    }
    
    public static Vector3D centroid(BaseVector3D[] coordinates, double[] weights)   {
        if (coordinates == null) throw new NullPointerException();
        if (coordinates.length < 1) return null;
        
        Vector3D centroid = new Vector3D(0,0,0);
        double totalWeight = 0;
        double weight = 1.0;
        for (int i = 0; i < coordinates.length; i++) {
            if (weights != null) weight = weights[i];
            centroid = centroid.plus(coordinates[i].scale(weight));
            totalWeight += weight;
        }
        return centroid.scale(1.0/totalWeight);
    }
    
    public double getElement(int i) {
        if (i == 0) return getX();
        if (i == 1) return getY();
        if (i == 2) return getZ();
        throw new ArrayIndexOutOfBoundsException();
    }
    
    public double get(int i) {return getElement(i);}
    
    public Vector3D plus(BaseVector3D v2) {
        return new Vector3D(getX() + v2.getX(), getY() + v2.getY(), getZ() + v2.getZ());
    }
    
    public Vector3D minus(BaseVector3D v2) {
        return new Vector3D(getX() - v2.getX(), getY() - v2.getY(), getZ() - v2.getZ());
    }
    
    public double dot(BaseVector3D v2) {
        double answer = 0;
        answer += getX() * v2.getX();
        answer += getY() * v2.getY();
        answer += getZ() * v2.getZ();
        return answer;
    }
    
    public double length() {
        return Math.sqrt(this.dot(this));
    }
    
    public double distance(BaseVector3D v2) {
        return this.minus(v2).length();
    }
    
    public double distanceSquared(BaseVector3D v2) {
        BaseVector3D difference = this.minus(v2);
        return difference.dot(difference);
    }
    
    public Vector3D unit() {
        return this.scale(1.0/length());
    }
    
    public Vector3D scale(double s) {
        return new Vector3D(getX()*s, getY()*s, getZ()*s);
    }
    
    public Vector3D cross(BaseVector3D v2) {
        double x = getY()*v2.getZ() - getZ()*v2.getY();
        double y = getZ()*v2.getX() - getX()*v2.getZ();
        double z = getX()*v2.getY() - getY()*v2.getX();
        return new Vector3D(x,y,z);
    }
    
    public String toString() {
        String answer = "" + getX() + ", " + getY() + ", " + getZ();
        return answer;
    }
    
    
    // So we can hash on BaseVector3D
    public boolean equals(Object v) {
        if (this == v) return true;
        if (v == null) return false;
        if (v instanceof BaseVector3D) {
            BaseVector3D v2 = (BaseVector3D) v;
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
    
    public BaseVector3D rotate(BaseVector3D axis, double angle) {
        double cosAngle = Math.cos(angle);
        BaseVector3D answer = this.scale(cosAngle).plus
        (axis.scale(axis.dot(this) * (1.0 - cosAngle))).plus
        (this.cross(axis).scale(Math.sin(angle)));
        return answer;
    }
    
    public Iterator<Double> iterator() {
        return new VectorIterator(this);        
    }
    class VectorIterator implements Iterator<Double> {
        int coordinateIndex;
        BaseVector3D vector3d;
        VectorIterator(BaseVector3D v) {
            coordinateIndex = -1;
            vector3d = v;
        }
        public Double next() {
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
}
