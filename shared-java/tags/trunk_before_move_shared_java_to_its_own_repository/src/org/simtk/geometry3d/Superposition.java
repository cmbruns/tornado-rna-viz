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

import Jama.EigenvalueDecomposition;
import java.util.*;

public class Superposition {

    /**
     * Return a transformation that can place one set of vectors onto another
     * while minimizing the least-squares distance between the corresponding
     * points.
     * 
     * The transformation is meant to be applied to the first set of coordinates.
     * 
     * W. Kabsch (1978) A discussion of the solution for the best rotation to
     * relate two sets of vectors.  Acta Cryst. A34: 827-828
     * 
     * W. Kabsch (1976) A solution to the best rotation to relate two sets
     * of vectors.  Acta Cryst. A32: 922
     * 
     * @param v1
     * @param v2
     * @param weights
     * @return
     */
    public static HomogeneousTransform kabsch78(Vector3D[] v1, Vector3D[] v2, double[] weights) {

        // 1) check array sizes
        int vectorCount = v1.length;
        
        if (v2.length != vectorCount) 
            throw new VectorSizeException("The two sets of points must contain the same number of points");

        if ( (weights != null) && (weights.length != vectorCount) )
            throw new VectorSizeException("The number of weights must match the number of points");

        
        // 2) remove translation component
        // Compute center of mass
        MutableVector3D centroid1 = new Vector3DClass(0, 0, 0);
        MutableVector3D centroid2 = new Vector3DClass(0, 0, 0);
        double mass = 0.0;
        for (int i = 0; i < vectorCount; i++) {
            double weight = 1.0;
            if (weights != null) weight = weights[i];
            centroid1.plusEquals(v1[i].times(weight));
            centroid2.plusEquals(v2[i].times(weight));
            mass += weight;
        }
        centroid1.timesEquals(1.0 / mass);
        centroid2.timesEquals(1.0 / mass);
        // Vector3D translation = centroid2.minus(centroid1);
        
        // 3) generate matrix R from equation 3 of Kabsh 1978
        MutableMatrix3D R = new Matrix3DClass();
        for (int i = 0; i < 3; i ++)
            for (int j = 0; j < 3; j++) {
                double Rij = 0.0;
                for (int n = 0; n < vectorCount; n++) {
                    double weight = 1.0;
                    if (weights != null) weight = weights[n];
                    Vector3D x = v1[n].minus(centroid1);
                    Vector3D y = v2[n].minus(centroid2);
                    Rij += weight * y.get(i) * x.get(j);
                }
                R.set(i, j, Rij);
            }
        
        // System.out.println("R = "+R);
        
        // 4) compute eigenvectors of Rtranspose * R
        JamaMatrix RtR = new JamaMatrix(R.transpose().times(R));
        EigenvalueDecomposition eigens = RtR.getEigenvalueDecomposition();
        Vector3D eigenValues = new Vector3DClass(new JamaMatrix(eigens.getD()).getDiagonal());
        Matrix3D eigenVectors = new Matrix3DClass(new JamaMatrix(eigens.getV()));
        
        // System.out.println("RtR = "+RtR);
        // System.out.println("eigenvalues = "+eigenValues);
        // System.out.println("eigenvectors = "+eigenVectors);
        
        // Note order of eigenvalues from largest to smallest
        Integer[] eigenOrder = {new Integer(0),new Integer(1),new Integer(2)};
        Comparator<Integer> eigenComparator = new EigenComparator(eigenValues);
        Arrays.sort(eigenOrder, eigenComparator);
        
        // a is the matrix of eigenvectors, in decreasing order
        MutableMatrix3D a = new Matrix3DClass();
        for (int i = 0; i < 2; i++)
            a.setRow(i, eigenVectors.getColumn(eigenOrder[i].intValue()));
        // Compute the third eigenvector to form a right-handed system with the other two
        a.setRow(2, a.getRow(0).cross(a.getRow(1)));
        
        MutableMatrix3D b = new Matrix3DClass();
        for (int i = 0; i < 2; i++)
            b.setRow( i, R.times(a.getRow(i)).times(1.0/Math.sqrt(eigenValues.get(eigenOrder[i].intValue()))) );
        // Compute the third eigenvector to form a right-handed system with the other two
        b.setRow(2, b.getRow(0).cross(b.getRow(1)));
        
        // if b3 * R * a3 < 0, this is a reflection, not a rotation
        // make it a rotation
        double sigma[] = {1.0, 1.0, 1.0};
        double discriminant = b.getRow(2).dot(R.times(a.getRow(2)));
        if (discriminant < 0) {
            sigma[2] = -1.0; // For use in computing rms deviation
            // TODO what has to be done to make it a rotation?
        }
        
        // 5) generate rotation matrix
        MutableMatrix3D rotation = new Matrix3DClass(); // rotation matrix
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                double uij = 0.0;
                for (int k = 0; k < 3; k++)
                    uij += b.get(k, i) * a.get(k, j);
                rotation.set(i, j, uij);
            }
        
        // 6) combine rotation and translation into homogeneous 4x4 transform
        MutableHomogeneousTransform translate1ToOrigin = new HomogeneousTransformClass();
        translate1ToOrigin.setTranslation(centroid1.times(-1));
        
        MutableHomogeneousTransform rotate = new HomogeneousTransformClass();
        rotate.setRotation(rotation);

        MutableHomogeneousTransform translateOriginTo2 = new HomogeneousTransformClass();
        translateOriginTo2.setTranslation(centroid2);
        
        return translateOriginTo2.times(rotate.times(translate1ToOrigin));
    }
}

class EigenComparator implements java.util.Comparator<Integer> {
    MathVector eigenvalues;
    EigenComparator(MathVector eigenvalues) {this.eigenvalues = eigenvalues;}
    public int compare(Integer o1, Integer o2) {
        int i1 = ((Integer)o1).intValue();
        int i2 = ((Integer)o2).intValue();
        if (eigenvalues.get(i1) < eigenvalues.get(i2)) return 1;
        if (eigenvalues.get(i1) > eigenvalues.get(i2)) return -1;
        return 0;
    }
}

