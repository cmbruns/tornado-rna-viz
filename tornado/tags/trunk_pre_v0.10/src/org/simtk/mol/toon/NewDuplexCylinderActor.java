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
 * Created on Jul 31, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import java.awt.Color;
import java.util.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.InsufficientAtomsException;
import org.simtk.molecularstructure.atom.Atom;
import org.simtk.molecularstructure.nucleicacid.BasePair;
import org.simtk.molecularstructure.nucleicacid.Duplex;
import vtk.*;

public class NewDuplexCylinderActor extends ActorCartoonClass {

    // Size of the cylinders surrounding the double helices
    static double defaultbarrelRadius = 8.0; // 11.5 to consume most atoms
    Color defaultCylinderColor = new Color(0, 255, 255, 255);
    double defaultCylinderResolution = 4.0;
    double defaultCylinderOpacity = 1.0;

    
    NewDuplexCylinderActor(Duplex duplex) {
        if (duplex == null) return;
        
        Cylinder helixCylinder;
        try {
            helixCylinder = doubleHelixCylinder(duplex);
            vtkActor cylinderActor =  getVtkMeshCylinder(helixCylinder, defaultCylinderColor, defaultCylinderResolution);
            cylinderActor.GetProperty().SetOpacity(defaultCylinderOpacity);
            actor = cylinderActor;
            // actor.GetProperty().SetRepresentationToWireframe();
            // actor.SetMapper(mapper);
            isPopulated = true;
        } catch (InsufficientPointsException exc) {}
    }

    
    /**
     * Given a set of base paired residues, construct cylinder geometry to represent them.
     * 
     * One side effect of this routine is to populate the private residueWedges data structure.
     * 
     * @return a cylinder that approximates the location of the base stack.
     */
    static public Cylinder doubleHelixCylinder(Duplex h) 
    throws InsufficientPointsException
    {
        if (h.basePairs().size() < 1) return null;

        // Remember where each residue goes in the cylinder
        Map<BasePair, Vector3D> basePairCentroids = new HashMap<BasePair, Vector3D>();
        
        // Average the direction of the base plane normals
        // Make the helix axis pass through the centroid of the base pair helix center guesses
        Vector3D helixDirection = new Vector3DClass(0,0,0);
        Vector3D helixCentroid = new Vector3DClass(0,0,0);
        // Vector<Vector3D> cylinderPoints = new Vector<Vector3D>();
        BASEPAIR: for (Iterator iterBasePair = h.basePairs().iterator(); iterBasePair.hasNext(); ) {
        // for (int p = 0; p < h.basePairs().size(); p++) {
            BasePair bp = (BasePair) iterBasePair.next();
            
            // Accumulate normals
            try {
                Plane3D basePlane = bp.getBasePlane();
                MutableVector3D normal = new Vector3DClass(bp.getBasePlane().getNormal());
                if (normal.dot(helixDirection) < 0) normal.timesEquals(-1.0);
                helixDirection = helixDirection.plus(normal);
            
                // Accumulate centroid
                Vector3D helixCenter = bp.getHelixCenter();
                basePairCentroids.put(bp, helixCenter);
                helixCentroid = helixCentroid.plus(helixCenter);

            } catch (InsufficientAtomsException exc) {
                continue BASEPAIR; // skip pairs with ill defined planes
            }

            // cylinderPoints.addElement(helixCenter);
        }
        helixDirection = helixDirection.unit();
        helixCentroid = helixCentroid.times(1.0/h.basePairs().size());
        Vector3D helixOffset = helixCentroid.minus(helixDirection.times(helixDirection.dot(helixCentroid)));
        
        Line3D helixAxis = new Line3D( new Vector3DClass(helixDirection), new Vector3DClass(helixOffset) );
                
        // Find ends of helix
        TreeMap alphaBasePairs = new TreeMap();
        
        if (basePairCentroids.size() < 1) throw new InsufficientPointsException();

        Vector3D somePoint = (Vector3DClass) basePairCentroids.values().iterator().next();

        double minAlpha = somePoint.dot(helixAxis.getDirection());
        double maxAlpha = minAlpha;
        for (Iterator i = basePairCentroids.keySet().iterator(); i.hasNext(); ) {
            BasePair bp = (BasePair) i.next();
            Vector3DClass cylinderPoint = (Vector3DClass) basePairCentroids.get(bp);
            double alpha = cylinderPoint.dot(helixAxis.getDirection());
            alphaBasePairs.put(new Double(alpha), bp);
            if (alpha > maxAlpha) maxAlpha = alpha;
            if (alpha < minAlpha) minAlpha = alpha;
        }
        // Extend helix to enclose end base pairs
        minAlpha -= 1.6;
        maxAlpha += 1.6;
        Vector3D cylinderHead = helixAxis.getDirection().times(maxAlpha).plus(helixAxis.getOrigin());
        Vector3D cylinderTail = helixAxis.getDirection().times(minAlpha).plus(helixAxis.getOrigin());
        
        double cylinderRadius = defaultbarrelRadius;
        Cylinder cylinder = new Cylinder( new Vector3DClass(cylinderHead), new Vector3DClass(cylinderTail), cylinderRadius);
        
        // Populate half cylinders for each residue
        // Using TreeMap data structure to ensure that the alpha values are in increasing order
        // Determine range of alpha for each base pair
        Double previousAlpha = null;
        BasePair previousBasePair = null;
        Hashtable basePairStartAlphas = new Hashtable();
        Hashtable basePairEndAlphas = new Hashtable();
        Hashtable residueNormals = new Hashtable();
        for (Iterator i = alphaBasePairs.keySet().iterator(); i.hasNext(); ) {
            double alpha = ((Double) i.next()).doubleValue();
            BasePair basePair = (BasePair) alphaBasePairs.get(new Double(alpha));
            
            double startAlpha;
            if (previousAlpha == null) startAlpha = minAlpha;
            else startAlpha = (alpha + previousAlpha.doubleValue()) / 2;
            basePairStartAlphas.put(basePair, new Double(startAlpha));
            
            if (previousBasePair != null)
                basePairEndAlphas.put(previousBasePair, new Double(startAlpha));
            
            // Create cylinder slicing plane using vector between residue atoms
            Atom atom1 = basePair.getResidue1().getAtom(" C1*");
            Atom atom2 = basePair.getResidue2().getAtom(" C1*");
            Vector3D direction = atom2.getCoordinates().minus(atom1.getCoordinates()).unit();
            // Make sure direction is perpendicular to the helix axis
            direction = direction.minus(helixDirection.times(helixDirection.dot(direction))).unit();
            residueNormals.put(basePair.getResidue1(), direction);
            residueNormals.put(basePair.getResidue2(), direction.times(-1));
            
            previousAlpha = new Double(alpha);
            previousBasePair = basePair;
        }
        basePairEndAlphas.put(previousBasePair, new Double(maxAlpha));  

        return cylinder;
    }
    
