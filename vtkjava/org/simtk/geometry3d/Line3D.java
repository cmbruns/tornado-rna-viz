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
        direction = d;
        origin = o;
    }

    public Vector3D getDirection() {return direction;}
    public Vector3D getOrigin() {return origin;}
	
	public static Line3D bestLine3D(Vector<Vector3D> bagOfPoints)	{
        // 1) Compute the centroid or mean point
        Vector3D centroid = Vector3D.centroid(bagOfPoints);
		
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
		Vector3D largestEigenVector = new Vector3D (
				eigenVectors.get(0, largestEigenValueIndex),
				eigenVectors.get(1, largestEigenValueIndex),
				eigenVectors.get(2, largestEigenValueIndex));
		
		Line3D answer = new Line3D();
		
		// Normalize the line direction to unit length
		answer.direction = largestEigenVector.unit();
		
		// Normalize the line offset to be the point closest to the origin
		answer.origin = centroid.minus( answer.direction.scale(centroid.dot(answer.direction)) );
		
		return answer;
	}
	
}
