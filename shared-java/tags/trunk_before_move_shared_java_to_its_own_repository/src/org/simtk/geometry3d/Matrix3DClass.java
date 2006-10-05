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

public class Matrix3DClass extends MathMatrixClass implements MutableMatrix3D {
    static public Matrix3D identity = new Matrix3DClass(1,0,0, 0,1,0, 0,0,1);

    public static Matrix3D axisAngle(Vector3D axis, double angle) {
        // 1) Move axis to x axis
        Vector3D v1 = axis;
        Vector3D v0 = Vector3DClass.X_AXIS;
        if (v1.unit().dot(v0) > 0.8) v0 = Vector3DClass.Y_AXIS;
        Vector3D v2 = v0.cross(v1).unit();
        Vector3D v3 = v1.cross(v2);
        Matrix3D rot1 = new Matrix3DClass(
                v1.x(), v1.y(), v1.z(),
                v2.x(), v2.y(), v2.z(),
                v3.x(), v3.y(), v3.z()
                );

        // 2) rotate about x axis
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        Matrix3D rot2 = new Matrix3DClass(
                1, 0, 0,
                0, c, -s,
                0, s, c
                );
        
        // 3) move back from x axis to original
        
        return rot1.transpose().times(rot2.times(rot1));
    }
    
    public Matrix3DClass() {super(3,3);}

    public Matrix3DClass(MathMatrix m) {
        super(3,3);
        checkShape(m);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                set(i, j, m.get(i, j));
    }
    
    public Matrix3DClass(
            double e00, double e01, double e02, 
            double e10, double e11, double e12, 
            double e20, double e21, double e22) {
        super(3,3);

        set(0,0,e00);
        set(0,1,e01);
        set(0,2,e02);

        set(1,0,e10);
        set(1,1,e11);
        set(1,2,e12);

        set(2,0,e20);
        set(2,1,e21);
        set(2,2,e22);

    }

    public void plusEquals(MathMatrix m2) {
        checkShape(m2);
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                set(m, n, get(m, n) +  m2.get(m, n));
    }

    public void minusEquals(MathMatrix m2) {
        checkShape(m2);
        for (int m = 0; m < 3; m++)
            for (int n = 0; n < 3; n++)
                set(m, n, get(m, n) -  m2.get(m, n));
    }

    public Matrix3D plus(MathMatrix m2) {
        checkShape(m2);
        MutableMatrix3D answer = new Matrix3DClass(this);
        answer.plusEquals(m2);
        return answer;
    }

    public Matrix3D minus(MathMatrix m2) {
        checkShape(m2);
        MutableMatrix3D answer = new Matrix3DClass(this);
        answer.minusEquals(m2);
        return answer;
    }

    public Matrix3D transpose() {
        MutableMatrix3D answer = new Matrix3DClass();
        for (int m = 0; m < getColumnCount(); m++)
            for (int n = 0; n < getRowCount(); n++)
                answer.set(m, n, get(n, m));
        return answer;
    }

    public Vector3D times(MathVector v) {
        checkShape(v);
        MutableVector3D answer = new Vector3DClass();
        for (int i = 0; i < 3; i++) {
            double d = 0.0;
            for (int j = 0; j < 3; j++)
                d += get(i, j) * v.get(j);
            answer.set(i, d);
        }
        return answer;
    }

    public Matrix3D times(MathMatrix m2) {
        checkShape(m2);
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
    
    public Matrix3D times(double d) {
        MutableMatrix3D answer = new Matrix3DClass(this);
        for (int i = 0; i < getRowCount(); i++)
            for (int j = 0; j < getColumnCount(); j++)
                answer.set(i, j, get(i, j) * d);
        return answer;
    }

    public Vector3D getColumn(int j) {
        MutableVector3D answer = new Vector3DClass();
        for (int i = 0; i < getRowCount(); i++)
            answer.set(i, get(i,j));
        return answer;
    }
    
    public Vector3D getRow(int i) {
        MutableVector3D answer = new Vector3DClass();
        for (int j = 0; j < getColumnCount(); j++)
            answer.set(j, get(i,j));
        return answer;
    }
    
    public Vector3D getDiagonal() {
        MutableVector3D answer = new Vector3DClass();
        for (int i = 0; i < 3; i ++)
            answer.set(i, get(i, i));
        return answer;
    }

    public double[][] toArray() {
        double[][] m = new double[3][];
        for (int i = 0; i < 3; ++i) {
            m[i] = new double[3];
            for (int j = 0; j < 3; ++j) {
                m[i][j] = this.get(i,j);
            }
        }
        return m;
    }
    
    private static void checkShape(MathMatrix m) {
        if ( (m.getRowCount() != 3) || (m.getColumnCount() != 3) )
            throw new MatrixSizeMismatchException("Cannot construct Matrix3D from non 3x3 matrix");        
    }
    private static void checkShape(MathVector v) {
        if (v.getDimension() != 3)
            throw new MatrixSizeMismatchException("Cannot construct Vector3D from non 3-vector");        
    }
}
