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
 * Created on Jul 12, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import org.simtk.geometry3d.*;
import vtk.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Set scalar values on a polyline to make an arrow with vtkRibbonFilter
 */
public class vtkTaperedWidthFilter extends vtkProgrammableFilter {
    protected double ribbonWidth = 2.0;
    protected double tipWidth = 0.05;
    protected double thickness = ribbonWidth * 0.25;
    protected double taperLength = ribbonWidth;
    
    public vtkTaperedWidthFilter() {
        SetExecuteMethod(this, "Execute");
        updateFromWidth();
    }

    public void SetWidth(double width) {
        this.ribbonWidth = width;
        updateFromWidth();
    }
    
    public double GetRibbonFilterWidth() {return 0.5 * tipWidth;}

    public double GetRibbonFilterWidthFactor() {return ribbonWidth / tipWidth;}
    
    public void SetThickness(double t) {
        thickness = t;
        tipWidth = thickness; // Some users might not want this...
    }
    
    public double GetThickness() {return thickness;}
    
    protected void updateFromWidth() {
        taperLength = ribbonWidth;
    }
    
    public void Execute() {
        vtkPolyData input = GetPolyDataInput();
        
        vtkFloatArray newScalars = new vtkFloatArray();
        newScalars.SetNumberOfComponents(1);
        newScalars.SetName("taper width");
        
        int numPts = input.GetNumberOfPoints();
        Vector3D finalPoint = new Vector3DClass(input.GetPoint(numPts - 1));
        Vector3D initialPoint = new Vector3DClass(input.GetPoint(0));
        for (int i = 0; i < numPts; ++i) {
            double width = ribbonWidth; // start with default

            Vector3D currentPoint = new Vector3DClass(input.GetPoint(i));

            double distanceToFinal = finalPoint.minus(currentPoint).length();
            double distanceToInitial = initialPoint.minus(currentPoint).length();

            // If necessary, insert neck region
            if (distanceToFinal < taperLength) {            
                double alpha = distanceToFinal / taperLength;
                width = tipWidth + alpha * (ribbonWidth - tipWidth);
            }
            else if (distanceToInitial < taperLength) {            
                double alpha = distanceToInitial / taperLength;
                width = tipWidth + alpha * (ribbonWidth - tipWidth);
            }
            
            newScalars.InsertNextValue(0.5 * width);

       }


        vtkPolyData output = GetPolyDataOutput();
        output.ShallowCopy(input);

        output.GetPointData().AddArray(newScalars);
        output.GetPointData().SetActiveScalars("taper width");
    }
}
