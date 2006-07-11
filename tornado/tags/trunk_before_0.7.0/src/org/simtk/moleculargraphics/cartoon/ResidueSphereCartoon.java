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

import java.awt.*;
import java.util.*;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.geometry3d.*;

import vtk.*;

public class ResidueSphereCartoon extends GlyphCartoon {
    double defaultSphereRadius = 1.50;
    double aminoAcidSphereRadius = 3.00;
    double nucleotideSphereRadius = 5.0;

    private int baseColorIndex = 150;
    private Hashtable colorIndices = new Hashtable();
    
    vtkSphereSource sphereSource = new vtkSphereSource();

    public ResidueSphereCartoon() {

        super();

        sphereSource.SetRadius(1.0);
        sphereSource.SetThetaResolution(8);
        sphereSource.SetPhiResolution(8);
        
        setGlyphSource(sphereSource.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        scaleByNormal();  // Do not adjust size
        colorByScalar(); // Take color from glyph scalar

        glyphActor.GetProperty().BackfaceCullingOn();
    }
    
    
    public void add(LocatedMolecule molecule) {
        addMolecule(molecule, null);
        super.add(molecule);
    }

    void addMolecule(LocatedMolecule molecule, Vector parentObjects) {
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
            PDBResidueClass residue = (PDBResidueClass) molecule;
            currentObjects.remove(currentObjects.size() - 1); // This object will be re-added
            addResidue(residue, currentObjects);
        }
        else if (molecule instanceof BiopolymerClass) {
            BiopolymerClass biopolymer = (BiopolymerClass) molecule;
            for (Iterator iterResidue = biopolymer.getResidueIterator(); iterResidue.hasNext(); ) {
                addResidue((PDBResidueClass) iterResidue.next(), currentObjects);
            }
        }
    }
    
