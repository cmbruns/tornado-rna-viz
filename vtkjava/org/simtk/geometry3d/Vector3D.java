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
public class Vector3D {
    double[] coordinates = new double[3];
    
    public Vector3D() {coordinates[0] = coordinates[1] = coordinates[2] = 0;}
    public Vector3D(double x, double y, double z) {
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = z;
    }
    
    /**
     * 
     * Compute the centroid or mean point
     */
    public static Vector3D centroid(Vector<Vector3D> bagOfPoints) {
        Vector3D[] coordinates = new Vector3D[0];
        return centroid(bagOfPoints.toArray(coordinates), null);
    }

    public static Vector3D centroid(Vector3D[] coordinates, double[] weights)   {
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

	public double getX() {return coordinates[0];}
	public double getY() {return coordinates[1];}
	public double getZ() {return coordinates[2];}
	
	public double getElement(int i) {return coordinates[i];}
	public double get(int i) {return coordinates[i];}
	
	public Vector3D plus(Vector3D v2) {
		return new Vector3D(getX() + v2.getX(), getY() + v2.getY(), getZ() + v2.getZ());
	}

	public Vector3D minus(Vector3D v2) {
		return new Vector3D(getX() - v2.getX(), getY() - v2.getY(), getZ() - v2.getZ());
	}
	
	public double dot(Vector3D v2) {
		double answer = 0;
		answer += getX() * v2.getX();
		answer += getY() * v2.getY();
		answer += getZ() * v2.getZ();
		return answer;
	}
	
	public double length() {
		return Math.sqrt(this.dot(this));
	}
	
	public double distance(Vector3D v2) {
		return this.minus(v2).length();
	}

    public double distanceSquared(Vector3D v2) {
        Vector3D difference = this.minus(v2);
        return difference.dot(difference);
    }
	
	public Vector3D unit() {
		return this.scale(1.0/length());
	}
	
	public Vector3D scale(double s) {
		return new Vector3D(getX()*s, getY()*s, getZ()*s);
	}
	
	public Vector3D cross(Vector3D v2) {
		double x = getY()*v2.getZ() - getZ()*v2.getY();
		double y = getZ()*v2.getX() - getX()*v2.getZ();
		double z = getX()*v2.getY() - getY()*v2.getX();
		return new Vector3D(x,y,z);
	}
	
	public String toString() {
		String answer = "" + getX() + ", " + getY() + ", " + getZ();
		return answer;
	}
    

    // So we can hash on Vector3D
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
        Vector3D answer = this.scale(cosAngle).plus
            (axis.scale(axis.dot(this) * (1.0 - cosAngle))).plus
            (this.cross(axis).scale(Math.sin(angle)));
        return answer;
    }
}
