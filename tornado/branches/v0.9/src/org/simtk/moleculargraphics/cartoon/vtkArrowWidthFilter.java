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

import org.simtk.geometry3d.*;
import vtk.*;
import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Set scalar values on a polyline to make an arrow with vtkRibbonFilter
 */
public class vtkArrowWidthFilter extends vtkProgrammableFilter {
    protected double width = 2.0;
    protected double neckLength = 0.01;
    protected double tipWidth = 0.05;
    protected double thickness = width * 0.25;
    protected double headWidth = width * 1.60;
    protected double headLength = 0.95 * headWidth;
    
    public vtkArrowWidthFilter() {
        SetExecuteMethod(this, "Execute");
        updateFromWidth();
    }

    public void SetWidth(double width) {
        this.width = width;
        updateFromWidth();
    }
    
    public double GetRibbonFilterWidth() {return 0.5 * tipWidth;}

    public double GetRibbonFilterWidthFactor() {return headWidth / tipWidth;}
    
    public void SetThickness(double t) {
        thickness = t;
        tipWidth = thickness; // Some users might not want this...
    }
    
    public double GetThickness() {return thickness;}
    
    protected void updateFromWidth() {
        headWidth = width * 1.60;
        headLength = 0.95 * headWidth;        
    }
    
    public void Execute() {
        vtkPolyData input = GetPolyDataInput();
        
        vtkPoints newPoints = new vtkPoints();
        
        vtkFloatArray newScalars = new vtkFloatArray();
        newScalars.SetNumberOfComponents(1);
        newScalars.SetName("arrow width");
        
        vtkFloatArray newNormals = new vtkFloatArray();
        newNormals.SetNumberOfComponents(3);
        
        Vector3D previousPoint = null;
        Vector3D previousNormal = null;
        double previousDistanceToFinal = Double.NaN;
        boolean isInHead = true;

        // Copy all Arrays from input
        int numArrays = input.GetPointData().GetNumberOfArrays();
        List<vtkDataArray> outArrays = new Vector<vtkDataArray>();
        for (int a = 0; a < numArrays; ++a) {
        // for (int i = 0; i < numArrays; i++) {
            vtkDataArray inArray = input.GetPointData().GetArray(a);

            vtkDataArray outArray = inArray.CreateDataArray(inArray.GetDataType());
            outArray.SetName(inArray.GetName());
            outArray.SetNumberOfComponents(inArray.GetNumberOfComponents());
            
            outArrays.add(outArray);
        }

        int numPts = input.GetNumberOfPoints();
        
        if (numPts < 1) return;
        
        Vector3D finalPoint = new Vector3DClass(input.GetPoint(numPts - 1));
        
        // We cannot rely upon distance from the end to determine what is in 
        // the head region, because sometimes an earlier part of the path might
        // glance close to the end.
        // So we preprocess final points to establish the beginning of the head region
        int firstHeadIndex = 0;
        for (int i = numPts - 1; i >= 0; --i) {
            Vector3D currentPoint = new Vector3DClass(input.GetPoint(i));            
            double distanceToFinal = finalPoint.minus(currentPoint).length();
            if (distanceToFinal > headLength) {
                firstHeadIndex = i + 1;
                isInHead = false;
                break;
            }
        }
        
        for (int i = 0; i < numPts; ++i) {
            
            double currentWidth = width;

            Vector3D currentPoint = new Vector3DClass(input.GetPoint(i));
            Vector3D currentNormal = new Vector3DClass(input.GetPointData().GetNormals().GetTuple3(i));

            double distanceToFinal = finalPoint.minus(currentPoint).length();

            // If necessary, insert neck region
            if (i == firstHeadIndex) {
                isInHead = true;

                // Insert neck region
                // Linear interpolation between current and previous points
                if (previousPoint != null) {
                    double l1 = (previousDistanceToFinal - headLength);
                    double l2 = (headLength - distanceToFinal);

                    double neckAlpha1 = l1 / (l1 + l2);
                    double neckIncrement = neckLength / (l1 + l2);
                    double neckAlpha2 = neckAlpha1 + neckIncrement;

                    if (neckAlpha2 >= 1.0) {
                        neckAlpha2 = 1.0 - neckIncrement;
                        neckAlpha1 = neckAlpha2 - neckIncrement;
                    }
                    
                    double[] widths = {width, headWidth};
                    double[] alphas = {neckAlpha1, neckAlpha2};
                    for (int j = 0; j < 2; j++) {
                        double a2 = alphas[j];
                        double a1 = 1.0 - a2;
                        double w = widths[j];

                        Vector3D p = previousPoint.times(a1).plus(currentPoint.times(a2));
                        Vector3D n = previousNormal.times(a1).plus(currentNormal.times(a2)).unit();
                        
                        newPoints.InsertNextPoint(p.x(), p.y(), p.z());
                        newNormals.InsertNextTuple3(n.x(), n.y(), n.z());
                        newScalars.InsertNextValue(0.5 * w);

                        // Copy all arrays, duplicating previous values for neck region
                        for (int a = 0; a < outArrays.size(); ++a) {
                            vtkDataArray outArray = outArrays.get(a);
                            vtkDataArray inArray = input.GetPointData().GetArray(a);
                            
                            int numComps = outArray.GetNumberOfComponents();
                            switch (numComps) {
                                case 1:
                                    double datum1 = inArray.GetTuple1(i);
                                    outArray.InsertNextTuple1(datum1);
                                    break;
                                case 2:
                                    double[] datum2 = inArray.GetTuple2(i);
                                    outArray.InsertNextTuple2(datum2[0], datum2[1]);
                                    break;
                                case 3:
                                    double[] datum3 = inArray.GetTuple3(i);
                                    outArray.InsertNextTuple3(datum3[0], datum3[1], datum3[2]);
                                    break;
                                case 4:
                                    double[] datum4 = inArray.GetTuple4(i);
                                    outArray.InsertNextTuple4(datum4[0], datum4[1], datum4[2], datum4[3]);
                                    break;
                                case 9:
                                    double[] datum9 = inArray.GetTuple9(i);
                                    outArray.InsertNextTuple9(
                                            datum9[0], datum9[1], datum9[2], 
                                            datum9[3], datum9[4], datum9[5], 
                                            datum9[6], datum9[7], datum9[8]);
                                    break;
                            }
                        }

                    }
                }
            }
            
            // Adjust width for points in head region
            if (isInHead) {
                double alpha = distanceToFinal / headLength;
                currentWidth = tipWidth + alpha * (headWidth - tipWidth);
            }
            
            newPoints.InsertNextPoint(currentPoint.x(), currentPoint.y(), currentPoint.z());
            newNormals.InsertNextTuple3(currentNormal.x(), currentNormal.y(), currentNormal.z());
            newScalars.InsertNextValue(0.5 * currentWidth);
            
            // Copy all arrays, duplicating previous values for neck region
            for (int a = 0; a < outArrays.size(); ++a) {
                vtkDataArray outArray = outArrays.get(a);
                vtkDataArray inArray = input.GetPointData().GetArray(a);
                
                int numComps = outArray.GetNumberOfComponents();
                switch (numComps) {
                    case 1:
                        double datum1 = inArray.GetTuple1(i);
                        outArray.InsertNextTuple1(datum1);
                        break;
                    case 2:
                        double[] datum2 = inArray.GetTuple2(i);
                        outArray.InsertNextTuple2(datum2[0], datum2[1]);
                        break;
                    case 3:
                        double[] datum3 = inArray.GetTuple3(i);
                        outArray.InsertNextTuple3(datum3[0], datum3[1], datum3[2]);
                        break;
                    case 4:
                        double[] datum4 = inArray.GetTuple4(i);
                        outArray.InsertNextTuple4(datum4[0], datum4[1], datum4[2], datum4[3]);
                        break;
                    case 9:
                        double[] datum9 = inArray.GetTuple9(i);
                        outArray.InsertNextTuple9(
                                datum9[0], datum9[1], datum9[2], 
                                datum9[3], datum9[4], datum9[5], 
                                datum9[6], datum9[7], datum9[8]);
                        break;
                }
            }

            previousPoint = currentPoint;
            previousNormal = currentNormal;
            previousDistanceToFinal = distanceToFinal;
        }

        // Connect points into a line
        int numberOfPoints = newPoints.GetNumberOfPoints();
        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i ++)
            lineCells.InsertCellPoint(i);

        vtkPolyData output = GetPolyDataOutput();
        output.CopyStructure(input);

        output.SetPoints(newPoints);
        output.GetPointData().AddArray(newScalars);
        output.GetPointData().SetNormals(newNormals); // TODO -- Can we do without this?
        
        // Put some version of all input arrays into the output
        for (int a = 0; a < outArrays.size(); ++a) {
            vtkDataArray outArray = outArrays.get(a);
            output.GetPointData().AddArray(outArray);
        }

        output.GetPointData().SetActiveScalars("arrow width");
        
        output.SetLines(lineCells);
    }
}