    protected static vtkPolyData getVtkMeshCylinder(double radius, double length, double resolution) {
        vtkPoints linePoints = new vtkPoints();
        int nCapPoints = (int)Math.ceil(radius/resolution) + 1;
        double capStep = radius / nCapPoints;
        // Top of cylinder
        for (int i = 0; i <= nCapPoints; i++) {
            linePoints.InsertNextPoint(i * capStep, 0, length * 0.5);
        }
        int nSidePoints = (int)Math.ceil(length/resolution) + 1;
        double sideStep = length / nSidePoints;
        // Side of cylinder
        for (int i = 0; i <= nSidePoints; ++i) {
            linePoints.InsertNextPoint(radius, 0, length * 0.5 - i * sideStep);            
        }
        // Bottom of cylinder
        for (int i = 0; i <= nCapPoints; i++) {
            linePoints.InsertNextPoint(radius - i * capStep, 0, -length * 0.5);
        }
        
        vtkPolyData lineData = new vtkPolyData();
        lineData.SetPoints(linePoints);
        vtkCellArray lineCells = new vtkCellArray();
        lineCells.InsertNextCell(lineData.GetNumberOfPoints());
        for (int i = 0; i < lineData.GetNumberOfPoints(); i ++)
            lineCells.InsertCellPoint(i);
        lineData.SetLines(lineCells);
        
        int numberOfSides = (int)Math.ceil(2 * Math.PI * radius / resolution) + 1;
        vtkRotationalExtrusionFilter extrusionFilter = new vtkRotationalExtrusionFilter();
        extrusionFilter.SetAngle(360.0);
        extrusionFilter.SetResolution(numberOfSides);
        extrusionFilter.SetInput(lineData);
        
        // Get rid of seam
        vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
        cleanFilter.SetInput(extrusionFilter.GetOutput());
        cleanFilter.SetTolerance(0.001);
        
        // Move Z axis to Y axis, for consistency with vtkCylinderSource
        vtkTransform transform = new vtkTransform();
        transform.Identity();
        transform.RotateX(90.0);
        
        vtkTransformPolyDataFilter transformFilter = new vtkTransformPolyDataFilter();
        transformFilter.SetInput(cleanFilter.GetOutput());
        transformFilter.SetTransform(transform);
        
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetInput(transformFilter.GetOutput());
        normalsFilter.SetFeatureAngle(88.0);
        normalsFilter.FlipNormalsOn();
        
        return normalsFilter.GetOutput();        
    }

