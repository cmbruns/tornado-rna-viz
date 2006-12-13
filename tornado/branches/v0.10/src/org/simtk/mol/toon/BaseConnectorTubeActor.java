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
 * Created on Jul 19, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.Atom;

import vtk.*;

public class BaseConnectorTubeActor extends ActorCartoonClass {
    private double tubeRadius = 0.5;

    protected BaseConnectorTubeActor() {}
    
    public BaseConnectorTubeActor(Residue residue) 
    throws NoCartoonCreatedException {

        Atom p = residue.getAtom("P");
        Atom o5 = residue.getAtom("O5*");
        Atom c1 = residue.getAtom("C1*");
        Atom n = residue.getAtom("N9");
        if (n == null)
            n = residue.getAtom("N1");
        
        Atom[] atoms = {p,o5,c1,n};

        setResidue(residue, atoms);
    }

    protected void setResidue(Residue residue, Atom[] atoms) 
    throws NoCartoonCreatedException
    {
        int colorScalar = getColorIndex(residue);
        vtkFloatArray colorScalars = new vtkFloatArray();
        colorScalars.SetNumberOfComponents(1);
        
        vtkPoints linePoints = new vtkPoints();

        for (Atom atom : atoms) {
            if (atom == null) continue;
            linePoints.InsertNextPoint(atom.getCoordinates().toArray());
            colorScalars.InsertNextValue(colorScalar);
        }

        int numberOfPoints = linePoints.GetNumberOfPoints();
        if (numberOfPoints < 2) 
            throw new NoCartoonCreatedException("Not enough points for nucleotide connector");
        
        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i ++)
            lineCells.InsertCellPoint(i);
        
        vtkPolyData lineData = new vtkPolyData();
        lineData.SetPoints(linePoints);
        lineData.SetLines(lineCells);
        lineData.GetPointData().SetScalars(colorScalars);

        vtkTubeFilter highglightTubeFilter = new vtkTubeFilter();
        highglightTubeFilter.SetRadius(tubeRadius * 1.02);
        highglightTubeFilter.SetNumberOfSides(6);
        highglightTubeFilter.SetCapping(1);
        highglightTubeFilter.SetVaryRadius(0);
        highglightTubeFilter.SetInput(lineData);

        highlightMapper.SetInput(highglightTubeFilter.GetOutput());
        highlightActor.SetMapper(highlightMapper);
        
        vtkTubeFilter tubeFilter = new vtkTubeFilter();
        tubeFilter.SetRadius(tubeRadius);
        tubeFilter.SetNumberOfSides(6);
        tubeFilter.SetCapping(1);
        tubeFilter.SetVaryRadius(0);
        tubeFilter.SetInput(lineData);
                
        mapper.SetInput(tubeFilter.GetOutput());
        actor.SetMapper(mapper);
        
        isPopulated = true;
    }    

}
