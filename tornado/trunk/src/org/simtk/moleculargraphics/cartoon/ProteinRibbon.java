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
 * Created on Dec 8, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import java.util.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.protein.*;
import vtk.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Richardson style protein cartoon with arrows for strands, ribbons for helices,
  * and tubes for coil
 */
public class ProteinRibbon extends MoleculeCartoonClass {
    // Need ability to get a set of vtkActors or vtkGlyphs for each atom/residue/molecule
    private HashMap moleculeCartoons = new HashMap();
    
    // Alpha helix parameters
    private double helixWidth = 1.40;
    private double helixTaperLength = 1.0;
    private double helixThickness = 0.4;
    
    // Beta strand parameters
    private double strandThickness = 0.3;
    private double strandWidth = 1.30;
    private double headWidth = 1.50;
    private double headLength = 2.00;

    // Parameters for structures other than helices and strands
    private double coilThickness = 0.7;
    private int coilResolution = 5;
    
    private int splineFactor = 10; // Number of points per residue for smoothing, must be at least 2
    private Spline3D spline = new Spline3D(); // Path of chain
    private Spline3D splineNormal = new Spline3D(); // Orientation of chain

    // Store a unique integer for each residue in the spline
    private HashMap residueSplineIndices = new HashMap();

    // private vtkSphereSource sphereSource = new vtkSphereSource(); // For testing
    
    vtkAssembly assembly = new vtkAssembly(); // All the final rendered stuff wil be in here
    
    public void updateCoordinates() {
        // TODO
    }
    
    public vtkProp3D getVtkProp3D() {return assembly;}
    
    public void addMolecule(Molecule m) {
        if (m instanceof LocatedProtein) {
            insertOneProtein((LocatedProtein) m);
        }
    }

    public void hide() {
        for (Iterator i = moleculeCartoons.values().iterator(); i.hasNext(); ) {
            Collection set = (Collection) i.next();
            for (Iterator j = set.iterator(); j.hasNext(); ) {
                Hidable cartoon = (Hidable) j.next();
                cartoon.hide();
            }
        }
    }

    public void show() {
        for (Iterator i = moleculeCartoons.values().iterator(); i.hasNext(); ) {
            Collection set = (Collection) i.next();
            for (Iterator j = set.iterator(); j.hasNext(); ) {
                Hidable cartoon = (Hidable) j.next();
                cartoon.show();
            }
        }
    }

    public void hide(Molecule m) {
        if (moleculeCartoons.containsKey(m)) {
            Collection cartoons = (Collection) moleculeCartoons.get(m);
            for (Iterator i = cartoons.iterator(); i.hasNext();) {
                Hidable c = (Hidable) i.next();
                c.hide();
            }
        }
        if (m instanceof Biopolymer) {
            Iterator i = ((Biopolymer)m).residues().iterator();
            while (i.hasNext()) {
                Object residue = i.next();
                if (residue instanceof Molecule)
                    hide((Molecule) residue);
            }
        }
    }

