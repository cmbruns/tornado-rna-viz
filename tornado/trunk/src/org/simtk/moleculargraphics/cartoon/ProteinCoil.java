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
import org.simtk.molecularstructure.*;

import vtk.*;

public class ProteinCoil extends ProteinRibbonSegment {
    public ProteinCoil(
            List<Residue> residues,
            double diameter) 
    throws NoCartoonCreatedException
    {
        createCoil(residues, diameter);
    }

    protected void createCoil(
            List<Vector3D> positions, 
            List<Vector3D> normals, 
            List<Object> objects,
            double diameter) {

        vtkTubeFilter tubeFilter = new vtkTubeFilter();
        tubeFilter.SetRadius(0.5 * diameter);
        tubeFilter.SetCapping(1);
        tubeFilter.SetNumberOfSides(8);
        tubeFilter.SetVaryRadiusToVaryRadiusOff();
        tubeFilter.SetInput(createPolyLine(positions, normals, objects));
        
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetFeatureAngle(120);
        normalsFilter.SetInput(tubeFilter.GetOutput());
        
        vtkSetScalarsFilter setScalarsFilter = new vtkSetScalarsFilter();
        setScalarsFilter.SetScalars("colors");
        setScalarsFilter.SetInput(normalsFilter.GetOutput());

        finishVtkPipeline((vtkPolyData)setScalarsFilter.GetOutput());
    }
    
    protected void createCoil(
            List<Residue> residues, 
            double diameter) 
    throws NoCartoonCreatedException {
        List<Vector3D> positions = new Vector<Vector3D>();
        List<Vector3D> normals = new Vector<Vector3D>();
        List<Object> aminoAcids = new Vector<Object>();
        
        int resCount = 0;
        Vector3D previousNormal = null;
        RES: for (Residue residue : residues) {
            resCount ++;
            try {
                Vector3D ca = residue.getAtom("CA").getCoordinates();

                int endFlag = 0;
                if (resCount == 1) endFlag = -1;
                else if (resCount == residues.size()) endFlag = 1;
                Vector3D norm = hBondNormal(residue, endFlag);
                if (norm == null) continue RES;
                
                Vector3D chainDirection = chainDirection(residue);
                
                // Flip normal if it points opposite the previous one
                if (previousNormal != null) {
                    Vector3D prevNormRot = chainDirection.cross(previousNormal).unit();
                    Vector3D normRot = chainDirection.cross(norm).unit();
                    if (normRot.dot(prevNormRot) < 0) { // More than 90 degrees apart
                        // Flip normal
                        norm = norm.times(-1.0);
                    }
                }

                // Extend halfway to previous residue
                if (resCount == 1) {
                    try {
                        Vector3D prev = (residue.getPreviousResidue()).getAtom("CA").getCoordinates();
                        Vector3D p = ca.plus(prev).times(0.50);

                        positions.add(p);
                        normals.add(norm);
                        aminoAcids.add(residue);
                    } 
                    catch (NullPointerException exc) {}
                    catch (ClassCastException exc) {}
                    
                }
                
                Vector3D pos = ca;

                positions.add(pos);
                normals.add(norm);
                aminoAcids.add(residue);
                
                // Extend halfway to following residue
                if (resCount == residues.size()) {
                    try {
                        Vector3D next = (residue.getNextResidue()).getAtom("CA").getCoordinates();
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

        createCoil(positions, normals, aminoAcids, diameter);        
    }

}
