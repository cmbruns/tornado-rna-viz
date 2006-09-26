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
public class Vector3DFloatArrayWrapper extends Vector3DClass {
    /**
     * Get xyz values from three consecutive values in a float array
     * @param colorIndexArray
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
    
    public void set(int index, double value) {
        if (index == 0) dataArray[xIndex] = (float) value;
        else if (index == 1) dataArray[yIndex] = (float) value;
        else if (index == 2) dataArray[zIndex] = (float) value;
        else 
            throw new ArrayIndexOutOfBoundsException();
    }
    
    public double get(int index) {
        if (index == 0) return dataArray[xIndex];
        else if (index == 1) return dataArray[yIndex];
        else if (index == 2) return dataArray[zIndex];
        else 
            throw new ArrayIndexOutOfBoundsException();
        
    }    
}
