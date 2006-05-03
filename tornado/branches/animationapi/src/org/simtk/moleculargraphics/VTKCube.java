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
 * Created on Apr 6, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import vtk.*;
import java.awt.*;
import javax.swing.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Create window with a cube made of cylinders
  * for testing graphics effects
 */
public class VTKCube extends JFrame {
    vtkPanel vtkPanel;
    JFrame frame = this;
    
    // In the static contructor we load in the native code.
    // The libraries must be in your path to work.
    static { 
      System.loadLibrary("vtkCommonJava"); 
      System.loadLibrary("vtkFilteringJava"); 
      System.loadLibrary("vtkIOJava"); 
      System.loadLibrary("vtkImagingJava"); 
      System.loadLibrary("vtkGraphicsJava"); 
      System.loadLibrary("vtkRenderingJava"); 
    }

    public VTKCube() {

        vtkPanel = new vtkPanel();
        vtkPanel.GetRenderWindow().SetSize(300,300);

        frame.getContentPane().add(vtkPanel);

        vtkConeSource cone = new vtkConeSource();
        cone.SetHeight( 3.0 );
        cone.SetRadius( 1.0 );
        cone.SetResolution( 10 );
        
        vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
        coneMapper.SetInput( cone.GetOutput() );

        vtkActor coneActor = new vtkActor();
        coneActor.SetMapper( coneMapper );

        vtkPanel.GetRenderer().AddActor(coneActor);
        
        frame.show();
    }
    
    public static void main(String[] args) {
        VTKCube cube = new VTKCube();
        cube.frame.show();
    }
}
