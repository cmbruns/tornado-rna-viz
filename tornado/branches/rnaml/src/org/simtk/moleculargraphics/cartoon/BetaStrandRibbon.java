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
import org.simtk.molecularstructure.protein.BetaStrand;

/** 
 * @author Christopher Bruns
 * 
 * Draw a ribbon with rectangular cross section along the backbone of a biopolymer.
 * This version requires a patch to vtkRibbonFilter.cxx in vtk libraries, at least for
 * vtk libraries no newer than vtk 5.0.0
 */
public class BetaStrandRibbon extends MolecularCartoonClass {
    double ribbonThickness = 0.5;
    double ribbonWidth = 1.20;
    double ribbonResolution = 0.2; // For spline smoothing
    
    ColorScheme colorScheme = new ConstantColor(Color.pink);
    
    // Coloring
    vtkLookupTable lut = new vtkLookupTable();
    private int baseColorIndex = 1;
    private Map<Color, Integer> colorIndices = new HashMap<Color, Integer>();

    vtkAssembly assembly = new vtkAssembly();
    
    public BetaStrandRibbon() {
        
        // TODO move color table stuff to a baser class
        lut.SetNumberOfTableValues(256);
        lut.SetRange(1.0, 60.0);
        lut.SetAlphaRange(1.0, 1.0);
        lut.SetValueRange(1.0, 1.0);
        lut.SetHueRange(0.0, 1.0);
        lut.SetSaturationRange(0.5, 0.5);
        lut.Build();
    }  

    public void updateCoordinates() {        // TODO
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
        super.add(m);
        if (m instanceof Biopolymer)
            addBiopolymer((Biopolymer) m);
    }

    public void addBiopolymer(Biopolymer biopolymer) {
        System.out.println("Biopolymer found");
        for (SecondaryStructure structure : biopolymer.secondaryStructures()) {
            System.out.println("Secondary structure found");
            if (! (structure instanceof BetaStrand)) continue;
            BetaStrand strand = (BetaStrand) structure;
            System.out.println("Strand found");

            java.util.List<Residue> residueList = new Vector<Residue>();
            for (Residue residue : strand.residues()) {
                residueList.add(residue);
            }
            addResidues(residueList);
        }
    }
    
    public void addResidues(java.util.List<Residue> residues) {
        LineData lineData = new LineData();
        Vector3D previousNormal = null;
        Vector3D previousPosition = null;
        
        RESIDUE: for (Residue res : residues) {
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
            
            Vector3D position = backbonePosition;
            Vector3D normal = sideChainPosition.minus(backbonePosition).unit();
            
            // Smooth pleating of beta strands
            Residue next = residue.getNextResidue();
            Residue previous = residue.getPreviousResidue();
            if ( (next != null) && (previous != null)
                 && (next instanceof LocatedResidue)
                 && (previous instanceof LocatedResidue)
                 ) {
                try {
                    Vector3D p0 = ((LocatedResidue)previous).getBackbonePosition();
                    Vector3D p1 = backbonePosition;
                    Vector3D p2 = ((LocatedResidue)next).getBackbonePosition();
                    position = p1.times(2.0).plus(p0).plus(p2).times(0.25);                    
                } catch (InsufficientAtomsException exc) {}
            }
            
            if (position == null) continue RESIDUE;
            if (normal == null) continue RESIDUE;
            
            if (previousPosition != null) {
                // Minimize rotation along direction of chain path
                // For coil, flip anything greater than 90 degrees away
                Vector3D chainDirection = position.minus(previousPosition).unit();
                Vector3D prevNormProj = chainDirection.cross(previousNormal).unit();
                Vector3D currNormProj = chainDirection.cross(normal).unit();
                if (prevNormProj.dot(currNormProj) < 0) // greater than 90 degree angle
                    normal = normal.times(-1.0);
            }

            int colorScalar = chemColorScalar(residue);
            
            lineData.appendPoint(position, normal, colorScalar);
            
            previousNormal = normal;
            previousPosition = position;
        }
        
        if (lineData.getNumberOfPoints() < 2) return;
        
        addLinearSegment(lineData.generatePolyData());
    }

