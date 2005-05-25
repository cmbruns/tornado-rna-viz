/*
 * Created on May 1, 2005
 *
 */
package org.simtk.geometry3d;

/** 
 * @author Christopher Bruns
 * 
 * A generic cylinder shape in 3 dimensions
 */
public class Cylinder {
    Vector3D head;
    Vector3D tail;
    double radius;
    
    public Cylinder(Vector3D hd, Vector3D tl, double rd) {
        head = hd;
        tail = tl;
        radius = rd;
    }
    public Vector3D getHead() {return head;}
    public Vector3D getTail() {return tail;}
    public double getRadius() {return radius;}
    public Vector3D getMidpoint() {return getHead().plus(getTail()).scale(0.5);}
    

}
