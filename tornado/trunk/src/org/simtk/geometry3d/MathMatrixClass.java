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

public class MathMatrixClass implements MutableMathMatrix {
    private double m_matrix[][];

    public MathMatrixClass(int  m, int n) {
        initialize(m, n);
    }

    public MathMatrixClass(MathMatrix m) {
        initialize(m);
    }

    private void initialize(int m, int n) {
        m_matrix = new double[m][n];        
    }
    
    private void initialize(MathMatrix m) {
        initialize(m.getRowCount(), m.getColumnCount());
        for (int i = 0; i < getRowCount(); i++)
            for (int j = 0; j < getColumnCount(); j++)
                set(i, j, m.get(i, j));
    }
    
    public double get(int i, int j) {return m_matrix[i][j];}

    public void set(int i, int j, double d) {
        m_matrix[i][j] = d;
    }

    public MathMatrix transpose() {
        MutableMathMatrix answer = new MathMatrixClass(getColumnCount(), getRowCount());
        for (int m = 0; m < getColumnCount(); m++)
            for (int n = 0; n < getRowCount(); n++)
                answer.set(m, n, get(n, m));
        return answer;
    }

    public void plusEquals(MathMatrix m2) {
        for (int m = 0; m < getRowCount(); m++)
            for (int n = 0; n < getColumnCount(); n++)
                set(m, n, get(m, n) +  m2.get(m, n));
    }

    public void minusEquals(MathMatrix m2) {
        for (int m = 0; m < getRowCount(); m++)
            for (int n = 0; n < getColumnCount(); n++)
                set(m, n, get(m, n) -  m2.get(m, n));
    }

    public void timesEquals(double d) {
        for (int m = 0; m < getRowCount(); m++)
            for (int n = 0; n < getColumnCount(); n++)
                set(m, n, get(m, n) * d);
    }

    public int getRowCount() {return m_matrix.length;}
    public int getColumnCount() {
        if (getRowCount() < 1) return 0; 
        return m_matrix[0].length;
    }
    
    public MathMatrix plus(MathMatrix m2) {
        if ( getColumnCount() != m2.getColumnCount() ) throw new MatrixSizeMismatchException();
        if ( getRowCount() != m2.getRowCount() ) throw new MatrixSizeMismatchException();
        MutableMathMatrix answer = new MathMatrixClass(this);
        for (int m = 0; m < getRowCount(); m++)
            for (int n = 0; n < getColumnCount(); n++)
                answer.set(m, n, get(m, n) + m2.get(m, n));
        return answer;
    }

    public MathMatrix minus(MathMatrix m2) {
        if ( getColumnCount() != m2.getColumnCount() ) throw new MatrixSizeMismatchException();
        if ( getRowCount() != m2.getRowCount() ) throw new MatrixSizeMismatchException();
        MutableMathMatrix answer = new MathMatrixClass(this);
        for (int m = 0; m < getRowCount(); m++)
            for (int n = 0; n < getColumnCount(); n++)
                answer.set(m, n, get(m, n) - m2.get(m, n));
        return answer;
    }

    public MathMatrix times(MathMatrix m2) {
        if ( getColumnCount() != m2.getRowCount() ) throw new MatrixSizeMismatchException();
        MathMatrixClass answer = new MathMatrixClass(getRowCount(), m2.getColumnCount());
        for (int i = 0; i < getRowCount(); i++)
            for (int j = 0; j < m2.getColumnCount(); j++) {
                double delta = 0;
                for (int k = 0; k < getColumnCount(); k++)
                    delta += get(i, k) * m2.get(k, j);
                answer.set(i, j, delta);
            }
        return answer;
    }

    public MathVector times(MathVector v) {
        if ( getColumnCount() != v.getDimension() ) throw new MatrixSizeMismatchException();
        MutableMathVector answer = new MathVectorClass(getRowCount());
        for (int i = 0; i < getRowCount(); i++) {
            double d = 0;
            for (int j = 0; j < getColumnCount(); j++)
                d += v.get(j) * get(i, j);
            answer.set(i, d);
        }
        return answer;
    }

    public MathMatrix times(double d) {
        MathMatrixClass answer = new MathMatrixClass(this);
        for (int i = 0; i < getRowCount(); i++)
            for (int j = 0; j < getColumnCount(); j++)
                answer.set(i, j, get(i, j) * d);
        return answer;
    }

    public double trace() {
        int diagonalLength = Math.min(getRowCount(), getColumnCount());
        double d = 0.0;
        for (int i = 0; i < diagonalLength; i ++)
            d += get(i, i);
        return d;
    }

}
