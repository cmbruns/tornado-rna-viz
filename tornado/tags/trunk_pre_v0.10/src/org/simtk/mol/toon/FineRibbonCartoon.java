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
 * Created on Jun 23, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import java.util.*;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;

public class FineRibbonCartoon extends CompositeCartoon {
    private BasePairOval ovalToon = new BasePairOval();
    private BasePairBrick brickToon = new BasePairBrick();
    private BaseStickTube nucStick = new BaseStickTube();
    private BallAndStickCartoon stickToon = new BallAndStickCartoon();
    private BackboneCurve backboneRibbon = new BackboneCurve();
    private BackboneStick backboneStick = new BackboneStick();
    private RichardsonProteinRibbon proteinRibbon = new RichardsonProteinRibbon();
    
    // private BasePairConnectorStick pairStick = new BasePairConnectorStick();
    private BaseConnectorTube baseConnectors = new BaseConnectorTube();

    public FineRibbonCartoon() {

        addSubToon(backboneStick);

        addSubToon(proteinRibbon);

        addSubToon(stickToon);

        addSubToon(backboneRibbon);
        addSubToon(nucStick);

        // addSubToon(pairStick);
        addSubToon(baseConnectors);
        addSubToon(ovalToon);
        addSubToon(brickToon);
    }

    public void addMolecule(Molecule m) {
        // wireToon.add(m);
        
        if (m instanceof NucleicAcid) {
            NucleicAcid nucleicAcid = (NucleicAcid)m;

            backboneRibbon.addMolecule(nucleicAcid);

            Set<Residue> basePairResidues = new HashSet<Residue>();
            Set<SecondaryStructure> structs = nucleicAcid.displayableStructures();
            for (SecondaryStructure structure : structs)
                if (structure instanceof BasePair) {
                    BasePair basePair = (BasePair) structure;

                    boolean isCanonicalPair = true;
                    if (basePair.getEdge(basePair.getResidue1()) == BasePair.EdgeType.HOOGSTEEN)
                        isCanonicalPair = false;
                    if (basePair.getEdge(basePair.getResidue1()) == BasePair.EdgeType.SUGAR)
                        isCanonicalPair = false;
                    if (basePair.getEdge(basePair.getResidue2()) == BasePair.EdgeType.HOOGSTEEN)
                        isCanonicalPair = false;
                    if (basePair.getEdge(basePair.getResidue2()) == BasePair.EdgeType.SUGAR)
                        isCanonicalPair = false;
                    
                    if (isCanonicalPair) 
                        ovalToon.addBasePair(basePair);
                    else
                        brickToon.addBasePair(basePair);

                    basePairResidues.add(basePair.getResidue1());
                    basePairResidues.add(basePair.getResidue2());
                }
                    
            
            for (Residue residue : ((NucleicAcid)m).residues()) {
                if (! (residue.getResidueType() instanceof Nucleotide)) continue;
                Residue nucleotide = (Residue) residue;
                
                if (basePairResidues.contains(nucleotide))
                    // pairStick.addNucleotide(nucleotide);
                    baseConnectors.addNucleotide(nucleotide);
                else
                    nucStick.addNucleotide(nucleotide);
            }
        }
        
        else if (m instanceof Protein) {
            try {proteinRibbon.addMolecule(m);}
            catch (NoCartoonCreatedException e) {
                // TODO - use another representation
            }
        }
        
        else if (m instanceof Biopolymer) {
            backboneStick.addMolecule(m);
        }
        
        else if (m.isSolvent()) {
            return; // No solvent please
        }
        
        // Everything else ball and stick
        else {
            // try {
                stickToon.addMolecule(m);                
            // } catch (NoCartoonCreatedException exc) {}
        }
        
        updateActors();
    }
    
}
