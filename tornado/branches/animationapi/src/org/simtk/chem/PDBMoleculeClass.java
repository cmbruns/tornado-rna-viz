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
package org.simtk.chem;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.*; // Vector

import org.simtk.hash3d.*;
import org.simtk.geometry3d.Vector3D;
import org.simtk.geometry3d.Vector3DClass;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule structure.
 */
public class PDBMoleculeClass extends BasePDBMolecular implements PDBMolecule {

    protected PDBMoleculeClass() {} // Empty molecule

    public static PDBMolecule createPDBMolecule(URL url) throws IOException {
        InputStream inStream = url.openStream();
        PDBMolecule molecule = createPDBMolecule(inStream);        
        inStream.close();
        return molecule;
    }

    public static PDBMolecule createPDBMolecule(String fileName) throws IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
        PDBMolecule molecule = createPDBMolecule(fileStream);
        fileStream.close();
        return molecule;
    }

    static PDBMolecule createPDBMolecule(InputStream is) throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
		return createPDBMolecule(reader);
    }

    /**
     * Read one molecule from a PDB format stream.  Stop before the first atom of the next molecule.
     * @param reader
     * @return
     * @throws IOException
     */
    public static PDBMolecule createPDBMolecule(LineNumberReader reader) throws IOException {
        AtomCollection currentMoleculeAtoms = new AtomCollection();
        
        char chainIdentifier = '\0';
        // String residueName;
        char insertionCode = '\0';
        int residueIndex = -1;
        
		String PDBLine;
		reader.mark(200);
        
        // NOTE - parsing of the header section of PDB files occurs in the class MoleculeCollection
        // This routine parses coordinates, presumably after the header has already been parsed.
        
		FILE_LINE: while ((PDBLine = reader.readLine()) != null) {

            // Stop parsing after the END record
			if (PDBLine.substring(0,3).equals("END")) {
			    reader.reset(); // Leave the END tag for the next guy
                PDBMolecule molecule = PDBMoleculeClass.createPDBMolecule(currentMoleculeAtoms); // empty molecule
				return molecule;
			}

			// Lines with atomic coordinates are used to create new atoms
			else if ((PDBLine.substring(0,6).equals("ATOM  ")) || (PDBLine.substring(0,6).equals("HETATM"))) {
                PDBAtom atom;
                try {
                    atom = PDBAtomClass.createAtom(PDBLine);
				} catch (ParseException exc) {
					System.err.println("ERROR: Line " + reader.getLineNumber() + " of PDB file: " + exc);
					continue FILE_LINE;
				}
				
				// Separate molecules when:
				//  a) The chain ID changes
				//  b) Known solvent molecule types have a new residue number
				//  c) This is the very first atom
				if (currentMoleculeAtoms.size() == 0) { // The very first atom of the molecule
					currentMoleculeAtoms.add(atom);

					// residueName = atom.getPDBResidueName();
					insertionCode = atom.getInsertionCode();
					chainIdentifier = atom.getChainIdentifier();
					residueIndex = atom.getResidueNumber();
				}
				else { // not the first atom

				    boolean isSameResidue = ( (atom.getResidueNumber() == residueIndex)
				       &&(atom.getInsertionCode() == insertionCode) );
				    boolean isSameChain = ( atom.getChainIdentifier() == chainIdentifier );
				    boolean isSolvent = PDBResidueClass.isSolvent(atom.getPDBResidueName());

				    if ( isSameChain && (isSameResidue || !isSolvent) ) {
					    // Still the same molecule
						currentMoleculeAtoms.add(atom);
				    }
				    else { // Not the same molecule - return
				        reader.reset(); // Put latest atom back into the stream
                        PDBMolecule molecule = PDBMoleculeClass.createPDBMolecule(currentMoleculeAtoms);
                        return molecule;
				    }
				}
			} // ATOM or HETATM record
			
            
			reader.mark(200); // Commit to reading this far into the file
		}

        PDBMolecule answer =  PDBMoleculeClass.createPDBMolecule(currentMoleculeAtoms);
        
        if (answer != null) answer.setChainId(chainIdentifier);
        
        return answer;
    }

    static PDBMolecule createPDBMolecule(AtomCollection bagOfAtoms) {
        if (bagOfAtoms == null) return null;
        if (bagOfAtoms.size() == 0) return null;
        
        // Determine if molecule is protein, nucleic acid, biopolymer, or other
        HashSet<String> residues = new HashSet<String>();
        int solventCount = 0;
        int proteinCount = 0;
        int nucleicCount = 0;
        int RNACount = 0;
        int DNACount = 0;
        int atomO2Count = 0;
        int atomC2Count = 0;
        
        int residueCount = 0;
        // Count the residues of each type
        for (Atom a : bagOfAtoms) {
            PDBAtom atom = (PDBAtom) (a);
            
            // Look for 2' hydroxyl atom to distinguish DNA from RNA
            if (atom.getAtomName().trim().equals("O2*")) atomO2Count ++;
            if (atom.getAtomName().trim().equals("C2*")) atomC2Count ++;            
            
			String residueKey = "" + atom.getChainIdentifier() + atom.getResidueNumber() + atom.getInsertionCode();
			if (residues.contains(residueKey)) continue; // Already saw this residue before
			
			if (PDBResidueClass.isSolvent(atom.getPDBResidueName())) solventCount ++;
			if (PDBResidueClass.isProtein(atom.getPDBResidueName())) proteinCount ++;
			if (PDBResidueClass.isNucleicAcid(atom.getPDBResidueName())) nucleicCount ++;
			if (PDBResidueClass.isDNA(atom.getPDBResidueName())) DNACount ++;
			if (PDBResidueClass.isRNA(atom.getPDBResidueName())) RNACount ++;
			
			residues.add(residueKey);
			residueCount ++;
        }

        if (residueCount == 1)
            return PDBResidueClass.createResidue(bagOfAtoms);
        
        // If there are protein residues, this is a protein
        if ((proteinCount >= 1) && (proteinCount >= nucleicCount)) {
            // This is a protein molecule
            return new PDBProteinClass(bagOfAtoms);
        }
 
        // If there are nucleic acid residues, this is a nucleic acid
        if ((nucleicCount >= 1)) {
            // This is a nucleic acid molecule
            // Distinguish between RNA and DNA
            if ( (atomC2Count/nucleicCount > 0.2) // Some residues have ribose atoms
               &&(atomC2Count > 1) ) {
                if (atomO2Count/atomC2Count > 0.5) {
                    // RNA has O2 atoms, DNA does not
                    return  new RNA(bagOfAtoms);
                }
                else {
                    return new DNA(bagOfAtoms);
                }
            }
            if (DNACount > RNACount) {
                return new DNA(bagOfAtoms);
            }
            if (RNACount > DNACount) {
                return new RNA(bagOfAtoms);
            }
            return new NucleicAcid(bagOfAtoms);
        }

        // If it has enough residues, it may still be a polymer
        if ((residueCount - solventCount) > 2) {
            return new BiopolymerClass(bagOfAtoms);
        }
        
        // OK, it's some other kind of molecule
        return new PDBMoleculeClass(bagOfAtoms);
    }
}