    public void show(Molecule m) {
        if (moleculeCartoons.containsKey(m)) {
            Collection cartoons = (Collection) moleculeCartoons.get(m);
            for (Iterator i = cartoons.iterator(); i.hasNext();) {
                Hidable c = (Hidable) i.next();
                c.show();
            }
        }
        if (m instanceof Biopolymer) {
            Iterator i = ((Biopolymer)m).residues().iterator();
            while (i.hasNext()) {
                Object residue = i.next();
                if (residue instanceof Molecule)
                    show((Molecule) residue);
            }
        }
    }

//    private vtkActor insertOneAminoAcid(Residue aa) {
//        // TODO check for secondary structure type
//        vtkActor actor = createCoilActor(aa);
//        assembly.AddPart(actor);
//        addToIndex(aa, new HidableActorClass(actor));
//        return actor;
//    }
//    
    private void insertOneProtein(LocatedProtein protein) {

        createMoleculeSpline(protein);
        
        // Remember which residues have already been rendered, so we can draw coil for the rest
        HashSet renderedResidues = new HashSet();
        
        // Add alpha helices
        for (SecondaryStructure structure : protein.secondaryStructures()) {
            if (structure instanceof Helix)  {
                // System.out.println("One helix found");
                Vector helixResidues = new Vector();
                for (Iterator res = structure.residues().iterator(); res.hasNext(); ) {
                    Residue residue = (Residue) res.next();
                    helixResidues.add(residue);
                    renderedResidues.add(residue);
                }
                Residue[] helixResidueArray = new Residue[helixResidues.size()];
                helixResidueArray = (Residue[]) helixResidues.toArray(helixResidueArray);
                vtkActor helixActor = createHelixActor(helixResidueArray);
                assembly.AddPart(helixActor);
            }
        }
        
        // Add beta strands
        for (SecondaryStructure structure : protein.secondaryStructures()) {
            if (structure instanceof BetaStrand) {
                Vector strandResidues = new Vector();
                for (Iterator res = structure.residues().iterator(); res.hasNext(); ) {
                    Residue residue = (Residue) res.next();
                    strandResidues.add(residue);
                    renderedResidues.add(residue);
                }
                Residue[] strandResidueArray = new Residue[strandResidues.size()];
                strandResidueArray = (Residue[]) strandResidues.toArray(strandResidueArray);
                vtkActor strandActor = createStrandActor(strandResidueArray);
                assembly.AddPart(strandActor);
            }
        }
        
        // Add coils
        Vector coilResidues = new Vector();
        for (Iterator i = protein.residues().iterator(); i.hasNext();) {
            Residue residue = (Residue) i.next();
            if (! (residue.getResidueType() instanceof AminoAcid)) continue;

            if (renderedResidues.contains(residue)) {
                if (coilResidues.size() > 0) {
                    // flush coil residues when a non-coil is found
                    Residue[] coil = new Residue[coilResidues.size()];
                    coil = (Residue[]) coilResidues.toArray(coil);
                    vtkActor coilActor = createCoilActor(coil);
                    assembly.AddPart(coilActor);
                    coilResidues.clear();
                }
            }
            else // this is a coil residue
                coilResidues.add(residue);
        }
        // flush final coil segment
        if (coilResidues.size() > 0) {
            // flush coil residues
            Residue[] coil = new Residue[coilResidues.size()];
            coil = (Residue[]) coilResidues.toArray(coil);
            vtkActor coilActor = createCoilActor(coil);
            assembly.AddPart(coilActor);
            coilResidues.clear();
        }
    }
    
    private vtkActor createCoilActor(Residue[] residues) {
        Vector pathVectors = new Vector();
        Vector normalVectors = new Vector();        

        // Add one actor for each residue
        for (int i = 0; i < residues.length; i ++) {
            Residue residue = residues[i];

            Vector3D[] v = createCoilPath(residue);
            Vector3D[] n = createNormalPath(residue);
            for (int j = 0; j < v.length; j++) {
                pathVectors.add(v[j]);
                normalVectors.add(n[j]);
            }
        }

        Vector3D[] paths = new Vector3D[pathVectors.size()];
        Vector3D[] normals = new Vector3D[normalVectors.size()];        
        paths = (Vector3D[]) pathVectors.toArray(paths);
        normals = (Vector3D[]) normalVectors.toArray(normals);

        return createCoilActor(paths, normals);
    }
    
    private vtkActor createStrandActor(Residue[] residues) {
        Vector pathVectors = new Vector();
        Vector normalVectors = new Vector();        

        // Add one actor for each residue
        for (int i = 0; i < residues.length; i ++) {
            Residue residue = residues[i];

            Vector3D[] v = createCoilPath(residue);
            Vector3D[] n = createNormalPath(residue);
            for (int j = 0; j < v.length; j++) {
                pathVectors.add(v[j]);
                normalVectors.add(n[j]);
            }
        }

        Vector3D[] paths = new Vector3D[pathVectors.size()];
        Vector3D[] normals = new Vector3D[normalVectors.size()];        
        paths = (Vector3D[]) pathVectors.toArray(paths);
        normals = (Vector3D[]) normalVectors.toArray(normals);

        return createStrandActor(paths, normals);
    }
    
