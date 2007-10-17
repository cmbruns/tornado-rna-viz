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
package org.simtk.mol.toon;

import java.awt.Color;

import vtk.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.Atom;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class BackboneCurveActor extends ActorCartoonClass {
    double ribbonThickness = 1.00;
    double ribbonWidth = 1.50;
    double lengthResolution = 1.00; // (Angstroms) for smoothing
    boolean useArrowHead = true;
    protected double highlightGap = 0.05;
    
    /**
     * How many spline segments per residue
     */
    static double splineFactor = 5.0;
    
    public BackboneCurveActor(
            double width, 
            double thickness,
            Molecule molecule)
    throws NoCartoonCreatedException
    {
        // Something is screwy with back vs. front faces of ribbon
        // I replaced this section with a SetFlipNormals(1) in the pipeline
        // actor.GetProperty().BackfaceCullingOff();
        // actor.GetProperty().FrontfaceCullingOn();
        // highlightActor.GetProperty().BackfaceCullingOff();
        // highlightActor.GetProperty().FrontfaceCullingOn();
        
        this.ribbonWidth = width;
        this.ribbonThickness = thickness;

        if (! (molecule instanceof Biopolymer)) 
            throw new NoCartoonCreatedException("Not a biopolymer for backbone curve");
        Biopolymer biopolymer = (Biopolymer) molecule;
        
        vtkPoints linePoints = new vtkPoints();
        vtkFloatArray lineNormals = new vtkFloatArray();
        lineNormals.SetNumberOfComponents(3);
        lineNormals.SetName("normals");

        vtkFloatArray colorScalars = new vtkFloatArray();
        colorScalars.SetNumberOfComponents(1);
        colorScalars.SetName("colors");
        
        Vector3D previousNormal = null;
        boolean isFirstResidue = true;
        Residue previousResidue = null;
        double colorScalar = 0;
        RESIDUE: for (Residue res : biopolymer.residues()) {
            if (! (res instanceof Residue)) continue RESIDUE;
            Residue residue = (Residue) res;
            
            Vector3D backbonePosition;
            try {
                backbonePosition = residue.getBackbonePosition();
            } catch (InsufficientAtomsException exc) {
                continue RESIDUE;
            }
            
            Vector3D sideChainPosition = null;
            try {
                sideChainPosition = residue.getSideChainPosition();
            } catch (InsufficientAtomsException exc) {}

            // colorScalar = getColorIndex(residue.getBackbone());
            colorScalar = getColorIndex(residue);
            
            if (backbonePosition == null) continue RESIDUE;
            
            Vector3D normal = null;
            if (sideChainPosition != null) {
                normal = sideChainPosition.minus(backbonePosition).unit();
            }
            else {
                // Compute normal another way - for case with phosphate positions only
                if (previousNormal != null) normal = previousNormal;
                if (normal == null) normal = new Vector3DClass(1,0,0);

                // Adjust normal to be perpendicular to chain direction
                Vector3D chainDirection = new Vector3DClass(0,0.01,0);
                
                try {
                    Vector3D dir1 = ((Residue)residue.getNextResidue()).getBackbonePosition().minus(backbonePosition);
                    chainDirection = chainDirection.plus(dir1);
                } catch (Exception e) {}                
                try {
                    Vector3D dir2 = backbonePosition.minus(((Residue)residue.getPreviousResidue()).getBackbonePosition());
                    chainDirection = chainDirection.plus(dir2);
                } catch (Exception e) {}
                
                chainDirection = chainDirection.unit();
                
                Vector3D right = chainDirection.cross(normal).unit();
                normal = right.cross(chainDirection).unit();  
            }
            
            // Extend first residue to the phosphate or 5' hydroxyl
            if (isFirstResidue) {
                isFirstResidue = false;
                Atom o5 = residue.getAtom("P");
                if (o5 == null) o5 = residue.getAtom("O5*");
                if (o5 != null) {
                    Vector3D o5Pos = o5.getCoordinates();
                    if (o5Pos != null) {
                        linePoints.InsertNextPoint(o5Pos.toArray());
                        lineNormals.InsertNextTuple3(normal.x(), normal.y(), normal.z());
                        colorScalars.InsertNextValue(colorScalar);
                    }
                }
            }
            
            linePoints.InsertNextPoint(backbonePosition.toArray());
            lineNormals.InsertNextTuple3(normal.x(), normal.y(), normal.z());
            colorScalars.InsertNextValue(colorScalar);
            
            previousNormal = normal;            
            previousResidue = residue;
        }
        
        // Extend final residue to the 3' hydroxyl
        try {
            Vector3D oh3 = previousResidue.getAtom("O5*").getCoordinates();
            linePoints.InsertNextPoint(oh3.toArray());
            lineNormals.InsertNextTuple3(previousNormal.x(), previousNormal.y(), previousNormal.z());
            colorScalars.InsertNextValue(colorScalar);                    
        } catch (Exception exc) {}

        int numberOfPoints = linePoints.GetNumberOfPoints();
        if (numberOfPoints < 2) 
            throw new NoCartoonCreatedException("Not enough points for backbone curve");
        
        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i ++)
            lineCells.InsertCellPoint(i);
        
        vtkPolyData lineData = new vtkPolyData();

        lineData.SetPoints(linePoints);
        lineData.SetLines(lineCells);
        lineData.GetPointData().SetScalars(colorScalars);
        lineData.GetPointData().SetNormals(lineNormals);

//        System.out.println("Backbone curve line data:");
//        for (int i = 0; i < numberOfPoints; i ++) {
//            double[] p = lineData.GetPoint(i);
//            double[] n = lineData.GetPointData().GetNormals().GetTuple3(i);
//            System.out.println("  "+i+": ("+p[0]+", "+p[1]+", "+p[2]+")");
//        }
        
        // Remove duplicate points
        vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
        cleanFilter.SetInput(lineData);
        
        // Smooth the path of the backbone
        vtkSplineFilter splineFilter = new vtkSplineFilter();
        splineFilter.SetSubdivideToLength();
        splineFilter.SetLength(lengthResolution);
        splineFilter.SetInput(cleanFilter.GetOutput());
        splineFilter.Update(); // TODO - for debugging
        
        mapper.SetScalarModeToUsePointData();
        mapper.ColorByArrayComponent("colors", 0);
        mapper.SetInput(createArrowRibbon(0.00, splineFilter.GetOutput()));

        actor.SetMapper(mapper);
        
        highlightMapper.SetScalarModeToUsePointData();
        highlightMapper.ColorByArrayComponent("colors", 0);
        highlightMapper.SetInput(createArrowRibbon(highlightGap, splineFilter.GetOutput()));

        highlightActor.SetMapper(highlightMapper);
        highlightActor.GetProperty().SetRepresentationToWireframe();

        isPopulated = true;
    } 

    @Override
    public void highlightResidue(Residue residue, Color color) {
        highlightResidueByBackboneScalars(residue, color);
    }
    @Override
    public void unhighlightResidue(Residue residue) {
        unhighlightResidueByBackboneScalars(residue);
    }
    
    private vtkPolyData createArrowRibbon(double growSize, vtkDataSet polyLine) {
        // Correct the interpolated scalars back to integer values
        vtkRoundScalarsFilter roundingFilter = new vtkRoundScalarsFilter();
        roundingFilter.SetInput(polyLine);
        
        // Shift positions so that extruded ribbon will be centered
        vtkPreExtrusionCenteringFilter centeringFilter = new vtkPreExtrusionCenteringFilter();
        centeringFilter.SetThickness(ribbonThickness);
        
        // TODO - this is a kludge that depends upon the order in which
        // the main actor and the highlight actor are created.
        // Is the centering being applied twice to the same points?
        if (growSize != 0.0) {
            centeringFilter.SetThickness(growSize);
        }
        
        centeringFilter.SetInput(roundingFilter.GetOutput());
        
        // Widen the line into a ribbon
        vtkRibbonFilter ribbonFilter = new vtkRibbonFilter();
        ribbonFilter.SetAngle(0); // Perpendicular to normals
        
        if (useArrowHead) {            
            // Insert scalars and extra points to make a nice arrow head
            vtkArrowWidthFilter arrowFilter = new vtkArrowWidthFilter();
            arrowFilter.SetWidth(ribbonWidth + growSize);
            arrowFilter.SetThickness(ribbonThickness + growSize);
            arrowFilter.SetInput(centeringFilter.GetOutput());

            ribbonFilter.SetInput(arrowFilter.GetOutput());
            ribbonFilter.VaryWidthOn();
            ribbonFilter.SetWidth(arrowFilter.GetRibbonFilterWidth()); // minimum width
            ribbonFilter.SetWidthFactor(arrowFilter.GetRibbonFilterWidthFactor()); // maximum / minimum
        }
        else {
            ribbonFilter.SetInput(centeringFilter.GetOutput());
            ribbonFilter.VaryWidthOff();
            ribbonFilter.SetWidth(ribbonWidth + growSize); // minimum width
            ribbonFilter.SetWidthFactor(1.0); // maximum / minimum
        }
        
        vtkSetScalarsFilter setScalarsFilter = new vtkSetScalarsFilter();
        setScalarsFilter.SetScalars("colors");
        setScalarsFilter.SetInput(ribbonFilter.GetOutput());

        vtkLinearExtrusionFilter extrusionFilter = new vtkLinearExtrusionFilter();
        extrusionFilter.SetCapping(1);
        extrusionFilter.SetExtrusionTypeToNormalExtrusion();
        extrusionFilter.SetScaleFactor(ribbonThickness + growSize);
        extrusionFilter.SetInput(setScalarsFilter.GetOutput());
        
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetFeatureAngle(80.0);
        normalsFilter.SetInput(extrusionFilter.GetOutput());
        // TODO - flip normals is required for some reason?!?!
        normalsFilter.SetFlipNormals(1);
        
        return normalsFilter.GetOutput();
    }
}
