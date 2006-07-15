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
 * Created on Jul 6, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.*;

import vtk.*;

public class MolecularCartoonClass implements MolecularCartoon {
    protected Set<vtkActor> actorSet = new HashSet<vtkActor>(); 
    protected ToonColors toonColors = new ToonColors();
    protected Set<MolecularCartoon> subToons = new HashSet<MolecularCartoon>();
    protected vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    
    public void colorToon(Object object, ColorScheme colorScheme) {
        for (MolecularCartoon subToon : subToons) {
            subToon.colorToon(object, colorScheme);
        }
        toonColors.setColor(object, colorScheme);
    }
    
    public void colorToon(ColorScheme colorScheme) {
        for (MolecularCartoon subToon : subToons) {
            subToon.colorToon(colorScheme);
        }
        toonColors.setColor(colorScheme);
    }
    
    public Set<vtkActor> vtkActors() {
        return actorSet;
    }
    
    public void finalizeCartoon(ColorScheme colorScheme) {
        for (MolecularCartoon subToon : subToons) {
            subToon.finalizeCartoon(colorScheme);
        }
        toonColors.commitTableSize();
        toonColors.applyMapper(mapper);
        colorToon(colorScheme);
    }
    
    protected class ToonColors {
        protected vtkLookupTable lut = new vtkLookupTable();
        protected Map<Object, Integer> colorIndices = new HashMap<Object, Integer>();
        protected int nextColorIndex = 0;
        boolean isFinalized = false;
        
        void commitTableSize() {
            lut.SetNumberOfTableValues(nextColorIndex);
            lut.Build();
            isFinalized = true;
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
        
        double getColorIndex(Object colorable) {
            if (! (colorIndices.containsKey(colorable)) ) {
                if (isFinalized) 
                    throw new ToonColorsFinalizedException();
                colorIndices.put(colorable, nextColorIndex);
                nextColorIndex ++;
            }
            
            // Add 0.5 because the mapper seems to use floor() to convert 
            // interpolated values to integers
            return colorIndices.get(colorable) + 0.5;
        }
        
        void applyMapper(vtkPolyDataMapper mapper) {
            if (!isFinalized) throw new ToonColorsNotFinalizedException();
            mapper.ScalarVisibilityOn();
            mapper.SetColorModeToMapScalars();
            mapper.SetLookupTable(lut);
            mapper.SetScalarRange(0.0, nextColorIndex);
        }
    }
    
    class ToonColorsNotFinalizedException extends RuntimeException {}
    class ToonColorsFinalizedException extends RuntimeException {}
}
