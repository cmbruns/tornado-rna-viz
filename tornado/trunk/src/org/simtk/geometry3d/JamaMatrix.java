/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Nov 29, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.geometry3d;

/**
 *  
  * @author Christopher Bruns
  * 
  * Wrapper around Jama.Matrix class, to support MathMatrix interface
 */
public class JamaMatrix extends MathMatrixClass {
    private Jama.Matrix jamaMatrix;
    
    JamaMatrix(int m, int n) {
        super(0, 0); // parent internal data will not be used
        jamaMatrix = new Jama.Matrix(m, n);
    }
    
    JamaMatrix(MathMatrix m) {
        super(m.getRowCount(), m.getColumnCount());
        for (int i = 0; i < m.getRowCount(); i++)
            for (int j = 0; j < m.getColumnCount(); j++)
                set(i, j, m.get(i, j));
    }
    
    JamaMatrix(Jama.Matrix m) {
        super(0, 0);
        jamaMatrix = m.copy();
    }

    // Override the methods that access the internal data structure
    public double get(int i, int j) {return jamaMatrix.get(i, j);}
    public void set(int i, int j, double d) {jamaMatrix.set(i, j, d);}
    public int getColumnCount() {return jamaMatrix.getColumnDimension();}
    public int getRowCount() {return jamaMatrix.getRowDimension();}

    public Jama.EigenvalueDecomposition getEigenvalueDecomposition() {
        Jama.EigenvalueDecomposition answer = 
            new Jama.EigenvalueDecomposition(jamaMatrix);
        return answer;
    }
    
    static final long serialVersionUID = 01L;
}
