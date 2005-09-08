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
public class HomogeneousTransform extends MathMatrix {

    public HomogeneousTransform() { super(4,4); }
    
    DoubleVector3D mult(Vector3D v) {
        DoubleVector3D answer = new DoubleVector3D();

        double wOut = 0.0; // 4th coordinate of answer vector
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 4; j++) {
                double inputCoordinate = 1.0;
                if (j != 4) inputCoordinate = v.get(j);
                
                double delta = inputCoordinate * get(i,j);
                if (i == 4) wOut += inputCoordinate;
                else answer.set(i, answer.get(i) + delta);
            }
        }

        if ( (wOut != 1.0) && (wOut != 0.0) ) {
            answer.setX(answer.getX()/wOut);
            answer.setY(answer.getY()/wOut);
            answer.setZ(answer.getZ()/wOut);
        }
        
        return answer;
    }
}
