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
 * Created on May 1, 2005
 *
 */
package org.simtk.geometry3d;

import java.util.Vector;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class Plane3D {
    
    Vector3D normal; // Unit vector perpendicular to the plane
    Vector3D origin; // A point on the plane, closest to the origin

    /**
     * Distance between a point and a plane
     * @return
     */
    public double distance(BaseVector3D p) {
        double pointAlpha = p.dot(normal);
        double planeAlpha = origin.dot(normal);
        double distance = pointAlpha - planeAlpha;
        if (distance < 0) distance = -distance;
        return distance;
    }
    
    public Vector3D getNormal() {return normal;}
    
    /**
     * Create a plane that minimizes the sum of squared distances to a set of points
     * @param bagOfPoints
     * @return
     */
    
    public static Plane3D bestPlane3D(Vector bagOfPoints)   {
        Vector3D[] coordinates = new Vector3D[0];
        return bestPlane3D((BaseVector3D[])bagOfPoints.toArray(coordinates), null);
    }

    public static Plane3D bestPlane3D(BaseVector3D[] coordinates, double[] weights)   {
        // 1) Compute the centroid or mean point
        Vector3D centroid = Vector3D.centroid(coordinates, weights);
        
        // 2) Compute the covariance or variance-covariance matrix
        Matrix covarianceMatrix = new Matrix(3, 3);
        for (int i = 0; i < 3; i++) {
            for (int j = i; j < 3; j++) { // Only visit upper triangle of symmetric matrix
                double matrixElement = 0;
                double totalWeight = 0;
                double weight = 1.0;
                for (int p = 0; p < coordinates.length; p++) {
                    BaseVector3D point = coordinates[p];
                    double deltaJ = point.getElement(j) - centroid.getElement(j);
                    double deltaI = point.getElement(i) - centroid.getElement(i);
                    if (weights != null) weight = weights[p];
                    
                    matrixElement += deltaI * deltaJ * weight;
                    totalWeight += weight;
                }
                // minus one because that's how statistics works
                covarianceMatrix.set( i,j, matrixElement/totalWeight );              
                // Populate both halves of matrix
                if (j != i)
                    covarianceMatrix.set( j,i, matrixElement/totalWeight ); 
            }
        }
        
        EigenvalueDecomposition eigenSystem = new EigenvalueDecomposition(covarianceMatrix);

        // Figure out where the largest eigen value is in the system
        double[] eigenValues = eigenSystem.getRealEigenvalues();
        int largestEigenValueIndex = 0;
        int smallestEigenValueIndex = 0;
        for (int i = 1; i < 3; i++) {
            if (eigenValues[i] > eigenValues[largestEigenValueIndex])
                largestEigenValueIndex = i;
            if (eigenValues[i] < eigenValues[smallestEigenValueIndex])
                smallestEigenValueIndex = i;
        }

        Matrix eigenVectors = eigenSystem.getV();
        Vector3D largestEigenVector = new Vector3D (
                eigenVectors.get(0, largestEigenValueIndex),
                eigenVectors.get(1, largestEigenValueIndex),
                eigenVectors.get(2, largestEigenValueIndex));
        Vector3D smallestEigenVector = new Vector3D (
                eigenVectors.get(0, smallestEigenValueIndex),
                eigenVectors.get(1, smallestEigenValueIndex),
                eigenVectors.get(2, smallestEigenValueIndex));
        
        Plane3D answer = new Plane3D();
        
        // Normalize the plane normal vector to unit length
        answer.normal = smallestEigenVector.unit();
        
        // Normalize the plane origin to be the point closest to the origin
        answer.origin = answer.normal.scale(centroid.dot(answer.normal));
        
        return answer;
    }
}
