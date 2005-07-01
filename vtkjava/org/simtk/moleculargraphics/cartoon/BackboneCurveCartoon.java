/*
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.*;

import vtk.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class BackboneCurveCartoon extends MolecularCartoon {

    static boolean drawWireFrame = false;
    static boolean drawSmoothSpline = true;
    static boolean drawFlatRibbon = true;
    static double ribbonWidth = 1.5;
    static double ribbonThickness = 0.7;
    static double tubeThickness = 0.8;
    static double baseRodLength = 9.0;
    static double baseRodRadius = 0.50;

    /**
     * How many spline segments per residue
     */
    static double splineFactor = 5.0;
    
    /**
     * Update graphical primitives to reflect a change in atomic positions
     *
     */
    @Override
    public void updateCoordinates() {
        // TODO
    }

    @Override
    public vtkAssembly highlight(Residue residue, Color color) {
        return null; // TODO
    }

    @Override
    public vtkAssembly represent(Molecule molecule) {
        return represent(molecule, 1.00, null, 1.00);
    }
    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr, double opacity) {
        if (! (molecule instanceof Biopolymer)) return null;
        Biopolymer biopolymer = (Biopolymer) molecule;
        
        vtkPoints linePoints = new vtkPoints();
        vtkPoints lineNormals = new vtkPoints();
        for (Residue residue : biopolymer.residues()) {
            BaseVector3D backbonePosition = residue.getBackbonePosition();
            BaseVector3D sideChainPosition = residue.getSideChainPosition();
            if ( (backbonePosition != null) && (sideChainPosition != null) ) {
                linePoints.InsertNextPoint(backbonePosition.getX(), backbonePosition.getY(), backbonePosition.getZ());
                Vector3D normal = sideChainPosition.minus(backbonePosition).unit();
                lineNormals.InsertNextPoint(normal.getX(), normal.getY(), normal.getZ());
            }
        }
        
        vtkPoints backbonePoints = linePoints;
        vtkPoints backboneNormals = lineNormals;

        int numberOfInputPoints = linePoints.GetNumberOfPoints();
        if (numberOfInputPoints < 1) return null;
        
        int numberOfOutputPoints = numberOfInputPoints;
        
        if (drawSmoothSpline) {
            // Replace linePoints and lineNormals with smoothed
            //  oversampled versions of themselves, by means of splines
            
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

                Vector3D normal = (new Vector3D(
                        normalSplineX.Evaluate(t),
                        normalSplineY.Evaluate(t),
                        normalSplineZ.Evaluate(t))).unit();
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
        
        if (drawWireFrame) {
            vtkPolyLine backboneLine = new vtkPolyLine();
            backboneLine.GetPointIds().SetNumberOfIds(numberOfOutputPoints);
            for (int i = 0; i < linePoints.GetNumberOfPoints(); i++) {
                backboneLine.GetPointIds().SetId(i, i);
            }

            // Wireframe
            vtkUnstructuredGrid lineGrid = new vtkUnstructuredGrid();
            lineGrid.Allocate(1, 1);
            lineGrid.InsertNextCell(backboneLine.GetCellType(), backboneLine.GetPointIds());

            lineGrid.SetPoints(linePoints);
            
            vtkDataSetMapper lineMapper = new vtkDataSetMapper();
            lineMapper.SetInput(lineGrid);

            lineActor.SetMapper(lineMapper); // wireframe
        }

        else { // Tube or ribbon
            vtkCellArray lineCells = new vtkCellArray();
            lineCells.InsertNextCell(numberOfOutputPoints);
            for (int i = 0; i < numberOfOutputPoints; i ++)
                lineCells.InsertCellPoint(i);
            
            vtkPolyData tubeData = new vtkPolyData();
            tubeData.SetPoints(linePoints);
            tubeData.SetLines(lineCells);

            // Incorporate the lineNormals
            tubeData.GetPointData().SetNormals(lineNormals.GetData());
    
            vtkPolyDataMapper tubeMapper = new vtkPolyDataMapper();
            
            if (drawFlatRibbon) { // Ribbon not Tube
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
                lineRibbon.SetInput(tubeData);
                lineRibbon.SetWidth(ribbonWidth);
                lineRibbon.SetAngle(0);
                
                vtkLinearExtrusionFilter ribbonThicknessFilter = new vtkLinearExtrusionFilter();
                ribbonThicknessFilter.SetCapping(1);
                ribbonThicknessFilter.SetExtrusionTypeToNormalExtrusion();
                ribbonThicknessFilter.SetScaleFactor(ribbonThickness);
                ribbonThicknessFilter.SetInput(lineRibbon.GetOutput());
                
                // The polygons on the newly extruded edges are not smoothly shaded
                vtkPolyDataNormals dataNormals = new vtkPolyDataNormals();
                dataNormals.SetFeatureAngle(80.0); // Angles smaller than this are smoothed
                dataNormals.SetInput(ribbonThicknessFilter.GetOutput());
                
                // tubeMapper.SetInput(ribbonThicknessFilter.GetOutput()); // ribbon
                tubeMapper.SetInput(dataNormals.GetOutput()); // ribbon
            }
            else { // Tube not ribbon
                vtkTubeFilter lineTube = new vtkTubeFilter();
                lineTube.SetInput(tubeData);
                lineTube.SetRadius(tubeThickness);
                lineTube.SetNumberOfSides(5);
                tubeMapper.SetInput(lineTube.GetOutput()); // tube
            }

            lineActor.SetMapper(tubeMapper);
        }

        lineActor.AddPosition(0.0, 0.0, 0.0);

        // Shiny black ribbon
        // lineActor.GetProperty().SetColor(0.0,0.0,0.0);
        // lineActor.GetProperty().SetSpecular(1.0);
        // lineActor.GetProperty().SetSpecularColor(1.0,1.0,1.0);
        // lineActor.GetProperty().SetSpecularPower(100);
        
        vtkAssembly assembly = new vtkAssembly();
        assembly.AddPart(lineActor);

        
        // Spheres at Backbone residue positions
        vtkSphereSource sphere = new vtkSphereSource();
        sphere.SetThetaResolution(6);
        sphere.SetPhiResolution(6);
        sphere.SetRadius(ribbonThickness + 0.2);
        
        vtkPolyData backbonePointData = new vtkPolyData();
        backbonePointData.SetPoints(backbonePoints);

        vtkGlyph3D spheres = new vtkGlyph3D();
        spheres.SetInput(backbonePointData);
        spheres.SetSource(sphere.GetOutput());

        vtkPolyDataMapper spheresMapper = new vtkPolyDataMapper();
        spheresMapper.SetInput(spheres.GetOutput());
        
        vtkActor spheresActor = new vtkActor();
        spheresActor.SetMapper(spheresMapper);
        spheresActor.GetProperty().SetOpacity(opacity);
        spheresActor.GetProperty().BackfaceCullingOn();
        
        assembly.AddPart(spheresActor);
        
        
        // One rod per base
        vtkCylinderSource rod = new vtkCylinderSource();
        rod.SetHeight(baseRodLength);
        rod.SetCapping(1);
        rod.SetRadius(baseRodRadius);
        rod.SetResolution(5);
        
        // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
        vtkTransform cylinderTransform = new vtkTransform();
        cylinderTransform.Identity();
        cylinderTransform.RotateZ(90);
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        cylinderFilter.SetInput(rod.GetOutput());
        cylinderFilter.SetTransform(cylinderTransform);

        vtkPoints rodMidpoints = new vtkPoints();
        for (int i = 0; i < numberOfInputPoints; i++) {
            double[] point = backbonePoints.GetPoint(i);
            double[] normal = backboneNormals.GetPoint(i);
            double[] midPoint = {
                    point[0] + normal[0] * 0.5 * baseRodLength,
                    point[1] + normal[1] * 0.5 * baseRodLength,
                    point[2] + normal[2] * 0.5 * baseRodLength
            };
            rodMidpoints.InsertNextPoint(midPoint);
        }
        
        vtkPolyData rodData = new vtkPolyData();
        rodData.SetPoints(rodMidpoints);
        rodData.GetPointData().SetNormals(backboneNormals.GetData());

        vtkGlyph3D rods = new vtkGlyph3D();
        rods.SetInput(rodData);
        rods.SetSource(cylinderFilter.GetOutput());
        rods.SetVectorModeToUseNormal();
        
        vtkPolyDataMapper rodsMapper = new vtkPolyDataMapper();
        rodsMapper.SetInput(rods.GetOutput());
        
        vtkActor rodsActor = new vtkActor();
        rodsActor.SetMapper(rodsMapper);
        
        assembly.AddPart(rodsActor);
        
        return assembly;
    }
}
