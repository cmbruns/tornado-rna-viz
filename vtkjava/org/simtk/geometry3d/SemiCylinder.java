/*
 * Created on May 9, 2005
 *
 */
package org.simtk.geometry3d;

/**
 *  
  * @author Christopher Bruns
  * 
  * Half of a cylinder, cut lengthwise along the cylinder axis
 */
public class SemiCylinder extends Cylinder {
    /**
     * Vector normal to the plane dividing the cylinder
     */
    Vector3D normal;  
    public SemiCylinder(Vector3D head, Vector3D tail, double radius, Vector3D normalArg) {
        super(head, tail, radius);
        normal = normalArg;
    }
    public Vector3D getNormal() {return normal;}
}
