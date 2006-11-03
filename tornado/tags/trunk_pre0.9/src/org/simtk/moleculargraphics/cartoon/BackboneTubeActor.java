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
import org.simtk.molecularstructure.atom.Atom;
import java.util.List;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class BackboneTubeActor extends ActorCartoonClass {
    double radius = 1.5;
    double lengthResolution = 1.00; // (Angstroms) for smoothing
    int numberOfSides = 5;
    
    /**
     * How many spline segments per residue
     */
    static double splineFactor = 5.0;
    
    public BackboneTubeActor(
            double radius, 
            List<Residue> residues)
    throws NoCartoonCreatedException
    {        
        this.radius = radius;

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
        RESIDUE: for (Residue res : residues) {
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

        // Remove duplicate points
        vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
        cleanFilter.SetInput(lineData);
        
        // Smooth the path of the backbone
        vtkSplineFilter splineFilter = new vtkSplineFilter();
        splineFilter.SetSubdivideToLength();
        splineFilter.SetLength(lengthResolution);
        splineFilter.SetInput(cleanFilter.GetOutput());
        
        vtkSetScalarsFilter setScalarsFilter = new vtkSetScalarsFilter();
        setScalarsFilter.SetScalars("colors");
        setScalarsFilter.SetInput(splineFilter.GetOutput());

        // Correct the interpolated scalars back to integer values
        vtkRoundScalarsFilter roundingFilter = new vtkRoundScalarsFilter();
        roundingFilter.SetInput(setScalarsFilter.GetOutput());
        
        // Widen the line into a tube
        vtkTubeFilter tubeFilter = new vtkTubeFilter();
        tubeFilter.SetRadius(radius);
        tubeFilter.SetVaryRadiusToVaryRadiusOff();
        tubeFilter.SetNumberOfSides(numberOfSides);
        tubeFilter.SetInput(roundingFilter.GetOutput());
        
        mapper.SetScalarModeToUsePointData();
        mapper.ColorByArrayComponent("colors", 0);
        mapper.SetInput(tubeFilter.GetOutput());

        actor.SetMapper(mapper);
        
        // For highlighting
        
        // Widen the line into a tube
        vtkTubeFilter highlightTubeFilter = new vtkTubeFilter();
        highlightTubeFilter.SetRadius(radius * 1.01);
        highlightTubeFilter.SetVaryRadiusToVaryRadiusOff();
        highlightTubeFilter.SetNumberOfSides(numberOfSides);
        highlightTubeFilter.SetInput(roundingFilter.GetOutput());
        highlightTubeFilter.Update();
        
        highlightMapper.SetScalarModeToUsePointData();
        highlightMapper.ColorByArrayComponent("colors", 0);
        highlightMapper.SetInput(highlightTubeFilter.GetOutput());

        highlightActor.SetMapper(highlightMapper);
        highlightActor.GetProperty().SetRepresentationToWireframe();
        
        isPopulated = true;
    }    
}
