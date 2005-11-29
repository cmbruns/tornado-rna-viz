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

    public MathMatrixClass(int  m, int n, double d) {
        initialize(m, n);
        for (int i = 0; i < getRowCount(); i++)
            for (int j = 0; j < getColumnCount(); j++)
                set(i, j, d);
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
    
    public void copy(MathMatrix m) {
        if ( (m.getRowCount() != getRowCount()) || (m.getColumnCount() != getColumnCount()) ) 
            throw new MatrixSizeMismatchException();
        for (int i = 0; i < getRowCount(); i++)
            for (int j = 0; j < getColumnCount(); j++)
                set(i, j, m.get(i, j));
    }
    
    public void setRow(int i, MathVector v) {
        if (v.getDimension() != getColumnCount())
            throw new MatrixSizeMismatchException();
        for (int j = 0; j < getColumnCount(); j++)
            set(i, j, v.get(j));
    }
    
    public void setColumn(int j, MathVector v) {
        if (v.getDimension() != getRowCount())
            throw new MatrixSizeMismatchException();
        for (int i = 0; i < getColumnCount(); i++)
            set(i, j, v.get(i));
    }
    
    public MathVector getColumn(int j) {
        MutableMathVector answer = new MathVectorClass(getRowCount());
        for (int i = 0; i < getRowCount(); i++)
            answer.set(i, get(i,j));
        return answer;
    }
    
    public MathVector getRow(int i) {
        MutableMathVector answer = new MathVectorClass(getColumnCount());
        for (int j = 0; j < getColumnCount(); j++)
            answer.set(j, get(i,j));
        return answer;
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

    public void timesEquals(MathMatrix m) {
        MathMatrix product = this.times(m);
        copy(product);
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
        MathVector diagonal = getDiagonal();
        double d = 0;
        for (int i = 0; i < diagonal.getDimension(); i ++)
            d += diagonal.get(i);
        return d;
    }

    public MathVector getDiagonal() {
        int diagonalLength = Math.min(getRowCount(), getColumnCount());
        MutableMathVector answer = new MathVectorClass(diagonalLength);
        for (int i = 0; i < diagonalLength; i ++)
            answer.set(i, get(i, i));
        return answer;
    }

    public Matrix3D m3() {
        if (this instanceof Matrix3D) return (Matrix3D) this;
        if ( getRowCount() != 3 ) throw new MatrixSizeMismatchException("Matrix3D must have exactly three rows");
        if ( getColumnCount() != 3 ) throw new MatrixSizeMismatchException("Matrix3D must have exactly three columns");
        return new Matrix3DClass(this);
    }
    
}
