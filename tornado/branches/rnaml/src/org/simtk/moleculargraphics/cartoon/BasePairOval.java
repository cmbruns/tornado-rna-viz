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
 * Created on Jun 22, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.geometry3d.Vector3D;

import vtk.*;

public class BasePairOval extends TensorGlyphCartoon {
    
    BasePairOval() {
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetCapping(1);
        cylinderSource.SetRadius(4.0);
        cylinderSource.SetHeight(1.0);

        // Stretch the cylinder in one direction to make an oval
        vtkTransform transform = new vtkTransform();
        transform.Identity();
        transform.Scale(3.0, 1.0, 1.0);

        vtkTransformPolyDataFilter transformFilter = new vtkTransformPolyDataFilter();
        transformFilter.SetTransform(transform);
        transformFilter.SetInput(cylinderSource.GetOutput());
        
        setGlyphSource(transformFilter.GetOutput());
    }

    public void add(LocatedMolecule molecule) {
        if (molecule instanceof Nucleotide)
            addNucleotide((Nucleotide)molecule);
        super.add(molecule); // TODO make sure all cartoon classes do this
    }
    
    public void addNucleotide(Nucleotide molecule) {
        for (SecondaryStructure structure : molecule.secondaryStructures())
            if (structure instanceof BasePair)
                addBasePair((BasePair) structure);
    }
    
    public void addBasePair(BasePair basePair) {
        Nucleotide res1 = basePair.getResidue1();
        Nucleotide res2 = basePair.getResidue2();
        
        // Choose the ends at which to join the oval
        // Choose the first atom type from the following list
        // Purines link to sugar at N9
        // Pyrimidines link to sugar at N1, but have no N9
        String[] atomTypes = {" N9 "," N1 "," C5*", " P  "};

        Vector3D pos1 = null;
        for (String atomType : atomTypes) {
            Atom atom1 = res1.getAtom(atomType);
            if (atom1 == null) continue; // No such atom
            if (! (atom1 instanceof LocatedAtom)) continue; // We need coordinates
            pos1 = ((LocatedAtom)atom1).getCoordinates();
            if (pos1 == null) continue;
            break;
        }

        Vector3D pos2 = null;
        for (String atomType : atomTypes) {
            Atom atom2 = res2.getAtom(atomType);
            if (atom2 == null) continue; // No such atom
            if (! (atom2 instanceof LocatedAtom)) continue; // We need coordinates
            pos2 = ((LocatedAtom)atom2).getCoordinates();
            if (pos2 == null) continue;
            break;
        }
        
        if (pos1 == null) return;
        if (pos2 == null) return;
        
        Vector3D midpoint = pos1.plus(pos2).times(0.5);
    }
}
