/*
 * Created on May 1, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.Color;
import java.util.*;

import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;

import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * Graphical representation of an RNA molecule that shows cylinders where the double
 * helices are, and a backbone trace where other residues are.
 */
public class RopeAndCylinderCartoon extends MolecularCartoon {

    // Size of ropes connecting the barrels
    double ropeRadius = 0.6;
    double residueSphereRadius = ropeRadius;
    double ropeCylinderRadius = ropeRadius;
    
    // Size of the cylinders surrounding the double helices
    double barrelRadius = 8.0; // 11.5 to consume most atoms
    
    Color defaultRopeColor = new Color(0, 0, 255);
    Color defaultCylinderColor = new Color(0, 255, 255);
    int defaultCylinderResolution = 10;
    int defaultRopeResolution = 5;
    double defaultCylinderOpacity = 0.5;
    
    // Location of residue representation
    Hashtable<Residue, Vector3D> residueCenters = new Hashtable<Residue, Vector3D>();
    // Location of midpoint of link between this residue and the previous one
    Hashtable<Residue, Vector3D> residuePreBonds = new Hashtable<Residue, Vector3D>();
    // Location of midpoint of link between this residue and the subsequent one
    Hashtable<Residue, Vector3D> residuePostBonds = new Hashtable<Residue, Vector3D>();
    // Section of double-helix cylinder assigned to a residue
    Hashtable<Residue, SemiCylinder> residueWedges = new Hashtable<Residue, SemiCylinder>();

    Hashtable<Double, vtkSphereSource> residueSphereSources = new Hashtable<Double, vtkSphereSource>();
    
    // Note which residues are internal to duplexes, at the ends of duplexes, or in loops
    HashSet<Residue> duplexResidues = new HashSet<Residue>();
    HashSet<Residue> endResidues = new HashSet<Residue>();
    HashSet<Residue> loopResidues = new HashSet<Residue>();
    
    /**
     * Keep just one copy of the sphere source for residue objects
     * @return
     */
    vtkSphereSource getResidueSphereSource(double scale) {
        // Only intialize it the first time
        if (!residueSphereSources.containsKey(scale)) {
            vtkSphereSource source = new vtkSphereSource();
            source.SetRadius(residueSphereRadius * scale);
            source.SetPhiResolution(defaultRopeResolution);
            source.SetThetaResolution(defaultRopeResolution);
            residueSphereSources.put(scale, source);
        }
        return residueSphereSources.get(scale);
    }
    
    public vtkProp3D highlight(Residue residue, Color color) {
        return represent(residue, 1.05, color);
    }

    public vtkAssembly representRope(Nucleotide residue, double scaleFactor, Color clr) {
        // Don't draw ropes for strictly internal duplex residues
        if ( duplexResidues.contains(residue) &&
             (! endResidues.contains(residue)) ) {
            return null;
        }

        // Assuming that the residues have already been processed at the molecule level
        if (!residueCenters.containsKey(residue)) {
            // Perhaps this residue is the entire molecule, so residue centers is not populated yet
            Vector3D center = createResidueCenter(residue);
            if (center == null) return null;
        }
        vtkAssembly assembly = new vtkAssembly();
        
        // Put one sphere at the residue's position;
        vtkPolyDataMapper sphereMapper = new vtkPolyDataMapper();
        sphereMapper.SetInput(getResidueSphereSource(scaleFactor).GetOutput());           
        vtkActor sphereActor = new vtkActor();
        sphereActor.SetMapper(sphereMapper);
        Color c;
        if (clr != null) c = clr;
        else c = defaultRopeColor;
        sphereActor.GetProperty().SetColor(c.getRed()/255.0, c.getGreen()/255.0, c.getBlue()/255.0);
        
        Vector3D center = residueCenters.get(residue);
        sphereActor.SetPosition(center.getX(), center.getY(), center.getZ());
        assembly.AddPart(sphereActor);
        
        // Put one bond linking toward the previous residue
        if (residuePreBonds.containsKey(residue)) {
            Vector3D prePoint = residuePreBonds.get(residue);
            
            // Construct cylinder from center to midway to the previous residue
            Vector3D midPoint = prePoint;
            Cylinder cylinder = new Cylinder(center, midPoint, ropeCylinderRadius * scaleFactor);
            vtkActor cylinderActor = GraphicsCylinder.getVtkCylinder(cylinder, c, defaultRopeResolution);
            cylinderActor.GetProperty().SetColor(c.getRed()/255.0, c.getGreen()/255.0, c.getBlue()/255.0);
            cylinderActor.GetProperty().SetOpacity(1.0);
            
            assembly.AddPart(cylinderActor);
        }
        
        // Put one bond linking toward the subsequent residue
        if (residuePostBonds.containsKey(residue)) {
            Vector3D postPoint = residuePostBonds.get(residue);
            
            // Construct cylinder from center to midway to the previous residue
            Vector3D midPoint = postPoint;
            Cylinder cylinder = new Cylinder(postPoint, center, ropeCylinderRadius * scaleFactor);
            vtkActor cylinderActor = GraphicsCylinder.getVtkCylinder(cylinder, c, defaultRopeResolution);
            cylinderActor.GetProperty().SetColor(c.getRed()/255.0, c.getGreen()/255.0, c.getBlue()/255.0);
            cylinderActor.GetProperty().SetOpacity(1.0);
            
            assembly.AddPart(cylinderActor);
        }
        return assembly;
    }