    private vtkActor createStrandActor(Vector3D[] path, Vector3D[] normals) {

        vtkPoints strandPoints = new vtkPoints();
        vtkPoints normalPoints = new vtkPoints();
        vtkFloatArray widthScalars = new vtkFloatArray();
        
        widthScalars.SetNumberOfComponents(1); // prevents later crash?
        widthScalars.SetName("sheet data");

        Vector3D previousNormal = null;
        Vector3D previousPath = null;
        Vector3D tip = path[path.length - 1];
        
        // Where to put arrow head?
        // int headStart = (int)(numberOfPoints - 1.0 - splineFactor * headLength / 3.80);
        
        boolean headIsInserted = false;
        int extraHeadPoints = 0;
        
        for (int i = 0; i < path.length; i++) {

            Vector3D normal = normals[i];

            // Adjust path point to be on inner face of strand
            Vector3D innerPoint = path[i].minus(normal.unit().times(strandThickness/2.0));
            
            float width = (float) strandWidth;

            // Arrow shape on head
            double headAlpha = (headLength - tip.distance(innerPoint)) / headLength;
            if (headAlpha >= 0) {
                
                // Insert extra points where the arrow neck flares out
                if (! headIsInserted) {
                    double smidgen = 0.05; // small distance between points
                    
                    // Place neck somewhere between previous and this point
                    double p = headLength - tip.distance(previousPath);
                    double c = headLength - tip.distance(innerPoint);

                    double neckAlpha1 = c/(c-p);
                    double maxAlpha1 = 1.0 - 2.0 * smidgen;
                    double minAlpha1 = smidgen;
                    if (neckAlpha1 < minAlpha1) neckAlpha1 = minAlpha1;
                    if (neckAlpha1 > maxAlpha1) neckAlpha1 = maxAlpha1;

                    double neckAlpha2 = neckAlpha1 + smidgen;

                    Vector3D neck1 = innerPoint.times(neckAlpha1).plus(previousPath.times(1.0 - neckAlpha1));
                    Vector3D neck2 = innerPoint.times(neckAlpha2).plus(previousPath.times(1.0 - neckAlpha2));

                    strandPoints.InsertNextPoint(
                            neck1.getX(),
                            neck1.getY(),
                            neck1.getZ()
                    );
                    normalPoints.InsertNextPoint(
                            normal.getX(),
                            normal.getY(),
                            normal.getZ()
                    );                    
                    widthScalars.SetComponent(i, 0, strandWidth);

                    strandPoints.InsertNextPoint(
                            neck2.getX(),
                            neck2.getY(),
                            neck2.getZ()
                    );
                    normalPoints.InsertNextPoint(
                            normal.getX(),
                            normal.getY(),
                            normal.getZ()
                    );                    
                    widthScalars.InsertNextValue(headWidth);
                    
                    extraHeadPoints = 2;
                    headIsInserted = true;
                }
                
                width = (float) ((1.0 - headAlpha) * headWidth + 0.05);
                System.out.println("strand width = " + width);
            }
            
            // System.out.println(path[i]);
            strandPoints.InsertPoint(i, 
                    innerPoint.getX(),
                    innerPoint.getY(),
                    innerPoint.getZ()
            );

            normalPoints.InsertPoint(i, 
                    normal.getX(),
                    normal.getY(),
                    normal.getZ()
            );
            
            widthScalars.InsertNextValue(width);
            
            previousNormal = normal;
            previousPath = innerPoint;
        }
        
        int numberOfPoints = strandPoints.GetNumberOfPoints();
        vtkCellArray strandCells = new vtkCellArray();
        strandCells.InsertNextCell(numberOfPoints);
        for (int i = 0; i < numberOfPoints; i ++)
            strandCells.InsertCellPoint(i);

        vtkPolyData strandData = new vtkPolyData();
        strandData.SetPoints(strandPoints);
        strandData.SetLines(strandCells);
        strandData.GetPointData().SetNormals(normalPoints.GetData());
        
        // Removing this line prevents application crash during render
        strandData.GetPointData().SetScalars(widthScalars);

        vtkPolyDataMapper strandMapper = mapper;        

        // Remove duplicate points - tubefilter is fussy about this
        vtkCleanPolyData dataCleaner = new vtkCleanPolyData();
        dataCleaner.SetToleranceIsAbsolute(1);
        dataCleaner.SetTolerance(0.001);
        dataCleaner.SetInput(strandData);
        
        vtkRibbonFilter ribbonFilter = new vtkRibbonFilter();
        // ribbonFilter.SetWidth(1.0);
        ribbonFilter.SetVaryWidth(1);
        ribbonFilter.SetInput(dataCleaner.GetOutput());

//        vtkTubeFilter tubeFilter = new vtkTubeFilter();
//        tubeFilter.SetVaryRadius(1);
//        tubeFilter.SetInput(dataCleaner.GetOutput());
//        tubeFilter.SetNumberOfSides(4);

        vtkLinearExtrusionFilter ribbonThicknessFilter = new vtkLinearExtrusionFilter();
        ribbonThicknessFilter.SetCapping(1);
        ribbonThicknessFilter.SetExtrusionTypeToNormalExtrusion();
        ribbonThicknessFilter.SetScaleFactor(strandThickness);
        ribbonThicknessFilter.SetInput(ribbonFilter.GetOutput());
        
        // The polygons on the newly extruded edges are not smoothly shaded
        vtkPolyDataNormals smoothNormals = new vtkPolyDataNormals();
        smoothNormals.SetFeatureAngle(80.0); // Angles smaller than this are smoothed
        smoothNormals.SetInput(ribbonThicknessFilter.GetOutput());
        
        // strandMapper.SetInput(tubeFilter.GetOutput());
        strandMapper.SetInput(smoothNormals.GetOutput());
        strandMapper.SetColorModeToDefault();
        
        vtkActor strandActor = new vtkActor();
        strandActor.SetMapper(strandMapper);
        
        return strandActor;        
    }
    
