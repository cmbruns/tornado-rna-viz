/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Jul 17, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.*;
import org.simtk.molecularstructure.Chemical;
import vtk.vtkLookupTable;
import vtk.vtkPolyDataMapper;

public class ToonColors {
    protected vtkLookupTable lut = new vtkLookupTable();
    protected Map<Chemical, Integer> chemicalIndices = new HashMap<Chemical, Integer>();
    protected Map<Integer, Chemical> indexChemicals = new HashMap<Integer, Chemical>();
    protected int nextColorIndex = 0;
    protected int initialTableSize = 5;
    protected vtkPolyDataMapper mapper;
    protected Color defaultColor = new Color(100,100,255,0);
    
    // protected boolean hasVisibleColor = false;
    protected Set<Integer> visibleColorIndices = new HashSet<Integer>();
    
    ToonColors(vtkPolyDataMapper mapper) {
        lut.SetNumberOfTableValues(initialTableSize);
        lut.Build();

        this.mapper = mapper;
        mapper.ScalarVisibilityOn();
        mapper.SetColorModeToMapScalars();
        mapper.SetLookupTable(lut);
        mapper.SetScalarRange(0, lut.GetNumberOfTableValues() - 1);
    }

    public void setDefaultColor(Color color) {
        defaultColor = color;
    }
    
    void setColor(Object colorable, ColorScheme colorScheme) {
        if (! (chemicalIndices.containsKey(colorable))) return;
        
        int index = chemicalIndices.get(colorable);
        try {
            Color color = colorScheme.colorOf(colorable);
            double[] c = {
                    color.getRed()/255.0, 
                    color.getGreen()/255.0, 
                    color.getBlue()/255.0, 
                    color.getAlpha()/255.0};
            lut.SetTableValue(index, c);
            
            // Keep track of visible vs. invisible colors
            if (c[3] > 0) visibleColorIndices.add(index);
            else visibleColorIndices.remove(index);
                
        } catch (UnknownObjectColorException exc) {}            
    }
    
    void setColor(ColorScheme colorScheme) {
        // Color all objects
        for (Object object : chemicalIndices.keySet()) {
            int index = chemicalIndices.get(object);
            try {
                Color color = colorScheme.colorOf(object);
                double[] c = {
                        color.getRed()/255.0, 
                        color.getGreen()/255.0, 
                        color.getBlue()/255.0, 
                        color.getAlpha()/255.0};
                lut.SetTableValue(index, c);

                if (c[3] > 0) visibleColorIndices.add(index);
                else visibleColorIndices.remove(index);

            } catch (UnknownObjectColorException exc) {}
        }
    }
    
    int getColorIndex(Chemical colorable) {
        if (! (chemicalIndices.containsKey(colorable)) ) {
            int index = nextColorIndex;
            chemicalIndices.put(colorable, index);
            indexChemicals.put(index, colorable);
            nextColorIndex ++;
            
            if (nextColorIndex > lut.GetNumberOfTableValues()) {
                // 1) remember old table values
                double[][] existingColorScalars = new double[(lut.GetNumberOfTableValues())][];
                for (int c = 0; c < existingColorScalars.length; c++) {
                    existingColorScalars[c] = lut.GetTableValue(c);
                }
                
                // 2) construct larger table
                lut.SetNumberOfTableValues(2 * nextColorIndex);
                lut.Build();
                mapper.SetScalarRange(0, lut.GetNumberOfTableValues() - 1);
                
                // 3) insert previous values into the color table
                for (int c = 0; c < existingColorScalars.length; c++) {
                   lut.SetTableValue(c, existingColorScalars[c]);
                }
                
            }
            
            double[] c = {
                    defaultColor.getRed()/255.0, 
                    defaultColor.getGreen()/255.0, 
                    defaultColor.getBlue()/255.0, 
                    defaultColor.getAlpha()/255.0};
            lut.SetTableValue(index, c);

            if (c[3] > 0) visibleColorIndices.add(index);
            else visibleColorIndices.remove(index);

        }
        
        // Add 0.5 because the mapper seems to use floor() to convert 
        // interpolated values to integers
        // return colorIndices.get(colorable) + 0.5;
        // return colorIndices.get(colorable) + 0.25;

        // BUT, with large color tables (above 3300 entries), the indices 
        // seem to get rounded UP
        // So instead of adding 0.5 here, include a rounding filter in the
        // vtk pipeline in cases where interpolation might occur.
        
        return chemicalIndices.get(colorable);
    }
    
    public Chemical getChemicalFromScalar(int scalar) {
        return indexChemicals.get(scalar);
    }
    
    public boolean hasVisibleColor() {return (visibleColorIndices.size() > 0);}
}
