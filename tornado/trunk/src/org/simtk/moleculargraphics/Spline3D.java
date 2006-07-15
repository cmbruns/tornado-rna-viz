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
 * Created on Jul 14, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import org.simtk.geometry3d.*;
import vtk.*;

public class Spline3D {
    protected vtkCardinalSpline splineX = new vtkCardinalSpline();
    protected vtkCardinalSpline splineY = new vtkCardinalSpline();
    protected vtkCardinalSpline splineZ = new vtkCardinalSpline();
    
    public void addPoint(double t, Vector3D v) {
        splineX.AddPoint(t, v.x());
        splineY.AddPoint(t, v.y());
        splineZ.AddPoint(t, v.z());
    }
    
    public Vector3D evaluate(double t) {
        return new Vector3DClass(
                splineX.Evaluate(t),
                splineY.Evaluate(t),
                splineZ.Evaluate(t));
    }
}
