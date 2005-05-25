/*
 * Created on Apr 28, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.util.*;
import java.awt.*;

import vtk.*;

import org.simtk.molecularstructure.*;
import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;

/** 
 * @author Christopher Bruns
 * 
 * Small spheres on each atom with cylinders connecting bonded atoms
 */
public class BallAndStickCartoon extends MolecularCartoon {

    public vtkProp highlight(Residue residue, Color color) {
        // Make it a little bigger than usual
        vtkAssembly answer = represent(residue, 1.05, color);
        return answer;
    }

    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr) {

            vtkAssembly assembly = new vtkAssembly();
            
            // Store each atom type in a separate vtkPoints structure
            Hashtable<String, vtkPoints> elementPoints = new Hashtable<String, vtkPoints>(); // Map all atoms of same element to the same structure
            Hashtable<String, Double> elementRadii = new Hashtable<String, Double>(); // Map element names to sphere radius
            Hashtable<String, Color> elementColors = new Hashtable<String, Color>(); // Map element names to color
            
            // Notice in case we generate zero graphics primitives
            boolean hasContents = false;

            // Fill elementPoints with arrays of atomic centers
            for (int a = 0; a < molecule.getAtomCount(); a++) {
                Atom atom = molecule.getAtom(a);
                Vector3D coord = molecule.getAtom(a).getCoordinates();
                String elementSymbol = molecule.getAtom(a).getElementSymbol();
                
                vtkPoints atomPoints;
                
                // Each time we see a new element, create a set of structures to hold such atoms
                if (!elementPoints.containsKey(elementSymbol)) { // New element type
                    atomPoints = new vtkPoints();
                    elementPoints.put(elementSymbol, atomPoints);
                    elementRadii.put(elementSymbol, new Double(atom.getRadius() * 0.25 * scaleFactor));
                    if (clr == null)
                        elementColors.put(elementSymbol, atom.getDefaultColor());
                    else 
                        elementColors.put(elementSymbol, clr);
                }
                
                atomPoints = (vtkPoints) elementPoints.get(elementSymbol);               
                atomPoints.InsertNextPoint(coord.getX(), coord.getY(), coord.getZ());
                hasContents = true;
            }

            // Generate one Glyph3D for each set of atoms of the same element
            // Why? because it is faster than the first way I tried rendering atoms with 4000 sphere actors
            Enumeration<String> e = elementPoints.keys();
            while (e.hasMoreElements()) {
                  String elementSymbol = (String) e.nextElement();
                  // System.out.println(elementSymbol);

                  vtkPoints atomPoints = (vtkPoints) elementPoints.get(elementSymbol);
                  
                  vtkPolyData points = new vtkPolyData();
                  points.SetPoints(atomPoints);
                  
                  vtkSphereSource sphere = new vtkSphereSource();
                  sphere.SetThetaResolution(7);
                  sphere.SetPhiResolution(7);
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
                  
                  assembly.AddPart(spheresActor);
            }

            
            // Generate sticks for bonds
            // Generate one half-bond poking out of each atom with a bond
            Hashtable<String, vtkPoints> elementBonds = new Hashtable<String, vtkPoints>();
            Hashtable<String, vtkFloatArray> elementBondDirections = new Hashtable<String, vtkFloatArray>();            
            // vtkPoints points = new vtkPoints();
            // vtkFloatArray normals = new vtkFloatArray();
            // normals.SetNumberOfComponents(3);

            // Create all half-bonds from this atom
            float bondCount = 0;
            BONDS: for (int a = 0; a < molecule.getAtomCount(); a++) {
                
                Atom atom1 = molecule.getAtom(a);
                String element1 = atom1.getElementSymbol();
                                
                for (Atom atom2 : atom1.getBonds()) {
                    // Note that sometimes atom2 may not be in this "molecule"
                    
                    Vector3D bondVector = atom2.getCoordinates().minus(atom1.getCoordinates());                    
                    Vector3D midBond = atom1.getCoordinates().plus(bondVector.scale(0.5));

                    // This half-bond cylinder will be centered 1/4 of the way between the atoms
                    Vector3D quarterBond = atom1.getCoordinates().plus(bondVector.scale(0.25));

                    // Segregate bond data by element type
                    if (!elementBonds.containsKey(element1)) {
                        vtkPoints bonds = new vtkPoints();
                        elementBonds.put(element1, bonds);
                        vtkFloatArray normals = new vtkFloatArray();
                        normals.SetNumberOfComponents(3);
                        elementBondDirections.put(element1, normals);
                    }
                    vtkPoints points1 = (vtkPoints) elementBonds.get(element1);
                    vtkFloatArray normals1 = (vtkFloatArray) elementBondDirections.get(element1);
                    
                    // Center point of this half bond
                    points1.InsertNextPoint(quarterBond.getX(), quarterBond.getY(), quarterBond.getZ());

                    // Direction of this half bond
                    // To make the two half-bonds line up flush, choose a deterministic direction between the two atoms
                    boolean atom1First = true;
                    if ( (atom1.getName().compareTo(atom2.getName())) > 0 )
                        atom1First = false;
                    if (atom1First)
                        normals1.InsertNextTuple3(bondVector.getX(), bondVector.getY(), bondVector.getZ());
                    else
                        normals1.InsertNextTuple3(-bondVector.getX(), -bondVector.getY(), -bondVector.getZ());
                    
                    bondCount += 0.5;
                }
            }
            
            e = elementBonds.keys();
            while (e.hasMoreElements()) {
                String elementSymbol = (String) e.nextElement();
                  
                vtkPoints points = (vtkPoints) elementBonds.get(elementSymbol);
                vtkFloatArray normals = (vtkFloatArray) elementBondDirections.get(elementSymbol);
                
	            vtkPolyData glyphInput = new vtkPolyData();
	            glyphInput.SetPoints(points);
	            glyphInput.GetPointData().SetNormals(normals);
	
	            // Make a cylinder to use as the basis of all bonds
	            vtkCylinderSource cylinderSource = new vtkCylinderSource();
	            cylinderSource.SetResolution(4);
	            cylinderSource.SetRadius(0.12 * scaleFactor);
	            cylinderSource.SetHeight(0.505);
	            cylinderSource.SetCapping(0);
	            // Rotate the cylinder so that the cylinder axis goes along the normals during glyphing
	            vtkTransform cylinderTransform = new vtkTransform();
	            cylinderTransform.Identity();
	            cylinderTransform.RotateZ(90);
	            vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
	            cylinderFilter.SetInput(cylinderSource.GetOutput());
	            cylinderFilter.SetTransform(cylinderTransform);
	
	            vtkGlyph3D halfBonds = new vtkGlyph3D();
	            halfBonds.SetInput(glyphInput);
	            halfBonds.SetSource(cylinderFilter.GetOutput());
	
	            halfBonds.SetVectorModeToUseNormal();
	            halfBonds.SetScaleModeToScaleByVector();
	
	            vtkPolyDataMapper bondsMapper = new vtkPolyDataMapper();
	            bondsMapper.SetInput(halfBonds.GetOutput());
	            
	            vtkActor bondsActor = new vtkActor();
	            bondsActor.SetMapper(bondsMapper);
	            Color color = (Color) elementColors.get(elementSymbol);
	            // Color color = Color.yellow;
	            bondsActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
	            
	            if (bondCount > 0) assembly.AddPart(bondsActor);
            }
            
            if (hasContents) return assembly;
            else return null;
        }
}