    void addResidue(PDBResidueClass residue, Vector parentObjects) {
        if (residue == null) return;
        
        // Don't add things that have already been added
        if (glyphColors.containsKey(residue)) return;

        // Collect molecular objects on which to index the glyphs
        Vector currentObjects = new Vector();
        if (parentObjects != null) {
            for (int i = 0; i < parentObjects.size(); i++)
                currentObjects.add(parentObjects.get(i));
        }
        currentObjects.add(residue);

        Vector3D c = residue.getCenterOfMass();

        Color color = residue.getDefaultColor();
        if (! (colorIndices.containsKey(color))) {
            colorIndices.put(color, new Integer(baseColorIndex));
            lut.SetTableValue(baseColorIndex, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
            baseColorIndex ++;
        }
        int colorScalar = ((Integer) colorIndices.get(color)).intValue();        

        // Draw a sphere for each atom
        linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
        
        double sphereRadius = defaultSphereRadius;
        if (residue instanceof Nucleotide) sphereRadius = nucleotideSphereRadius;
        if (residue instanceof AminoAcid) sphereRadius = aminoAcidSphereRadius;
        
        lineNormals.InsertNextTuple3(sphereRadius, 0.0, 0.0);

        glyphColors.add(currentObjects, lineData, lineScalars.GetNumberOfTuples(), colorScalar);
        lineScalars.InsertNextValue(colorScalar);
    }

    // Hashtable sphereSources = new Hashtable();
    
    // Color defaultColor = new Color(100, 100, 255); // Blue

//    /**
//     * Update graphical primitives to reflect a change in atomic positions
//     *
//     */
//    public void updateCoordinates() {
//        // TODO
//    }
//    
//    public vtkProp highlight(Residue residue, Color color) {
//        return represent(residue, 1.05, color);
//    }
//
//    // One sphere per residue
//    public vtkAssembly represent(Molecule molecule) {
//        return represent(molecule, 1.00, null);
//    }
//    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr) {
//        boolean hasContents = false;
//        vtkAssembly assembly = new vtkAssembly();
//        
//        if (molecule instanceof Residue) {
//            Residue residue = (Residue) molecule;
//            
//            if (Residue.isSolvent(residue.getResidueName())) return null;
//
//            // Make just one sphere
//            Vector3D centerOfMass = residue.getCenterOfMass();
//            vtkPoints vPoints = new vtkPoints();
//            vPoints.InsertNextPoint(centerOfMass.getX(), centerOfMass.getY(), centerOfMass.getZ());
//
//            double radius = defaultSphereRadius * scaleFactor;
//            if (residue instanceof AminoAcid) radius = aminoAcidSphereRadius * scaleFactor;
//            if (residue instanceof Nucleotide) radius = nucleotideSphereRadius * scaleFactor;
//            
//            if (! sphereSources.containsKey(new Double(radius)))
//                sphereSources.put(new Double(radius), newSphereSource(radius));
//            vtkSphereSource sphereSource = (vtkSphereSource) sphereSources.get(new Double(radius));
//            assembly.AddPart(getGlyphs(vPoints, sphereSource, clr));
//            hasContents = true;
//            return assembly;
//        }
//        
//        // Figure out if its a Biopolymer
//        // if so, do residues
//        else if (molecule instanceof Biopolymer) {
//            Biopolymer biopolymer = (Biopolymer) molecule;
//            Hashtable spherePoints = new Hashtable();
//
//            for (Iterator i = biopolymer.residues().iterator(); i.hasNext();) {
//                Residue residue = (Residue) i.next();
//                
//                if (Residue.isSolvent(residue.getResidueName())) continue;
//
//                Vector3D centerOfMass = residue.getCenterOfMass();
//
//                double radius = defaultSphereRadius * scaleFactor;
//                if (residue instanceof AminoAcid) radius = aminoAcidSphereRadius * scaleFactor;
//                if (residue instanceof Nucleotide) radius = nucleotideSphereRadius * scaleFactor;
//                Double radiusObject = new Double(radius);
//
//                if (! spherePoints.containsKey(radiusObject))
//                    spherePoints.put(radiusObject, new vtkPoints());
//                vtkPoints vPoints = (vtkPoints) spherePoints.get(radiusObject);
//                vPoints.InsertNextPoint(centerOfMass.getX(), centerOfMass.getY(), centerOfMass.getZ());
//
//                hasContents = true;
//            }
//
//            for (Iterator i = spherePoints.keySet().iterator(); i.hasNext(); ) {
//                Double radiusObject = (Double) i.next();
//                vtkPoints vPoints = (vtkPoints) spherePoints.get(radiusObject);
//                if (! sphereSources.containsKey(radiusObject))
//                    sphereSources.put(radiusObject, newSphereSource(radiusObject.doubleValue()));
//                vtkSphereSource source = (vtkSphereSource) sphereSources.get(radiusObject);
//                assembly.AddPart(getGlyphs(vPoints, source, clr));
//            }
//        }
//
//        if (hasContents)
//            return assembly;
//
//        else return null;
//    }
//        
//    private vtkSphereSource newSphereSource(double radius) {
//        vtkSphereSource answer = new vtkSphereSource();
//        answer.SetRadius(radius);
//        answer.SetThetaResolution(12);
//        answer.SetPhiResolution(12);
//        return answer;
//    }
//
//    private vtkActor getGlyphs(vtkPoints vPoints, vtkSphereSource source, Color clr) {
//        vtkPolyData points = new vtkPolyData();
//        points.SetPoints(vPoints);
//        
//        vtkGlyph3D spheres = new vtkGlyph3D();
//        spheres.SetInput(points);
//        spheres.SetSource(source.GetOutput());
//
//        vtkPolyDataMapper spheresMapper = new vtkPolyDataMapper();
//        spheresMapper.SetInput(spheres.GetOutput());
//        
//        vtkActor spheresActor = new vtkActor();
//        spheresActor.SetMapper(spheresMapper);
//        Color color = clr;
//        if (color == null) color = defaultColor;
//        spheresActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
//        spheresActor.GetProperty().BackfaceCullingOn();
//        
//        return spheresActor;        
//    }
//        
}
