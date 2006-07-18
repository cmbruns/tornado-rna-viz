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
    
    public void addMolecule(LocatedMolecule molecule) {
        if (! (molecule instanceof Protein)) return;
        Protein protein = (Protein) molecule;
        
        // First pass, note the secondary structure of each residue
        Set<AminoAcid> betaResidues = new HashSet<AminoAcid>();
        Set<AminoAcid> alphaResidues = new HashSet<AminoAcid>();
        for (SecondaryStructure structure : protein.secondaryStructures()) {

            if (structure instanceof BetaStrand) {
                for (Residue residue : structure.residues()) {
                    if (residue instanceof AminoAcid)
                        betaResidues.add((AminoAcid)residue);
                }                
            }            

            else if (structure instanceof Helix) {
                for (Residue residue : structure.residues()) {
                    if (residue instanceof AminoAcid)
                        alphaResidues.add((AminoAcid)residue);
                }                
            }            
        }

        // Second pass, Spline the whole structure in one go, so directions line up
        int resIndex = 0;
        Spline3D positionSpline = new Spline3D();
        Spline3D normalSpline = new Spline3D();
        AminoAcid previousResidue = null;
        Vector3D previousNormal = null;
        List<AminoAcid> molResidueList = new Vector<AminoAcid>();
        for (Residue residue : protein.residues()) {            
            if (! (residue instanceof AminoAcid)) continue;
            AminoAcid aminoAcid = (AminoAcid) residue;
            resIndex ++;
            molResidueList.add(aminoAcid);

            // Compute normal vector
            // TODO - the normals of beta strands are unsmooth, due
            // ripple effects from ends
            int endFlag = 0; // Default to middle of structure
            if (resIndex == 1) endFlag = -1; // Beginning of chain
            else if (resIndex == protein.residues().size()) endFlag = 1;
            Vector3D normal = ProteinRibbonSegment.hBondNormal(aminoAcid, endFlag);
            if (normal == null) continue;
            
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
                    Vector3D prev = ((AminoAcid)residue.getPreviousResidue()).getAtom("CA").getCoordinates();
                    Vector3D next = ((AminoAcid)residue.getNextResidue()).getAtom("CA").getCoordinates();
                    position = position.plus(position.plus(prev.plus(next))).times(0.25);
                } catch (NullPointerException exc) {}
            }
            positionSpline.addPoint(resIndex, position);

            
            // At the very beginning, add one special point point
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
        List<AminoAcid> residueList = new Vector<AminoAcid>();
        resIndex = 0;
        int structureStart = 1;
        for (AminoAcid aminoAcid : molResidueList) {
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
            List<AminoAcid> residues, 
            ResType type,
            int startIndex,
            Spline3D positionSpline, 
            Spline3D normalSpline
            ) {
        if (residues.size() < 1) return;
        
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
                actorSet.add(strand.getActor());
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
                actorSet.add(helix.getActor());
                subToons.add(helix);
            } catch (NoCartoonCreatedException exc) {}            
        }
        
        else {
            try {
                ProteinCoil coil = new ProteinCoil(residues, ribbonThickness);
                actorSet.add(coil.getActor());
                subToons.add(coil);
            } catch (NoCartoonCreatedException exc) {}                        
        }
    }
}

enum ResType {STRAND, HELIX, COIL, NONE};
