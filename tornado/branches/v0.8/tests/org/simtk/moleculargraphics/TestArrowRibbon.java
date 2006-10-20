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
 * Created on Jul 11, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import junit.framework.TestCase;
import vtk.*;
import javax.swing.JFrame;
import org.simtk.moleculargraphics.cartoon.vtkArrowWidthFilter;

public class TestArrowRibbon extends TestCase {
    static {org.simtk.moleculargraphics.VTKLibraries.load();}
    
    public static void main(String[] args) {
        // junit.textui.TestRunner.run(TestArrowRibbon.class);
        testArrowRibbon();
    }

    static public void testArrowRibbon() {
        double ribbonWidth = 1.0;
        double headWidth = ribbonWidth * 1.60;
        double headLength = headWidth * 0.95;

        double ribbonThickness = 0.25;
        
        double tipWidth = 0.10; // Sorry, zero doesn't work
        double neckLength = 0.01; // Sorry, zero doesn't work

        // Data structures for points, normals, and colors
        vtkPoints linePoints = new vtkPoints();
        vtkFloatArray lineNormals = new vtkFloatArray();
        lineNormals.SetNumberOfComponents(3);
        vtkFloatArray lineWidths = new vtkFloatArray();
        lineWidths.SetNumberOfComponents(1);
       
        double[][] points = {
                {-5, 0, 0},      
                { 0, 0, 0},
                { 5, 0, 0}
        };

        double[][] normals = {
                {-0.44, 0.90, 0.0},      
                {    0, 1.00, 0.0},
                { 0.44, 0.90, 0.0}
        };

        double[] finalPoint = points[points.length - 1]; // final point
        boolean isInHead = false;
        
        double[] previousPoint = null;
        double[] previousNormal = null;
        double previousDistanceToFinal = Double.NaN;
        
        for (int i = 0; i < points.length; i++) {
            double[] currentPoint = points[i];
            double[] currentNormal = normals[i];
            double width = ribbonWidth; // default outside of arrow head region

            double[] v = { // direction from here to final vector
                    finalPoint[0] - currentPoint[0],
                    finalPoint[1] - currentPoint[1],
                    finalPoint[2] - currentPoint[2]};
            double distanceToFinal = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);

            // If necessary, insert neck region
            if ((!isInHead) && (distanceToFinal < headLength)) {
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
                    
                    double[] widths = {ribbonWidth, headWidth};
                    double[] alphas = {neckAlpha1, neckAlpha2};
                    for (int j = 0; j < 2; j++) {
                        double a2 = alphas[j];
                        double a1 = 1.0 - a2;
                        double w = widths[j];

                        double[] p = { // interpolated point
                                a1 * previousPoint[0] + a2 * currentPoint[0],
                                a1 * previousPoint[1] + a2 * currentPoint[1],
                                a1 * previousPoint[2] + a2 * currentPoint[2],
                        };
                        
                        double[] n = { // interpolated normal
                                a1 * previousNormal[0] + a2 * currentNormal[0],
                                a1 * previousNormal[1] + a2 * currentNormal[1],
                                a1 * previousNormal[2] + a2 * currentNormal[2],
                        };
                        // Normalize normal vector to unit length
                        double nScale = 1.0/Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);
                        n[0] *= nScale;
                        n[1] *= nScale;
                        n[2] *= nScale;
                        
                        linePoints.InsertNextPoint(p);
                        lineNormals.InsertNextTuple3(n[0], n[1], n[2]);
                        lineWidths.InsertNextValue(0.5 * w);
                    }
                }
            }
            
            // Adjust width for points in head region
            if (isInHead) {
                double alpha = distanceToFinal / headLength;
                width = tipWidth + alpha * (headWidth - tipWidth);
            }
            
            linePoints.InsertNextPoint(currentPoint);
            lineNormals.InsertNextTuple3(currentNormal[0], currentNormal[1], currentNormal[2]);
            lineWidths.InsertNextValue(0.5 * width);
            
            previousPoint = currentPoint;
            previousNormal = currentNormal;
            previousDistanceToFinal = distanceToFinal;
        }
        
        // Connect points into a line
        int numberOfPoints = linePoints.GetNumberOfPoints();
        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i ++)
            lineCells.InsertCellPoint(i);
       
        // Create a proper vtkPolyData object from the line
        vtkPolyData lineData = new vtkPolyData();
        lineData.SetPoints(linePoints);
        lineData.GetPointData().SetNormals(lineNormals);
        lineData.GetPointData().SetScalars(lineWidths);
        // Coloring by adding scalars causes crash with extruded shape
        lineData.SetLines(lineCells);       

        vtkArrowWidthFilter arrowFilter = new vtkArrowWidthFilter();
        arrowFilter.SetWidth(1.0);
        arrowFilter.SetInput(lineData);
        
        // Widen the line into a ribbon
        vtkRibbonFilter ribbonFilter = new vtkRibbonFilter();
        // ribbonFilter.SetWidth(0.4);
        ribbonFilter.SetAngle(0); // Perpendicular to normals
        ribbonFilter.SetInput(arrowFilter.GetOutput()); // skip spline
        
        // Vary width
        // The completely retarded API for vtkRibbonFilter requires
        // that Width be set to the actual minimum width, and that
        // WidthFactor be set to the actual maximum width.
        
        ribbonFilter.SetWidth(arrowFilter.GetRibbonFilterWidth()); // minimum width
        ribbonFilter.SetWidthFactor(arrowFilter.GetRibbonFilterWidthFactor()); // maximum / minimum
        ribbonFilter.VaryWidthOn();

        vtkLinearExtrusionFilter extrusionFilter = new vtkLinearExtrusionFilter();
        extrusionFilter.SetCapping(1);
        extrusionFilter.SetExtrusionTypeToNormalExtrusion();
        extrusionFilter.SetScaleFactor(ribbonThickness);
        extrusionFilter.SetInput(ribbonFilter.GetOutput());
        
        vtkPolyDataMapper mapper = new vtkPolyDataMapper();       
        mapper.SetInput(extrusionFilter.GetOutput());

        vtkActor actor = new vtkActor();
        actor.SetMapper(mapper);

        vtkPanel panel = new vtkPanel();
        panel.GetRenderer().SetBackground(1,1,1); // white backgrounds rule
        panel.GetRenderer().AddViewProp(actor);        
       
        JFrame frame = new JFrame();
        frame.setLocationRelativeTo(null);        
        frame.getContentPane().add(panel);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
