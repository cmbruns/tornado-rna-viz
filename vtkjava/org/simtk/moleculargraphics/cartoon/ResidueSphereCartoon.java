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

public class ResidueSphereCartoon extends MolecularCartoon {
    
    Hashtable<Double, vtkSphereSource> sphereSources = new Hashtable<Double, vtkSphereSource>();
    
    Color defaultColor = new Color(100, 100, 255); // Blue

    double defaultSphereRadius = 1.50;
    double aminoAcidSphereRadius = 3.00;
    double nucleotideSphereRadius = 5.0;
    
    /**
     * Update graphical primitives to reflect a change in atomic positions
     *
     */
    @Override
    public void updateCoordinates() {
        // TODO
    }
    
    @Override
    public vtkProp highlight(Residue residue, Color color) {
        return represent(residue, 1.05, color);
    }

    // One sphere per residue
    @Override
    public vtkAssembly represent(Molecule molecule) {
        return represent(molecule, 1.00, null);
    }
    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr) {
        boolean hasContents = false;
        vtkAssembly assembly = new vtkAssembly();
        
        if (molecule instanceof Residue) {
            Residue residue = (Residue) molecule;
            
            if (Residue.isSolvent(residue.getResidueName())) return null;

            // Make just one sphere
            Vector3D centerOfMass = residue.getCenterOfMass();
            vtkPoints vPoints = new vtkPoints();
            vPoints.InsertNextPoint(centerOfMass.getX(), centerOfMass.getY(), centerOfMass.getZ());

            double radius = defaultSphereRadius * scaleFactor;
            if (residue instanceof AminoAcid) radius = aminoAcidSphereRadius * scaleFactor;
            if (residue instanceof Nucleotide) radius = nucleotideSphereRadius * scaleFactor;
            
            if (! sphereSources.containsKey(radius))
                sphereSources.put(radius, newSphereSource(radius));
            vtkSphereSource sphereSource = sphereSources.get(radius);
            assembly.AddPart(getGlyphs(vPoints, sphereSource, clr));
            hasContents = true;
            return assembly;
        }
        
        // Figure out if its a Biopolymer
        // if so, do residues
        else if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            Hashtable<Double, vtkPoints> spherePoints = new Hashtable<Double, vtkPoints>();

            for (Residue residue : biopolymer.residues()) {
                
                if (Residue.isSolvent(residue.getResidueName())) continue;

                Vector3D centerOfMass = residue.getCenterOfMass();

                double radius = defaultSphereRadius * scaleFactor;
                if (residue instanceof AminoAcid) radius = aminoAcidSphereRadius * scaleFactor;
                if (residue instanceof Nucleotide) radius = nucleotideSphereRadius * scaleFactor;

                if (! spherePoints.containsKey(radius))
                    spherePoints.put(radius, new vtkPoints());
                vtkPoints vPoints = spherePoints.get(radius);
                vPoints.InsertNextPoint(centerOfMass.getX(), centerOfMass.getY(), centerOfMass.getZ());

                hasContents = true;
            }

            for (double radius : spherePoints.keySet()) {
                vtkPoints vPoints = spherePoints.get(radius);
                if (! sphereSources.containsKey(radius))
                    sphereSources.put(radius, newSphereSource(radius));
                vtkSphereSource source = sphereSources.get(radius);
                assembly.AddPart(getGlyphs(vPoints, source, clr));
            }
        }

        if (hasContents)
            return assembly;

        else return null;
    }
        
    private vtkSphereSource newSphereSource(double radius) {
        vtkSphereSource answer = new vtkSphereSource();
        answer.SetRadius(radius);
        answer.SetThetaResolution(12);
        answer.SetPhiResolution(12);
        return answer;
    }

    private vtkActor getGlyphs(vtkPoints vPoints, vtkSphereSource source, Color clr) {
        vtkPolyData points = new vtkPolyData();
        points.SetPoints(vPoints);
        
        vtkGlyph3D spheres = new vtkGlyph3D();
        spheres.SetInput(points);
        spheres.SetSource(source.GetOutput());

        vtkPolyDataMapper spheresMapper = new vtkPolyDataMapper();
        spheresMapper.SetInput(spheres.GetOutput());
        
        vtkActor spheresActor = new vtkActor();
        spheresActor.SetMapper(spheresMapper);
        Color color = clr;
        if (color == null) color = defaultColor;
        spheresActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
        spheresActor.GetProperty().BackfaceCullingOn();
        
        return spheresActor;        
    }
        
}
