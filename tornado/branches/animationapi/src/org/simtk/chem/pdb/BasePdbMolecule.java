/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.chem.pdb;

import java.text.ParseException;
import java.util.*; // Vector

import org.simtk.chem.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule structure.
 */
public class BasePdbMolecule extends BasePdbMolecular implements PdbMolecule {
    private Polymer polymer = BasePolymer.createPolymer(null);
    private Map<String, PdbResidue> keyResidues = new HashMap<String, PdbResidue>();
    
    protected BasePdbMolecule() {} // Empty molecule

    public MoleculeType getMoleculeType() {return polymer.getMoleculeType();}
    
    public Iterable<Residue> residues() {return polymer.residues();}
    public Iterable<SecondaryStructure> secondaryStructures() {
        return polymer.secondaryStructures();
    }
    public void addResidue(Residue residue) {
        // Put residue into index
        if (residue instanceof PdbResidue) {
            PdbResidue pdbResidue = (PdbResidue) residue;
            String key = residueKey(pdbResidue.getThreeLetterCode(), pdbResidue.getResidueNumber(), pdbResidue.getInsertionCode());
            keyResidues.put(key, pdbResidue);
        }
        polymer.addResidue(residue); // delegate to polymer
    }

    public Residue getResidueByNumber(int resNum) {
        return polymer.getResidueByNumber(resNum);
    }

    public void addSecondaryStructure(SecondaryStructure structure) {
        polymer.addSecondaryStructure(structure);
    }
    
    public static PdbMolecule createPdbMolecule(char chainId) {
        BasePdbMolecule answer = new BasePdbMolecule();
        answer.setChainId(chainId);
        return answer;
    }
    
    public PdbAtom getAtom(String pdbLine) throws ParseException {
        String resName = BasePdbAtom.getResidueName(pdbLine);
        int resNum = BasePdbAtom.getResidueNum(pdbLine);
        char iCode = BasePdbAtom.getInsertionCode(pdbLine);
        
        PdbResidue residue = getResidue(resName, resNum, iCode);
        if (residue == null) return null;
        else return residue.getAtomFromPdbLine(pdbLine);
    }
    public PdbAtom creativeGetAtom(String pdbLine) throws ParseException {
        String resName = BasePdbAtom.getResidueName(pdbLine);
        int resNum = BasePdbAtom.getResidueNum(pdbLine);
        char iCode = BasePdbAtom.getInsertionCode(pdbLine);
        
        PdbResidue residue = creativeGetResidue(resName, resNum, iCode);

        PdbAtom atom = residue.getAtomFromPdbLine(pdbLine);
        if (atom == null) {
            atom = residue.creativeGetAtomFromPdbLine(pdbLine);
            addAtom(atom);
        }
        return atom;
    }
    
    private String residueKey(String resName, int resNum, char iCode) {
        return resName + ":" + resNum + ":" + iCode;
    }
    public PdbResidue getResidue(String resName, int resNum, char iCode) {
        return keyResidues.get(residueKey(resName, resNum, iCode));
    }
    public PdbResidue creativeGetResidue(String resName, int resNum, char iCode) {
        PdbResidue answer = getResidue(resName, resNum, iCode);
        if (answer == null) {
            answer = BasePdbResidue.createResidue(resName, getChainId(), resNum, iCode);
            addResidue(answer);
        }
        return answer;
    }
    
    protected MoleculeType guessMoleculeType() {
        // Keep track of residue properties
        int solventCount = 0;
        int proteinCount = 0;
        int nucleicCount = 0;
        int RnaCount = 0;
        int DnaCount = 0;
        int atomO2Count = 0;
        int atomC2Count = 0;
        int residueCount = 0;

        MoleculeType answer = MoleculeType.OTHER;
        
        for (Residue residue : residues()) {
            residueCount ++;
            String threeLetterCode = residue.getThreeLetterCode();
            if (residue.getAtomByName(" O2*") != null)
                atomO2Count ++;
            if (residue.getAtomByName(" C2*") != null)
                atomC2Count ++;
            if (residue.getResidueType() instanceof Nucleotide) {
                nucleicCount ++;
                if (Nucleotide.isDnaCode(threeLetterCode))
                    DnaCount ++;
                if (Nucleotide.isRnaCode(threeLetterCode))
                    RnaCount ++;
            }
            if (residue.getResidueType() instanceof AminoAcid) {
                proteinCount ++;
            }
        }
        
        // Deduce molecule type
        if (residueCount == 1)
            answer = MoleculeType.OTHER;
        
        // If there are protein residues, this is a protein
        else if ((proteinCount >= 1) && (proteinCount >= nucleicCount)) {
            // This is a protein molecule
            answer = MoleculeType.PROTEIN;
        }
 
        // If there are nucleic acid residues, this is a nucleic acid
        else if ((nucleicCount >= 1)) {
            // This is a nucleic acid molecule
            answer = MoleculeType.NUCLEIC_ACID;
            
            // Distinguish between Rna and Dna
            if ( (atomC2Count/nucleicCount > 0.2) // Some residues have ribose atoms
               &&(atomC2Count > 1) ) {
                if (atomO2Count/atomC2Count >= 0.5) {
                    // Rna has O2 atoms, Dna does not
                    answer = MoleculeType.RNA;
                }
                else {
                    answer = MoleculeType.DNA;
                }
            }
            else if (DnaCount > RnaCount) {
                answer = MoleculeType.DNA;
            }
            else if (RnaCount > DnaCount) {
                answer = MoleculeType.RNA;
            }
        }

        // If it has enough residues, it may still be a polymer
        else if ((residueCount - solventCount) > 2) {
            answer = MoleculeType.POLYMER;
        }
        
        return answer;
    }
}