    private vtkActor createHelixActor(Residue[] residues) {
        Vector pathVectors = new Vector();
        Vector normalVectors = new Vector();        

        // Add one actor for each residue
        for (int i = 0; i < residues.length; i ++) {
            Residue residue = residues[i];

            Vector3D[] v = createCoilPath(residue);
            Vector3D[] n = createNormalPath(residue);
            for (int j = 0; j < v.length; j++) {
                pathVectors.add(v[j]);
                normalVectors.add(n[j]);
            }
        }

        Vector3D[] paths = new Vector3D[pathVectors.size()];
        Vector3D[] normals = new Vector3D[normalVectors.size()];        
        paths = (Vector3D[]) pathVectors.toArray(paths);
        normals = (Vector3D[]) normalVectors.toArray(normals);

        return createHelixActor(paths, normals);
    }
    
    private vtkActor createHelixActor(Vector3D[] path, Vector3D[] normals) {

        vtkPoints helixPoints = new vtkPoints();
        vtkPoints normalPoints = new vtkPoints();
        
        Vector3D previousNormal = null;
        
        for (int i = 0; i < path.length; i++) {
            // Adjust path point to be on inner face of helix
            Vector3D innerPoint = path[i].minus(normals[i].unit().times(helixThickness/2.0));
            
            // System.out.println(path[i]);
            helixPoints.InsertPoint(i, 
                    innerPoint.getX(),
                    innerPoint.getY(),
                    innerPoint.getZ()
            );

            Vector3D normal = normals[i];
            
            normalPoints.InsertPoint(i, 
                    normal.getX(),
                    normal.getY(),
                    normal.getZ()
            );
            
            previousNormal = normal;
        }
        
        vtkCellArray helixCells = new vtkCellArray();
        helixCells.InsertNextCell(path.length);
        for (int i = 0; i < path.length; i ++)
            helixCells.InsertCellPoint(i);

        vtkPolyData helixData = new vtkPolyData();
        helixData.SetPoints(helixPoints);
        helixData.SetLines(helixCells);
        helixData.GetPointData().SetNormals(normalPoints.GetData());

        vtkPolyDataMapper helixMapper = new vtkPolyDataMapper();        

        // Remove duplicate points - tubefilter is fussy about this
        vtkCleanPolyData dataCleaner = new vtkCleanPolyData();
        dataCleaner.SetInput(helixData);
        
        vtkRibbonFilter ribbonFilter = new vtkRibbonFilter();
        ribbonFilter.SetWidth(helixWidth);
        ribbonFilter.SetInput(dataCleaner.GetOutput());

        vtkLinearExtrusionFilter ribbonThicknessFilter = new vtkLinearExtrusionFilter();
        ribbonThicknessFilter.SetCapping(1);
        ribbonThicknessFilter.SetExtrusionTypeToNormalExtrusion();
        ribbonThicknessFilter.SetScaleFactor(helixThickness);
        ribbonThicknessFilter.SetInput(ribbonFilter.GetOutput());
        
        // The polygons on the newly extruded edges are not smoothly shaded
        vtkPolyDataNormals smoothNormals = new vtkPolyDataNormals();
        smoothNormals.SetFeatureAngle(80.0); // Angles smaller than this are smoothed
        smoothNormals.SetInput(ribbonThicknessFilter.GetOutput());
        
        helixMapper.SetInput(smoothNormals.GetOutput());
        
        vtkActor helixActor = new vtkActor();
        helixActor.SetMapper(helixMapper);
        
        return helixActor;        
    }
    
