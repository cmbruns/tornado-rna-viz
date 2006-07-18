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

import java.util.*;
import vtk.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class NewBackboneCurveActor extends ActorCartoonClass {
    double ribbonThickness = 1.00;
    double ribbonWidth = 1.50;
    double lengthResolution = 1.00; // (Angstroms) for smoothing
    boolean useArrowHead = true;
    
    /**
     * How many spline segments per residue
     */
    static double splineFactor = 5.0;
    
    public NewBackboneCurveActor(double width, double thickness) {
        this.ribbonWidth = width;
        this.ribbonThickness = thickness;
    }

    public void addMolecule(LocatedMolecule molecule) {
        if (! (molecule instanceof Biopolymer)) return;
        Biopolymer biopolymer = (Biopolymer) molecule;
        
        vtkPoints linePoints = new vtkPoints();
        vtkFloatArray lineNormals = new vtkFloatArray();
        lineNormals.SetNumberOfComponents(3);
        lineNormals.SetName("normals");

        vtkFloatArray colorScalars = new vtkFloatArray();
        colorScalars.SetNumberOfComponents(1);
        colorScalars.SetName("colors");
        
        RESIDUE: for (Residue res : biopolymer.residues()) {
            if (! (res instanceof LocatedResidue)) continue RESIDUE;
            LocatedResidue residue = (LocatedResidue) res;
            
            Vector3D backbonePosition;
            Vector3D sideChainPosition;
            try {
                backbonePosition = residue.getBackbonePosition();
                sideChainPosition = residue.getSideChainPosition();
            } catch (InsufficientAtomsException exc) {
                continue RESIDUE;
            }
            
            double colorScalar = toonColors.getColorIndex(residue);
            
            if ( (backbonePosition != null) && (sideChainPosition != null) ) {
                Vector3D normal = sideChainPosition.minus(backbonePosition).unit();

                linePoints.InsertNextPoint(backbonePosition.toArray());
                lineNormals.InsertNextTuple3(normal.x(), normal.y(), normal.z());
                colorScalars.InsertNextValue(colorScalar);
            }
        }
        
        int numberOfPoints = linePoints.GetNumberOfPoints();
        if (numberOfPoints < 2) return;
        
        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i ++)
            lineCells.InsertCellPoint(i);
        
        vtkPolyData lineData = new vtkPolyData();

        lineData.SetPoints(linePoints);
        lineData.SetLines(lineCells);
        lineData.GetPointData().SetScalars(colorScalars);
        lineData.GetPointData().SetNormals(lineNormals);

        // Smooth the path of the backbone
        vtkSplineFilter splineFilter = new vtkSplineFilter();
        splineFilter.SetSubdivideToLength();
        splineFilter.SetLength(lengthResolution);
        splineFilter.SetInput(lineData);
        
        // Shift positions so that extruded ribbon will be centered
        vtkPreExtrusionCenteringFilter centeringFilter = new vtkPreExtrusionCenteringFilter();
        centeringFilter.SetThickness(ribbonThickness);
        centeringFilter.SetInput(splineFilter.GetOutput());
        
        // Widen the line into a ribbon
        vtkRibbonFilter ribbonFilter = new vtkRibbonFilter();
        ribbonFilter.SetAngle(0); // Perpendicular to normals
        
        if (useArrowHead) {            
            // Insert scalars and extra points to make a nice arrow head
            vtkArrowWidthFilter arrowFilter = new vtkArrowWidthFilter();
            arrowFilter.SetWidth(ribbonWidth);
            arrowFilter.SetThickness(ribbonThickness);
            arrowFilter.SetInput(centeringFilter.GetOutput());

            ribbonFilter.SetInput(arrowFilter.GetOutput());
            ribbonFilter.VaryWidthOn();
            ribbonFilter.SetWidth(arrowFilter.GetRibbonFilterWidth()); // minimum width
            ribbonFilter.SetWidthFactor(arrowFilter.GetRibbonFilterWidthFactor()); // maximum / minimum
        }
        else {
            ribbonFilter.SetInput(centeringFilter.GetOutput());
            ribbonFilter.VaryWidthOff();
            ribbonFilter.SetWidth(ribbonWidth); // minimum width
            ribbonFilter.SetWidthFactor(1.0); // maximum / minimum
        }
        
        vtkSetScalarsFilter setScalarsFilter = new vtkSetScalarsFilter();
        setScalarsFilter.SetScalars("colors");
        setScalarsFilter.SetInput(ribbonFilter.GetOutput());

        vtkLinearExtrusionFilter extrusionFilter = new vtkLinearExtrusionFilter();
        extrusionFilter.SetCapping(1);
        extrusionFilter.SetExtrusionTypeToNormalExtrusion();
        extrusionFilter.SetScaleFactor(ribbonThickness);
        extrusionFilter.SetInput(setScalarsFilter.GetOutput());
        
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetFeatureAngle(80.0);
        normalsFilter.SetInput(extrusionFilter.GetOutput());

        mapper.SetScalarModeToUsePointData();
        mapper.ColorByArrayComponent("colors", 0);
        mapper.SetInput(normalsFilter.GetOutput());

        actor.SetMapper(mapper);
        
        isPopulated = true;
    }    
}
