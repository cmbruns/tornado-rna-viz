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
    
    Hashtable sphereSources = new Hashtable();
    
    Color defaultColor = new Color(100, 100, 255); // Blue

    double defaultSphereRadius = 1.50;
    double aminoAcidSphereRadius = 3.00;
    double nucleotideSphereRadius = 5.0;
    
    /**
     * Update graphical primitives to reflect a change in atomic positions
     *
     */
    public void updateCoordinates() {
        // TODO
    }
    
    public vtkProp highlight(Residue residue, Color color) {
        return represent(residue, 1.05, color);
    }

    // One sphere per residue
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
            
            if (! sphereSources.containsKey(new Double(radius)))
                sphereSources.put(new Double(radius), newSphereSource(radius));
            vtkSphereSource sphereSource = (vtkSphereSource) sphereSources.get(new Double(radius));
            assembly.AddPart(getGlyphs(vPoints, sphereSource, clr));
            hasContents = true;
            return assembly;
        }
        
        // Figure out if its a Biopolymer
        // if so, do residues
        else if (molecule instanceof Biopolymer) {
            Biopolymer biopolymer = (Biopolymer) molecule;
            Hashtable spherePoints = new Hashtable();

            for (Iterator i = biopolymer.residues().iterator(); i.hasNext();) {
                Residue residue = (Residue) i.next();
                
                if (Residue.isSolvent(residue.getResidueName())) continue;

                Vector3D centerOfMass = residue.getCenterOfMass();

                double radius = defaultSphereRadius * scaleFactor;
                if (residue instanceof AminoAcid) radius = aminoAcidSphereRadius * scaleFactor;
                if (residue instanceof Nucleotide) radius = nucleotideSphereRadius * scaleFactor;
                Double radiusObject = new Double(radius);

                if (! spherePoints.containsKey(radiusObject))
                    spherePoints.put(radiusObject, new vtkPoints());
                vtkPoints vPoints = (vtkPoints) spherePoints.get(radiusObject);
                vPoints.InsertNextPoint(centerOfMass.getX(), centerOfMass.getY(), centerOfMass.getZ());

                hasContents = true;
            }

            for (Iterator i = spherePoints.keySet().iterator(); i.hasNext(); ) {
                Double radiusObject = (Double) i.next();
                vtkPoints vPoints = (vtkPoints) spherePoints.get(radiusObject);
                if (! sphereSources.containsKey(radiusObject))
                    sphereSources.put(radiusObject, newSphereSource(radiusObject.doubleValue()));
                vtkSphereSource source = (vtkSphereSource) sphereSources.get(radiusObject);
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
