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
 * Created on Jul 6, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;

import vtk.*;

public class BackboneSpheres extends GlyphCartoon {
    int stickResolution = 5;
    double stickRadius = 1.00;
    private int baseColorIndex = 150;
    private Hashtable colorIndices = new Hashtable();

    public BackboneSpheres(double r) {
        super();

        stickRadius = r;        
        initialize();
    }
    
    public BackboneSpheres(double r, int res) {
        super();

        stickRadius = r;
        stickResolution = res;
        initialize();
    }
    
    private void initialize() {
        // Make a sphere to use as the basis of all positions
        vtkSphereSource sphereSource = new vtkSphereSource();
        sphereSource.SetPhiResolution(8);
        sphereSource.SetThetaResolution(stickResolution);
        sphereSource.SetRadius(stickRadius);

        // Use lines as the glyph primitive
        setGlyphSource(sphereSource.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleNone();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar

        glyphActor.GetProperty().BackfaceCullingOn();        
    }

    public void show(StructureMolecule molecule) {
        addMolecule(molecule, null);
        glyphColors.show(molecule);
    }

    void addMolecule(StructureMolecule molecule, Vector parentObjects) {
        if (molecule == null) return;

        // Don't add things that have already been added
        if (glyphColors.containsKey(molecule)) return;
        
        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(molecule);
        
        // If it's a biopolymer, index the glyphs by residue
        if (molecule instanceof PDBResidueClass) {
            PDBResidue residue = (PDBResidue) molecule;
            currentObjects.remove(currentObjects.size() - 1); 
            addResidue(residue, currentObjects);
        }
        else if (molecule instanceof BiopolymerClass) {
            BiopolymerClass biopolymer = (BiopolymerClass) molecule;
            for (Iterator iterResidue = biopolymer.getResidueIterator(); iterResidue.hasNext(); ) {
                addMolecule((PDBResidueClass) iterResidue.next(), currentObjects);
            }
        }
    }
    
    void addResidue(PDBResidue residue, Vector parentObjects) {
        if (residue == null) return;
        
        // Don't add things that have already been added
        if (glyphColors.containsKey(residue)) return;

        Vector3D backbonePosition = residue.getBackbonePosition();
        
        if (backbonePosition == null) return;

        
        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(residue);

        Color color = residue.getDefaultColor();
        if (! (colorIndices.containsKey(color))) {
            colorIndices.put(color, new Integer(baseColorIndex));
            lut.SetTableValue(baseColorIndex, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
            baseColorIndex ++;
        }
        int colorScalar = ((Integer) colorIndices.get(color)).intValue();
        
        linePoints.InsertNextPoint(backbonePosition.getX(), backbonePosition.getY(), backbonePosition.getZ());
        glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
        lineScalars.InsertNextValue(colorScalar);        
    }    
}