    /**
     * Utility routine for keeping track of which molecule parts go with which graphics.
     * 
     * @param m
     * @param c
     */
    private void addToIndex(Molecule m, Hidable c) {
        if (! (moleculeCartoons.containsKey(m)))
            moleculeCartoons.put(m, new HashSet());
        Collection set = (Collection) moleculeCartoons.get(m);
        set.add(c);
    }
    
//    /**
//     * Generate a smooth tube from before the first residue to after the final residue
//     * @param startResidue
//     * @return
//     */
//    private vtkActor createCoilActor(Residue startResidue) {
//        Vector3D[] vectorPath = createCoilPath(startResidue);
//        Vector3D[] normalPath = createNormalPath(startResidue);
//        return createCoilActor(vectorPath, normalPath);
//    }

    private vtkActor createCoilActor(Vector3D[] vectorPath, Vector3D[] normalPath) {
        // System.out.println("One amino acid");
        vtkPoints coilPoints = new vtkPoints();
        vtkPoints normalPoints = new vtkPoints();
        
        // Vector3D defaultNormal = new Vector3DClass(1, 0, 0);
        Vector3D previousNormal = null;
        
        for (int i = 0; i < vectorPath.length; i++) {
            // System.out.println(vectorPath[i]);
            coilPoints.InsertPoint(i, 
                    vectorPath[i].getX(),
                    vectorPath[i].getY(),
                    vectorPath[i].getZ()
            );

            Vector3D normal = normalPath[i];
            
            normalPoints.InsertPoint(i, 
                    normal.getX(),
                    normal.getY(),
                    normal.getZ()
            );
            
            previousNormal = normal;
        }
        
        vtkCellArray coilCells = new vtkCellArray();
        coilCells.InsertNextCell(vectorPath.length);
        for (int i = 0; i < vectorPath.length; i ++)
            coilCells.InsertCellPoint(i);

        vtkPolyData coilData = new vtkPolyData();
        coilData.SetPoints(coilPoints);
        coilData.SetLines(coilCells);
        coilData.GetPointData().SetNormals(normalPoints.GetData());

        vtkPolyDataMapper coilMapper = new vtkPolyDataMapper();        

        // Remove duplicate points - tubefilter is fussy about this
        vtkCleanPolyData dataCleaner = new vtkCleanPolyData();
        dataCleaner.SetInput(coilData);
        
        vtkTubeFilter tubeFilter = new vtkTubeFilter();
        tubeFilter.SetRadius(coilThickness / 2.0);
        tubeFilter.SetCapping(1);
        tubeFilter.SetInput(dataCleaner.GetOutput());
        tubeFilter.SetNumberOfSides(coilResolution);
        coilMapper.SetInput(tubeFilter.GetOutput());

        vtkActor coilActor = new vtkActor();
        coilActor.SetMapper(coilMapper);
        
        return coilActor;
    }
    
