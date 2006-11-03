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
 * Created on Jul 13, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import java.util.*;

import org.simtk.geometry3d.Vector3D;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.moleculargraphics.Spline3D;

public class RichardsonProteinRibbon extends MoleculeCartoonClass {
    protected double ribbonThickness = 0.50;
    protected double helixWidth = 2.80;
    protected double strandWidth = 2.20;
    
    public void addMolecule(Molecule molecule) 
    throws NoCartoonCreatedException
    {
        if (! (molecule instanceof Protein)) {
            throw new NoCartoonCreatedException("Not a protein");
        }
        Protein protein = (Protein) molecule;
        
        // First pass, note the secondary structure of each residue
        Set<Residue> betaResidues = new HashSet<Residue>();
        Set<Residue> alphaResidues = new HashSet<Residue>();
        for (SecondaryStructure structure : protein.secondaryStructures()) {

            if (structure instanceof BetaStrand) {
                for (Residue residue : structure.residues()) {
                    if (residue.getResidueType() instanceof AminoAcid)
                        betaResidues.add((Residue)residue);
                }                
            }            

            else if (structure instanceof Helix) {
                for (Residue residue : structure.residues()) {
                    if (residue.getResidueType() instanceof AminoAcid)
                        alphaResidues.add((Residue)residue);
                }                
            }            
        }

        // Second pass, Spline the whole structure in one go, so directions line up
        int resIndex = 0;
        Spline3D positionSpline = new Spline3D();
        Spline3D normalSpline = new Spline3D();
        Residue previousResidue = null;
        Vector3D previousNormal = null;
        List<Residue> molResidueList = new Vector<Residue>();
        for (Residue residue : protein.residues()) {            
            if (! (residue instanceof Residue)) continue;
            Residue aminoAcid = (Residue) residue;
            resIndex ++;
            molResidueList.add(aminoAcid);

            // Compute normal vector
            // TODO - the normals of beta strands are unsmooth, due
            // ripple effects from ends
            int endFlag = 0; // Default to middle of structure
            if (resIndex == 1) endFlag = -1; // Beginning of chain
            else if (resIndex == protein.residues().size()) endFlag = 1;
            Vector3D normal = ProteinRibbonSegment.hBondNormal(aminoAcid, endFlag);
            if (normal == null) {
                // TODO - fudge normal for case where protein is CA only
                continue;
            }
            
            // TODO - it should not be necessary to exclude helices, if this
            // were working correctly
            if (previousNormal != null
                    && (!(alphaResidues.contains(aminoAcid)))
                    ) {
                // Flip normal if it points opposite the previous one
                Vector3D chainDirection = ProteinRibbonSegment.chainDirection(aminoAcid);
                Vector3D prevNormRot = chainDirection.cross(previousNormal).unit();
                Vector3D normRot = chainDirection.cross(normal).unit();
                if (normRot.dot(prevNormRot) < 0) { // More than 90 degrees apart
                    // Flip normal
                    normal = normal.times(-1.0);
                }
            }
            normalSpline.addPoint(resIndex, normal);
            
            // Compute position vector
            Vector3D position = aminoAcid.getAtom("CA").getCoordinates();
            if (betaResidues.contains(aminoAcid)) {
                // Adjust CA position to minimize pleating in beta strands
                // If there are residues before and after, use position that minimizes pleating
                try {
                    Vector3D prev = ((Residue)residue.getPreviousResidue()).getAtom("CA").getCoordinates();
                    Vector3D next = ((Residue)residue.getNextResidue()).getAtom("CA").getCoordinates();
                    position = position.plus(position.plus(prev.plus(next))).times(0.25);
                } catch (NullPointerException exc) {}
            }
            positionSpline.addPoint(resIndex, position);

            
            // At the very beginning, add one special point
            if (resIndex == 1) {
                Vector3D b = aminoAcid.getAtom("N").getCoordinates();
                positionSpline.addPoint(resIndex - 0.5, b);
                normalSpline.addPoint(resIndex - 0.5, normal);
            } 
            
            previousNormal = normal;
            previousResidue = aminoAcid;
        }
        
        // At the very end, add one more point
        try {
            Vector3D e = previousResidue.getAtom("C").getCoordinates();
            positionSpline.addPoint(resIndex + 0.5, e);
            normalSpline.addPoint(resIndex + 0.5, previousNormal);
        }
        catch (NullPointerException exc) {}
        
        
        // Third pass, note stretches of same structure type
        ResType previousType = ResType.NONE;
        List<Residue> residueList = new Vector<Residue>();
        resIndex = 0;
        int structureStart = 1;
        for (Residue aminoAcid : molResidueList) {
            resIndex ++;

            ResType resType = ResType.COIL; // default
            if (betaResidues.contains(aminoAcid)) resType = ResType.STRAND;
            else if (alphaResidues.contains(aminoAcid)) resType = ResType.HELIX;
            
            if (resType != previousType) {
                addResidues(residueList, previousType, structureStart, positionSpline, normalSpline);
                residueList.clear();
                structureStart = resIndex;
            }
            residueList.add(aminoAcid);
            
            previousType = resType;
        }
        // Flush remaining residues
        addResidues(residueList, previousType, structureStart, positionSpline, normalSpline);        
    }
    
    protected void addResidues(
            List<Residue> residues, 
            ResType type,
            int startIndex,
            Spline3D positionSpline, 
            Spline3D normalSpline
            ) {
        if (residues.size() < 2) return;
        
        if (type == ResType.STRAND) {
            try {
                BetaStrandRibbon strand = new BetaStrandRibbon(
                        residues, 
                        ribbonThickness, 
                        strandWidth,
                        startIndex,
                        positionSpline,
                        normalSpline
                        );
                actorSet.add(strand);
                subToons.add(strand);
            } catch (NoCartoonCreatedException exc) {}            
        }
        
        else if (type == ResType.HELIX) {
            try {
                ProteinHelix helix = new ProteinHelix(
                        residues, 
                        ribbonThickness, 
                        helixWidth,
                        startIndex,
                        positionSpline,
                        normalSpline
                        );
                actorSet.add(helix);
                subToons.add(helix);
            } catch (NoCartoonCreatedException exc) {}            
        }
        
        else {
            try {
                ProteinCoil coil = new ProteinCoil(
                        residues, 
                        ribbonThickness * 0.8, 
                        startIndex,
                        positionSpline,
                        normalSpline
                        );
                // ProteinCoil coil = new ProteinCoil(residues, ribbonThickness);
                actorSet.add(coil);
                subToons.add(coil);
            } catch (NoCartoonCreatedException exc) {}                        
        }
    }
}

enum ResType {STRAND, HELIX, COIL, NONE};
