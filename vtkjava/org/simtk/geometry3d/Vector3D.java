/*
 * Created on Apr 20, 2005
 *
 */
package org.simtk.geometry3d;

/**
 * @author Christopher Bruns
 *
 * A point or direction vector in three dimensions.
 */
public class Vector3D extends BaseVector3D {
    private double[] privateCoordinates = new double[3];
    public Vector3D() {
        setX(0);
        setY(0);
        setZ(0);
        }
    public Vector3D(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }
    
    public void setX(double d) {privateCoordinates[0] = d;}
    public void setY(double d) {privateCoordinates[1] = d;}
    public void setZ(double d) {privateCoordinates[2] = d;}

    public double getX() {return privateCoordinates[0];}
    public double getY() {return privateCoordinates[1];}
    public double getZ() {return privateCoordinates[2];}
}
