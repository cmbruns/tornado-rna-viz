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
import java.util.*;
import java.net.*;
import org.simtk.rnaml.RnamlDocument;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;

import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * A collection of one or more molecules, as might be found in a PDB file.
 */
public class MoleculeCollection 
extends MolecularClass
{
    // Set<Atom> atoms = new LinkedHashSet<Atom>();
    List<Molecule> molecules = new Vector<Molecule>();

    // Vector3D centerOfMass = new Vector3DClass();
    // double mass = 0;
    
    private String mTitle = "";
    public String getTitle() {return mTitle;}
    public void setTitle(String t) {mTitle = t;}
    
    private String mPdbId = null;
    public String getPdbId() {return mPdbId;}
    public void setPdbId(String i) {mPdbId = i;}

    private String m_inputStructureFileName = null;
    public String getInputStructureFileName() {return m_inputStructureFileName;}
    public void setInputStructureFileName(String s) {m_inputStructureFileName = s;}

    @Override
    public double getMass() {
        double mass = 0.0;
        for (Molecule molecule : molecules()) {
            mass += molecule.getMass();
        }
        return mass;
    }
    
    @Override
    public Vector3D getCenterOfMass() {
        Vector3D com = new Vector3DClass(0,0,0);
        double totalMass = 0.0;
        for (Molecule molecule : molecules()) {
            double mass = molecule.getMass();
            com = com.plus(molecule.getCenterOfMass().times(mass));
            totalMass += mass;
        }
        if (totalMass == 0.0) return null;
        else return com.times(1.0 / totalMass);
    }
    
    public Collection<Molecule> molecules() {return molecules;}
    
    public Molecule getMolecule(int i) {return (Molecule) molecules.get(i);}
    
    public void loadPDBFormat(String fileName) throws FileNotFoundException, IOException, InterruptedException {
		FileInputStream fileStream = new FileInputStream(fileName);
		loadPDBFormat(fileStream);
        fileStream.close();
	}

    public void loadPDBFormat(URL url) throws IOException, InterruptedException {
        InputStream inStream = url.openStream();
        loadPDBFormat(inStream);
        inStream.close();
    }

    /**
     * @param is
     * @throws IOException
     * @throws InterruptedException
     */
    public void loadPDBFormat(InputStream is) throws IOException, InterruptedException {
        
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
        
        // TODO read header information, specfically the name(s) of the molecules
        String PDBLine;
        String title = "";
        
        Collection<SecondaryStructure> secondaryStructures = new Vector<SecondaryStructure>();
        Map<SecondaryStructure, String> secondaryStructureStarts = new HashMap<SecondaryStructure, String>();
        Map<SecondaryStructure, String> secondaryStructureEnds = new HashMap<SecondaryStructure, String>();
        Map<String, Collection<Molecule> > chainMolecules = new HashMap<String, Collection<Molecule> >();
        
        reader.mark(200);
        FILE_LINE: while ((PDBLine = reader.readLine()) != null) {

            // Stop parsing after the END record
            if (PDBLine.substring(0,3).equals("END")) {
                reader.reset(); // Leave the END tag for the next guy
                break FILE_LINE;
            }

            // Stop parsing after the END record
            else if (PDBLine.substring(0,6).equals("ENDMDL")) {
                reader.reset(); // Leave the END tag for the next guy
                break FILE_LINE;
            }

            // If we get to ATOM records, we need to stop and send the stream to the Molecule parser
            else if ((PDBLine.substring(0,6).equals("ATOM  ")) || (PDBLine.substring(0,6).equals("HETATM"))) {
                // Oops, we passed the header, and are now in the coordinates
                // Pass this off to the Molecule parser
                reader.reset();
                break FILE_LINE;
            }

            else if (PDBLine.substring(0,6).equals("TITLE ")) {
                String titlePart = PDBLine.substring(10,60).trim();
                if (titlePart.length() > 0) {
                    if (title.length() > 0) title = title + " " + titlePart;
                    else title = titlePart;
                }
            }

            else if (PDBLine.substring(0,6).equals("HEADER")) {
                if (PDBLine.length() >= 66) {
                    String id = PDBLine.substring(62,66).trim();
                    if (id.length() == 4) {
                    	setPdbId(id);
                    	//title = "PDB ID "+id+"-- "+title;
                    }
                }
            }

            else if (PDBLine.substring(0,6).equals("HETNAM")) {
                String newHet = PDBLine.substring(11,14).trim();
                if (newHet.length() > 0) {
                    if (!ResidueClass.knownHetatms.contains(newHet)) {
                    	ResidueClass.knownHetatms.add(newHet);
                    }
                }
            }

            // Parse secondary structure
            else if (PDBLine.substring(0,6).equals("HELIX ")) {

//                String helixID = PDBLine.substring(11, 14);
//
//                String initResidueName = PDBLine.substring(15, 18);
//                String initResidueChain = PDBLine.substring(19, 20);
//                int initResidueNumber = new Integer(PDBLine.substring(21, 25).trim()).intValue();
//                String initICode = PDBLine.substring(25, 26);
//                
//                String endResidueName = PDBLine.substring(27, 30);
//                String endResidueChain = PDBLine.substring(31, 32);
//                int endResidueNumber = new Integer(PDBLine.substring(33, 37).trim()).intValue();
//                String endICode = PDBLine.substring(37, 38);
                
                int helixClass = new Integer(PDBLine.substring(38, 40).trim()).intValue();

                Helix helix = new Helix();
                switch(helixClass) {
                case 1:
                    helix.setHelixType(Helix.ALPHA); break;
                case 2:
                    helix.setHelixType(Helix.RIGHT_HANDED_OMEGA); break;
                case 3:
                    helix.setHelixType(Helix.RIGHT_HANDED_PI); break;
                case 4:
                    helix.setHelixType(Helix.RIGHT_HANDED_GAMMA); break;
                case 5:
                    helix.setHelixType(Helix.RIGHT_HANDED_310); break;
                case 6:
                    helix.setHelixType(Helix.LEFT_HANDED_ALPHA); break;
                case 7:
                    helix.setHelixType(Helix.LEFT_HANDED_OMEGA); break;
                case 8:
                    helix.setHelixType(Helix.LEFT_HANDED_GAMMA); break;
                case 9:
                    helix.setHelixType(Helix.RIBBON_HELIX_27); break;
                case 10:
                    helix.setHelixType(Helix.POLYPROLINE); break;
                default:
                    helix.setHelixType(Helix.ALPHA); break;                    
                }
                
                // System.out.println("HELIX record found");
                
                // Remember residue ranges
                secondaryStructureStarts.put(helix, PDBLine.substring(15, 26));
                secondaryStructureEnds.put(helix, PDBLine.substring(27, 38));                
                
                secondaryStructures.add(helix);
            }
            else if (PDBLine.substring(0,6).equals("SHEET ")) {
                // System.out.println("Parsing strand...");
                BetaStrand strand = new BetaStrand();

                // Make beta strand residue descriptions look like alpha helix residue descriptions
                String initString = PDBLine.substring(17, 22) + " " + PDBLine.substring(22, 27);
                String endString = PDBLine.substring(28, 33) + " " + PDBLine.substring(33, 38);                
                
                secondaryStructureStarts.put(strand, initString);
                secondaryStructureEnds.put(strand, endString);
                
                secondaryStructures.add(strand);
            }
            
            reader.mark(200); // Commit to reading this far into the file
        }
        
        // Populate title
        if (title.length() > 0) setTitle(title);
        
        // Now that the header section of the PDB file has been parsed,
        // Read one molecule at a time
        Molecule mol = null;
        do {
            mol = MoleculeClass.createFactoryPDBMolecule(reader);
		    if (mol == null) break;

            molecules.add(mol);

            // Hash molecule by chain ID for later resolution of secondary structure
            String chainID = mol.getPdbChainId();
            if (! chainMolecules.containsKey(chainID))
                chainMolecules.put(chainID, new Vector<Molecule>());
            Collection<Molecule> m = chainMolecules.get(chainID);
            m.add(mol);
            
            // Populate the collection-wide atoms array
            atoms().addAll(mol.atoms());

        } while (mol.atoms().size() > 0);

        // Apply PDB secondary structures
        SS: for (SecondaryStructure structure : secondaryStructures) {
            
            String startResidueString = (String) secondaryStructureStarts.get(structure);
            String chainID = startResidueString.substring(4, 5);
            // String startInsertionCode = startResidueString.substring(10, 11);
            int startResidueNumber = (new Integer(startResidueString.substring(6, 10).trim())).intValue();

            String endResidueString = (String) secondaryStructureEnds.get(structure);
            // String endInsertionCode = endResidueString.substring(10, 11);
            int endResidueNumber = (new Integer(endResidueString.substring(6, 10).trim())).intValue();

            Collection<Molecule> m = chainMolecules.get(chainID);
            
            if (m == null) continue SS;

            // MOL: for (Molecule molecule : m) {
            Molecule molecule = m.iterator().next(); // Use first molecule of chain only

            if (! (molecule instanceof Biopolymer)) continue SS;
            Biopolymer biopolymer = (Biopolymer) molecule;
            for (int res = startResidueNumber; res <= endResidueNumber; res++) {
                Residue residue = biopolymer.getResidueByNumber(res);

                // Set relationship among residue/biopolymer/structure
                structure.addResidue(residue);
                structure.setMolecule(biopolymer);
                biopolymer.secondaryStructures().add(structure);
                residue.secondaryStructures().add(structure);
            }
        }
	}    
}

