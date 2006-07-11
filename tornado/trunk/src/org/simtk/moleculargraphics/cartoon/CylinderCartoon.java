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
 * Created on Jun 13, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import vtk.*;
import java.util.*;
import java.awt.Color;
import org.simtk.geometry3d.*;

public abstract class CylinderCartoon extends GlyphCartoon {
    protected double cylinderRadius;
    protected double minLength;
    protected vtkCylinderSource cylinderSource;
    
    // Manage color table - one residue gets one color
    protected Map<Object, Integer> modelColors = new HashMap<Object, Integer>();
    int nextUnusedColorIndex = 0;
    int maxColorIndex = lut.GetNumberOfColors() - 4;
    
    protected CylinderCartoon(double radius, double minLength) {
        this.cylinderRadius = radius;
        this.minLength = minLength;

        // Make a cylinder to use as the basis of all bonds
        cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(5);
        cylinderSource.SetRadius(cylinderRadius);
        cylinderSource.SetHeight(minLength);
        cylinderSource.SetCapping(0);
        // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
        vtkTransform cylinderTransform = new vtkTransform();
        cylinderTransform.Identity();
        cylinderTransform.RotateZ(90);
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        cylinderFilter.SetInput(cylinderSource.GetOutput());
        cylinderFilter.SetTransform(cylinderTransform);
        
        // Use lines as the glyph primitive
        setGlyphSource(cylinderFilter.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleNone();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar
        orientByNormal();

        glyphActor.GetProperty().BackfaceCullingOn();
    }
    
    protected void addCylinder(Vector3D begin, Vector3D end, Color color, Object modelObject) {
        // Vector3D midPoint = begin.plus(end.minus(begin).times(0.5)).v3();
        Vector3D normal = end.minus(begin).unit();
        
        // Use sticks to tile path from begin to end
        int numberOfSticks = (int) Math.ceil(begin.distance(end) / minLength);

        // Initial and final values of substick center position
        Vector3D beginStickCenter = new Vector3DClass( begin.plus(normal.times(minLength * 0.5)) );
        Vector3D endStickCenter = new Vector3DClass( end.minus(normal.times(minLength * 0.5)) );

        Vector3D stickCenterVector = new Vector3DClass( endStickCenter.minus(beginStickCenter) );

        for (int s = 0; s < numberOfSticks; s++) {

            // Parameter alpha goes from zero to one along tiling path
            double alpha = 0.0;
            if (numberOfSticks > 1)
                alpha = s / (numberOfSticks - 1.0);
            else // only one stick
                alpha = 0.5; // place it in the middle
            
            Vector3D stickCenter = new Vector3DClass( beginStickCenter.plus(stickCenterVector.times(alpha)) );
        
            linePoints.InsertNextPoint(stickCenter.getX(), stickCenter.getY(), stickCenter.getZ());
            lineNormals.InsertNextTuple3(normal.getX(), normal.getY(), normal.getZ());

            // TODO - put this color logic into a baser class
            // If necessary insert a new color into the color table
            if (! (modelColors.containsKey(modelObject)) ) {
                int colorScalar = nextUnusedColorIndex;
                lut.SetTableValue(colorScalar, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
                modelColors.put(modelObject, colorScalar);

                if (nextUnusedColorIndex < maxColorIndex)
                        nextUnusedColorIndex ++;
                else {
                    // TODO - color table overflow
                }
            }
            
            int colorScalar = modelColors.get(modelObject);

            Collection<Object> modelObjects = new Vector<Object>();
            modelObjects.add(modelObject);

            glyphColors.add(modelObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
            lineScalars.InsertNextValue(colorScalar);
            
        }

    }
}
