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

import org.simtk.geometry3d.*;
import java.awt.*;

import vtk.vtkSphereSource;

public class VtkSphereGlyphPipeline extends VtkGlyphPipeline {
    private vtkSphereSource sphereSource = new vtkSphereSource();

    VtkSphereGlyphPipeline(double radius, int resolution) {
        super();

        sphereSource.SetRadius(radius);
        sphereSource.SetThetaResolution((int)(1.5 * resolution));
        sphereSource.SetPhiResolution(resolution);
        
        // Use lines as the glyph primitive
        setGlyphSource(sphereSource.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleByNormal(); // Take sphere size from glyph normal
        colorByScalar(); // Take color from glyph scalar
    }

    public void placeToon(MolToon toon, Vector3D v, double radius, Color color) {
        super.placeToon(toon, v, new Vector3DClass(radius, 0, 0), color);
    }    
}
