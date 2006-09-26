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
 * Created on Jul 25, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.geometry3d;

/**
 *  
  * @author Christopher Bruns
  * 
  * 4x4 matrix that includes both rotation and translation components
 */
public class HomogeneousTransformClass extends MathMatrixClass implements MutableHomogeneousTransform {

    public HomogeneousTransformClass() { 
        super(4, 4, 0.0);
        initialize();
    }
    
    public HomogeneousTransformClass(MathMatrix m) { 
        super(4, 4);
        if ( (m.getRowCount() != 4) || (m.getColumnCount() != 4) )
            throw new MatrixSizeMismatchException();
        copy(m);
    }
    
    private void initialize() {
        // Put ones on the diagonal
        set(0, 0, 1.0);
        set(1, 1, 1.0);
        set(2, 2, 1.0);
        set(3, 3, 1.0);        
    }
    
    public void setTranslation(Vector3D t) {
        for (int i = 0; i < 3; i++)
            set(3, i, t.get(i));
    }
    
    public void setRotation(Matrix3D r) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                set(i, j, r.get(i, j));
    }
    
    public Vector3D getTranslation() {
        return new Vector3DClass(get(3,0), get(3,1), get(3,2));
    }
    
    public Matrix3D getRotation() {
        MutableMatrix3D answer = new Matrix3DClass();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                answer.set(i, j, get(i, j));
        return answer;
    }
    
    public Vector3D times(Vector3D v) {
        Vector3DClass answer = new Vector3DClass();

        double wOut = 0.0; // 4th coordinate of answer vector
        for (int i = 0; i < 4; i ++) {
            double d = 0;
            for (int j = 0; j < 4; j++) {
                double inputCoordinate = 1.0;
                if (j != 3) inputCoordinate = v.get(j);                
                d += inputCoordinate * get(i,j);
            }
            if (i == 3) wOut = d;
            else answer.set(i, d);
        }

        if ( (wOut != 1.0) && (wOut != 0.0) ) {
            answer.setX(answer.getX()/wOut);
            answer.setY(answer.getY()/wOut);
            answer.setZ(answer.getZ()/wOut);
        }
        
        return answer;
    }
    
    public HomogeneousTransform times(HomogeneousTransform m) {
        MutableHomogeneousTransform answer = new HomogeneousTransformClass(this);
        answer.timesEquals(m);
        return answer;
    }
}
