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

import org.simtk.molecularstructure.protein.*;
import org.simtk.molecularstructure.*;
import org.simtk.geometry3d.*;
import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Base Class for protein Alpha Helix, Beta Strand, and Coil
 */
public class ProteinRibbonSegment extends ActorCartoonClass {
    // protected ColorScheme initialColorScheme = new ConstantColor(Color.pink);
    protected double lengthResolution = 0.5; // Affects sharpness of color boundaries
    
    public ProteinRibbonSegment() {
    }

    /**
     * 
     * @param residue
     * @param endFlag -1 for first residue, +1 for final residue, zero for others
     * @return
     */
    protected static Vector3D hBondNormal(Residue residue, int endFlag) {
        Vector3D ca,c,n,o;
        try {
            ca = residue.getAtom("CA").getCoordinates();
            c =  residue.getAtom("C").getCoordinates();
            n =  residue.getAtom("N").getCoordinates();
            o =  residue.getAtom("O").getCoordinates();
        }
        catch (NullPointerException exc) {return null;}

        Vector3D chainDirection = chainDirection(residue);
        
        // Compute main chain hydrogen bond directions
        Vector3D co = o.minus(c).unit();
        Vector3D nh = null;
        try {
            Vector3D prevC = ((Residue)residue.getPreviousResidue()).getAtom("C").getCoordinates();
            nh = ca.minus(n).cross(prevC.minus(n)).unit();
            nh = nh.cross(chainDirection).unit();
            // point N-H bond in same direction as C-O bond, so they agree
            if (nh.dot(co) < 0) nh = nh.times(-1.0);
        } 
        catch (NullPointerException exc) {}
        catch (ClassCastException exc) {}
        Vector3D hBondDirection = co;
        if (nh != null) hBondDirection = co.plus(nh).unit();
        // Ignore the NH component of the first residue
        if (endFlag < 0) hBondDirection = co;
        // Ignore the CO component of the final residue
        else if ( (endFlag > 0) && (nh != null) ) hBondDirection = nh;
        
        // Set normal perpendicular to chain and hydrogen bond
        Vector3D norm = hBondDirection.cross(chainDirection).unit();
        
        return norm;
    }
    
    protected static Vector3D chainDirection(Residue residue) {
        // If there are residues before and after, get chain direction from them
        try {
            Vector3D ca = residue.getAtom("CA").getCoordinates();
            Vector3D prev = ((Residue)residue.getPreviousResidue()).getAtom("CA").getCoordinates();
            Vector3D chainDirection = ca.minus(prev).unit();
            return chainDirection;
        } 
        catch (NullPointerException exc) {}
        catch (ClassCastException exc) {}

        try {
            Vector3D c =  residue.getAtom("C").getCoordinates();
            Vector3D n =  residue.getAtom("N").getCoordinates();
            Vector3D chainDirection = c.minus(n).unit();
            return chainDirection;
        } catch (NullPointerException exc) {}            
        
        return null;
    }

    protected vtkPolyData createPolyLine(List<Vector3D> positions, List<Vector3D> normals, List<Object> objects) {
        // Data structures for points, normals, and colors
        vtkPoints linePoints = new vtkPoints();
        vtkFloatArray lineNormals = new vtkFloatArray();
        lineNormals.SetNumberOfComponents(3);
        lineNormals.SetName("normals");
        
        vtkFloatArray colorScalars = new vtkFloatArray();
        colorScalars.SetNumberOfComponents(1);
        colorScalars.SetName("colors");
       
        vtkFloatArray dummyScalars = new vtkFloatArray();
        dummyScalars.SetNumberOfComponents(1);
        dummyScalars.SetName("nothing");
       
        for (int i = 0; i < positions.size(); i++) {
            Vector3D currentPoint = positions.get(i);
            Vector3D currentNormal = normals.get(i);
            linePoints.InsertNextPoint(currentPoint.x(), currentPoint.y(), currentPoint.z());
            lineNormals.InsertNextTuple3(currentNormal.x(), currentNormal.y(), currentNormal.z());

            // Add 0.5 because the mapper seems to use floor() to convert 
            // interpolated values to integers
            colorScalars.InsertNextValue(toonColors.getColorIndex(objects.get(i)));

            dummyScalars.InsertNextValue(1.0);
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
        lineData.SetLines(lineCells);
        lineData.GetPointData().SetScalars(dummyScalars);
        lineData.GetPointData().AddArray(colorScalars);

        vtkSplineFilter splineFilter = new vtkSplineFilter();
        splineFilter.SetSubdivideToLength();
        splineFilter.SetLength(lengthResolution);
        splineFilter.SetInput(lineData);
        
        return splineFilter.GetOutput();
    }
    
    protected void createExtrudedRibbon(
            vtkPolyData polyLine, 
            double thickness,
            double ribbonFilterWidth,
            double ribbonFilterWidthFactor
            ) {
        
        // arrowFilter.Update();        
        // System.out.println("Arrow data = " + arrowFilter.GetOutput());
        
        // Shift positions so that extruded ribbon will be centered
        vtkPreExtrusionCenteringFilter centeringFilter = new vtkPreExtrusionCenteringFilter();
        centeringFilter.SetThickness(thickness);
        centeringFilter.SetInput(polyLine);
        
        // Widen the line into a ribbon
        vtkRibbonFilter ribbonFilter = new vtkRibbonFilter();
        ribbonFilter.SetAngle(0); // Perpendicular to normals
        ribbonFilter.SetInput(centeringFilter.GetOutput());
        
        ribbonFilter.SetWidth(ribbonFilterWidth); // minimum width
        ribbonFilter.SetWidthFactor(ribbonFilterWidthFactor); // maximum / minimum
        ribbonFilter.VaryWidthOn();

        vtkSetScalarsFilter setScalarsFilter = new vtkSetScalarsFilter();
        setScalarsFilter.SetScalars("colors");
        // setScalarsFilter.SetInput(normalsFilter.GetOutput());
        setScalarsFilter.SetInput(ribbonFilter.GetOutput());

        vtkLinearExtrusionFilter extrusionFilter = new vtkLinearExtrusionFilter();
        extrusionFilter.SetCapping(1);
        extrusionFilter.SetExtrusionTypeToNormalExtrusion();
        extrusionFilter.SetScaleFactor(thickness);
        extrusionFilter.SetInput(setScalarsFilter.GetOutput());
        
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetFeatureAngle(80);
        normalsFilter.SetInput(extrusionFilter.GetOutput());
        
        finishVtkPipeline(normalsFilter.GetOutput());
    }
    
    protected void finishVtkPipeline(vtkPolyData polyData) {
        // vtkPolyDataMapper mapper = mapper;
        mapper.SetScalarModeToUsePointData();
        mapper.ColorByArrayComponent("colors", 0);
        // mapper.SetInput((vtkPolyData)setScalarsFilter.GetOutput());
        mapper.SetInput(polyData);

        actor.SetMapper(mapper);
        
        isPopulated = true;
    }
}
