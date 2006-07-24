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
import org.simtk.geometry3d.*;
import org.simtk.moleculargraphics.Spline3D;
import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * A single beta strand in a protein
 */
public class BetaStrandRibbon extends ProteinRibbonSegment {
    // protected ColorScheme initialColorScheme = new ConstantColor(Color.pink);
    protected double lengthResolution = 0.5; // Affects sharpness of color boundaries
    
    public BetaStrandRibbon(
            List<AminoAcid> residues, 
            double ribbonThickness, 
            double strandWidth,
            int startIndex,
            Spline3D positionSpline,
            Spline3D normalSpline) 
    throws NoCartoonCreatedException 
    {
        // Something is screwy with back vs. front faces of ribbon
        actor.GetProperty().BackfaceCullingOff();
        actor.GetProperty().FrontfaceCullingOn();

        createBetaStrand(residues, 
                ribbonThickness, 
                strandWidth,
                startIndex,
                positionSpline,
                normalSpline
                );
    }

    protected void createBetaStrand(
            List<AminoAcid> residues, 
            double ribbonThickness, 
            double strandWidth,
            int startIndex,
            Spline3D positionSpline,
            Spline3D normalSpline) 
    throws NoCartoonCreatedException {
        List<Vector3D> positions = new Vector<Vector3D>();
        List<Vector3D> normals = new Vector<Vector3D>();
        List<Object> aminoAcids = new Vector<Object>();

        double minT = startIndex - 0.5;
        double maxT = startIndex + residues.size() - 0.5;

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

        createArrow(positions, normals, aminoAcids, ribbonThickness, strandWidth);        
    }

    protected void oldCreateBetaStrand(
            List<AminoAcid> residues, 
            double ribbonThickness, 
            double strandWidth,
            int startIndex,
            Spline3D positionSpline,
            Spline3D normalSpline) 
    throws NoCartoonCreatedException {
        List<Vector3D> positions = new Vector<Vector3D>();
        List<Vector3D> normals = new Vector<Vector3D>();
        List<Object> aminoAcids = new Vector<Object>();
        
        Vector3D previousNormal = null;
        int resCount = 0;
        for (AminoAcid residue : residues) {
            resCount ++;
            try {
                Vector3D ca = residue.getAtom("CA").getCoordinates();
                Vector3D o =  residue.getAtom("O").getCoordinates();

                Vector3D chainDirection = chainDirection(residue);

                int endFlag = 0;
                if (resCount == 1) endFlag = -1;
                else if (resCount == residues.size()) endFlag = 1;
                Vector3D norm = hBondNormal(residue, endFlag);
                
                // Flip normal if it points opposite the previous one
                if (previousNormal != null) {
                    Vector3D prevNormRot = chainDirection.cross(previousNormal).unit();
                    Vector3D normRot = chainDirection.cross(norm).unit();
                    if (normRot.dot(prevNormRot) < 0) { // More than 90 degrees apart
                        // Flip normal
                        norm = norm.times(-1.0);
                    }
                }

                // Adjust CA position to minimize pleating
                // first try -- Project CA onto C-O-N plane
                Vector3D pos = ca.plus(norm.times(o.dot(norm) - ca.dot(norm)));
                
                // If there are residues before and after, use position that minimizes pleating
                try {
                    Vector3D prev = ((AminoAcid)residue.getPreviousResidue()).getAtom("CA").getCoordinates();
                    Vector3D next = ((AminoAcid)residue.getNextResidue()).getAtom("CA").getCoordinates();
                    pos = ca.plus(ca.plus(prev.plus(next))).times(0.25);
                } catch (NullPointerException exc) {}
                
                // Extend halfway to previous residue
                if (resCount == 1) {
                    try {
                        Vector3D prev = ((AminoAcid)residue.getPreviousResidue()).getAtom("CA").getCoordinates();
                        Vector3D p = ca.plus(prev).times(0.50);

                        positions.add(p);
                        normals.add(norm);
                        aminoAcids.add(residue);
                    } catch (NullPointerException exc) {}
                    
                }
                
                positions.add(pos);
                normals.add(norm);
                aminoAcids.add(residue);
                
                // Extend halfway to following residue
                if (resCount == residues.size()) {
                    try {
                        Vector3D next = ((AminoAcid)residue.getNextResidue()).getAtom("CA").getCoordinates();
                        Vector3D p = ca.plus(next).times(0.50);

                        positions.add(p);
                        normals.add(norm);
                        aminoAcids.add(residue);
                    } catch (NullPointerException exc) {}
                    
                }
                
                previousNormal = norm;
            } catch (NullPointerException exc) {continue;}

        }
        
        if (positions.size() < 1) 
            throw new NoCartoonCreatedException("No positions found for Secondary Structure");

        createArrow(positions, normals, aminoAcids, ribbonThickness, strandWidth);        
    }
    
    protected void createArrow(
            List<Vector3D> positions, 
            List<Vector3D> normals, 
            List<Object> objects,
            double ribbonThickness,
            double arrowWidth
            ) {
        
        // Insert scalars and extra points to make a nice arrow head
        vtkArrowWidthFilter arrowFilter = new vtkArrowWidthFilter();
        arrowFilter.SetWidth(arrowWidth);
        arrowFilter.SetThickness(ribbonThickness);
        arrowFilter.SetInput(createPolyLine(positions, normals, objects));
        // arrowFilter.SetInput(lineData);

        createExtrudedRibbon(
                (vtkPolyData) arrowFilter.GetOutput(), 
                arrowFilter.GetThickness(),
                arrowFilter.GetRibbonFilterWidth(),
                arrowFilter.GetRibbonFilterWidthFactor()
                );
    }
    
}
