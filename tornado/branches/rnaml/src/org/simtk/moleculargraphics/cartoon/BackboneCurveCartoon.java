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

import java.awt.*;
import java.util.*;

import vtk.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class BackboneCurveCartoon extends MolecularCartoonClass {
    double ribbonThickness = 1.50;
    double ribbonWidth = 2.20;
    /**
     * How many spline segments per residue
     */
    static double splineFactor = 5.0;
    boolean drawSmoothSpline = true;
    boolean drawSmoothSpline2 = false;

    // Coloring
    vtkLookupTable lut = new vtkLookupTable();
    static final int selectionColorIndex = 255;
    static final int highlightColorIndex = 254;
    static final int invisibleColorIndex = 253;
    static final Color selectionColor = new Color(80, 80, 255);
    static final Color highlightColor = new Color(250, 250, 50);
    private int baseColorIndex = 150;
    private Map<Color, Integer> colorIndices = new HashMap<Color, Integer>();

    vtkAssembly assembly = new vtkAssembly();
    
    public BackboneCurveCartoon() {
        
        // TODO move color table stuff to a baser class
        lut.SetNumberOfTableValues(256);
        lut.SetRange(1.0, 60.0);
        lut.SetAlphaRange(1.0, 1.0);
        lut.SetValueRange(1.0, 1.0);
        lut.SetHueRange(0.0, 1.0);
        lut.SetSaturationRange(0.5, 0.5);
        lut.Build();
        
        lut.SetTableValue(selectionColorIndex, selectionColor.getRed()/255.0, selectionColor.getGreen()/255.0, selectionColor.getBlue()/255.0, 1.0);        
        lut.SetTableValue(highlightColorIndex, highlightColor.getRed()/255.0, highlightColor.getGreen()/255.0, highlightColor.getBlue()/255.0, 1.0);        
        lut.SetTableValue(invisibleColorIndex, 0.0, 0.0, 0.0, 0.0);
    }
    
    public void updateCoordinates() {
        // TODO
    }

    public void hide(LocatedMolecule m) {
    }
    public void hide() {
    }
    public void show(LocatedMolecule m) {
    }
    public void show() {
    }
    public vtkAssembly getAssembly() {return assembly;}
    
    @Override
    public void add(LocatedMolecule m) {
        addMolecule(m);
        super.add(m);
    }

    public void addMolecule(LocatedMolecule molecule) {
        if (! (molecule instanceof BiopolymerClass)) return;
        BiopolymerClass biopolymer = (BiopolymerClass) molecule;
        
        vtkPoints linePoints = new vtkPoints();
        vtkFloatArray lineNormals = new vtkFloatArray();
        lineNormals.SetNumberOfComponents(3);
        vtkFloatArray lineScalars = new vtkFloatArray();
        lineScalars.SetNumberOfComponents(1);
        
        RESIDUE: for (Iterator i = biopolymer.getResidueIterator(); i.hasNext();) {
            PDBResidue residue = (PDBResidue) i.next();
            
            Vector3D backbonePosition;
            Vector3D sideChainPosition;
            try {
                backbonePosition = residue.getBackbonePosition();
                sideChainPosition = residue.getSideChainPosition();
            } catch (InsufficientAtomsException exc) {
                continue RESIDUE;
            }
            
            Color color = residue.getDefaultColor();
            if (! (colorIndices.containsKey(color))) {
                colorIndices.put(color, baseColorIndex);
                lut.SetTableValue(baseColorIndex, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
                baseColorIndex ++;
            }
            int colorScalar = colorIndices.get(color);        
            
            if ( (backbonePosition != null) && (sideChainPosition != null) ) {
                linePoints.InsertNextPoint(backbonePosition.getX(), backbonePosition.getY(), backbonePosition.getZ());
                Vector3DClass normal = new Vector3DClass( sideChainPosition.minus(backbonePosition).unit() );
                lineNormals.InsertNextTuple3(normal.getX(), normal.getY(), normal.getZ());
                lineScalars.InsertNextValue(colorScalar);
            }
        }
        
        int numberOfInputPoints = linePoints.GetNumberOfPoints();
        if (numberOfInputPoints < 2) return;
        
        int numberOfOutputPoints = numberOfInputPoints;
        
        if (drawSmoothSpline) {
            // Replace linePoints and lineNormals with smoothed
            //  oversampled versions of themselves, by means of splines
            
            // TODO - put scalar data in too
            
            numberOfOutputPoints = (int)(numberOfInputPoints * splineFactor);
            
            // Set up one spline for each dimension
            vtkCardinalSpline splineX = new vtkCardinalSpline();
            vtkCardinalSpline splineY = new vtkCardinalSpline();
            vtkCardinalSpline splineZ = new vtkCardinalSpline();
            
            // Another set of splines for the normal vectors
            vtkCardinalSpline normalSplineX = new vtkCardinalSpline();
            vtkCardinalSpline normalSplineY = new vtkCardinalSpline();
            vtkCardinalSpline normalSplineZ = new vtkCardinalSpline();
            
            for (int i = 0; i < linePoints.GetNumberOfPoints(); i++) {
                splineX.AddPoint(i, linePoints.GetPoint(i)[0]);
                splineY.AddPoint(i, linePoints.GetPoint(i)[1]);
                splineZ.AddPoint(i, linePoints.GetPoint(i)[2]);
                
                normalSplineX.AddPoint(i, lineNormals.GetTuple3(i)[0]);
                normalSplineY.AddPoint(i, lineNormals.GetTuple3(i)[1]);
                normalSplineZ.AddPoint(i, lineNormals.GetTuple3(i)[2]);                
            }
            
            vtkPoints splinePoints = new vtkPoints();
            vtkFloatArray splineNormals = new vtkFloatArray();
            splineNormals.SetNumberOfComponents(3);
            vtkFloatArray splineScalars = new vtkFloatArray();
            splineScalars.SetNumberOfComponents(1);

            for (int i = 0; i < numberOfOutputPoints; i++) {
                double t = (numberOfInputPoints - 1.0)/(numberOfOutputPoints - 1.0) * i;

                // Points
                splinePoints.InsertNextPoint(
                        splineX.Evaluate(t),
                        splineY.Evaluate(t),
                        splineZ.Evaluate(t)
                );
                
                // Normals
                Vector3DClass normal = new Vector3DClass( (new Vector3DClass(
                        normalSplineX.Evaluate(t),
                        normalSplineY.Evaluate(t),
                        normalSplineZ.Evaluate(t))).unit() );
                splineNormals.InsertNextTuple3(
                        normal.getX(),
                        normal.getY(),
                        normal.getZ()
                    );
                
                // Scalars
                int closestInputIndex = (int) Math.round(t);
                splineScalars.InsertNextValue(lineScalars.GetValue(closestInputIndex));

                System.out.println("" + i + " " + closestInputIndex);
            }
            
            linePoints = splinePoints;
            lineNormals = splineNormals;
            lineScalars = splineScalars;
        }
        
        // TODO - expand scalars for spline representation
        // TODO - how to make this color the items?
        // tubeData.GetPointData().SetScalars(lineScalars); // causes crash
        
        // Shift each point backwards along normal,
        // so that extruded ribbon is centered on backbone position
        vtkPoints shiftedLinePoints = new vtkPoints();
        for (int i = 0; i < linePoints.GetNumberOfPoints(); i++) {
            double[] point, normal;
            point = linePoints.GetPoint(i);
            normal = lineNormals.GetTuple3(i);
            double[] shiftedPoint = {
                    point[0] - normal[0] * 0.5 * ribbonThickness,
                    point[1] - normal[1] * 0.5 * ribbonThickness,
                    point[2] - normal[2] * 0.5 * ribbonThickness
            };
            shiftedLinePoints.InsertNextPoint(shiftedPoint);
        }

        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(numberOfOutputPoints);
        for (int i = 0; i < numberOfOutputPoints; i ++)
            lineCells.InsertCellPoint(i);
        
        vtkPolyData tubeData = new vtkPolyData();

        tubeData.SetPoints(shiftedLinePoints);
        tubeData.SetLines(lineCells);

        // Incorporate the lineNormals
        tubeData.GetPointData().SetNormals(lineNormals);
        
        // causes crash
        // with extruded smoothed ribbon
        // and extruded ribbon (vtkLinearExtrusionFilter)
        // but not with thin ribbon
        // tubeData.GetPointData().SetScalars(lineScalars);

        vtkRibbonFilter lineRibbon = new vtkRibbonFilter();
        lineRibbon.SetWidth(ribbonWidth);
        lineRibbon.SetAngle(0);
        
        if (drawSmoothSpline2) {
            // TODO try to use vtkSplineFilter
            // causes crash
            vtkSplineFilter splineFilter = new vtkSplineFilter();
            splineFilter.SetInput(tubeData);
            splineFilter.SetMaximumNumberOfSubdivisions((int)(splineFactor + 0.9));
            lineRibbon.SetInput(splineFilter.GetOutput());
        } 
        else {
            lineRibbon.SetInput(tubeData);            
        }
        
        vtkLinearExtrusionFilter ribbonThicknessFilter = new vtkLinearExtrusionFilter();
        ribbonThicknessFilter.SetCapping(1);
        ribbonThicknessFilter.SetExtrusionTypeToNormalExtrusion();
        ribbonThicknessFilter.SetScaleFactor(ribbonThickness);
        ribbonThicknessFilter.SetInput(lineRibbon.GetOutput());
        
        // The polygons on the newly extruded edges are not smoothly shaded
        vtkPolyDataNormals dataNormals = new vtkPolyDataNormals();
        dataNormals.SetFeatureAngle(80.0); // Angles smaller than this are smoothed
        dataNormals.SetInput(ribbonThicknessFilter.GetOutput());
        
        vtkPolyDataMapper tubeMapper = new vtkPolyDataMapper();        

        // tubeMapper.SetInput(tubeData); // line
        // tubeMapper.SetInput(lineRibbon.GetOutput()); // ribbon
        // tubeMapper.SetInput(ribbonThicknessFilter.GetOutput()); // ribbon
        tubeMapper.SetInput(dataNormals.GetOutput()); // ribbon

        // tubeMapper.SetColorModeToMapScalars();
        tubeMapper.SetLookupTable(lut);
        tubeMapper.SetScalarRange(0.0, lut.GetNumberOfTableValues());

        vtkActor lineActor = new vtkActor(); // tube        
        lineActor.SetMapper(tubeMapper);

        lineActor.AddPosition(0.0, 0.0, 0.0);

        assembly.AddPart(lineActor);
    }    
}
