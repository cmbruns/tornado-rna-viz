/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Sep 7, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.Atom;
import java.util.*;
import vtk.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Bond between two atoms with a nice hemisphere cap at the ends
 */
public class CappedBondActor extends TensorGlyphCartoon {
    double radius = 0.2;
    private Map<Atom, Set<HalfBond>> atomBonds = new HashMap<Atom, Set<HalfBond>>();
    private Map<HalfBond, Integer> bondIndices = new HashMap<HalfBond, Integer>();
    
    public CappedBondActor() {
        // Make a cylinder to use as the basis of all bonds
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        
        double cylHeight = 1.0;
        // Use an even number, for symmetry
        int cylRes = 14;

        cylinderSource.SetResolution(cylRes);        
        
        cylinderSource.SetRadius(radius);
        cylinderSource.SetHeight(cylHeight);
        cylinderSource.SetCapping(0);
        
        vtkSphereSource sphereSource = new vtkSphereSource();
        sphereSource.SetRadius(radius);
        sphereSource.SetCenter(0, 0, 0);
        sphereSource.SetThetaResolution(cylRes);
        sphereSource.SetPhiResolution(7);
        
        // Rotate the sphere so that the equator goes along the end of the cylinder
        vtkTransform sphereTransform = new vtkTransform();
        sphereTransform.Identity();
        sphereTransform.Translate(0, cylHeight * 0.5, 0);
        sphereTransform.RotateX(90);
        vtkTransformPolyDataFilter sphereFilter = new vtkTransformPolyDataFilter();
        sphereFilter.SetInput(sphereSource.GetOutput());
        sphereFilter.SetTransform(sphereTransform);

        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        appendFilter.AddInput(cylinderSource.GetOutput());
        appendFilter.AddInput(sphereFilter.GetOutput());
        
        // Use lines as the glyph primitive
        setGlyphSource(appendFilter.GetOutput());
        // lineGlyph.SetSource(sphereSource.GetOutput());

        colorByScalar(); // Take color from glyph scalar
    }
    
    public void addMolecule(Molecule molecule) {
        if (molecule == null) return;
        
        for (Atom atom : molecule.atoms())
            addAtom(atom);
    }

    protected void addAtom(Atom atom1) {
        if (atom1 == null) return;
        
        // For bonded atoms, draw a line for each bond
        for (Atom atom2 : atom1.bonds())
            addHalfBond(new HalfBond(atom1, atom2));
    }
    
    protected void addHalfBond(HalfBond halfBond) {        
        int glyphIndex = colorScalars.GetNumberOfTuples();
        
        Vector3D quarterBondPos = halfBond.getCenter(); // middle of half-bond
    
        linePoints.InsertNextPoint(quarterBondPos.getX(), quarterBondPos.getY(), quarterBondPos.getZ());
        
        // Set orientation
        double[] t = halfBond.getScaledOrientation();
        tensors.InsertNextTuple9(
                t[0], t[1], t[2],
                t[3], t[4], t[5],
                t[6], t[7], t[8]);

        double colorScalar = getColorIndex(halfBond.atom1);
        colorScalars.InsertNextValue(colorScalar);
        
        // Remember which graphics primitives go with which atoms
        if (! atomBonds.containsKey(halfBond.atom1))
            atomBonds.put(halfBond.atom1, new HashSet<HalfBond>());
        atomBonds.get(halfBond.atom1).add(halfBond);
        if (! atomBonds.containsKey(halfBond.atom2))
            atomBonds.put(halfBond.atom2, new HashSet<HalfBond>());
        atomBonds.get(halfBond.atom2).add(halfBond);
        
        bondIndices.put(halfBond, glyphIndex);
        
        isPopulated = true;        
    }
    
    /**
     * Proposed interface for updating cartoon conformation.
     * TODO - if this works out, make it part of Cartoon interface
     * @param atoms
     */
    public void updateAtomPositions(Collection<Atom> atoms) {
        // TODO - don't update atoms that have not really changed...
        
        // Avoid updating halfbonds more than once...
        Set<HalfBond> updatedHalfBonds = new HashSet<HalfBond>();
        
        for (Atom atom : atoms) {
            for (HalfBond halfBond : atomBonds.get(atom)) {
                // Avoid updating halfbonds more than once...
                if (updatedHalfBonds.contains(halfBond)) continue;

                int index = bondIndices.get(halfBond);
                linePoints.SetPoint(index, halfBond.getCenter().toArray());
                double[] t = halfBond.getScaledOrientation();
                tensors.SetTuple9(index, 
                        t[0], t[1], t[2],
                        t[3], t[4], t[5],
                        t[6], t[7], t[8]);
                updatedHalfBonds.add(halfBond);
            }
        }
        
        linePoints.Modified();
        tensors.Modified();
    }
}

/**
 *  
  * @author Christopher Bruns
  * 
  * Hashable class of ordered pair of atoms
 */
class HalfBond {
    public Atom atom1;
    public Atom atom2;
    
    public HalfBond(Atom atom1, Atom atom2) {
        this.atom1 = atom1;
        this.atom2 = atom2;
    }
    
    public int hashCode() {
        return atom1.hashCode() + 2 * atom2.hashCode();
    }
    
    public boolean equals(Object object) {
        if (object == null) return false;
        if (this == object) return true;
        if (! (object instanceof HalfBond)) return false;
        HalfBond other = (HalfBond) object;
        if (! (atom1.equals(other.atom1)) ) return false;
        if (! (atom2.equals(other.atom2)) ) return false;
        return true;
    }

    public Vector3D getCenter() {
        Vector3D p1 = atom1.getCoordinates();
        Vector3D p2 = atom2.getCoordinates();
        return p1.times(0.75).plus(p2.times(0.25));
    }
    
    public Vector3D getDirection() {
        return atom1.getCoordinates().minus(atom2.getCoordinates()).unit();
    }
    
    private static Vector3D xAxis = new Vector3DClass(1,0,0);
    private static Vector3D yAxis = new Vector3DClass(0,1,0);

    public double[] getScaledOrientation() {
        // Form a basis for the rotation matrix
        Vector3D dir1 = atom1.getCoordinates().minus(atom2.getCoordinates()).times(0.5);

        Vector3D dir2;
        if (Math.abs(dir1.unit().dot(xAxis)) < 0.8)
            dir2 = dir1.cross(xAxis).unit();
        else
            dir2 = dir1.cross(yAxis).unit();
        Vector3D dir3 = dir2.cross(dir1).unit();

        double[] answer = {
                dir2.x(), dir2.y(), dir2.z(),
                dir1.x(), dir1.y(), dir1.z(),
                dir3.x(), dir3.y(), dir3.z()
        };
        
        return answer;
    }
    
}
