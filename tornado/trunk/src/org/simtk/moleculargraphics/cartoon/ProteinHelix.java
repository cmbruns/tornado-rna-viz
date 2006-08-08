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
package org.simtk.moleculargraphics.cartoon;

import java.util.List;
import java.util.Vector;

import org.simtk.geometry3d.Vector3D;
import org.simtk.moleculargraphics.Spline3D;
import org.simtk.molecularstructure.*;

import vtk.vtkPolyData;

public class ProteinHelix extends ProteinRibbonSegment {
    public ProteinHelix(
            List<Residue> residues,
            double ribbonThickness, 
            double helixWidth,
            int startIndex,
            Spline3D positionSpline,
            Spline3D normalSpline)
    throws NoCartoonCreatedException
    {
        // Something is screwy with back vs. front faces of ribbon
        // actor.GetProperty().BackfaceCullingOff();
        // actor.GetProperty().FrontfaceCullingOn();

        createAlphaHelix(
                residues, 
                ribbonThickness, 
                helixWidth,
                startIndex,
                positionSpline,
                normalSpline);

    }

    protected void createTaperedRibbon(
            List<Vector3D> positions, 
            List<Vector3D> normals, 
            List<Chemical> objects,
            double ribbonThickness,
            double ribbonWidth) {
        // Insert scalars and extra points to make a nice arrow head
        vtkTaperedWidthFilter taperedFilter = new vtkTaperedWidthFilter();
        taperedFilter.SetWidth(ribbonWidth);
        taperedFilter.SetThickness(ribbonThickness);
        taperedFilter.SetInput(createPolyLine(positions, normals, objects));
        // TaperedFilter.SetInput(lineData);

        createExtrudedRibbon(
                (vtkPolyData) taperedFilter.GetOutput(), 
                taperedFilter.GetThickness(),
                taperedFilter.GetRibbonFilterWidth(),
                taperedFilter.GetRibbonFilterWidthFactor()
                );
    }
    
    protected void createAlphaHelix(
            List<Residue> residues, 
            double ribbonThickness, 
            double strandWidth,
            int startIndex,
            Spline3D positionSpline,
            Spline3D normalSpline) 
    throws NoCartoonCreatedException {
        List<Vector3D> positions = new Vector<Vector3D>();
        List<Vector3D> normals = new Vector<Vector3D>();
        List<Chemical> aminoAcids = new Vector<Chemical>();

        double extraBit = 0.05; // Make structures overlap a bit
        double minT = startIndex - 0.5 - extraBit;
        double maxT = startIndex + residues.size() - 0.5 + extraBit;

        double strandResolution = 0.25; // In units of residues
        int numPoints = (int)(1.0 + (maxT - minT) / strandResolution);
        double increment = (maxT - minT) / (numPoints - 1.0);
        
        for (int i = 0; i < numPoints; ++i) {
            double t = minT + i * increment;
            Vector3D normal = normalSpline.evaluate(t).unit();
            Vector3D position = positionSpline.evaluate(t);
            
            normals.add(normal);
            positions.add(position);

            int residueIndex = (int)Math.round(t - minT);
            if (residueIndex < 0) residueIndex = 0;
            if (residueIndex >= residues.size()) residueIndex = residues.size() - 1;
            aminoAcids.add(residues.get(residueIndex));
        }

        if (positions.size() < 1) 
            throw new NoCartoonCreatedException("No positions found for Secondary Structure");

        createTaperedRibbon(positions, normals, aminoAcids, ribbonThickness, strandWidth);        
    }

    protected void oldCreateAlphaHelix(
            List<Residue> residues,
            double ribbonThickness,
            double helixWidth)
    throws NoCartoonCreatedException {
        List<Vector3D> positions = new Vector<Vector3D>();
        List<Vector3D> normals = new Vector<Vector3D>();
        List<Chemical> aminoAcids = new Vector<Chemical>();
        
        int resCount = 0;
        for (Residue residue : residues) {
            resCount ++;
            try {
                Vector3D ca = residue.getAtom("CA").getCoordinates();

                int endFlag = 0;
                if (resCount == 1) endFlag = -1;
                else if (resCount == residues.size()) endFlag = 1;
                Vector3D norm = hBondNormal(residue, endFlag);
                
                // Extend halfway to previous residue
                if (resCount == 1) {
                    try {
                        Vector3D prev = residue.getPreviousResidue().getAtom("CA").getCoordinates();
                        Vector3D p = ca.plus(prev).times(0.50);

                        positions.add(p);
                        normals.add(norm);
                        aminoAcids.add(residue);
                    } catch (NullPointerException exc) {}
                    
                }
                
                Vector3D pos = ca;

                positions.add(pos);
                normals.add(norm);
                aminoAcids.add(residue);
                
                // Extend halfway to following residue
                if (resCount == residues.size()) {
                    try {
                        Vector3D next = residue.getNextResidue().getAtom("CA").getCoordinates();
                        Vector3D p = ca.plus(next).times(0.50);

                        positions.add(p);
                        normals.add(norm);
                        aminoAcids.add(residue);
                    } catch (NullPointerException exc) {}
                    
                }
                
            } catch (NullPointerException exc) {continue;}

        }
        
        if (positions.size() < 1) 
            throw new NoCartoonCreatedException("No positions found for Secondary Structure");

        createTaperedRibbon(positions, normals, aminoAcids, ribbonThickness, helixWidth);
    }

}
