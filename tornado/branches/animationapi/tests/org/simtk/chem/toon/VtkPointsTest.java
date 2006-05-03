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
 * Created on Apr 20, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.toon;

import javax.swing.*;
import org.simtk.moleculargraphics.*;
import vtk.*;

public class VtkPointsTest {

    public static void main(String[] args) {
        VTKLibraries.load();
        
        // Create graphics window
        JFrame frame = new JFrame("Test VTK Point");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vtkPanel canvas = new vtkPanel();
        frame.getContentPane().add(canvas);

        // Prototype shape is a line
        vtkPointSource point = new vtkPointSource();
        point.SetNumberOfPoints(1);
        point.SetRadius(0);

        // vtk data pipeline objects
        vtkPolyData polyData = new vtkPolyData();
        vtkPoints locations = new vtkPoints();    
        vtkFloatArray normals = new vtkFloatArray(); // bond directions/lengths
        vtkFloatArray colors = new vtkFloatArray();
        vtkGlyph3D glyph3D = new vtkGlyph3D();
        vtkPolyDataMapper polyDataMapper = new vtkPolyDataMapper();
        vtkActor actor = new vtkActor();

        // Create two points to locate the points at
        locations.InsertNextPoint(0, 0, 0);
        locations.InsertNextPoint(3, 0, 0);

        normals.SetNumberOfComponents(3);
        normals.InsertNextTuple3(1, 0, 0);
        normals.InsertNextTuple3(1, 0, 0);

        colors.SetNumberOfComponents(1);
        colors.InsertNextTuple1(1);
        colors.InsertNextTuple1(1);

        polyData.SetPoints(locations);

        polyData.GetPointData().SetNormals(normals);

        // Glyph3D convolutes the point with the points
        glyph3D.SetInput(polyData);
        glyph3D.SetSource(point.GetOutput());
        
        polyDataMapper.SetInput(glyph3D.GetOutput());

        actor.SetMapper(polyDataMapper);
        
        vtkRenderer ren1 = canvas.GetRenderer();
        ren1.AddActor(actor);        

        frame.pack();
        frame.setVisible(true);
    }

}
