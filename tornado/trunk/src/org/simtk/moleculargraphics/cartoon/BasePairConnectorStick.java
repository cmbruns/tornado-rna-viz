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
 * Created on Jun 23, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;

import vtk.*;

public class BasePairConnectorStick extends TensorGlyphCartoon {
    public BasePairConnectorStick() {
        super();
        
        // tensorGlyph.ExtractEigenvaluesOn(); // scale by eigenvalues
        
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(8);
        cylinderSource.SetCapping(1);
        cylinderSource.SetRadius(0.5);
        cylinderSource.SetHeight(5.5);

        setGlyphSource(cylinderSource.GetOutput());        
    }

    public void addMolecule(Molecule molecule) {
        if (molecule instanceof NucleicAcid)
            addNucleicAcid((NucleicAcid)molecule);
    }

    public void addNucleicAcid(NucleicAcid molecule) {
        for (SecondaryStructure structure : molecule.secondaryStructures())
            if (structure instanceof BasePair)
                addBasePair((BasePair) structure);
    }

    public void addBasePair(BasePair basePair) {
        Residue res1 = basePair.getResidue1();
        Residue res2 = basePair.getResidue2();
        
        addNucleotide(res1);
        addNucleotide(res2);
    }
    
    public void addNucleotide(Residue res) {
        Vector3D pos1;
        try {
            pos1 = res.getBackbonePosition();
        } catch (Exception exc) {return;}
        
        
        Vector3D pos2;
        try {
            pos2 = res.getAtom(" N9 ").getCoordinates();
        } catch (NullPointerException exc) {
            try {
                pos2 = res.getAtom(" N1 ").getCoordinates();
            } catch (NullPointerException exc2) {return;}
        }

        if (pos1 == null) return;
        if (pos2 == null) return;
        
        Vector3D midpoint = pos1.plus(pos2).times(0.5);
        Vector3D direction = pos1.minus(pos2).unit();
        
        // Need two more orthogonal directions
        Vector3D direction2 = direction.cross(new Vector3DClass(1,0,0)).unit();
        Vector3D direction3 = direction.cross(direction2).unit();

        linePoints.InsertNextPoint(midpoint.x(), midpoint.y(), midpoint.z());
        
        tensors.InsertNextTuple9(
                direction3.x(), direction3.y(), direction3.z(), 
                direction.x(), direction.y(), direction.z(), 
                direction2.x(), direction2.y(), direction2.z()
                );

        int glyphIndex = colorScalars.GetNumberOfTuples();
        
        double colorScalar = toonColors.getColorIndex(res);

        // glyphColors.add(currentObjects, lineData, glyphIndex, colorScalar);
        
        colorScalars.InsertNextValue(colorScalar);
    }

}
