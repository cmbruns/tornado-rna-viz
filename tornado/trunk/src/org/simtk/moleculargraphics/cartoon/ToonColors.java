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
import java.util.HashMap;
import java.util.Map;

import vtk.vtkLookupTable;
import vtk.vtkPolyDataMapper;

public class ToonColors {
    protected vtkLookupTable lut = new vtkLookupTable();
    protected Map<Object, Integer> colorIndices = new HashMap<Object, Integer>();
    protected int nextColorIndex = 0;
    protected int initialTableSize = 50;
    protected vtkPolyDataMapper mapper;
    
    ToonColors(vtkPolyDataMapper m) {
        lut.SetNumberOfTableValues(initialTableSize);
        lut.Build();

        this.mapper = m;
        mapper.ScalarVisibilityOn();
        mapper.SetColorModeToMapScalars();
        mapper.SetLookupTable(lut);
        mapper.SetScalarRange(0, lut.GetNumberOfTableValues() - 1);

    }
    
    void setColor(Object colorable, ColorScheme colorScheme) {
        if (! (colorIndices.containsKey(colorable))) return;
        
        int index = colorIndices.get(colorable);
        try {
            Color color = colorScheme.colorOf(colorable);
            double[] c = {
                    color.getRed()/255.0, 
                    color.getGreen()/255.0, 
                    color.getBlue()/255.0, 
                    1.0};
            lut.SetTableValue(index, c);
        } catch (UnknownObjectColorException exc) {}            
    }
    
    void setColor(ColorScheme colorScheme) {
        // Color all objects
        for (Object object : colorIndices.keySet()) {
            int index = colorIndices.get(object);
            try {
                Color color = colorScheme.colorOf(object);
                double[] c = {
                        color.getRed()/255.0, 
                        color.getGreen()/255.0, 
                        color.getBlue()/255.0, 
                        1.0};
                lut.SetTableValue(index, c);
            } catch (UnknownObjectColorException exc) {}
        }
    }
    
    int getColorIndex(Object colorable) {
        if (! (colorIndices.containsKey(colorable)) ) {
            colorIndices.put(colorable, nextColorIndex);
            nextColorIndex ++;
            
            if (nextColorIndex > lut.GetNumberOfTableValues()) {
                // 1) remember old table values
                // double[][] existingColorScalars = new double[(lut.GetNumberOfTableValues())][];
                // for (int c = 0; c < existingColorScalars.length; c++) {
                //     existingColorScalars[c] = lut.GetTableValue(c);
                // }
                
                // 2) construct larger table
                lut.SetNumberOfTableValues(2 * nextColorIndex);
                lut.Build();
                mapper.SetScalarRange(0, lut.GetNumberOfTableValues() - 1);
                
                // 3) insert previous values into the color table
                // for (int c = 0; c < existingColorScalars.length; c++)
                //     lut.SetTableValue(c, existingColorScalars[c]);
                
            }
        }
        
        // Add 0.5 because the mapper seems to use floor() to convert 
        // interpolated values to integers
        // return colorIndices.get(colorable) + 0.5;
        // return colorIndices.get(colorable) + 0.25;

        // BUT, with large color tables (above 3300 entries), the indices 
        // seem to get rounded UP
        return colorIndices.get(colorable);
    }
    
//    void applyMapper(vtkPolyDataMapper mapper) {
//        mapper.ScalarVisibilityOn();
//        mapper.SetColorModeToMapScalars();
//        mapper.SetLookupTable(lut);
//        mapper.SetScalarRange(0.0, 10000);
//    }

    class ToonColorsNotFinalizedException extends RuntimeException {}
    class ToonColorsFinalizedException extends RuntimeException {}
}