    /**
     * Generate a smooth path for a coil joining the given residues.
     * Path begins one half residue before the start, and ends one half residue after the end.
     * Throws a runtime exception if the residues are not part of a continuous chain.
     * 
     * @param startResidue
     * @return
     */
    private Vector3D[] createCoilPath(Residue startResidue) {
        Vector3D[] answer = new Vector3D[splineFactor];

        int residueIndex = getSplineIndex(startResidue);
        for (int i = 0; i < splineFactor; i++) {
            double t = residueIndex - 0.5 + i / (double)(splineFactor - 1.0);
            answer[i] = spline.evaluate(t);
        }
        return answer;
    }
    
    private Vector3D[] createNormalPath(Residue startResidue) {
        Vector3D[] answer = new Vector3D[splineFactor];

        int residueIndex = getSplineIndex(startResidue);
        for (int i = 0; i < splineFactor; i++) {
            double t = residueIndex - 0.5 + i / (double)(splineFactor - 1.0);
            answer[i] = splineNormal.evaluate(t).unit();
        }
        return answer;
    }
    
    public void clear() {
        assembly = new vtkAssembly();
        moleculeCartoons.clear();
        spline.clear();
    }

    // Alpha carbon position or other canonical location for use in constructing splines
    Vector3D getSplinePosition(Residue residue) throws InsufficientAtomsException {
        
        Vector3D answer = residue.getBackbonePosition();
        
        // TODO - return flattened position for beta strands
        if (residue.isStrand()) {
            try {
                Vector3D previous = ((Residue)residue.getPreviousResidue()).getBackbonePosition();
                Vector3D next = ((Residue)residue.getNextResidue()).getBackbonePosition();
                Vector3D current = residue.getBackbonePosition();
                // Average of midpoints to previous and next backbone positions
                answer = current.plus(current).plus(previous).plus(next).times(0.25);
            } 
            catch (NullPointerException exc) {} // no previous/next? -> drop to default
        }

        return answer;
    }

    Vector3D getSplineNormal(Residue residue) throws InsufficientAtomsException {
        Vector3D answer = new Vector3DClass(1, 0, 0); // default if all else fails
        
        // If the residue is in the middle of a continuous chain, set the normal in the plane of
        // the backbone curvature
        Vector3D p = null;
        Vector3D f = null;
        Vector3D v = residue.getBackbonePosition();

        Residue previousResidue = residue.getPreviousResidue();
        Residue followingResidue = residue.getNextResidue();
        if ( (previousResidue != null) && (previousResidue instanceof Residue) ) {
            p = ((Residue)previousResidue).getBackbonePosition();
        }
        if ( (followingResidue != null) && (followingResidue instanceof Residue) ) {
            f = ((Residue)followingResidue).getBackbonePosition();
        }
        
        // If there are not previous and following residues, use N and C atoms instead
        if (p == null) {
            Atom atom = residue.getAtom(" N  ");
            if (atom != null)
                p = atom.getCoordinates();
        }
        if (f == null) {
            Atom atom = residue.getAtom(" C  ");
            if (atom != null)
                f = atom.getCoordinates();
        }
        
        if ( (p != null) && (f != null) ) {
            Vector3D normal = v.minus(p).unit().plus(v.minus(f).unit()).unit();
            answer = normal;
        }
        
        // Try setting normal to N->C cross C->O, to smooth out beta strands
        if (residue.isStrand()) try {
            Vector3D nc = residue.getAtom(" C  ").getCoordinates().minus(residue.getAtom(" N  ").getCoordinates());
            Vector3D co = residue.getAtom(" O  ").getCoordinates().minus(residue.getAtom(" C  ").getCoordinates());
            answer = nc.cross(co).unit();
        } catch (NullPointerException exc) {} // drop to default if atoms not found
                
        return answer;
    }
    
