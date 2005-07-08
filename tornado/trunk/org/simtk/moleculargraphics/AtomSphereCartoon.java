/*
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.util.*;
import vtk.*;

import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;

/** 
 * @author Christopher Bruns
 * 
 * Draw a space-filling van der Waals sphere around each atom in the structure
 */
public class AtomSphereCartoon extends MolecularCartoon {
    
    boolean useSphereActors = false;
    double maxPickDistance = 5.0; // Angstroms from atom center for pick
    Hash3D<Residue> residuePositions = new Hash3D<Residue>(maxPickDistance);

    @Override
    public Residue getNearbyResidue(Vector3D v) {
        Residue residue = residuePositions.getClosest(v, maxPickDistance);
        return residue;
    }
    
    // Produce a renderable highlighted representation of a residue, slightly larger than the original
    @Override
    public vtkProp highlight(Residue residue, Color color) {
        // Make it a little bigger than usual
        vtkAssembly answer = represent(residue, 1.05, color, 0.70);
        return answer;
    }
    
    @Override
    public vtkAssembly represent(Molecule molecule) {
        return represent(molecule, 1.00, null, 1.00);
    }
    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr, double opacity) {

        // This routine uses glyphs instead of actors, so try something else
        if (useSphereActors) return super.represent(molecule);
        
        vtkAssembly assembly = new vtkAssembly();
        
        // Store each atom type in a separate vtkPoints structure
        Hashtable<String, vtkPoints> elementPoints = new Hashtable<String, vtkPoints>(); // Map all atoms of same element to the same structure
        Hashtable<String, Double> elementRadii = new Hashtable<String, Double>(); // Map element names to sphere radius
        Hashtable<String, Color> elementColors = new Hashtable<String, Color>(); // Map element names to color
        
        boolean hasContents = false;

        for (int a = 0; a < molecule.getAtomCount(); a++) {
            Atom atom = molecule.getAtom(a);
            BaseVector3D coord = molecule.getAtom(a).getCoordinates();
            String elementSymbol = molecule.getAtom(a).getElementSymbol();
            
            vtkPoints atomPoints;
            if (!elementPoints.containsKey(elementSymbol)) { // New element type
                atomPoints = new vtkPoints();
                elementPoints.put(elementSymbol, atomPoints);
                elementRadii.put(elementSymbol, new Double(atom.getRadius() * scaleFactor));
                if (clr == null)
                    elementColors.put(elementSymbol, atom.getDefaultColor());
                else 
                    elementColors.put(elementSymbol, clr);
            }
            atomPoints = (vtkPoints) elementPoints.get(elementSymbol);
            
            atomPoints.InsertNextPoint(coord.getX(), coord.getY(), coord.getZ());
            hasContents = true;
        }

        String elementSymbol = "";
        Enumeration<String> e = elementPoints.keys();
        while (e.hasMoreElements()) {
              elementSymbol = (String) e.nextElement();
              // System.out.println(elementSymbol);

              vtkPoints atomPoints = (vtkPoints) elementPoints.get(elementSymbol);
              
              vtkPolyData points = new vtkPolyData();
              points.SetPoints(atomPoints);
              
              vtkSphereSource sphere = new vtkSphereSource();
              sphere.SetThetaResolution(8);
              sphere.SetPhiResolution(8);
              sphere.SetRadius( ((Double)elementRadii.get(elementSymbol)).doubleValue() );
              
              vtkGlyph3D spheres = new vtkGlyph3D();
              spheres.SetInput(points);
              spheres.SetSource(sphere.GetOutput());

              vtkPolyDataMapper spheresMapper = new vtkPolyDataMapper();
              spheresMapper.SetInput(spheres.GetOutput());
              
              vtkActor spheresActor = new vtkActor();
              spheresActor.SetMapper(spheresMapper);
              Color color = (Color) elementColors.get(elementSymbol);
              spheresActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
              spheresActor.GetProperty().SetOpacity(opacity);
              spheresActor.GetProperty().BackfaceCullingOn();
              
              assembly.AddPart(spheresActor);
        }
        
        if (hasContents) {

            // Map atomic positions to residues for picking
            if (molecule instanceof Biopolymer) {
                residuePositions.clear();
                Biopolymer biopolymer = (Biopolymer) molecule;
                for (Residue residue : biopolymer.residues()) {
                    for (Atom atom : residue.getAtoms()) {
                        residuePositions.put(atom.getCoordinates(), residue);
                    }
                }
            }
            
            return assembly;
        }
        else return null;
    }

    @Override
    public vtkAssembly represent(Atom atom) {
        return represent(atom, 1.00, null);
    }
    public vtkAssembly represent(Atom atom, double scaleFactor, Color clr) {
        if (!useSphereActors) return null; // This method is too slow in rendering big things

        vtkAssembly assembly = new vtkAssembly();
        
        // Generate a sphere at the atom position
        BaseVector3D center = atom.getCoordinates();

        vtkSphereSource sphere = new vtkSphereSource();
        sphere.SetCenter(center.getX(), center.getY(), center.getZ());
        sphere.SetRadius(atom.getRadius() * scaleFactor);
        sphere.SetPhiResolution(20);
        sphere.SetThetaResolution(20);
        vtkPolyDataMapper sphereMapper = new vtkPolyDataMapper();
        sphereMapper.SetInput(sphere.GetOutput());
        vtkActor sphereActor = new vtkActor();
        sphereActor.SetMapper(sphereMapper);
        sphereActor.GetProperty().BackfaceCullingOn();

        Color color;
        if (clr == null)
            color = atom.getDefaultColor();
        else 
            color = clr;
        sphereActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);

        assembly.AddPart(sphereActor);
        
        return assembly;
    }
}
