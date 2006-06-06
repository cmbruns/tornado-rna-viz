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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;

import org.jdom.*;
import org.jdom.filter.*;
import org.jdom.input.SAXBuilder;

import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.BasePair;
import org.simtk.molecularstructure.nucleicacid.Duplex;
import org.simtk.molecularstructure.nucleicacid.NucleicAcid;
import org.simtk.molecularstructure.nucleicacid.Nucleotide;
import org.simtk.molecularstructure.protein.*;

import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * A collection of one or more molecules, as might be found in a PDB file.
 */
public class MoleculeCollection {
    Vector atoms = new Vector();
    Vector molecules = new Vector();
    Vector<PDBMolecule> naChains = new Vector();
    Vector<ArrayList> nTables = new Vector();

    Vector3DClass centerOfMass = new Vector3DClass();
    double mass = 0;
    
    private String mTitle = "";
    public String getTitle() {return mTitle;}
    public void setTitle(String t) {mTitle = t;}
    
    private String mPdbId = null;
    public String getPdbId() {return mPdbId;}
    public void setPdbId(String i) {mPdbId = i;}

    private String m_inputStructureFileName = null;
    public String getInputStructureFileName() {return m_inputStructureFileName;}
    public void setInputStructureFileName(String s) {m_inputStructureFileName = s;}

    public double getMass() {
        return mass;
    }
    public Vector3DClass getCenterOfMass() {
        if (mass <= 0) return null;
        return centerOfMass;
    }
    
    public void relaxCoordinates() {
        for (Iterator i = molecules.iterator(); i.hasNext(); ) {
            PDBMoleculeClass m = (PDBMoleculeClass) i.next();
            m.relaxCoordinates();
        }
    }
    
    public Vector molecules() {return molecules;}
    
    public int getMoleculeCount() {return molecules.size();}
    public LocatedMolecule getMolecule(int i) {return (LocatedMolecule) molecules.elementAt(i);}
    
    public int getAtomCount() {return atoms.size();}
    
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
        
        Collection secondaryStructures = new Vector();
        HashMap secondaryStructureStarts = new HashMap();
        HashMap secondaryStructureEnds = new HashMap();
        HashMap chainMolecules = new HashMap();
        
        Document rnamlDoc;
        Element rnamlEl;
        List rnamlMolecules;
        Element interactionsEl;
        List helices;


