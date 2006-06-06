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
import org.simtk.util.Selectable;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class BackboneCurveCartoon extends MolecularCartoonClass {
    double baseRodRadius = 0.50;
    double ribbonThickness = 0.70;
    double studElevation = 0.50;
    double ribbonWidth = 1.5;
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
    private Hashtable colorIndices = new Hashtable();

    NucleotideStickCartoon baseRods;
    // NucleotideStickCaps baseCaps;
    BackboneSpheres studs;
    
    vtkAssembly assembly = new vtkAssembly();
    
    public BackboneCurveCartoon() {
        this(0.50);
    }

    public BackboneCurveCartoon(double radius) {
        baseRodRadius = radius;

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

        baseRods = new NucleotideStickCartoon(baseRodRadius);
        // baseCaps = new NucleotideStickCaps(baseRodRadius);
        studs = new BackboneSpheres(ribbonThickness/2.0 + studElevation);
        
        assembly.AddPart(baseRods.getActor());
        // assembly.AddPart(baseCaps.getActor());
        assembly.AddPart(studs.getActor());
    }
    
    public void updateCoordinates() {
        // TODO
    }
    
    public void select(Selectable s) {
        baseRods.select(s);
        // baseCaps.select(s);
        studs.select(s);
    }
    public void unSelect(Selectable s) {
        baseRods.unSelect(s);
        // baseCaps.unSelect(s);
        studs.unSelect(s);
    }
    public void unSelect() {
        baseRods.unSelect();
        // baseCaps.unSelect();
        studs.unSelect();
    }
    public void highlight(LocatedMolecule m) {
        baseRods.highlight(m);
        // baseCaps.highlight(m);
        studs.highlight(m);
    }
    public void hide(LocatedMolecule m) {
        baseRods.hide(m);
        // baseCaps.hide(m);
        studs.hide(m);
    }
    public void hide() {
        baseRods.hide();
        // baseCaps.hide(m);
        studs.hide();
    }
    public void show(LocatedMolecule m) {
        baseRods.show(m);
        // baseCaps.show(m);
        studs.show(m);
    }
    public void show() {
        baseRods.show();
        // baseCaps.show(m);
        studs.show();
    }
    public void clear() {
        baseRods.clear();
        // baseCaps.clear();
        studs.clear();
    }
    public vtkAssembly getAssembly() {return assembly;}
    

    
    public void add(LocatedMolecule m) {
        baseRods.add(m);
        // baseCaps.show(m);
        studs.add(m);        
        addMolecule(m);
        super.add(m);
    }

    public void addMolecule(LocatedMolecule molecule) {
        if (! (molecule instanceof BiopolymerClass)) return;
        BiopolymerClass biopolymer = (BiopolymerClass) molecule;
        
        vtkPoints linePoints = new vtkPoints();
        vtkPoints lineNormals = new vtkPoints();
        vtkFloatArray lineScalars = new vtkFloatArray();
        
        RESIDUE: for (Iterator i = biopolymer.getResidueIterator(); i.hasNext();) {
            PDBResidue residue = (PDBResidue) i.next();
            
            Vector3D backbonePosition = residue.getBackbonePosition();
            Vector3D sideChainPosition = residue.getSideChainPosition();
            
            Color color = residue.getDefaultColor();
            if (! (colorIndices.containsKey(color))) {
                colorIndices.put(color, new Integer(baseColorIndex));
                lut.SetTableValue(baseColorIndex, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
                baseColorIndex ++;
            }
            int colorScalar = ((Integer) colorIndices.get(color)).intValue();        
            
            if ( (backbonePosition != null) && (sideChainPosition != null) ) {
                linePoints.InsertNextPoint(backbonePosition.getX(), backbonePosition.getY(), backbonePosition.getZ());
                Vector3DClass normal = new Vector3DClass( sideChainPosition.minus(backbonePosition).unit() );
                lineNormals.InsertNextPoint(normal.getX(), normal.getY(), normal.getZ());
                lineScalars.InsertNextValue(colorScalar);
            }
        }
        vtkPoints backbonePoints = linePoints;
        vtkPoints backboneNormals = lineNormals;
        
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
                
                normalSplineX.AddPoint(i, lineNormals.GetPoint(i)[0]);
                normalSplineY.AddPoint(i, lineNormals.GetPoint(i)[1]);
                normalSplineZ.AddPoint(i, lineNormals.GetPoint(i)[2]);                
            }
            
            vtkPoints splinePoints = new vtkPoints();
            vtkPoints splineNormals = new vtkPoints();
            for (int i = 0; i < numberOfOutputPoints; i++) {
                double t = (numberOfInputPoints - 1.0)/(numberOfOutputPoints - 1.0) * i;
                splinePoints.InsertPoint(i, 
                        splineX.Evaluate(t),
                        splineY.Evaluate(t),
                        splineZ.Evaluate(t)
                );
                
                Vector3DClass normal = new Vector3DClass( (new Vector3DClass(
                        normalSplineX.Evaluate(t),
                        normalSplineY.Evaluate(t),
                        normalSplineZ.Evaluate(t))).unit() );
                splineNormals.InsertPoint(i, 
                        normal.getX(),
                        normal.getY(),
                        normal.getZ()
                );
            }
            
            linePoints = splinePoints;
            lineNormals = splineNormals;
        }
        
        vtkActor lineActor = new vtkActor(); // tube
        
        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(numberOfOutputPoints);
        for (int i = 0; i < numberOfOutputPoints; i ++)
            lineCells.InsertCellPoint(i);
        
        vtkPolyData tubeData = new vtkPolyData();
        tubeData.SetPoints(linePoints);
        tubeData.SetLines(lineCells);

        // Incorporate the lineNormals
        tubeData.GetPointData().SetNormals(lineNormals.GetData());

        // TODO - expand scalars for spline representation
        // TODO - how to make this color the items?
        // tubeData.GetPointData().SetScalars(lineScalars); // causes crash
        
        // Shift each point backwards along normal,
        // so that extruded ribbon is centered on backbone position
        vtkPoints shiftedLinePoints = new vtkPoints();
        for (int i = 0; i < linePoints.GetNumberOfPoints(); i++) {
            double[] point, normal;
            point = linePoints.GetPoint(i);
            normal = lineNormals.GetPoint(i);
            double[] shiftedPoint = {
                    point[0] - normal[0] * 0.5 * ribbonThickness,
                    point[1] - normal[1] * 0.5 * ribbonThickness,
                    point[2] - normal[2] * 0.5 * ribbonThickness
            };
            shiftedLinePoints.InsertNextPoint(shiftedPoint);
        }
        tubeData.SetPoints(shiftedLinePoints);

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
        // tubeMapper.SetInput(ribbonThicknessFilter.GetOutput()); // ribbon
        tubeMapper.SetInput(dataNormals.GetOutput()); // ribbon
        
        lineActor.SetMapper(tubeMapper);

        lineActor.AddPosition(0.0, 0.0, 0.0);

        assembly.AddPart(lineActor);
    }

    
