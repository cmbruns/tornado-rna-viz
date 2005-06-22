/*
 * Created on Jun 22, 2005
 *
 */
package org.simtk.geometry3d;

/**
 *  
  * @author Christopher Bruns
  * 
  * Vector3D whose underlying x, y, and z values are located in another object
 */
public class Vector3DFloatArrayWrapper extends BaseVector3D {
    /**
     * Get xyz values from three consecutive values in a float array
     * @param array
     * @param index
     */
    
    float[] dataArray;
    int xIndex;
    int yIndex;
    int zIndex;
    
    public Vector3DFloatArrayWrapper(float[] array, int index) {
        dataArray = array;
        xIndex = index;
        yIndex = index + 1;
        zIndex = index + 2;
    }
    
    public void setX(double d) {dataArray[xIndex] = (float) d;}
    public void setY(double d) {dataArray[yIndex] = (float) d;}
    public void setZ(double d) {dataArray[zIndex] = (float) d;}

    public double getX() {return dataArray[xIndex];}
    public double getY() {return dataArray[yIndex];}
    public double getZ() {return dataArray[zIndex];}

}
