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
package org.simtk.molecularstructure;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.*; // Vector

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.mvc.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule structure.
 */
public class MoleculeClass 
extends MolecularClass 
implements Molecule 
{
    protected String chainId = null;
    
    public String getPdbChainId() {return chainId;}
    
    protected void setPdbChainId(String c) {this.chainId = c;}
    
    public boolean isSolvent() {

        if (this instanceof Biopolymer) return false;

        // Assume that more than ten atoms is not solvent
        if (atoms().size() > 10) return false;
        if (atoms().size() < 1) return false;

        // Assume that elements other than H,O,P,S are not solvent
        // (by Rasmol definition)
        Set<String> solventSymbols = new HashSet<String>();
        solventSymbols.add("H");
        solventSymbols.add("O");
        // solventSymbols.add("S");
        // solventSymbols.add("P");
        for (Atom atom : atoms()) {
            if (! solventSymbols.contains(atom.getElementSymbol()))
                return false;
        }

        return true;
    }
    
    public MoleculeClass(char chainId) {
        setPdbChainId(""+chainId);
    } // Empty molecule
    
//	public MoleculeClass(PDBAtomSet atomSet) {
//        super(atomSet);
//        createBonds();
//	}

    public static Molecule createFactoryPDBMolecule(URL url) throws IOException {
        InputStream inStream = url.openStream();
        Molecule molecule = createFactoryPDBMolecule(inStream);        
        inStream.close();
        return molecule;
    }

    public static Molecule createFactoryPDBMolecule(String fileName) throws IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
        Molecule molecule = createFactoryPDBMolecule(fileStream);
        fileStream.close();
        return molecule;
    }

    static Molecule createFactoryPDBMolecule(InputStream is) throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
		return createFactoryPDBMolecule(reader);
    }

    /**
     * Read one molecule from a PDB format stream.  Stop before the first atom of the next molecule.
     * @param reader
     * @return
     * @throws IOException
     */
    public static Molecule createFactoryPDBMolecule(LineNumberReader reader) throws IOException {
        Molecule answer = null;
        // PDBAtomSet currentMoleculeAtoms = new PDBAtomSet();
        int atomCount = 0;
        
        char chainIdentifier = '\0';
        // String residueName;
        char insertionCode = '\0';
        int residueIndex = -1;
        
		String pdbLine;
		reader.mark(200);
        
        // NOTE - parsing of the header section of PDB files occurs in the class MoleculeCollection
        // This routine parses coordinates, presumably after the header has already been parsed.
        
        // Use ResidueClass here instead of Residue, so we can use .matchesPdbLine() method
        List<ResidueClass> residues = new Vector<ResidueClass>();
        
		FILE_LINE: while ((pdbLine = reader.readLine()) != null) {

            // Stop parsing after the END record
			if (pdbLine.substring(0,3).equals("END")) {
			    reader.reset(); // Leave the END tag for the next guy
				break FILE_LINE;
			}

            // Stop parsing after the END record
            else if (pdbLine.substring(0,6).equals("ENDMDL")) {
                reader.reset(); // Leave the END tag for the next guy
                break FILE_LINE;
            }

			// Lines with atomic coordinates are used to create new atoms
			else if ((pdbLine.substring(0,6).equals("ATOM  ")) || (pdbLine.substring(0,6).equals("HETATM"))) {
                
                // Separate molecules when:
                //  a) The chain ID changes
                //  b) Known solvent molecule types have a new residue number
                //  c) This is the very first atom

                try {
                    
                    // Always accept the atom into the molecule if it is the very first atom
                    if (atomCount == 0) {  // This is the very first atom for this molecule
                        chainIdentifier = pdbLine.charAt(21);
                        residues.add(new ResidueClass(pdbLine));
                    }
                    else { // not the very first atom
                        
                        // Flush if the molecule chainID has changed
                        if (chainIdentifier != pdbLine.charAt(21)) {
                            reader.reset(); // Put latest atom back into the stream
                            break FILE_LINE;                        
                        }
                        
                        // Has the residue changed?
                        ResidueClass previousResidue = residues.get(residues.size() - 1);
                        if (! previousResidue.matchesPdbLine(pdbLine)) {
                            // Flush molecule if previous residue was solvent
                            if (ResidueClass.isSolvent(previousResidue.getThreeLetterCode())) {
                                reader.reset(); // Put latest atom back into the stream
                                break FILE_LINE;
                            }
                            residues.add(new ResidueClass(pdbLine));
                        }
                    }
                    
                    atomCount ++;
                    Residue currentResidue = residues.get(residues.size() - 1);

                    String atomName = pdbLine.substring(12,16);
                    Atom atom = currentResidue.getAtom(atomName);
                    if (atom == null) { // need to create a new atom
                        AtomClass newAtom = new AtomClass(pdbLine);
                        newAtom.setResidue(currentResidue);
                        atom = newAtom;
                        currentResidue.atoms().add(atom);
                    }
                    else {  // Add position to existing atom
                        atom.addPosition(new AtomPosition(pdbLine));
                        currentResidue.updateAtomPosition(atom);
                    }

                } catch (ParseException exc) {}

			} // ATOM or HETATM record
            
			reader.mark(200); // Commit to reading this far into the file
		}

        // Determine molecule type
        int solventCount = 0;
        int proteinCount = 0;
        int nucleicCount = 0;
        int RNACount = 0;
        int DNACount = 0;
        int atomO2Count = 0;
        int atomC2Count = 0;
        int residueCount = 0;
        for (Residue residue : residues) {
            residueCount ++;
            if (ResidueClass.isSolvent(residue.getThreeLetterCode())) solventCount ++;
            if (ResidueClass.isProtein(residue.getThreeLetterCode())) proteinCount ++;
            if (ResidueClass.isNucleicAcid(residue.getThreeLetterCode())) nucleicCount ++;
            if (ResidueClass.isDNA(residue.getThreeLetterCode())) DNACount ++;
            if (ResidueClass.isRNA(residue.getThreeLetterCode())) RNACount ++;
            if (residue.getAtom("O2*") != null) atomO2Count++;
            if (residue.getAtom("C2*") != null) atomC2Count++;
        }
        
        // If there are protein residues, this is a protein
        if ((proteinCount >= 1) && (proteinCount >= nucleicCount)) {
            // This is a protein molecule
            answer = new PDBProteinClass(chainIdentifier);
        }
        // If there are nucleic acid residues, this is a nucleic acid
        else if ((nucleicCount >= 1)) {
            // This is a nucleic acid molecule
            // Distinguish between RNA and DNA
            if ( (atomC2Count/nucleicCount > 0.2) // Some residues have ribose atoms
               &&(atomC2Count > 1) ) {
                if (atomO2Count/atomC2Count > 0.5) {
                    // RNA has O2 atoms, DNA does not
                    answer = new RNA(chainIdentifier);
                }
                else {
                    answer = new DNA(chainIdentifier);
                }
            }
            else if (DNACount > RNACount) {
                answer = new DNA(chainIdentifier);
            }
            else if (RNACount > DNACount) {
                answer = new RNA(chainIdentifier);
            }
            else answer = new NucleicAcid(chainIdentifier);
        }
        // If it has enough residues, it may still be a polymer
        else if ((residueCount - solventCount) > 2) {
            answer = new BiopolymerClass(chainIdentifier);
        }        
        // OK, it's some other kind of molecule
        else answer = new MoleculeClass(chainIdentifier);

        for (Residue residue : residues) {
            if (answer instanceof Biopolymer) ((Biopolymer)answer).residues().add(residue);
            answer.atoms().addAll(residue.atoms());
        }

        return answer;
    }

    // Create covalent bonds where it seems that they are needed
	void createBonds() {

        // Maybe iodine has the largest "ordinary" covalent radius of 1.33
        double maxCovalentRadius = 1.40;

        // Create a hash for rapid access
        Hash3D<Atom> atomHash = new Hash3D<Atom>(maxCovalentRadius);
        for (Atom a : atoms()) {
            if (! (a instanceof Atom)) continue;
            Atom atom =  a;
            atomHash.put(atom.getCoordinates(), atom);
        }
        
        
        for (Atom atom1 : atoms()) {
            double cutoffDistance = (atom1.getCovalentRadius() + maxCovalentRadius) * 1.5;
            for (Atom atom2 : atomHash.neighborValues(atom1.getCoordinates(), cutoffDistance)) {
                if (atom1.equals(atom2)) continue;
                
                // Make sure the bond length is about right
                double distance;
                distance = atom1.distance(atom2);
                
                double covalentDistance = atom1.getCovalentRadius() + atom2.getCovalentRadius();
                double vanDerWaalsDistance = atom1.getVanDerWaalsRadius() + atom2.getVanDerWaalsRadius();
                // Bond length must be at least 3/4 of that expected
                double minDistance = 0.75 * (covalentDistance);
                // Bond length must be closer to covalent than to van der Waals distance
                if (covalentDistance >= vanDerWaalsDistance) continue;
                double discriminantDistance = vanDerWaalsDistance - covalentDistance;
                double maxDistance = covalentDistance + 0.25 * discriminantDistance;
                if (maxDistance > 1.25 * covalentDistance) maxDistance = 1.25 * covalentDistance;
                if (distance < minDistance) continue;
                if (distance > maxDistance) continue;
                
                // Make sure it is in the same molecule or part
                if ( (atom1 instanceof Atom) && (atom2 instanceof Atom) ) {
                    Atom pdbAtom1 = (Atom) atom1;
                    Atom pdbAtom2 = (Atom) atom2;
                    // Must be in the same chain
//                    if (pdbAtom1.getChainIdentifier() != pdbAtom2.getChainIdentifier()) continue;
//                    // Must be in the same alternate location group
//                    if (pdbAtom1.getAlternateLocationIndicator() != pdbAtom2.getAlternateLocationIndicator()) continue;
                }
                
                // bonds.add(new Bond(atom1, atom2));
                atom1.bonds().add(atom2);
                atom2.bonds().add(atom1);
            }
        }
    }
    
    
}
