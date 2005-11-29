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

public class Matrix3DClass implements MutableMatrix3D {
    private double[][] element = new double[3][3];

    public void set(int m, int n, double d) {
        element[m][n] = d;
    }

    public void plusEquals(Matrix3D m2) {
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                set(m, n, get(m, n) +  m2.get(m, n));
    }

    public void minusEquals(Matrix3D m2) {
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                set(m, n, get(m, n) -  m2.get(m, n));
    }

    public void timesEquals(double d) {
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                set(m, n, get(m, n) * d);
    }

    public double get(int m, int n) {
        return element[m][n];
    }

    public Matrix3D transpose() {
        MutableMatrix3D answer = new Matrix3DClass();
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                answer.set(m, n, get(n, m));
        return answer;
    }

    public Matrix3D plus(Matrix3D m2) {
        MutableMatrix3D answer = new Matrix3DClass();
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                answer.set(m, n, get(m, n) + m2.get(m, n));
        return answer;
    }

    public Matrix3D minus(Matrix3D m2) {
        MutableMatrix3D answer = new Matrix3DClass();
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                answer.set(m, n, get(m, n) - m2.get(m, n));
        return answer;
    }

    public Matrix3D times(double d) {
        MutableMatrix3D answer = new Matrix3DClass();
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                answer.set(m, n, get(m, n) * d);
        return answer;
    }

    public Vector3D times(Vector3D v) {
        MutableVector3D answer = new Vector3DClass();
        for (int i = 0; i < 3; i++) {
            double d = 0.0;
            for (int j = 0; j < 3; j++)
                d += get(i, j) * v.get(j);
            answer.set(i, d);
        }
        return answer;
    }

    public Matrix3D times(Matrix3D m2) {
        MutableMatrix3D answer = new Matrix3DClass();
        for (int i = 0; i < 3; i ++)
            for (int j = 0; j < 3; j++) {
                double d = 0;
                for (int k = 0; k < 3; k++)
                    d += get(i, k) * m2.get(k, j);
                answer.set(i, j, d);
            }
        return answer;
    }

    public double trace() {
        return get(0,0) + get(1,1) + get(2,2);
    }

}