        reader.mark(200);
        FILE_LINE: while ((PDBLine = reader.readLine()) != null) {

            // Stop parsing after the END record
            if (PDBLine.substring(0,3).equals("END")) {
                reader.reset(); // Leave the END tag for the next guy
                return;
            }

            // If we get to ATOM records, we need to stop and send the stream to the Molecule parser
            else if ((PDBLine.substring(0,6).equals("ATOM  ")) || (PDBLine.substring(0,6).equals("HETATM"))) {
                // Oops, we passed the header, and are now in the coordinates
                // Pass this off to the Molecule parser
                reader.reset();
                break;
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
                    if (id.length() == 4) setPdbId(id);
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
        PDBMolecule mol = null;
        do {
            mol = PDBMoleculeClass.createFactoryPDBMolecule(reader);
		    if (mol == null) break;

            molecules.addElement(mol);

            // Hash molecule by chain ID for later resolution of secondary structure
            String chainID = mol.getChainID();
            if (! chainMolecules.containsKey(chainID))
                chainMolecules.put(chainID, new Vector());
            Collection m = (Collection) chainMolecules.get(chainID);
            m.add(mol);
            if (mol instanceof NucleicAcid) {
            	naChains.add(mol);
            }
            
            // Update center of mass of entire molecule collection            
            double myMassProportion = getMass() / (getMass() + mol.getMass());
            centerOfMass = new Vector3DClass( centerOfMass.scale(myMassProportion).plus(
                    mol.getCenterOfMass().times(1.0 - myMassProportion) ) );
            
            mass += mol.getMass();

            // Populate the collection-wide atoms array
            for (Iterator i = mol.getAtomIterator(); i.hasNext(); ) {
                LocatedAtom a = (LocatedAtom) i.next();
                atoms.addElement(a);
            }
            
        } while (mol.getAtomCount() > 0);

        // Apply PDB secondary structures
        for (Iterator i = secondaryStructures.iterator(); i.hasNext(); ) {
            
            SecondaryStructure structure = (SecondaryStructure) i.next();

            String startResidueString = (String) secondaryStructureStarts.get(structure);
            String chainID = startResidueString.substring(4, 5);
            // String startInsertionCode = startResidueString.substring(10, 11);
            int startResidueNumber = (new Integer(startResidueString.substring(6, 10).trim())).intValue();

            String endResidueString = (String) secondaryStructureEnds.get(structure);
            // String endInsertionCode = endResidueString.substring(10, 11);
            int endResidueNumber = (new Integer(endResidueString.substring(6, 10).trim())).intValue();

            Collection m = (Collection) chainMolecules.get(chainID);
            
            if (m != null) {
                for (Iterator molIter = m.iterator(); molIter.hasNext();) {
                    PDBMolecule molecule = (PDBMolecule) molIter.next();

                    if (molecule instanceof Biopolymer) {
                        Biopolymer biopolymer = (Biopolymer) molecule;
                        for (int res = startResidueNumber; res <= endResidueNumber; res++) {
                            Residue residue = biopolymer.getResidueByNumber(res);
    
                            // Set relationship among residue/biopolymer/structure
                            structure.addResidue(residue);
                            structure.setMolecule(biopolymer);
                            biopolymer.addSecondaryStructure(structure);
                            residue.addSecondaryStructure(structure);
                        }
                    }
                }
            }
        }
        // Check for RNAML secondary structures
        // needs to be made more robust & expansive
        // looks in local direrctory, doesn't know loc of PDB
        if ((!naChains.isEmpty()) && (mPdbId != null))  {
        	File curDir = new File(".");
        	List dirList = Arrays.asList(curDir.list());
        	if (dirList.contains(mPdbId+".pdb.xml")){
        		System.out.println("processing xml file");
        		SAXBuilder builder = new SAXBuilder();
        		rnamlDoc = null;
        		try {
        			rnamlDoc = builder.build(mPdbId+".pdb.xml");
        			}
        		catch (Exception e){
        			System.out.println("Caught " + e);
        		}

        		Iterator docIt = rnamlDoc.getDescendants(new ElementFilter("rnaml"));
        		if (!docIt.hasNext()){
        			System.out.println("xml file has no rnaml element!");
        		}
        		rnamlEl = (Element) docIt.next();
        		for (Element rnamlMol: (List<Element>) rnamlEl.getChildren("molecule")){
                    int molID = Integer.parseInt(rnamlMol.getAttributeValue("id"));
                    /* The following line linking the rnaml molecule ID back to the 
                     * PDB chains works (and is required) only becuase of traits of
                     * rnamlview (and the rnaml generated by it) as of Spring 2006.
                     */
//                    PDBMolecule thisMol = (PDBMolecule) naChains.get(molID-1);
        			//String chainID = thisMol.getChainID();
                    List<Element> sequences = rnamlMol.getChildren("sequence");
                    if (sequences.size()!=1) {
                    	;//should add some kind of error reporting
                    }
                    /* nTable is retrieved via getDescendants even though rnaview appears to create the numbering table
                     * as a direct child of the sequence, because the DTD appears to require that the numbering 
                     * table be a child of a numbering-system which in turn is a child of the sequence. 
                     */
                    Element nTableEl = (Element) sequences.get(0).getDescendants( new ElementFilter("numbering-table")).next();
                    String nTableString = nTableEl.getTextNormalize();
                    ArrayList nTable = new ArrayList();
                    Matcher toke = Pattern.compile("\\d+").matcher(nTableString);
                    while (toke.find()) {
                    	nTable.add(Integer.parseInt(toke.group(0)));
                    }
                    nTables.add(nTable);
                    
                    for (Element struc : (List<Element>) rnamlMol.getChildren("structure")){ // only one made by rnaview
                        for (Element modl : (List<Element>) struc.getChildren("model")){
                            for (Element annot : (List<Element>) modl.getChildren("str-annotation")){
                                for (Element helix : (List<Element>) annot.getChildren("helix")){
                                	Element base5 = helix.getChild("base-id-5p").getChild("base-id");
                                	Element base3 = helix.getChild("base-id-3p").getChild("base-id");
                                	int pos5  = Integer.parseInt(base5.getChildText("position"));
                                	int pos3  = Integer.parseInt(base3.getChildText("position"));
                                	int hLen  = Integer.parseInt(helix.getChildText("length"));
                                	addHelix(molID, pos5, molID, pos3, hLen);
                                }

                            }
                        	
                        }
                    	
                    }
        		}

        		for (Element intrx: (List<Element>) rnamlEl.getChildren("interactions")){
                    for (Element annot : (List<Element>) intrx.getChildren("str-annotation")){
                        for (Element helix : (List<Element>) annot.getChildren("helix")){
                        	Element base5 = helix.getChild("base-id-5p").getChild("base-id");
                        	Element base3 = helix.getChild("base-id-3p").getChild("base-id");
                            int mol5ID = Integer.parseInt(base5.getChild("molecule-id").getAttributeValue("ref"));
                            int mol3ID = Integer.parseInt(base3.getChild("molecule-id").getAttributeValue("ref"));
                        	int pos5  = Integer.parseInt(base5.getChildText("position"));
                        	int pos3  = Integer.parseInt(base3.getChildText("position"));
                        	int hLen  = Integer.parseInt(helix.getChildText("length"));
                        	addHelix(mol5ID, pos5, mol3ID, pos3, hLen);
                        }

                    }
                    
        		}

        	}
        	else {
        		System.out.println("can't find xml file "+mPdbId+".pdb.xml");
        		System.out.println("directory listing includes "+dirList);
        		
        	}
        }
	}
    
    private void addHelix(int mol5i, int pos5i, int mol3i, int pos3i, int hlen) {
    	Biopolymer mol5 = (Biopolymer) naChains.get(mol5i - 1);
    	Biopolymer mol3 = (Biopolymer) naChains.get(mol3i - 1);
    	ArrayList nTable5 = nTables.get(mol5i-1);
    	ArrayList nTable3 = nTables.get(mol3i-1);
    	Duplex dup = null;
    	for (int idx=0; idx<hlen; idx++){
    		int PDBpos5 = ((Integer)nTable5.get(pos5i+idx-1));
    		int PDBpos3 = ((Integer)nTable3.get(pos3i-idx-1));
    		Residue r5 = mol5.getResidueByNumber(PDBpos5);
    		Residue r3 = mol3.getResidueByNumber(PDBpos3);
    		if ((r5!=null)&&(r3!=null)){
    			if (dup == null) {
    				dup = new Duplex();
    				dup.setSource("rnaml");
    			}
    			if ((r5 instanceof Nucleotide)&& (r3 instanceof Nucleotide)){
    				dup.addBasePair(new BasePair((Nucleotide)r5,(Nucleotide)r3));
    			}
    			else {
    				if (!(r5 instanceof Nucleotide)){
        				Collection<Atom> r5atoms = ((PDBMoleculeClass) r5).getAtoms();
        				Object[] r5atomsArr = r5atoms.toArray();
        				PDBAtom r5a0 = (PDBAtom) r5atomsArr[0];
        				System.out.println("unrecognized residue reported in rnaml as basepaired:");
        				System.out.println("mol5 id "+mol5i+", resno "+PDBpos5+", res name "+r5a0.getPDBResidueName()+", res "+r5);
    				}
    				if (!(r3 instanceof Nucleotide)){
        				Collection<Atom> r3atoms = ((PDBMoleculeClass) r3).getAtoms();
        				Object[] r3atomsArr = r3atoms.toArray();
        				PDBAtom r3a0 = (PDBAtom) r3atomsArr[0];
        				System.out.println("unrecognized residue reported in rnaml as basepaired:");
        				System.out.println("mol3 id "+mol3i+", resno "+PDBpos3+", res name "+r3a0.getPDBResidueName()+", res "+r5);
    				}
    			}
    		}
    	}
    	if (dup !=null){
    		mol5.addSecondaryStructure(dup);
    		if (mol5!=mol3){
    			mol3.addSecondaryStructure(dup);
    		}
    	}
    }
    
}

