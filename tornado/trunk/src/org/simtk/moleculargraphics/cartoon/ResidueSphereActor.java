/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Jun 13, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.util.*;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.geometry3d.*;

import vtk.*;

public class ResidueSphereActor extends GlyphCartoon {
    double defaultSphereRadius = 1.50;
    double aminoAcidSphereRadius = 3.00;
    double nucleotideSphereRadius = 5.0;

    vtkSphereSource sphereSource = new vtkSphereSource();

    public ResidueSphereActor() {

        super();

        sphereSource.SetRadius(1.0);
        sphereSource.SetThetaResolution(18);
        sphereSource.SetPhiResolution(12);
        
        setGlyphSource(sphereSource.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleByNormal();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar
    }
    
    
    public void addMolecule(Molecule molecule) {
        if (molecule == null) return;

        // Don't add things that have already been added
        // if (glyphColors.containsKey(molecule)) return;
        
        Set<Object> parentObjects = null;
        
        // Collect molecular objects on which to index the glyphs
        Set<Object> currentObjects = new HashSet<Object>();
        if (parentObjects != null) {
            currentObjects.addAll(parentObjects);
        }
        currentObjects.add(molecule);
        
        // If it's a biopolymer, index the glyphs by residue
        if (molecule instanceof Residue) {
            Residue residue = (Residue) molecule;
            currentObjects.remove(currentObjects.size() - 1); // This object will be re-added
            addResidue(residue, currentObjects);
        }
        else if (molecule instanceof BiopolymerClass) {
            BiopolymerClass biopolymer = (BiopolymerClass) molecule;
            for (Iterator<Residue> iterResidue = biopolymer.residues().iterator(); iterResidue.hasNext(); ) {
                addResidue(iterResidue.next(), currentObjects);
            }
        }        
    }
    
    void addResidue(Residue residue, Set<Object> parentObjects) {
        if (residue == null) return;

        // Collect molecular objects on which to index the glyphs
        Set<Object> currentObjects = new HashSet<Object>();
        if (parentObjects != null) {
            currentObjects.addAll(parentObjects);
        }
        currentObjects.add(residue);

        Vector3D c = residue.getCenterOfMass();

        double colorScalar = getColorIndex(residue);

        // Draw a sphere for each atom
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        
        double sphereRadius = defaultSphereRadius;
        if (residue.getResidueType() instanceof Nucleotide) sphereRadius = nucleotideSphereRadius;
        if (residue.getResidueType() instanceof AminoAcid) sphereRadius = aminoAcidSphereRadius;
        
        lineNormals.InsertNextTuple3(sphereRadius, 0.0, 0.0);

        // glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
        colorScalars.InsertNextValue(colorScalar);
        
        isPopulated = true;
    }
}
