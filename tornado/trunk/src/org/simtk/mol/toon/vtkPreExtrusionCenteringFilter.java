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
public class vtkPreExtrusionCenteringFilter extends vtkProgrammableFilter {
    protected double thickness = 1.0;
    
    public vtkPreExtrusionCenteringFilter() {
        SetExecuteMethod(this, "Execute");
    }

    public void SetThickness(double thickness) {
        this.thickness = thickness;
    }
    
    public double GetThickness() {return thickness;}
    
    public void Execute() {
        vtkPolyData input = GetPolyDataInput();

        vtkPolyData output = GetPolyDataOutput();
        output.DeepCopy(input);

        int numPoints = input.GetNumberOfPoints();
        for (int i = 0; i < numPoints; ++i) {
            Vector3D point = new Vector3DClass(input.GetPoint(i));
            Vector3D normal = new Vector3DClass(input.GetPointData().GetNormals().GetTuple3(i)).unit();
            Vector3D newPoint = point.minus(normal.times(0.5 * thickness));

            output.GetPoints().SetPoint(i, newPoint.toArray());
        }
    }
}
