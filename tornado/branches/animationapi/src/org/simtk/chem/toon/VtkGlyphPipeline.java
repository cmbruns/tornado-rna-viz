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
package org.simtk.chem.toon;

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
    private vtkFloatArray scalars = new vtkFloatArray(); // save scalars for non-color use
    
    // Store color, show/hide, and highlight information in three arrays:
    private vtkIntArray colors = new vtkIntArray(); // store toon colors, even when invisible
    private vtkIntArray colorsOrVisibility = new vtkIntArray(); // actual displayed color, can be invisible
    private vtkIntArray highlights = new vtkIntArray(); // Whether toon is highlighted

    private vtkGlyph3D glyph3D = new vtkGlyph3D();
    private vtkPolyDataMapper polyDataMapper = new vtkPolyDataMapper();
    private vtkActor actor = new vtkActor();
    
    // parallel pipeline for highlight highlight toons
    private vtkActor highlightActor = new vtkActor();
    private vtkPolyDataMapper highlightMapper = new vtkPolyDataMapper();
    private vtkAssembly assembly = new vtkAssembly();

    // Keep track of MolToon/Index relationships
    Map<MolToon, Integer> toonIndex = new HashMap<MolToon, Integer>();
    
    ColorLookUpTable colorLookUpTable = new ColorLookUpTable(255);
    
    // I experimented with two ways of highlighting graphics
    enum HighlightMode {EdgeOutline, SolidTint}

    public VtkGlyphPipeline() {
        // Connect the vtk pipeline components together

        polyData.SetPoints(locations); // Locations
        
        colors.SetNumberOfComponents(1);
        colorsOrVisibility.SetNumberOfComponents(1);
        highlights.SetNumberOfComponents(1);

        scalars.SetNumberOfComponents(1);
        normals.SetNumberOfComponents(3);
        polyData.GetPointData().SetNormals(normals);
        
        glyph3D.SetInput(polyData);
        setGlyphSource(getDefaultSource());

        // Glyph colors
        polyData.GetPointData().SetScalars(scalars); // array 0 (zero)
        
        polyData.GetPointData().AddArray(colors); // array 1
        polyData.GetPointData().AddArray(colorsOrVisibility); // array 2
        polyData.GetPointData().AddArray(highlights); // array 3
        
        polyDataMapper.SetScalarModeToUsePointFieldData();
        polyDataMapper.SelectColorArray(2); // colors modified by visibility
        
        highlightMapper.SetScalarModeToUsePointFieldData();
        highlightMapper.SelectColorArray(3); // highlight color
        
        // Use same table for both normal rendering and highlighting
        // (highlighting only uses invisible(0) and highlight(1) color)
        colorLookUpTable.color(polyDataMapper);
        colorLookUpTable.color(highlightMapper);

        polyDataMapper.SetInput(glyph3D.GetOutput());
        
        actor.SetMapper(polyDataMapper);
        // actor.GetProperty().BackfaceCullingOn();

        // fork geometry pipeline for highlighting
        highlightMapper.SetInput(glyph3D.GetOutput());
        // highlightMapper.ScalarVisibilityOff(); // don't use scalars for colors
        highlightActor.SetMapper(highlightMapper);
        
        // Turn off shading for highlighting
        highlightActor.GetProperty().SetAmbient(1.0);
        highlightActor.GetProperty().SetDiffuse(0.0);
        highlightActor.GetProperty().SetSpecular(0.0);
        // highlightActor.GetProperty().SetColor(1,1,0); // only works when main model is shown?
        // highlightActor.SetVisibility(0); // Turn off
        
        // Polygon offset is a global property that lets selection and main graphic coexist
        highlightMapper.SetResolveCoincidentTopologyToPolygonOffset();

        HighlightMode highlightMode;
        // highlightMode = HighlightMode.EdgeOutline;
        highlightMode = HighlightMode.SolidTint;

        switch (highlightMode) {
        case EdgeOutline: // Outline edges that "drop away" in Z
            highlightActor.GetProperty().SetLineWidth(1.0);
            highlightActor.GetProperty().SetRepresentationToWireframe();
            highlightActor.GetProperty().FrontfaceCullingOn();
            break;
        case SolidTint: // paint a thin coat of color over graphics
            // highlightActor.GetProperty().SetOpacity(0.6);
            break;
        }

        // Order here is important sometimes
        assembly.AddPart(highlightActor);        
        assembly.AddPart(actor);
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

            int colorIndex = colorLookUpTable.getColorIndex(color);
            colors.SetTuple1(index, colorIndex);
            colorsOrVisibility.SetTuple1(index, colorIndex);
        }
        else { // Create a new glyph3D entry
            addToon(toon, location, normal, color);
        }
    }
    
    protected void addToon(MolToon toon, Vector3D location, Vector3D normal, Color color) {
        int index = locations.InsertNextPoint(location.getX(), location.getY(), location.getZ());
        toonIndex.put(toon, index);
        normals.InsertNextTuple3(normal.getX(), normal.getY(), normal.getZ());

        int colorIndex = colorLookUpTable.getColorIndex(color);
        colors.InsertNextTuple1(colorIndex);
        colorsOrVisibility.InsertNextTuple1(colorIndex); // default to shown

        highlights.InsertNextTuple1(colorLookUpTable.getHighlightIndex()); // default to highlighted
        // highlights.InsertNextTuple1(colorLookUpTable.getInvisibleIndex()); // default to not highlighted
        
        scalars.InsertNextTuple1(0);
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
    
    public void show(MolToon toon) {
        int i = toonIndex.get(toon);
        int color = colors.GetValue(i);
        colorsOrVisibility.InsertTuple1(i, color);
        // TODO handle errors and set "Modified" if needed
    }
    
    public void hide(MolToon toon) {
        int i = toonIndex.get(toon);
        colorsOrVisibility.InsertTuple1(i, colorLookUpTable.getInvisibleIndex());
        // TODO handle errors and set "Modified" if needed
    }
    
    public void highlight(MolToon toon) {
        show(toon);
        int i = toonIndex.get(toon);
        highlights.InsertTuple1(i, colorLookUpTable.getHighlightIndex());
        // TODO handle errors and set "Modified" if needed
    }
    
    public void unHighlight(MolToon toon) {
        show(toon);
        int i = toonIndex.get(toon);
        highlights.InsertTuple1(i, colorLookUpTable.getInvisibleIndex());
        // TODO handle errors and set "Modified" if needed
    }

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
        
        private Color highlightColor = new Color(255,255,50); // yellow
        private Color selectionColor = new Color(100, 100, 255); // blue

        private int numberOfEntries = 255;

        public int getInvisibleIndex() {return 0;}
        public int getHighlightIndex() {return 1;}
        public int getSelectionIndex() {return 2;}
        private int firstUnusedIndex = 3;

        ColorLookUpTable(int numberOfEntries) {
            this.numberOfEntries = numberOfEntries;
            
            // Start by creating a default color gradient
            lut.SetNumberOfTableValues(numberOfEntries);
            lut.SetRange(1.0, 60.0);
            lut.SetAlphaRange(1.0, 1.0);
            lut.SetValueRange(1.0, 1.0);
            lut.SetHueRange(0.0, 1.0);
            lut.SetSaturationRange(0.5, 0.5);

            // lut.SetVectorComponent(1); // Does not affect which scalar component is used?

            lut.Build();

            // Set special transparent colors
            // partial transparency for selection film
            // but set it partial at the actor level, not here
            double highlightAlpha = 0.6; 
            setSpecialIndexColor(getSelectionIndex(), selectionColor, highlightAlpha);
            setSpecialIndexColor(getHighlightIndex(), highlightColor, highlightAlpha);
            setSpecialIndexColor(getInvisibleIndex(), Color.white, 0.0);
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

        private void setSpecialIndexColor(int index, Color color, double alpha) {
            lut.SetTableValue(index, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, alpha);
            // tabulatedColors.put(color, index); // don't subject transparent colors to lookup
        }

    }
    
}