    public vtkAssembly represent(Molecule molecule, double scaleFactor, Color clr) {
        // Render one residue
        if (molecule instanceof Nucleotide) {
            Nucleotide residue = (Nucleotide) molecule;
            vtkAssembly assembly = new vtkAssembly();

            Color color = clr;
            if (clr == null)
                color = defaultCylinderColor;            
            // Create a wedge shape for each helical residue
            if (residueWedges.containsKey(residue)) {
                SemiCylinder startWedge = residueWedges.get(residue); // not yet scaled
                // Scale size of wedge by scale factor
                Vector3D direction = startWedge.getHead().minus(startWedge.getTail()).scale(scaleFactor);
                SemiCylinder wedge = new SemiCylinder(
                        startWedge.getTail().plus(direction),
                        startWedge.getHead().minus(direction),
                        startWedge.getRadius() * scaleFactor,
                        startWedge.getNormal());                        

                // Set the plane to use in cutting the cylinder in half
                vtkPlane clipPlane = new vtkPlane();
                Vector3D normal = wedge.getNormal();
                clipPlane.SetNormal(normal.getX(), normal.getY(), normal.getZ());                
                Vector3D origin = wedge.getHead();
                clipPlane.SetOrigin(origin.getX(), origin.getY(), origin.getZ());
                
                vtkTransformPolyDataFilter cylinderFilter = GraphicsCylinder.getVtkCylinderFilter(wedge, defaultCylinderResolution);

                vtkClipPolyData clipper = new vtkClipPolyData();
                clipper.SetInput(cylinderFilter.GetOutput());
                clipper.SetClipFunction(clipPlane); 
                clipper.GenerateClipScalarsOn();
                clipper.GenerateClippedOutputOn();
                clipper.SetValue(0.5);

                vtkPolyDataMapper wedgeMapper = new vtkPolyDataMapper();
                wedgeMapper.SetInput(clipper.GetClippedOutput());
                wedgeMapper.ScalarVisibilityOff();

                vtkActor wedgeActor = new vtkActor();
                wedgeActor.SetMapper(wedgeMapper);
                
                
                wedgeActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
                wedgeActor.GetProperty().SetOpacity(1.0);
                assembly.AddPart(wedgeActor);
                
                // Close up place where clip was
                vtkCutter cutEdges = new vtkCutter();
                cutEdges.SetInput(cylinderFilter.GetOutput());
                cutEdges.SetCutFunction(clipPlane);
                cutEdges.GenerateCutScalarsOn();
                cutEdges.SetValue(0, 0.5);
                vtkStripper cutStrips = new vtkStripper();
                cutStrips.SetInput(cutEdges.GetOutput());
                cutStrips.Update();
                vtkPolyData cutPoly = new vtkPolyData();
                cutPoly.SetPoints(cutStrips.GetOutput().GetPoints());
                cutPoly.SetPolys(cutStrips.GetOutput().GetLines());

                // Triangle filter is robust enough to ignore the duplicate point at
                // the beginning and end of the polygons and triangulate them.
                vtkTriangleFilter cutTriangles = new vtkTriangleFilter();
                cutTriangles.SetInput(cutPoly);
                vtkPolyDataMapper cutMapper = new vtkPolyDataMapper();
                cutMapper.SetInput(cutPoly);
                cutMapper.SetInput(cutTriangles.GetOutput());
                vtkActor cutActor = new vtkActor();
                cutActor.SetMapper(cutMapper);
                cutActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
                cutActor.GetProperty().SetOpacity(1.0);
                assembly.AddPart(cutActor);
            }

            // Draw the rope part after the wedge part, so that duplex end residues have updated centers
            vtkProp3D ropePart = representRope(residue, scaleFactor, clr);
            if (ropePart != null) assembly.AddPart(ropePart);
            
            return assembly;
        }
        
        // Molecule containing multiple residues
        else if (molecule instanceof NucleicAcid) {
            residueCenters.clear();
            residuePreBonds.clear();
            residuePostBonds.clear();
            
            NucleicAcid rna = (NucleicAcid) molecule;
            vtkAssembly assembly = new vtkAssembly();

            // Create cylinders
            Color color = clr;
            if (clr == null)
                color = defaultCylinderColor;            
            Vector<Hairpin> hairpins = rna.identifyHairpins();
            for (Hairpin pin : hairpins) {
                vtkActor cylinderActor = doubleHelixCylinderActor(pin, color, defaultCylinderResolution);
                cylinderActor.GetProperty().SetOpacity(defaultCylinderOpacity);
                assembly.AddPart(cylinderActor);
            }
            
            // Divide residues into 3 classes
            for (Hairpin pin : hairpins) {
                for (BasePair bp : pin) {
                    for (Residue residue : bp) {
                        // First put cylinder residues into one class
                        duplexResidues.add(residue);
                    }
                }
            }
            // Now distinguish end and loop residues
            for (Residue residue : rna.residues()) {
                if (! duplexResidues.contains(residue)) {
                    loopResidues.add(residue);
                }
                else { // residue is in a duplex
                    Residue next = residue.getNextResidue();
                    Residue previous = residue.getPreviousResidue();
                    if ( (next != null) &&
                         (previous != null) &&
                         duplexResidues.contains(next) &&
                         duplexResidues.contains(previous) ) 
                    {
                        // This is a strictly internal residue
                    }
                    else {
                        // This residue is at the end of a duplex
                        endResidues.add(residue);
                        // Update residue center information
                        createResidueCenter(residue);
                    }
                }
            }
            
            // Create ropes
            // First populate residue positions structure
            Residue previousResidue = null;
            Vector3D previousCenter = null;
            for (Residue residue : rna.residues()) {

                // Don't draw ropes for strictly internal duplex residues
                if ( duplexResidues.contains(residue) &&
                     (! endResidues.contains(residue)) ) {
                    previousResidue = null;
                    continue;
                }
                
                Vector3D center = createResidueCenter(residue);
                
                if (previousResidue != null) {
                    Vector3D midPoint = center.plus(previousCenter).scale(0.5);
                    residuePreBonds.put(residue, midPoint);
                    residuePostBonds.put(previousResidue, midPoint);
                }
                
                previousResidue = residue;
                previousCenter = center;
            }                        
            // Recursively use residue level rendering routine
            for (Residue residue : rna.residues()) {
                if (residue instanceof Nucleotide) {
                    vtkAssembly rope = representRope((Nucleotide)residue, scaleFactor, clr);
                    if (rope != null) {
                        assembly.AddPart(rope);
                    }
                }
            }
            return assembly;
        }
        else return null;
    }

