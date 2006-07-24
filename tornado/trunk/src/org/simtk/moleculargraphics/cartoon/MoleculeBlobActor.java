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

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class MoleculeBlobActor extends ActorCartoonClass {
    double resolution;
    // double densityCutoff = 0.10;
    double blurRatio = 1.50;
    Molecule molecule = null;

    public MoleculeBlobActor(LocatedMolecule molecule, double resolution) {
        this.resolution = resolution;
        addMolecule(molecule);
    }
    
    /**
     * 
     * @return index of nearest grid point
     */
    private int quantize(double v) {
        return (int) Math.round(v/resolution);
    }
    
    protected void addMolecule(LocatedMolecule molecule) {
        if (molecule == null) return;
        this.molecule = molecule;

        // Identify molecule bounding box
        BoundingBox molBox = null;
        for (LocatedAtom atom : molecule.atoms()) {
            double r = atom.getVanDerWaalsRadius();
            Vector3D c = atom.getCoordinates();
            double[] bounds = {
                    c.x() - r, c.x() + r,
                    c.y() - r, c.y() + r,
                    c.z() - r, c.z() + r,
            };
            BoundingBox atomBox = new BoundingBox(bounds);
            if (molBox == null) molBox = atomBox;
            else molBox.add(atomBox);
        }
        
        if (molBox == null) return;
        
        // Set up vtkImageData structure
        vtkImageData imageData = new vtkImageData();
        imageData.SetScalarTypeToFloat();
        imageData.SetSpacing(resolution, resolution, resolution);
        
        // Pad grid by two, so there will be zeros at all the edges
        Vector3D origin = new Vector3DClass(
                (quantize(molBox.xMin) - 2) * resolution,
                (quantize(molBox.yMin) - 2) * resolution,
                (quantize(molBox.zMin) - 2) * resolution
        );
        imageData.SetOrigin(origin.toArray());
        
        int[] dimensions = {
                quantize(molBox.xMax) - quantize(molBox.xMin) + 5,
                quantize(molBox.yMax) - quantize(molBox.yMin) + 5,
                quantize(molBox.zMax) - quantize(molBox.zMin) + 5,
        };
        
        imageData.SetDimensions(dimensions);
        imageData.SetNumberOfScalarComponents(1);
        imageData.AllocateScalars();

        vtkDataArray volArray = imageData.GetPointData().GetScalars();
        volArray.FillComponent(0, 0.0);
        volArray.SetName("atomic density");

        int numScalars = dimensions[0] * dimensions[1] * dimensions[2];
        
        int colorScalar = toonColors.getColorIndex(molecule);
        vtkDataArray colorScalars = new vtkFloatArray();
        colorScalars.SetNumberOfComponents(1);
        colorScalars.SetNumberOfTuples(numScalars);
        colorScalars.FillComponent(0, colorScalar);
        colorScalars.SetName("colors");
        
        imageData.GetPointData().AddArray(colorScalars);

        // Because we distribute volume over 27 cells, we need to scale the density down
        double b1 = blurRatio * blurRatio;
        double b2 = b1 * 2;
        double b3 = b1 * 3;
        double scaleFactor = 1.0 / (
                1.0 + // center cell
                6.0 * Math.exp(-b1) + // adjacent in one dimension
                12.0 * Math.exp(-b2) +
                8.0 * Math.exp(-b3) );
        // System.out.println("Scale factor = " + scaleFactor);
        
        // Populate density
        for (LocatedAtom atom : molecule.atoms()) {
            double r = atom.getVanDerWaalsRadius();
            Vector3D c = atom.getCoordinates();

            double vol = r * r * r * Math.PI * 4.0 / 3.0;
            
            int[] indices = {
                    quantize(c.x() - origin.x()),
                    quantize(c.y() - origin.y()),
                    quantize(c.z() - origin.z()),                    
            };
            
            // Blur atom by spreading some to nearby grid points
            // Gaussian blur
            for (int x = indices[0] - 1; x <= indices[0] + 1; ++x)
                for (int y = indices[1] - 1; y <= indices[1] + 1; ++y)
                    for (int z = indices[2] - 1; z <= indices[2] + 1; ++z) {
                
                        Vector3D gridCenter = new Vector3DClass(
                                x * resolution,
                                y * resolution,
                                z * resolution
                                );
            
                        double centerDist = blurRatio * c.minus(origin).distance(gridCenter) / resolution;
                        // System.out.println("centerDist = " + centerDist);
                        
                        // Scale contribution by distance from center
                        double scale = Math.exp(-(centerDist * centerDist));
                        // System.out.println("Scale = "+scale);
                        double sVol = vol * scale * scaleFactor;
                        

                        int index =                     
                            x + 
                            y * dimensions[0] +
                            z * dimensions[0] * dimensions[1];
            
                        sVol = sVol + volArray.GetTuple1(index);
                        volArray.SetTuple1(index, sVol);

                        colorScalars.SetTuple1(index, colorScalar);
                    }
            
        }
        
        // Choose cutoff based on actual density observed
        // Use the average non-zero density
        int cellCount = 0;
        double totalDensity = 0.0;
        for (int i = 0; i < volArray.GetNumberOfTuples(); ++i) {
            double vol = volArray.GetTuple1(i);
            if (vol > 0.0) {
                ++ cellCount;
                totalDensity += vol;
            }
        }
        if (cellCount < 1) return;
        if (totalDensity == 0.0) return;
        double averageDensity = totalDensity / cellCount;
        double densityCutoff = 1.0 * averageDensity;
        
        vtkContourFilter contourFilter = new vtkContourFilter();
        contourFilter.SetInput(imageData); 
        contourFilter.SetArrayComponent(0);
        contourFilter.SetValue(0, densityCutoff);
        
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.AutoOrientNormalsOn();
        normalsFilter.SetFeatureAngle(170.0);
        normalsFilter.SetInput(contourFilter.GetOutput());
        
        // Use filter of my creation to set the scalars to the "colors" array
        vtkSetScalarsFilter setScalarsFilter = new vtkSetScalarsFilter();
        setScalarsFilter.SetInput(normalsFilter.GetOutput());
        setScalarsFilter.SetScalars("colors");
        
        vtkSmoothPolyDataFilter smoothFilter = new vtkSmoothPolyDataFilter();
        smoothFilter.SetInput(setScalarsFilter.GetOutput());
        
        mapper.SetInput(smoothFilter.GetOutput());

        actor.SetMapper(mapper);
        
        isPopulated = true;
    }    
}