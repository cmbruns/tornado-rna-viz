/* Copyright (c) 2005 Stanford University and Christopher Bruns
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
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Jun 22, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.geometry3d.*;

import java.awt.Color;
import java.util.*;

import vtk.*;

public class BasePairOval extends TensorGlyphCartoon {
    
    // int colorScalar = 1;
    private int baseColorIndex = 1;
    private Map<Color, Integer> colorIndices = new HashMap<Color, Integer>();
    protected ColorScheme colorScheme = 
        SequencingNucleotideColorScheme.SEQUENCING_NUCLEOTIDE_COLOR_SCHEME;
    
    public BasePairOval() {
        super();
        
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(20);
        cylinderSource.SetCapping(1);
        cylinderSource.SetRadius(2.5);
        cylinderSource.SetHeight(1.5);

        // Stretch the cylinder in one direction to make an oval
        vtkTransform transform = new vtkTransform();
        transform.Identity();
        transform.Scale(2.0, 1.0, 1.0);

        vtkTransformPolyDataFilter transformFilter = new vtkTransformPolyDataFilter();
        transformFilter.SetTransform(transform);
        transformFilter.SetInput(cylinderSource.GetOutput());
        
        // Cut cylinder in half for one residue of base-pair
        vtkPlane cutPlane = new vtkPlane();
        cutPlane.SetNormal(1.0, 0, 0);
        cutPlane.SetOrigin(0.2, 0, 0);
        vtkClipPolyData clipper = new vtkClipPolyData();
        clipper.SetClipFunction(cutPlane);
        clipper.SetInput(transformFilter.GetOutput());
        
        // Fill in open hole from clipping operation
        vtkCutter cutter = new vtkCutter();
        cutter.SetInput(transformFilter.GetOutput());
        cutter.SetCutFunction(cutPlane);
        vtkStripper stripper = new vtkStripper();
        stripper.SetInput(cutter.GetOutput());
        stripper.Update();
        vtkPolyData cutPoly = new vtkPolyData();
        cutPoly.SetPoints(stripper.GetOutput().GetPoints());
        cutPoly.SetPolys(stripper.GetOutput().GetLines());
        vtkTriangleFilter cutTriangles = new vtkTriangleFilter();
        cutTriangles.SetInput(cutPoly);
        
        // combine shape and cap
        vtkAppendPolyData append = new vtkAppendPolyData();
        append.AddInput(clipper.GetOutput()); // clipped shape
        append.AddInput(cutTriangles.GetOutput()); // Cap
        
        // Repair normals after clip operation
        vtkPolyDataNormals normals = new vtkPolyDataNormals();
        normals.SetFeatureAngle(80);
        normals.SetInput(append.GetOutput());
        
        setGlyphSource(normals.GetOutput());
        
    }

    public void add(LocatedMolecule molecule) {
        super.add(molecule); // TODO make sure all cartoon classes do this
        if (molecule instanceof NucleicAcid)
            addNucleicAcid((NucleicAcid)molecule);
    }
    
    public void addNucleicAcid(NucleicAcid molecule) {
        Collection<Object> parentObjects = new HashSet<Object>();
        parentObjects.add(molecule);
        for (SecondaryStructure structure : molecule.secondaryStructures())
            if (structure instanceof BasePair)
                addBasePair((BasePair) structure, parentObjects);
    }
    
    public void addBasePair(BasePair basePair, Collection<Object> parentObjects) {
        Nucleotide res1 = basePair.getResidue1();
        Nucleotide res2 = basePair.getResidue2();
        
        Collection<Object> newParentObjects = new HashSet<Object>(parentObjects);
        newParentObjects.add(basePair);
        
        addNucleotide(res1, res2, parentObjects);
        addNucleotide(res2, res1, parentObjects);
    }
    
    public void addNucleotide(Nucleotide res1, Nucleotide res2, Collection<Object> parentObjects) {
        
        Collection<Vector3D> base1Points = new HashSet<Vector3D>();
        try {
            for (Atom baseAtom : res1.get(Nucleotide.baseGroup).atoms()) {
                if (baseAtom instanceof LocatedAtom)
                    base1Points.add( ((LocatedAtom)baseAtom).getCoordinates());
            }
        } catch (InsufficientAtomsException exc) {return;}
        
        Collection<Vector3D> base2Points = new HashSet<Vector3D>();
        try {
            for (Atom baseAtom : res2.get(Nucleotide.baseGroup).atoms()) {
                if (baseAtom instanceof LocatedAtom)
                    base2Points.add( ((LocatedAtom)baseAtom).getCoordinates());
            }
        } catch (InsufficientAtomsException exc) {return;}

        Vector3D pos1, pos2;
        try {
            pos1 = Vector3DClass.centroid(base1Points);
            pos2 = Vector3DClass.centroid(base2Points);
        } catch (InsufficientPointsException exc) {return;}
        
        Vector3D midpoint = pos1.plus(pos2).times(0.5);
        Vector3D basePairDirection = pos1.minus(pos2).unit();

        Collection<Vector3D> basePoints = new HashSet<Vector3D>(base1Points);
        basePoints.addAll(base2Points);
        Vector3D normalDirection;
        try {
            Plane3D pairPlane = Plane3D.bestPlane3D(basePoints);
            normalDirection = pairPlane.getNormal().unit();
        } catch (InsufficientPointsException exc) {
            return;
        }
        
        Vector3D edgeDirection = basePairDirection.cross(normalDirection).unit();
        
        // Normal direction is not yet guaranteed to be orthogonal to basePairDirection
        normalDirection = edgeDirection.cross(basePairDirection).unit();
        
        linePoints.InsertNextPoint(midpoint.x(), midpoint.y(), midpoint.z());
        
        tensors.InsertNextTuple9(
                basePairDirection.x(), basePairDirection.y(), basePairDirection.z(), 
                normalDirection.x(), normalDirection.y(), normalDirection.z(),
                edgeDirection.x(), edgeDirection.y(), edgeDirection.z()
                );

        int glyphIndex = lineScalars.GetNumberOfTuples();
        
        Collection<Object> currentObjects = new HashSet<Object>(parentObjects);
        currentObjects.add(res1);
        
        // Color color = res1.getDefaultColor();
        Color color;
        try {color = colorScheme.colorOf(res1);}
        catch (UnknownObjectColorException exc) {color = Color.white;}
        
        if (! (colorIndices.containsKey(color))) {
            colorIndices.put(color, new Integer(baseColorIndex));
            lut.SetTableValue(baseColorIndex, color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0, 1.0);
            baseColorIndex ++;
        }
        int colorScalar = ((Integer) colorIndices.get(color)).intValue();        

        glyphColors.add(currentObjects, lineData, glyphIndex, colorScalar);
        
        lineScalars.InsertNextValue(colorScalar);
    }
}
