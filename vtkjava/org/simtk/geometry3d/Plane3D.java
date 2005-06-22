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
    public double distance(Vector3D p) {
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
    
    public static Plane3D bestPlane3D(Vector<BaseVector3D> bagOfPoints)   {
        Vector3D[] coordinates = new Vector3D[0];
        return bestPlane3D(bagOfPoints.toArray(coordinates), null);
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
