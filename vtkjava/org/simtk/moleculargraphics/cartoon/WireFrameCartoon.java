/*
 * Created on Apr 26, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import vtk.*;

import java.awt.*;
import java.util.*;

import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;

/** 
 * @author Christopher Bruns
 * 
 * Draw a simple colored line joining each pair of bonded atoms.
 * Plus a cross at each non-bonded atom
 */
public class WireFrameCartoon extends MolecularCartoon {
    static double crossSize = 1.0;

    public void updateCoordinates() {} // TODO
    public vtkProp highlight(Residue r, Color c) {return null;} // TODO
    
    static vtkLineSource lineSource;
    static {
        lineSource = new vtkLineSource();
        lineSource.SetPoint1(-0.5, 0.0, 0.0);
        lineSource.SetPoint2(0.5, 0.0, 0.0);
    }
    
    public vtkAssembly represent(Molecule molecule) {
        vtkAssembly answer = new vtkAssembly();
        
        vtkPoints linePoints = new vtkPoints(); // bond centers
        vtkFloatArray lineNormals = new vtkFloatArray(); // bond directions/lengths
        lineNormals.SetNumberOfComponents(3);

        vtkFloatArray lineScalars = new vtkFloatArray();
        vtkLookupTable lut = new vtkLookupTable();
        lut.SetNumberOfTableValues(256);
        lut.SetRange(1.0, 60.0);
        lut.SetAlphaRange(1.0, 1.0);
        lut.SetValueRange(1.0, 1.0);
        lut.SetHueRange(0.0, 1.0);
        lut.SetSaturationRange(0.5, 0.5);
        lut.Build();
        
        for (Iterator i1 = molecule.getAtoms().iterator(); i1.hasNext(); ) {
            Atom atom = (Atom) i1.next();
            BaseVector3D c = atom.getCoordinates();

            // TODO make colors work
            int colorScalar = (int) (atom.getMass());

            Color col = atom.getDefaultColor();
            lut.SetTableValue(colorScalar, col.getRed()/255.0, col.getGreen()/255.0, col.getBlue()/255.0, 1.0);
            
            // For unbonded atoms, put a cross at atom position
            if (atom.getBonds().size() == 0) {
                // X
                linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
                lineNormals.InsertNextTuple3(crossSize, 0.0, 0.0);
                lineScalars.InsertNextValue(colorScalar);
                // Y
                linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
                lineNormals.InsertNextTuple3(0.0, crossSize, 0.0);
                lineScalars.InsertNextValue(colorScalar);
                // Z
                linePoints.InsertNextPoint(c.getX(), c.getY(), c.getZ());
                lineNormals.InsertNextTuple3(0.0, 0.0, crossSize);
                lineScalars.InsertNextValue(colorScalar);
            }
            // For bonded atoms, draw a line for each bond
            else for (Iterator i2 = atom.getBonds().iterator(); i2.hasNext(); ) {
                Atom atom2 = (Atom) i2.next();
                Vector3D midpoint = c.plus(atom2.getCoordinates()).scale(0.5); // middle of bond
                Vector3D b = c.plus(midpoint).scale(0.5); // middle of half-bond
                Vector3D n = midpoint.minus(c); // direction/length vector

                linePoints.InsertNextPoint(b.getX(), b.getY(), b.getZ());
                lineNormals.InsertNextTuple3(n.getX(), n.getY(), n.getZ());
                lineScalars.InsertNextValue(colorScalar);
            }
        }
        
        vtkPolyData lineData = new vtkPolyData();
        lineData.SetPoints(linePoints);
        lineData.GetPointData().SetNormals(lineNormals);
        lineData.GetPointData().SetScalars(lineScalars);
        
        vtkGlyph3D lineGlyph = new vtkGlyph3D();
        lineGlyph.SetScaleModeToScaleByVector(); // Take length from normal
        lineGlyph.SetVectorModeToUseNormal(); // Take direction from normal
        lineGlyph.SetColorModeToColorByScalar();
        
        lineGlyph.SetInput(lineData);
        lineGlyph.SetSource(lineSource.GetOutput());

        vtkPolyDataMapper bondsMapper = new vtkPolyDataMapper();
        bondsMapper.SetLookupTable(lut);
        bondsMapper.SetScalarRange(0, 255);
        bondsMapper.SetInput(lineGlyph.GetOutput());
        
        vtkActor bondsActor = new vtkActor();
        bondsActor.SetMapper(bondsMapper);
        
        answer.AddPart(bondsActor);
        
        return answer;
    }
}