    // Where to center a sphere for a particular residue
    Vector3D createResidueCenter(Residue residue) {
        if (residue == null) return null;
        Vector3D center = residue.getAtom(" C1*").getCoordinates();

        // Update residue center to be on edge of cylinder
        if (endResidues.contains(residue)) {
            if (residueWedges.containsKey(residue)) {
                SemiCylinder wedge = residueWedges.get(residue);
                Vector3D cylinderCenter = wedge.getHead().plus(wedge.getTail()).scale(0.5);
                Vector3D cylinderAxis = wedge.getHead().minus(wedge.getTail()).unit();
                Vector3D normal = wedge.getNormal();
                Vector3D residueCenter = normal.rotate(cylinderAxis, 110 * Math.PI/180.0).plus
                    (cylinderCenter).scale(wedge.getRadius());
                residueCenters.put(residue, residueCenter);
            }
        }
        
        residueCenters.put(residue, center);
        return center;
    }
    
    public vtkActor doubleHelixCylinderActor(Hairpin h, Color color, int resolution) {
        Cylinder helixCylinder = doubleHelixCylinder(h);
        vtkActor cylinderActor =  GraphicsCylinder.getVtkCylinder(helixCylinder, color, resolution);
        return cylinderActor;
    }
    
    /**
     * Given a set of base paired residues, construct cylinder geometry to represent them.
     * 
     * One side effect of this routine is to populate the private residueWedges data structure.
     * 
     * @param v a list of base pairs to be used to define the helix.
     * @return a cylinder that approximates the location of the base stack.
     */
    public Cylinder doubleHelixCylinder(Vector<BasePair> v) {
        if (v.size() < 1) return null;

        // Remember where each residue goes in the cylinder
        Hashtable<BasePair, Vector3D> basePairCentroids = new Hashtable<BasePair, Vector3D>();
        
        // Average the direction of the base plane normals
        // Make the helix axis pass through the centroid of the base pair helix center guesses
        Vector3D helixDirection = new Vector3D(0,0,0);
        Vector3D helixCentroid = new Vector3D(0,0,0);
        // Vector<Vector3D> cylinderPoints = new Vector<Vector3D>();
        for (int p = 0; p < v.size(); p++) {
            BasePair bp = (BasePair) v.get(p);
            
            // Accumulate normals
            Vector3D normal = bp.getBasePlane().getNormal();
            if (normal.dot(helixDirection) < 0) normal = normal.scale(-1.0);
            helixDirection = helixDirection.plus(normal);
            
            // Accumulate centroid
            Vector3D helixCenter = bp.getHelixCenter();
            basePairCentroids.put(bp, helixCenter);
            helixCentroid = helixCentroid.plus(helixCenter);

            // cylinderPoints.addElement(helixCenter);
        }
        helixDirection = helixDirection.unit();
        helixCentroid = helixCentroid.scale(1.0/v.size());
        Vector3D helixOffset = helixCentroid.minus(helixDirection.scale(helixDirection.dot(helixCentroid)));
        
        Line3D helixAxis = new Line3D(helixDirection, helixOffset);
                
        // Find ends of helix
        TreeMap<Double, BasePair> alphaBasePairs = new TreeMap<Double, BasePair>();
        Vector3D somePoint = basePairCentroids.values().iterator().next();
        double minAlpha = somePoint.dot(helixAxis.getDirection());
        double maxAlpha = minAlpha;
        for (BasePair bp : basePairCentroids.keySet()) {
            Vector3D cylinderPoint = basePairCentroids.get(bp);
            double alpha = cylinderPoint.dot(helixAxis.getDirection());
            alphaBasePairs.put(alpha, bp);
            if (alpha > maxAlpha) maxAlpha = alpha;
            if (alpha < minAlpha) minAlpha = alpha;
        }
        // Extend helix to enclose end base pairs
        minAlpha -= 1.6;
        maxAlpha += 1.6;
        Vector3D cylinderHead = helixAxis.getDirection().scale(maxAlpha).plus(helixAxis.getOrigin());
        Vector3D cylinderTail = helixAxis.getDirection().scale(minAlpha).plus(helixAxis.getOrigin());
        
        double cylinderRadius = barrelRadius;
        Cylinder cylinder = new Cylinder(cylinderHead, cylinderTail, cylinderRadius);
        
        // Populate half cylinders for each residue
        // Using TreeMap data structure to ensure that the alpha values are in increasing order
        // Determine range of alpha for each base pair
        Double previousAlpha = null;
        BasePair previousBasePair = null;
        Hashtable<BasePair, Double> basePairStartAlphas = new Hashtable<BasePair, Double>();
        Hashtable<BasePair, Double> basePairEndAlphas = new Hashtable<BasePair, Double>();
        Hashtable<Residue, Vector3D> residueNormals = new Hashtable<Residue, Vector3D>();
        for (Double alpha : alphaBasePairs.keySet()) {
            BasePair basePair = alphaBasePairs.get(alpha);
            
            Double startAlpha;
            if (previousAlpha == null) startAlpha = minAlpha;
            else startAlpha = (alpha + previousAlpha) / 2;
            basePairStartAlphas.put(basePair, startAlpha);
            
            if (previousBasePair != null)
                basePairEndAlphas.put(previousBasePair, startAlpha);
            
            // Create cylinder slicing plane using vector between residue atoms
            Atom atom1 = basePair.getResidue1().getAtom(" C1*");
            Atom atom2 = basePair.getResidue2().getAtom(" C1*");
            Vector3D direction = atom2.getCoordinates().minus(atom1.getCoordinates()).unit();
            // Make sure direction is perpendicular to the helix axis
            direction = direction.minus(helixDirection.scale(helixDirection.dot(direction))).unit();
            residueNormals.put(basePair.getResidue1(), direction);
            residueNormals.put(basePair.getResidue2(), direction.scale(-1));
            
            previousAlpha = alpha;
            previousBasePair = basePair;
        }
        basePairEndAlphas.put(previousBasePair, maxAlpha);  
        // Actually create semicylinders for each residue
        for (BasePair basePair : basePairStartAlphas.keySet()) {
            double startAlpha = basePairStartAlphas.get(basePair);
            double endAlpha = basePairEndAlphas.get(basePair);

            Vector3D head = helixAxis.getDirection().scale(endAlpha).plus(helixAxis.getOrigin());
            Vector3D tail = helixAxis.getDirection().scale(startAlpha).plus(helixAxis.getOrigin());

            Residue residue1 = basePair.getResidue1();
            Residue residue2 = basePair.getResidue2();
            Vector3D normal1 = residueNormals.get(residue1);
            Vector3D normal2 = residueNormals.get(residue2);
            SemiCylinder semiCylinder1 = new SemiCylinder(head, tail, cylinderRadius, normal1);
            SemiCylinder semiCylinder2 = new SemiCylinder(head, tail, cylinderRadius, normal2);
            residueWedges.put(residue1, semiCylinder1);
            residueWedges.put(residue2, semiCylinder2);
            // Update residue center information
            createResidueCenter(residue1);
            createResidueCenter(residue2);
        }
        
        return cylinder;
    }
}
