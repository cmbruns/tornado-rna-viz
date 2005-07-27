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

public class MathMatrix {
    private double m_matrix[][];

    public MathMatrix(int  m, int n) {
        m_matrix = new double[m][n];
    }

    double get(int i, int j) {return m_matrix[i][j];}

    void set(int i, int j, double d) {m_matrix[i][j] = d;}

    public int rowCount() {return m_matrix.length;}
    public int columnCount() {
        if (rowCount() < 1) return 0; 
        return m_matrix[0].length;
    }
    
    public MathMatrix mult(MathMatrix m2) {
        if ( columnCount() != m2.rowCount() ) throw new RuntimeException("Matrix size mismatch");
        MathMatrix answer = new MathMatrix(rowCount(), m2.columnCount());
        for (int i = 0; i < rowCount(); i++)
            for (int j = 0; j < m2.columnCount(); j++) {
                double delta = 0;
                for (int k = 0; k < columnCount(); k++)
                    delta += get(i, k) * m2.get(k, j);
                answer.set(i, j, delta);
            }
        return answer;
    }
}
