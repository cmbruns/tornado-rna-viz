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
    Hashtable< Atom, Vector<GraphicsPrimitivePosition> > atomPositions = 
        new Hashtable< Atom, Vector<GraphicsPrimitivePosition> >();

    /**
     * Update graphical primitives to reflect a change in atomic positions
     *
     */
    @Override
    public void updateCoordinates() {
        HashSet<vtkObject> vtkObjects = new HashSet<vtkObject>();
        
        for (Atom a : atomPositions.keySet()) {
            for (GraphicsPrimitivePosition p : atomPositions.get(a)) {
                vtkObject o = p.update(a.getCoordinates());
                if (o != null)
                    vtkObjects.add(o);
                p.update(a.getCoordinates());
            }
        }
        
        for (vtkObject object : vtkObjects) {
            object.Modified();
        }
    }
    
    /**
     * Update graphical primitives to reflect a change in atomic positions.
     * 
     * molecule argument specifies the (subset of) atoms that have changed.
     */
    @Override
    public void updateCoordinates(Molecule mol) {
        HashSet<vtkObject> vtkObjects = new HashSet<vtkObject>();
        
        for (Atom a : mol.getAtoms()) {
            if (! atomPositions.containsKey(a) ) continue;
            for (GraphicsPrimitivePosition p : atomPositions.get(a)) {
                vtkObject o = p.update(a.getCoordinates());
                if (o != null)
                    vtkObjects.add(o);
            }
        }
        
        for (vtkObject object : vtkObjects) {
            object.Modified();
        }
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

            if (! (atomPositions.containsKey(atom))) atomPositions.put(atom, new Vector<GraphicsPrimitivePosition>());
            atomPositions.get(atom).add( new VTKPointPosition(atomPoints, atomPoints.GetNumberOfPoints() - 1) );
            
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
        
        if (! (atomPositions.containsKey(atom))) atomPositions.put(atom, new Vector<GraphicsPrimitivePosition>());
        atomPositions.get(atom).add( new VTKSpherePosition(sphere) );
        
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