//    static boolean drawWireFrame = false;
//    static boolean drawSmoothSpline = true;
//    static boolean drawFlatRibbon = true;
//    static double ribbonWidth = 1.5;
//    static double ribbonThickness = 0.7;
//    static double tubeThickness = 0.8;
//
//    /**
//     * How many spline segments per residue
//     */
//    static double splineFactor = 5.0;
//    
//    /**
//     * Update graphical primitives to reflect a change in atomic positions
//     *
//     */
//    public void updateCoordinates() {
//        // TODO
//    }
//
//    public vtkProp highlight(Residue residue, Color color) {
//        return null; // TODO
//    }
//
//    public vtkAssembly represent(Molecule molecule) {
//        return represent(molecule, 1.00, null, 1.00);
//    }
//    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr, double opacity) {
//        if (! (molecule instanceof Biopolymer)) return null;
//        Biopolymer biopolymer = (Biopolymer) molecule;
//        
//        vtkPoints linePoints = new vtkPoints();
//        vtkPoints lineNormals = new vtkPoints();
//        for (Iterator i = biopolymer.residues().iterator(); i.hasNext();) {
//            Residue residue = (Residue) i.next();
//            BaseVector3D backbonePosition = residue.getBackbonePosition();
//            BaseVector3D sideChainPosition = residue.getSideChainPosition();
//            if ( (backbonePosition != null) && (sideChainPosition != null) ) {
//                linePoints.InsertNextPoint(backbonePosition.getX(), backbonePosition.getY(), backbonePosition.getZ());
//                Vector3D normal = sideChainPosition.minus(backbonePosition).unit();
//                lineNormals.InsertNextPoint(normal.getX(), normal.getY(), normal.getZ());
//            }
//        }
//        
//        vtkPoints backbonePoints = linePoints;
//        vtkPoints backboneNormals = lineNormals;
//
//        int numberOfInputPoints = linePoints.GetNumberOfPoints();
//        if (numberOfInputPoints < 1) return null;
//        
//        int numberOfOutputPoints = numberOfInputPoints;
//        
//        if (drawSmoothSpline) {
//            // Replace linePoints and lineNormals with smoothed
//            //  oversampled versions of themselves, by means of splines
//            
//            numberOfOutputPoints = (int)(numberOfInputPoints * splineFactor);
//
//            // Set up one spline for each dimension
//            vtkCardinalSpline splineX = new vtkCardinalSpline();
//            vtkCardinalSpline splineY = new vtkCardinalSpline();
//            vtkCardinalSpline splineZ = new vtkCardinalSpline();
//
//            // Another set of splines for the normal vectors
//            vtkCardinalSpline normalSplineX = new vtkCardinalSpline();
//            vtkCardinalSpline normalSplineY = new vtkCardinalSpline();
//            vtkCardinalSpline normalSplineZ = new vtkCardinalSpline();
//
//            for (int i = 0; i < linePoints.GetNumberOfPoints(); i++) {
//                splineX.AddPoint(i, linePoints.GetPoint(i)[0]);
//                splineY.AddPoint(i, linePoints.GetPoint(i)[1]);
//                splineZ.AddPoint(i, linePoints.GetPoint(i)[2]);
//                
//                normalSplineX.AddPoint(i, lineNormals.GetPoint(i)[0]);
//                normalSplineY.AddPoint(i, lineNormals.GetPoint(i)[1]);
//                normalSplineZ.AddPoint(i, lineNormals.GetPoint(i)[2]);                
//            }
//
//            vtkPoints splinePoints = new vtkPoints();
//            vtkPoints splineNormals = new vtkPoints();
//            for (int i = 0; i < numberOfOutputPoints; i++) {
//                double t = (numberOfInputPoints - 1.0)/(numberOfOutputPoints - 1.0) * i;
//                splinePoints.InsertPoint(i, 
//                        splineX.Evaluate(t),
//                        splineY.Evaluate(t),
//                        splineZ.Evaluate(t)
//                        );
//
//                Vector3D normal = (new Vector3D(
//                        normalSplineX.Evaluate(t),
//                        normalSplineY.Evaluate(t),
//                        normalSplineZ.Evaluate(t))).unit();
//                splineNormals.InsertPoint(i, 
//                        normal.getX(),
//                        normal.getY(),
//                        normal.getZ()
//                        );
//            }
//
//            linePoints = splinePoints;
//            lineNormals = splineNormals;
//        }
//
//        vtkActor lineActor = new vtkActor(); // tube
//        
//        if (drawWireFrame) {
//            vtkPolyLine backboneLine = new vtkPolyLine();
//            backboneLine.GetPointIds().SetNumberOfIds(numberOfOutputPoints);
//            for (int i = 0; i < linePoints.GetNumberOfPoints(); i++) {
//                backboneLine.GetPointIds().SetId(i, i);
//            }
//
//            // Wireframe
//            vtkUnstructuredGrid lineGrid = new vtkUnstructuredGrid();
//            lineGrid.Allocate(1, 1);
//            lineGrid.InsertNextCell(backboneLine.GetCellType(), backboneLine.GetPointIds());
//
//            lineGrid.SetPoints(linePoints);
//            
//            vtkDataSetMapper lineMapper = new vtkDataSetMapper();
//            lineMapper.SetInput(lineGrid);
//
//            lineActor.SetMapper(lineMapper); // wireframe
//        }
//
//        else { // Tube or ribbon
//            vtkCellArray lineCells = new vtkCellArray();
//            lineCells.InsertNextCell(numberOfOutputPoints);
//            for (int i = 0; i < numberOfOutputPoints; i ++)
//                lineCells.InsertCellPoint(i);
//            
//            vtkPolyData tubeData = new vtkPolyData();
//            tubeData.SetPoints(linePoints);
//            tubeData.SetLines(lineCells);
//
//            // Incorporate the lineNormals
//            tubeData.GetPointData().SetNormals(lineNormals.GetData());
//    
//            vtkPolyDataMapper tubeMapper = new vtkPolyDataMapper();
//            
//            if (drawFlatRibbon) { // Ribbon not Tube
//                // Shift each point backwards along normal,
//                // so that extruded ribbon is centered on backbone position
//                vtkPoints shiftedLinePoints = new vtkPoints();
//                for (int i = 0; i < linePoints.GetNumberOfPoints(); i++) {
//                    double[] point, normal;
//                    point = linePoints.GetPoint(i);
//                    normal = lineNormals.GetPoint(i);
//                    double[] shiftedPoint = {
//                            point[0] - normal[0] * 0.5 * ribbonThickness,
//                            point[1] - normal[1] * 0.5 * ribbonThickness,
//                            point[2] - normal[2] * 0.5 * ribbonThickness
//                    };
//                    shiftedLinePoints.InsertNextPoint(shiftedPoint);
//                }
//                tubeData.SetPoints(shiftedLinePoints);
//                
//                vtkRibbonFilter lineRibbon = new vtkRibbonFilter();
//                lineRibbon.SetInput(tubeData);
//                lineRibbon.SetWidth(ribbonWidth);
//                lineRibbon.SetAngle(0);
//                
//                vtkLinearExtrusionFilter ribbonThicknessFilter = new vtkLinearExtrusionFilter();
//                ribbonThicknessFilter.SetCapping(1);
//                ribbonThicknessFilter.SetExtrusionTypeToNormalExtrusion();
//                ribbonThicknessFilter.SetScaleFactor(ribbonThickness);
//                ribbonThicknessFilter.SetInput(lineRibbon.GetOutput());
//                
//                // The polygons on the newly extruded edges are not smoothly shaded
//                vtkPolyDataNormals dataNormals = new vtkPolyDataNormals();
//                dataNormals.SetFeatureAngle(80.0); // Angles smaller than this are smoothed
//                dataNormals.SetInput(ribbonThicknessFilter.GetOutput());
//                
//                // tubeMapper.SetInput(ribbonThicknessFilter.GetOutput()); // ribbon
//                tubeMapper.SetInput(dataNormals.GetOutput()); // ribbon
//            }
//            else { // Tube not ribbon
//                vtkTubeFilter lineTube = new vtkTubeFilter();
//                lineTube.SetInput(tubeData);
//                lineTube.SetRadius(tubeThickness);
//                lineTube.SetNumberOfSides(5);
//                tubeMapper.SetInput(lineTube.GetOutput()); // tube
//            }
//
//            lineActor.SetMapper(tubeMapper);
//        }
//
//        lineActor.AddPosition(0.0, 0.0, 0.0);
//
//        // Shiny black ribbon
//        // lineActor.GetProperty().SetColor(0.0,0.0,0.0);
//        // lineActor.GetProperty().SetSpecular(1.0);
//        // lineActor.GetProperty().SetSpecularColor(1.0,1.0,1.0);
//        // lineActor.GetProperty().SetSpecularPower(100);
//        
//        vtkAssembly assembly = new vtkAssembly();
//        assembly.AddPart(lineActor);
//
//        
//        // Spheres at Backbone residue positions
//        vtkSphereSource sphere = new vtkSphereSource();
//        sphere.SetThetaResolution(6);
//        sphere.SetPhiResolution(6);
//        sphere.SetRadius(ribbonThickness + 0.2);
//        
//        vtkPolyData backbonePointData = new vtkPolyData();
//        backbonePointData.SetPoints(backbonePoints);
//
//        vtkGlyph3D spheres = new vtkGlyph3D();
//        spheres.SetInput(backbonePointData);
//        spheres.SetSource(sphere.GetOutput());
//
//        vtkPolyDataMapper spheresMapper = new vtkPolyDataMapper();
//        spheresMapper.SetInput(spheres.GetOutput());
//        
//        vtkActor spheresActor = new vtkActor();
//        spheresActor.SetMapper(spheresMapper);
//        spheresActor.GetProperty().SetOpacity(opacity);
//        spheresActor.GetProperty().BackfaceCullingOn();
//        
//        assembly.AddPart(spheresActor);
//        
//        
//        // One rod per base
//        vtkCylinderSource rod = new vtkCylinderSource();
//        rod.SetHeight(baseRodLength);
//        rod.SetCapping(1);
//        rod.SetRadius(baseRodRadius);
//        rod.SetResolution(5);
//        
//        // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
//        vtkTransform cylinderTransform = new vtkTransform();
//        cylinderTransform.Identity();
//        cylinderTransform.RotateZ(90);
//        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
//        cylinderFilter.SetInput(rod.GetOutput());
//        cylinderFilter.SetTransform(cylinderTransform);
//
//        vtkPoints rodMidpoints = new vtkPoints();
//        for (int i = 0; i < numberOfInputPoints; i++) {
//            double[] point = backbonePoints.GetPoint(i);
//            double[] normal = backboneNormals.GetPoint(i);
//            double[] midPoint = {
//                    point[0] + normal[0] * 0.5 * baseRodLength,
//                    point[1] + normal[1] * 0.5 * baseRodLength,
//                    point[2] + normal[2] * 0.5 * baseRodLength
//            };
//            rodMidpoints.InsertNextPoint(midPoint);
//        }
//        
//        vtkPolyData rodData = new vtkPolyData();
//        rodData.SetPoints(rodMidpoints);
//        rodData.GetPointData().SetNormals(backboneNormals.GetData());
//
//        vtkGlyph3D rods = new vtkGlyph3D();
//        rods.SetInput(rodData);
//        rods.SetSource(cylinderFilter.GetOutput());
//        rods.SetVectorModeToUseNormal();
//        
//        vtkPolyDataMapper rodsMapper = new vtkPolyDataMapper();
//        rodsMapper.SetInput(rods.GetOutput());
//        
//        vtkActor rodsActor = new vtkActor();
//        rodsActor.SetMapper(rodsMapper);
//        
//        assembly.AddPart(rodsActor);
//        
//        return assembly;
//    }
}
