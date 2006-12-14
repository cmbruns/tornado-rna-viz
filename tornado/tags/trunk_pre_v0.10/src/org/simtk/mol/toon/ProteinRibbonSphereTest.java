/* Copyright (c) 2005 Stanford University and Christopher Bruns
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
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Dec 9, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import java.util.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.geometry3d.*;

import vtk.vtkCardinalSpline;

public class ProteinRibbonSphereTest extends AtomSphereActor {
    private int splineFactor = 5;
    
    ProteinRibbonSphereTest() {
        super(1.0);
    }

    void addMolecule(Molecule molecule, Vector parentObjects) {
        if (molecule == null) return;

        // Don't add things that have already been added
        // if (glyphColors.containsKey(molecule)) return;
        
        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(molecule);
        
        if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (Residue residue : biopolymer.residues()) {
                addResidue(residue);
            }
        }
    }
    
    public void addResidue(Residue residue) {
        // If it's a biopolymer, index the glyphs by residue
        if (! (residue.getResidueType() instanceof AminoAcid)) return;

        System.out.println("Amino acid found");

        double colorScalar = getColorIndex(residue);

        // Vector3D c = residue.getBackbonePosition();
        
        try {
            Vector3D[] points = createCoilPath(residue);

            double radius = 0.5;
            
            for (int i = 0; i < points.length; i++) {
                Vector3D c = points[i];
                
                linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
            
                lineNormals.InsertNextTuple3(radius, 0.0, 0.0);

                // glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
                colorScalars.InsertNextValue(colorScalar);
            }
        } catch (InsufficientAtomsException exc) {} // skip this residue        
    }

    /**
     * Generate a smooth path for a coil joining the given residues.
     * Path begins one half residue before the start, and ends one half residue after the end.
     * Throws a runtime exception if the residues are not part of a continuous chain.
     * 
     * @param startResidue
     * @return
     */
    private Vector3D[] createCoilPath(Residue startResidue) 
    throws InsufficientAtomsException 
    {
        int numberOfOutputPoints = splineFactor;
        Vector3D[] answer = new Vector3D[numberOfOutputPoints];

        // Set up one spline for each dimension
        vtkCardinalSpline splineX = new vtkCardinalSpline();
        vtkCardinalSpline splineY = new vtkCardinalSpline();
        vtkCardinalSpline splineZ = new vtkCardinalSpline();

        // 1) Populate spline with upstream residue positions, if available
        Residue residueMinusOne = startResidue;
        if (startResidue.getPreviousResidue() != null)
            residueMinusOne = (Residue) startResidue.getPreviousResidue();
        Vector3D positionMinusOne = residueMinusOne.getBackbonePosition();

        Residue residueMinusTwo = residueMinusOne;
        if (residueMinusOne.getPreviousResidue() != null)
            residueMinusTwo = (Residue) residueMinusOne.getPreviousResidue();
        Vector3D positionMinusTwo = residueMinusTwo.getBackbonePosition();

        splineX.AddPoint(-2, positionMinusTwo.getX());
        splineY.AddPoint(-2, positionMinusTwo.getY());
        splineZ.AddPoint(-2, positionMinusTwo.getZ());

        splineX.AddPoint(-1, positionMinusOne.getX());
        splineY.AddPoint(-1, positionMinusOne.getY());
        splineZ.AddPoint(-1, positionMinusOne.getZ());
        
        // 2) Populate spline with backbone positions in the supplied range
        Vector3D position = startResidue.getBackbonePosition();        
        splineX.AddPoint(0, position.getX());
        splineY.AddPoint(0, position.getY());
        splineZ.AddPoint(0, position.getZ());              
        
        // 3) Populate spline with downstream residue positions, if available
        Residue residuePlusOne = startResidue;
        if (startResidue.getNextResidue() != null)
            residuePlusOne = (Residue) startResidue.getNextResidue();
        Vector3D positionPlusOne = residuePlusOne.getBackbonePosition();

        Residue residuePlusTwo = residuePlusOne;
        if (residuePlusOne.getNextResidue() != null)
            residuePlusTwo = (Residue) residuePlusOne.getNextResidue();
        Vector3D positionPlusTwo = residuePlusTwo.getBackbonePosition();

        splineX.AddPoint(1, positionPlusOne.getX());
        splineY.AddPoint(1, positionPlusOne.getY());
        splineZ.AddPoint(1, positionPlusOne.getZ());

        splineX.AddPoint(2, positionPlusTwo.getX());
        splineY.AddPoint(2, positionPlusTwo.getY());
        splineZ.AddPoint(2, positionPlusTwo.getZ());
        
        // Generate coordinates

        for (int i = 0; i < numberOfOutputPoints; i++) {
            double t = -0.5 + i * (1 / (double) (numberOfOutputPoints - 1));
            answer[i] = new Vector3DClass(
                    splineX.Evaluate(t),
                    splineY.Evaluate(t),
                    splineZ.Evaluate(t));
        }
        
        return answer;
    }
}
