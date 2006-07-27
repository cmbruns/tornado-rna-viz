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
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import vtk.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class MoleculeTensorActor extends ActorCartoonClass {
    
    /**
     * How many spline segments per residue
     */
    static double splineFactor = 5.0;
    
    public MoleculeTensorActor(Molecule molecule) 
    throws NoCartoonCreatedException
    {
        // Only draw shapes for macromolecules
        if (! (molecule instanceof Biopolymer))
           throw new NoCartoonCreatedException("Not a biopolymer for molecule tensor");

        // Biopolymer biopolymer = (Biopolymer) molecule;
        
        if (molecule.atoms().size() < 2) 
            throw new NoCartoonCreatedException("Not enough atoms for molecule tensor");
        
        // 1) Compute center of mass of molecule
        Vector3D centerOfMass = new Vector3DClass(0,0,0);
        double totalMass = 0.0;
        for (Atom atom : molecule.atoms()) {
            centerOfMass = centerOfMass.plus(atom.getCoordinates());
            totalMass += 1.0;
        }
        if (totalMass <= 1.0) 
            throw new NoCartoonCreatedException("Zero mass for molecule tensor");
        centerOfMass = centerOfMass.times(1.0 / totalMass);
        
        // 2) Compute covariance matrix of mass distributions
        Matrix covarianceMatrix = new Matrix(3, 3);
        for (int i = 0; i < 3; i++) {
            for (int j = i; j < 3; j++) { // Only visit upper triangle of symmetric matrix
                double matrixElement = 0;
                double totalWeight = 0;
                double weight = 1.0;
                for (Atom atom : molecule.atoms()) {
                    Vector3D point = atom.getCoordinates();
                    double deltaJ = point.getElement(j) - centerOfMass.getElement(j);
                    double deltaI = point.getElement(i) - centerOfMass.getElement(i);
                    
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
                
        vtkSphereSource sphereSource = new vtkSphereSource();
        sphereSource.SetThetaResolution(20);
        sphereSource.SetPhiResolution(20);
        sphereSource.SetRadius(1.0);
        
        // One point: the center of mass
        vtkPoints points = new vtkPoints();
        points.InsertNextPoint(centerOfMass.toArray());

        // One color: the color of the molecule
        double colorScalar = toonColors.getColorIndex(molecule);
        vtkFloatArray colorScalars = new vtkFloatArray();
        colorScalars.SetNumberOfComponents(1);
        colorScalars.InsertNextTuple1(colorScalar);

        // One tensor: the covariance matrix
        vtkFloatArray tensors = new vtkFloatArray();
        tensors.SetNumberOfComponents(9);
        
        double[][] m = eigenSystem.getV().getArray();
        double[] lambda = eigenSystem.getRealEigenvalues();
        
        // Make certain system is right handed
        Vector3D[] eigs = {new Vector3DClass(m[0]), new Vector3DClass(m[1]), new Vector3DClass(m[2])};
        // Which is smallest eigenvector?
        int minIndex = 0;
        if (lambda[1] < lambda[minIndex]) minIndex = 1;
        if (lambda[2] < lambda[minIndex]) minIndex = 2;
        int index2 = minIndex - 2; if (index2 < 0) index2 += 3;
        int index3 = minIndex - 1; if (index3 < 0) index3 += 3;
        Vector3D minVector = eigs[index2].cross(eigs[index3]); // right handed now
        for (int i = 0; i < 3; i++) m[minIndex][i] = minVector.get(i);
        
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j) 
                // The eigenvalue is the mean squared distance from the centroid in one direction.
                // Take the square root to get the mean distance.
                // Multiply by two or so to enclose more than just the less-than-average distance points.
                m[i][j] *= 2.2 * Math.sqrt(lambda[j]);
        
        tensors.InsertNextTuple9(
                m[0][0], m[1][0], m[2][0],
                m[0][1], m[1][1], m[2][1],
                m[0][2], m[1][2], m[2][2]
                );
        
        vtkPolyData pointData = new vtkPolyData();
        pointData.SetPoints(points);
        pointData.GetPointData().SetScalars(colorScalars);
        pointData.GetPointData().SetTensors(tensors);
        
        vtkTensorGlyph tensorGlyph = new vtkTensorGlyph();
        tensorGlyph.SetInput(pointData);
        tensorGlyph.SetSource(sphereSource.GetOutput());
        tensorGlyph.SetExtractEigenvalues(0);
        
        mapper.SetInput((vtkPolyData)tensorGlyph.GetOutput());
        actor.SetMapper(mapper);
        
        isPopulated = true;
    }    
}