    static public vtkPolyData getVtkMeshCylinder(Cylinder c, double resolution) {
        vtkPolyData cylinderSource = getVtkMeshCylinder(c.getRadius(), c.getHead().distance(c.getTail()), resolution);

        Vector3DClass center = new Vector3DClass( c.getHead().plus(c.getTail()).times(0.5) );
        
        // Set orientation
        // vtkCylinderSource begins along the y axis
        Vector3DClass direction = new Vector3DClass( c.getHead().minus(c.getTail()).unit() );
        Vector3DClass xAxis = new Vector3DClass(1,0,0);
        Vector3DClass yAxis = new Vector3DClass(0,1,0);
        Vector3DClass zAxis = new Vector3DClass(0,0,1);
        // Project vector onto Y plane
        Vector3DClass yProjection = new Vector3DClass( new Vector3DClass(direction.getX(), 0, direction.getZ()).unit() );
        double radiansToDegrees = 180.0 / Math.PI;
        // How far is cylinder tilted from straight up?
        double yRotationAngle = Math.acos(yAxis.dot(direction)) * radiansToDegrees;
        double zRotationAngle = Math.acos(yProjection.dot(zAxis)) * radiansToDegrees;
        if (direction.getX() < 0) zRotationAngle *= -1.0;        
        vtkTransform orientation = new vtkTransform();
        // orientation.Identity();
        orientation.Translate(center.getX(), center.getY(), center.getZ());
        orientation.RotateY(zRotationAngle);
        orientation.RotateX(yRotationAngle); // Tilted this far from straight up
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        cylinderFilter.SetInput(cylinderSource);
        cylinderFilter.SetTransform(orientation);
        
        return cylinderFilter.GetOutput();
    }
    
    public vtkActor getVtkMeshCylinder(Cylinder c, Color color, double resolution) {
        vtkPolyData cylinderFilter = getVtkMeshCylinder(c, resolution);
        
        // vtkPolyDataMapper cylinderMapper = new vtkPolyDataMapper();
        mapper.SetInput(cylinderFilter);
        // vtkActor cylinderActor = new vtkActor();
        actor.SetMapper(mapper);
        // cylinderActor.GetProperty().SetOpacity(0.5);
        if (color != null)
            actor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
        return actor;
    }

}