    int getSplineIndex(Residue aa) {
        // TODO - can cause null pointer exception
        return ((Integer)residueSplineIndices.get(aa)).intValue();
    }

    void createMoleculeSpline(Biopolymer molecule) {
        Residue previousResidue = null;
        Vector3D previousPosition = null;
        Vector3D previousNormal = null;

        int residueIndex = 0;
        RESIDUE: for (Residue residue : molecule.residues()) {
            if (residue.getResidueType() instanceof AminoAcid) {
                residueIndex ++;
                
                Vector3D position = null;
                Vector3D normal = null;
                try {
                    position = getSplinePosition(residue);
                    normal = getSplineNormal(residue);
                }
                catch (InsufficientAtomsException exc) {
                    continue RESIDUE;
                } // could not get position or normal
    
                // Keep normals smooth, especially in beta strands
                // Flip normals that are too far from previous
                // Adjacent helix residues were being flipped using 90 degree
                // cutoff (dot product < 0), that is not right.
                // Only flip those with changes larger than 120 degrees
                // if ( (previousNormal != null) && (previousNormal.dot(normal) < -0.5) ) {
                    // System.out.println("flipped");
                    // System.out.println("  " + previousNormal);
                    // System.out.println("  " + normal);
                    // normal = normal.times(-1.0).v3();
                    // System.out.println("  " + normal);
                // }

                if (previousPosition != null) {
                    // Minimize rotation along direction of chain path
                    // For coil, flip anything greater than 90 degrees away
                    Vector3D chainDirection = position.minus(previousPosition).unit();
                    Vector3D prevNormProj = chainDirection.cross(previousNormal).unit();
                    Vector3D currNormProj = chainDirection.cross(normal).unit();
                    if (prevNormProj.dot(currNormProj) < 0) // greater than 90 degree angle
                        normal = normal.times(-1.0);
                }

                
                // If there is a break in the chain, pad the ends with extra points
                if ((previousResidue == null) || (residue.getPreviousResidue() != previousResidue)) {

                    if (previousResidue != null) {
                        spline.addPoint(residueIndex, previousPosition);
                        splineNormal.addPoint(residueIndex, previousNormal);
                        residueIndex ++;
                    }
                    
                    // This residue is at an N-terminus
                    spline.addPoint(residueIndex, position);
                    splineNormal.addPoint(residueIndex, normal);
                    residueIndex ++;
                }
                
                spline.addPoint(residueIndex, position);
                splineNormal.addPoint(residueIndex, normal);
                residueSplineIndices.put(residue, new Integer(residueIndex));
                
                previousResidue = residue;
                previousPosition = position;
                previousNormal = normal;
            }
        }

        // Pad C-terminus with an extra point, just like any break in the chain
        residueIndex ++;
        if (previousResidue != null) {
            spline.addPoint(residueIndex, previousPosition);
            splineNormal.addPoint(residueIndex, previousNormal);
        }
        
    }
    
    class Spline3D {
        // Set up one spline for each dimension
        vtkCardinalSpline splineX = new vtkCardinalSpline();
        vtkCardinalSpline splineY = new vtkCardinalSpline();
        vtkCardinalSpline splineZ = new vtkCardinalSpline();
        
        public void addPoint(double parameter, Vector3D position) {
            splineX.AddPoint(parameter, position.getX());
            splineY.AddPoint(parameter, position.getY());
            splineZ.AddPoint(parameter, position.getZ());            
        }
        
        public void clear() {
            splineX.RemoveAllPoints();
            splineY.RemoveAllPoints();
            splineZ.RemoveAllPoints();
        }
        
        public Vector3D evaluate(double parameter) {
            return new Vector3DClass(
                    splineX.Evaluate(parameter),
                    splineY.Evaluate(parameter),
                    splineZ.Evaluate(parameter));                    
        }
    }
}
