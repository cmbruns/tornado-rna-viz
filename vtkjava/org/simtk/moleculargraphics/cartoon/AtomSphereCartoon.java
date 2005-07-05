/*
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

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

    /**
     * Update graphical primitives to reflect a change in atomic positions
     *
     */
    public void updateCoordinates() {
        updateAtomCoordinates();
    }
    
    /**
     * Update graphical primitives to reflect a change in atomic positions.
     * 
     * molecule argument specifies the (subset of) atoms that have changed.
     */
    public void updateCoordinates(Molecule mol) {
        updateAtomCoordinates(mol);
    }
    
    // Produce a renderable highlighted representation of a residue, slightly larger than the original
    public vtkProp highlight(Residue residue, Color color) {
        // Make it a little bigger than usual
        vtkAssembly answer = represent(residue, 1.05, color, 0.70);
        return answer;
    }
    
    public vtkAssembly represent(Molecule molecule) {
        return represent(molecule, 1.00, null, 1.00);
    }
    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr, double opacity) {

        // This routine uses glyphs instead of actors, so try something else
        if (useSphereActors) return super.represent(molecule);
        
        vtkAssembly assembly = new vtkAssembly();
        
        // Store each atom type in a separate vtkPoints structure
        Hashtable elementPoints = new Hashtable(); // Map all atoms of same element to the same structure
        Hashtable elementRadii = new Hashtable(); // Map element names to sphere radius
        Hashtable elementColors = new Hashtable(); // Map element names to color
        
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

            if (! (atomPositions.containsKey(atom))) atomPositions.put(atom, new Vector());
            Vector atomPrimitives = (Vector) atomPositions.get(atom);
            atomPrimitives.add( new VTKPointPosition(atomPoints, atomPoints.GetNumberOfPoints() - 1) );
            
            hasContents = true;
        }

        String elementSymbol = "";
        Enumeration e = elementPoints.keys();
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
            return assembly;
        }
        else return null;
    }

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
        
        if (! (atomPositions.containsKey(atom))) atomPositions.put(atom, new Vector());
        Vector atomPrimitives = (Vector) atomPositions.get(atom);
        atomPrimitives.add( new VTKSpherePosition(sphere) );
        
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
