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
package org.simtk.moleculargraphics.cartoon;

import vtk.*;
import org.simtk.geometry3d.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Move points along normal direction
 */
public class vtkGrowPointsFilter extends vtkProgrammableFilter {
    protected double distance = 1.0;
    
    public vtkGrowPointsFilter() {
        SetExecuteMethod(this, "Execute");
    }

    public void SetDistance(double d) {
        this.distance = d;
    }
    
    public void Execute() {
        vtkPolyData input = GetPolyDataInput();

        vtkPolyData output = GetPolyDataOutput();
        output.DeepCopy(input);
        
        vtkPoints points = output.GetPoints();
        vtkDataArray normals = output.GetPointData().GetNormals();
        if (normals == null) return;
        for (int i = 0; i < normals.GetNumberOfTuples(); ++i) {
            Vector3D point = new Vector3DClass(points.GetPoint(i));
            Vector3D normal = new Vector3DClass(normals.GetTuple3(i));
            Vector3D newPoint = point.plus(normal.times(distance));
            points.SetPoint(i, newPoint.toArray());
        }
    }
}
