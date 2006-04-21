/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Apr 19, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import vtk.*;

import org.simtk.geometry3d.*;

import java.awt.Color;
import java.util.*;

/** 
 *  
  * @author Christopher Bruns
  * 
  * Class for use by MolToons that use vtkGlyph3D objects
 */
public class VtkGlyphPipeline {
    
    // vtk data pipeline objects
    private vtkPolyData polyData = new vtkPolyData();
    private vtkPoints locations = new vtkPoints();    
    protected vtkFloatArray normals = new vtkFloatArray(); // bond directions/lengths
    private vtkFloatArray colors = new vtkFloatArray();
    private vtkGlyph3D glyph3D = new vtkGlyph3D();
    private vtkPolyDataMapper polyDataMapper = new vtkPolyDataMapper();
    private vtkActor actor = new vtkActor();
    
    // parallel pipeline for wireframe highlight toons
    private vtkActor wireFrameActor = new vtkActor();
    private vtkAssembly assembly = new vtkAssembly();

    // Keep track of MolToon/Index relationships
    Map<MolToon, Integer> toonIndex = new HashMap<MolToon, Integer>();
    
    ColorLookUpTable colorLookUpTable = new ColorLookUpTable(255);
    
    public VtkGlyphPipeline() {
        // Connect the vtk pipeline components together

        polyData.SetPoints(locations); // Locations

        normals.SetNumberOfComponents(3);
        polyData.GetPointData().SetNormals(normals);
        
        colors.SetNumberOfComponents(1);
        
        glyph3D.SetInput(polyData);
        setGlyphSource(getDefaultSource());

        // Glyph colors
        polyData.GetPointData().SetScalars(colors); 
        colorLookUpTable.color(polyDataMapper);
        // polyDataMapper.SetLookupTable(lut);  // TODO
        // polyDataMapper.SetScalarRange(0, 255);

        polyDataMapper.SetInput(glyph3D.GetOutput());
        
        actor.SetMapper(polyDataMapper);
        // actor.GetProperty().BackfaceCullingOn();

        // Wireframe for highlighting
        wireFrameActor.SetMapper(polyDataMapper);
        wireFrameActor.GetProperty().SetRepresentationToWireframe();
        wireFrameActor.GetProperty().SetLineWidth(4.0);
        wireFrameActor.GetProperty().FrontfaceCullingOn();
        // Turn off shading for highlighting
        wireFrameActor.GetProperty().SetAmbient(1.0);
        wireFrameActor.GetProperty().SetDiffuse(0.0);
        wireFrameActor.GetProperty().SetSpecular(0.0);
        wireFrameActor.GetProperty().SetColor(0,1,0); // only works when main model is shown?
        wireFrameActor.SetVisibility(0); // Turn off
        
        polyDataMapper.SetResolveCoincidentTopologyToShiftZBuffer();
        polyDataMapper.SetResolveCoincidentTopologyZShift(0.10);

        // Order is important!
        assembly.AddPart(actor);
        assembly.AddPart(wireFrameActor);        
    }
    
    /**
     * 
     * @param v
     * @return the index of the point added
     */
    public void placeToon(MolToon toon, Vector3D location, Vector3D normal, Color color) {
        int index;
        if (toonIndex.containsKey(toon)) {// Update existing entry
            index = toonIndex.get(toon);
            locations.SetPoint(index, location.getX(), location.getY(), location.getZ());
            normals.SetTuple3(index, normal.getX(), normal.getY(), normal.getZ());
            colors.SetTuple1(index, colorLookUpTable.getColorIndex(color));
        }
        else { // Create a new glyph3D entry
            addToon(toon, location, normal, color);
        }
    }
    
    protected void addToon(MolToon toon, Vector3D location, Vector3D normal, Color color) {
        int index = locations.InsertNextPoint(location.getX(), location.getY(), location.getZ());
        toonIndex.put(toon, index);
        normals.InsertNextTuple3(normal.getX(), normal.getY(), normal.getZ());
        colors.InsertNextTuple1(colorLookUpTable.getColorIndex(color));
    }

    // Override these functions to use TensorGlyph rather than vtkGlyph3D
    public void setGlyphSource(vtkPolyData data) {
        glyph3D.SetSource(data);
    }
    public void scaleByNormal() {
        glyph3D.SetScaleModeToScaleByVector(); // Take length from normal
        glyph3D.SetVectorModeToUseNormal(); // Take direction from normal
    }
    public void scaleNone() {
        glyph3D.SetScaleModeToDataScalingOff(); // Do not adjust size
    }
    public void orientByNormal() {
        glyph3D.SetVectorModeToUseNormal(); // Take direction from normal        
    }
    public void colorByScalar() {
        glyph3D.SetColorModeToColorByScalar(); // Take color from scalar        
    }

    public vtkAssembly getVtkAssembly() {return assembly;}
    
    public void clear() {
        normals.Reset();
        normals.Squeeze();
        colors.Reset();
        colors.Squeeze();
        locations.Reset();
        locations.Squeeze();
    }
    
    public int getColorIndex(Color c) {return colorLookUpTable.getColorIndex(c);}
    
    private vtkPolyData getDefaultSource() {
        // Prototype shape is a line
        vtkPointSource point = new vtkPointSource();
        point.SetNumberOfPoints(1);
        point.SetRadius(0);
        return point.GetOutput();
    }
    
    private class ColorLookUpTable {
        
        vtkLookupTable lut = new vtkLookupTable();
        Map<Color, Integer> tabulatedColors = new HashMap<Color, Integer>();
        
        private Color selectionColor = new Color(255,255,150); // yellow
        private Color highlightColor = new Color(50, 250, 50); // green

        private int numberOfEntries = 255;
        private int firstUnusedIndex = 0;

        ColorLookUpTable(int numberOfEntries) {
            this.numberOfEntries = numberOfEntries;
            
            // Start by creating a default color gradient
            lut.SetNumberOfTableValues(numberOfEntries);
            lut.SetRange(1.0, 60.0);
            lut.SetAlphaRange(1.0, 1.0);
            lut.SetValueRange(1.0, 1.0);
            lut.SetHueRange(0.0, 1.0);
            lut.SetSaturationRange(0.5, 0.5);
            lut.Build();
            
            // Set colors for 
            setIndexColor(getSelectionIndex(), selectionColor);
            setIndexColor(getHighlightIndex(), highlightColor);
            // invisible needs alpha channel set
            lut.SetTableValue(getInvisibleIndex(), 0.0, 0.0, 0.0, 0.0);

        }
        
        void color(vtkPolyDataMapper mapper) {
            mapper.SetLookupTable(lut);
            mapper.SetScalarRange(0, numberOfEntries - 1);
        }
        
        int getColorIndex(Color color) {
            if (! tabulatedColors.containsKey(color)) {
                setIndexColor(firstUnusedIndex, color);
                firstUnusedIndex ++;
                // TODO - check for overflow of table size
            }
            return tabulatedColors.get(color);
        }
        
        private void setIndexColor(int index, Color color) {
            lut.SetTableValue(index, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
            tabulatedColors.put(color, index);
        }

        public int getInvisibleIndex() {return numberOfEntries - 3;}
        public int getHighlightIndex() {return numberOfEntries - 2;}
        public int getSelectionIndex() {return numberOfEntries - 1;}
    }
    
}
