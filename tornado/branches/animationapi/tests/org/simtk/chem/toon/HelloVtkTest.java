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

public class HelloVtkTest {

    public static void main(String[] args) {
        VTKLibraries.load();
        
        // Create graphics window
        JFrame frame = new JFrame("Test VTK Cone");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vtkPanel canvas = new vtkPanel();
        frame.getContentPane().add(canvas);

        vtkConeSource cone = new vtkConeSource();
        cone.SetHeight( 3.0 );
        cone.SetRadius( 1.0 );
        cone.SetResolution( 10 );

        vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
        coneMapper.SetInputConnection(cone.GetOutputPort());
        
        vtkActor coneActor = new vtkActor();
        coneActor.SetMapper(coneMapper);
        
        vtkRenderer ren1 = canvas.GetRenderer();
        ren1.AddActor(coneActor);        

        frame.pack();
        frame.setVisible(true);
    }

}