    protected void addLinearSegment(vtkPolyData lineData) {
        vtkSplineFilter splineFilter = new vtkSplineFilter();
        splineFilter.SetInput(lineData);
        // splineFilter.SetMaximumNumberOfSubdivisions((int)(splineFactor + 0.9));
        splineFilter.SetLength(ribbonResolution);
        
        vtkCleanPolyData cleaner = new vtkCleanPolyData();
        cleaner.SetInput(splineFilter.GetOutput());

        vtkRibbonFilter lineRibbon = new vtkRibbonFilter();
        lineRibbon.SetWidth(ribbonWidth);
        lineRibbon.SetInput(cleaner.GetOutput());
                
        vtkLinearExtrusionFilter ribbonThicknessFilter = new vtkLinearExtrusionFilter();
        ribbonThicknessFilter.SetCapping(1);
        ribbonThicknessFilter.SetExtrusionTypeToNormalExtrusion();
        ribbonThicknessFilter.SetScaleFactor(ribbonThickness);
        ribbonThicknessFilter.SetInput(lineRibbon.GetOutput());
        
        // The polygons on the newly extruded edges are not smoothly shaded
        vtkPolyDataNormals dataNormals = new vtkPolyDataNormals();
        dataNormals.SetFeatureAngle(80.0); // Angles smaller than this are smoothed
        dataNormals.SetInput(lineRibbon.GetOutput());
        
        vtkPolyDataMapper ribbonMapper = new vtkPolyDataMapper();        
        ribbonMapper.SetInput(dataNormals.GetOutput()); // ribbon

        ribbonMapper.SetLookupTable(lut);
        ribbonMapper.SetScalarRange(0.0, lut.GetNumberOfTableValues());

        vtkActor lineActor = new vtkActor();  
        lineActor.SetMapper(ribbonMapper);

        assembly.AddPart(lineActor);        
    }
    
    protected int chemColorScalar(Object chemical) {
        Color chemColor = colorScheme.colorOf(chemical);
        return getColorScalar(chemColor);
    }
    
    protected int getColorScalar(Color color) {
        if (! (colorIndices.containsKey(color))) {
            colorIndices.put(color, baseColorIndex);
            lut.SetTableValue(
                    baseColorIndex, 
                    color.getRed()/255.0, 
                    color.getGreen()/255.0, 
                    color.getBlue()/255.0,
                    1.0);
            baseColorIndex ++;
        }
        return colorIndices.get(color);
    }
    
    class LineData {
        private vtkPoints linePoints;
        private vtkFloatArray lineNormals;
        private vtkFloatArray colorScalars;
        
        public LineData() {
            linePoints = new vtkPoints();
            lineNormals = new vtkFloatArray();
            lineNormals.SetNumberOfComponents(3);
            colorScalars = new vtkFloatArray();
            colorScalars.SetNumberOfComponents(1);
        }

        public void appendPoint(Vector3D point, Vector3D normal, int colorScalar) {
            linePoints.InsertNextPoint(point.x(), point.y(), point.z());
            lineNormals.InsertNextTuple3(normal.x(), normal.y(), normal.z());
            colorScalars.InsertNextValue(colorScalar);
        }
        
        public int getNumberOfPoints() {
            return linePoints.GetNumberOfPoints();
        }
        
        public vtkPolyData generatePolyData() {            
            vtkCellArray lineCells = new vtkCellArray();
            int nPoints = linePoints.GetNumberOfPoints();
            lineCells.InsertNextCell(nPoints);
            for (int i = 0; i < nPoints; i ++)
                lineCells.InsertCellPoint(i);
            
            vtkPolyData answer = new vtkPolyData();

            answer.SetPoints(linePoints);
            answer.SetLines(lineCells);

            // Incorporate the normals vectors
            answer.GetPointData().SetNormals(lineNormals);

            // Set color scalars
            answer.GetPointData().SetScalars(colorScalars);
            
            return answer;
        }
    }
    
}
