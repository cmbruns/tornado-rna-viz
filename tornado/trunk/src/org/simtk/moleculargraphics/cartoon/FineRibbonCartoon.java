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
package org.simtk.moleculargraphics.cartoon;

import java.util.*;
import java.util.HashSet;
import java.util.Iterator;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.molecularstructure.atom.*;

public class FineRibbonCartoon extends CompositeCartoon {
    private BasePairOval ovalToon = new BasePairOval();
    private NucleotideStickCartoon nucStick = new NucleotideStickCartoon();
    private BallAndStickCartoon stickToon = new BallAndStickCartoon();
    private BackboneCurve backboneRibbon = new BackboneCurve();
    private BackboneStick backboneStick = new BackboneStick();
    private BasePairConnectorStick pairStick = new BasePairConnectorStick();
    private RichardsonProteinRibbon proteinRibbon = new RichardsonProteinRibbon();
    
    public FineRibbonCartoon() {

        addSubToon(backboneStick);

        addSubToon(proteinRibbon);

        addSubToon(stickToon);

        addSubToon(backboneRibbon);
        addSubToon(nucStick);

        addSubToon(pairStick);
        addSubToon(ovalToon);
    }

    public void addMolecule(LocatedMolecule m) {
        // wireToon.add(m);
        
        if (m instanceof NucleicAcid) {
            NucleicAcid nucleicAcid = (NucleicAcid)m;

            backboneRibbon.addMolecule(nucleicAcid);
            actorSet.addAll(backboneRibbon.vtkActors());

            Set<Nucleotide> basePairResidues = new HashSet<Nucleotide>();
            for (SecondaryStructure structure : nucleicAcid.secondaryStructures())
                if (structure instanceof BasePair) {
                    BasePair basePair = (BasePair) structure;
                    ovalToon.addBasePair(basePair);

                    basePairResidues.add(basePair.getResidue1());
                    basePairResidues.add(basePair.getResidue2());
                }
                    
            
            for (Residue residue : ((NucleicAcid)m).residues()) {
                if (! (residue instanceof Nucleotide)) continue;
                Nucleotide nucleotide = (Nucleotide) residue;
                
                if (basePairResidues.contains(nucleotide))
                    pairStick.addNucleotide(nucleotide);
                else
                    nucStick.addNucleotide(nucleotide);
            }
        }
        
        else if (m instanceof Protein) {
            proteinRibbon.addMolecule(m);
            actorSet.addAll(proteinRibbon.vtkActors());
        }
        
        else if (m instanceof Biopolymer) {
            backboneStick.addMolecule(m);
        }
        
        else {
            // Skip solvent
        	Iterator<LocatedAtom> atomIt = m.getAtomIterator();
        	if (atomIt.hasNext()) {
        		LocatedAtom atom = atomIt.next();
        		if ((atom instanceof PDBAtom)&& PDBResidueClass.isSolvent(((PDBAtom)atom).getPDBResidueName())){
        			return;
        		}
        	}
            stickToon.addMolecule(m);
        }
    }
    
}
