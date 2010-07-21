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
 * Created on Apr 21, 2005
 *
 */
package org.simtk.geometry3d;

import java.util.*;

import Jama.*; // Numerical methods

/**
 * @author Christopher Bruns
 *
 * Infinite line in three dimensions.
 */
public class Line3D {
	public Vector3D direction;
	public Vector3D origin;
    
    public Line3D() {}
    public Line3D(Vector3D d, Vector3D o) {
        direction = new Vector3DClass(d.unit());
        origin = new Vector3DClass( o.minus(direction.times(o.dot(direction))) );
    }

    public Vector3D getDirection() {return direction;}
    public Vector3D getOrigin() {return origin;}
	
    // Closest point on a line to a point in space
    public Vector3D getClosestPoint(Vector3D v) {
        return new Vector3DClass( origin.plus(direction.times(direction.dot(v))) );
    }
    
	public static Line3D bestLine3D(Vector<Vector3D> bagOfPoints)	
    throws InsufficientPointsException
    {
        // 1) Compute the centroid or mean point
        Vector3D centroid = new Vector3DClass ( Vector3DClass.centroid(bagOfPoints) );
		
		// 2) Compute the covariance or variance-covariance matrix
		Matrix covarianceMatrix = new Matrix(3, 3);
		for (int i = 0; i < 3; i++) {
			for (int j = i; j < 3; j++) { // Only visit upper triangle of symmetric matrix
				double matrixElement = 0;
				for (int p = 0; p < bagOfPoints.size(); p++) {
					Vector3D point = (Vector3D) bagOfPoints.elementAt(p);
					double deltaJ = point.getElement(j) - centroid.getElement(j);
					double deltaI = point.getElement(i) - centroid.getElement(i);
					matrixElement += deltaI * deltaJ;
				}
				// minus one because that's how statistics works
				covarianceMatrix.set( i,j, matrixElement/(bagOfPoints.size()-1) );				
				// Populate both halves of matrix
				if (j != i)
					covarianceMatrix.set( j,i, matrixElement/(bagOfPoints.size()-1) ); 
			}
		}
		
		EigenvalueDecomposition eigenSystem = new EigenvalueDecomposition(covarianceMatrix);

		// Figure out where the largest eigen value is in the system
		double[] eigenValues = eigenSystem.getRealEigenvalues();
		int largestEigenValueIndex = 0;
		for (int i = 1; i < 3; i++) 
			if (eigenValues[i] > eigenValues[largestEigenValueIndex])
				largestEigenValueIndex = i;

		Matrix eigenVectors = eigenSystem.getV();
		Vector3D largestEigenVector = new Vector3DClass (
				eigenVectors.get(0, largestEigenValueIndex),
				eigenVectors.get(1, largestEigenValueIndex),
				eigenVectors.get(2, largestEigenValueIndex));
		
		Line3D answer = new Line3D();
		
		// Normalize the line direction to unit length
		answer.direction = new Vector3DClass(largestEigenVector.unit());
		
		// Normalize the line offset to be the point closest to the origin
		answer.origin = new Vector3DClass( centroid.minus( answer.direction.times(centroid.dot(answer.direction)) ) );
		
		return answer;
	}
	
}
