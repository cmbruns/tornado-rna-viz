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
    BaseVector3D head;
    BaseVector3D tail;
    double radius;
    
    public Cylinder(BaseVector3D hd, BaseVector3D tl, double rd) {
        head = hd;
        tail = tl;
        radius = rd;
    }
    public BaseVector3D getHead() {return head;}
    public BaseVector3D getTail() {return tail;}
    public double getRadius() {return radius;}
    public BaseVector3D getMidpoint() {return getHead().plus(getTail()).scale(0.5);}
    

}
