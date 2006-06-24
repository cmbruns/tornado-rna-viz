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
 * Created on Jun 12, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.rnaml;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.*;
import org.jdom.filter.*;
import org.jdom.input.SAXBuilder;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.LocatedAtom;
import org.simtk.molecularstructure.atom.PDBAtom;
import org.simtk.molecularstructure.nucleicacid.*;

public class RnamlDocument {
    Map<Integer, NucleicAcid> rnamlIndexMolecules = new HashMap<Integer, NucleicAcid>();
    org.jdom.Document rnamlDoc;
    Map<Integer, List<Integer> > resNumTables = new HashMap<Integer, List<Integer> >();
    
    public RnamlDocument(String rnamlFileName, MoleculeCollection molecules) 
    throws JDOMException, java.io.IOException
    {

        // Find nucleic acid molecules
        // and index them by the expected rnaml index
        int nucleicAcidIndex = 1;
        for (Molecule mol : molecules.molecules()) {
            if (mol instanceof NucleicAcid) {
                rnamlIndexMolecules.put(nucleicAcidIndex, (NucleicAcid) mol);
                nucleicAcidIndex ++;
            }
        }
        
        // Read xml file
        SAXBuilder builder = new SAXBuilder();
        rnamlDoc = builder.build(rnamlFileName);        
    }
    
    
    /**
     * 
     */
    public void importSecondaryStructures() {
    	// Parse the xml file
        Element rnamlEl;
        Iterator docIt = rnamlDoc.getDescendants(new ElementFilter("rnaml"));
        if (!docIt.hasNext()){
            System.out.println("xml file has no rnaml element!");
        }
        rnamlEl = (Element) docIt.next();
        for (Element rnamlMol: (List<Element>) rnamlEl.getChildren("molecule")){
            int molID = Integer.parseInt(rnamlMol.getAttributeValue("id"));
            List<Element> sequences = rnamlMol.getChildren("sequence");
            if (sequences.size()!=1) {
                ;//should add some kind of error reporting
            }
            /* nTable is retrieved via getDescendants because the standard DTD requires that the numbering 
             * table be a child of a numbering-system which in turn is a child of the sequence, although 
             * rnaview for some reason creates the numbering table as a direct child of the sequence. 
             */
            Element nTableEl = (Element) sequences.get(0).getDescendants( new ElementFilter("numbering-table")).next();
            String nTableString = nTableEl.getTextNormalize();
            List<Integer> nTable = new ArrayList<Integer>();
            Matcher toke = Pattern.compile("\\d+").matcher(nTableString);
            while (toke.find()) {
                nTable.add(Integer.parseInt(toke.group(0)));
            }
            resNumTables.put(molID, nTable);
            
            // Parse intramolecular helices
            for (Element struc : (List<Element>) rnamlMol.getChildren("structure")){ // only one is made by rnaview
                for (Element modl : (List<Element>) struc.getChildren("model")){
                    for (Element annot : (List<Element>) modl.getChildren("str-annotation")){
                    	for (Element baseconf: (List<Element>) annot.getChildren("base-conformation")){
                    		// TODO add support for rnaml secondary structure
                    	} //end forloop baseconf 
                    	for (Element basepair: (List<Element>) annot.getChildren("base-pair")){
                    		addBasePair(basepair, molID);
                    	} //end forloop basepair
                    	for (Element basetriple: (List<Element>) annot.getChildren("base-triple")){
                    		// TODO add support for rnaml secondary structure
                    	} //end forloop basetriple                   	
                    	for (Element basestack: (List<Element>) annot.getChildren("base-stack")){
                    		// TODO add support for rnaml secondary structure
                    	} //end forloop basestack
                        for (Element helix : (List<Element>) annot.getChildren("helix")){
                            addHelix(helix, molID);
                        } //end forloop helix
                    	for (Element pseudoknot: (List<Element>) annot.getChildren("pseudoknot")){
                    		// TODO add support for rnaml secondary structure
                    	} //end forloop pseudoknot
                    	for (Element strand: (List<Element>) annot.getChildren("single-strand")){
                    		// TODO add support for rnaml secondary structure
                    	} //end forloop strand
                    	for (Element dist_const: (List<Element>) annot.getChildren("distance-constraint")){
                    		// TODO add support for rnaml secondary structure
                    	} //end forloop dist_const
                    	for (Element surface_const: (List<Element>) annot.getChildren("surface-constraint")){
                    		// TODO add support for rnaml secondary structure
                    	} //end forloop surface_const
                    }
                    
                }
                
            }
        }

        // Parse intermolecular secondary structure
        for (Element intrx: (List<Element>) rnamlEl.getChildren("interactions")){
            for (Element annot : (List<Element>) intrx.getChildren("str-annotation")){
            	for (Element baseconf: (List<Element>) annot.getChildren("base-conformation")){
            		// TODO add support for rnaml secondary structure
            	} //end forloop baseconf 
            	for (Element basepair: (List<Element>) annot.getChildren("base-pair")){
            		addBasePair(basepair, -1);
            	} //end forloop basepair
            	for (Element basetriple: (List<Element>) annot.getChildren("base-triple")){
            		// TODO add support for rnaml secondary structure
            	} //end forloop basetriple                   	
            	for (Element basestack: (List<Element>) annot.getChildren("base-stack")){
            		// TODO add support for rnaml secondary structure
            	} //end forloop basestack
            	for (Element helix : (List<Element>) annot.getChildren("helix")){
            		addHelix(helix, -1);
            	} //end forloop helix
            	for (Element pseudoknot: (List<Element>) annot.getChildren("pseudoknot")){
            		// TODO add support for rnaml secondary structure
            	} //end forloop pseudoknot
            	for (Element strand: (List<Element>) annot.getChildren("single-strand")){
            		// TODO add support for rnaml secondary structure
            	} //end forloop strand
            	for (Element dist_const: (List<Element>) annot.getChildren("distance-constraint")){
            		// TODO add support for rnaml secondary structure
            	} //end forloop dist_const
            	for (Element surface_const: (List<Element>) annot.getChildren("surface-constraint")){
            		// TODO add support for rnaml secondary structure
            	} //end forloop surface_const
            }
        }        
    }

    private void addBasePair(Element basepair, int molID) {
        int mol5i = molID;
        int mol3i = molID;
    	Element base5 = basepair.getChild("base-id-5p").getChild("base-id");
        Element base3 = basepair.getChild("base-id-3p").getChild("base-id");
        int pos5i  = Integer.parseInt(base5.getChildText("position"));
        int pos3i  = Integer.parseInt(base3.getChildText("position"));

        if (molID == -1) {
        mol5i = Integer.parseInt(base5.getChild("molecule-id").getAttributeValue("ref"));
        mol3i = Integer.parseInt(base3.getChild("molecule-id").getAttributeValue("ref"));
        }
        
    	NucleicAcid mol5 = rnamlIndexMolecules.get(mol5i);
    	NucleicAcid mol3 = rnamlIndexMolecules.get(mol3i);
    	
    	List<Integer> nTable5 = resNumTables.get(mol5i);
    	List<Integer> nTable3 = resNumTables.get(mol3i);

    	int PDBpos5 = ((Integer)nTable5.get(pos5i-1));
		int PDBpos3 = ((Integer)nTable3.get(pos3i-1));
		Residue r5 = mol5.getResidueByNumber(PDBpos5);
		Residue r3 = mol3.getResidueByNumber(PDBpos3);
		BasePair thisBP = BasePair.makeBasePair(r5, r3, "rnaml");

    	Element e5p = basepair.getChild("edge-5p"); //should be zero or one of these
    	if (e5p!=null){
    		try {
    			thisBP.setEdge(r5, e5p.getTextTrim());
    		}
    		catch (IllegalArgumentException e) {
                System.out.println("problem adding "+e5p.getTextTrim()+" edge to res "+r5+" in BP "+thisBP);
    		}
    		
    	}

    	Element e3p = basepair.getChild("edge-3p"); //should be zero or one of these
    	if (e3p!=null){ 
    		try {
        		thisBP.setEdge(r3, e3p.getTextTrim());
    		}
    		catch (IllegalArgumentException e) {
                System.out.println("problem adding "+e3p.getTextTrim()+" edge to res "+r3+" in BP "+thisBP);
    		}
    	}

    	Element bond = basepair.getChild("bond_orientation"); //should be zero or one of these
    	if (bond!=null){ 
    		try {
        		thisBP.setBond_orient(bond.getTextTrim());
    		}
    		catch (IllegalArgumentException e) {
                System.out.println("problem adding "+bond.getTextTrim()+" bond orientation to BP "+thisBP);
    		}
    	}
    	
    	Element strandO = basepair.getChild("strand_orientation"); //should be zero or one of these
    	if (strandO!=null){ 
    		try {
        		thisBP.setStrand_orient(strandO.getTextTrim());
    		}
    		catch (IllegalArgumentException e) {
                System.out.println("problem adding "+strandO.getTextTrim()+" strand orienation to BP "+thisBP);
    		}
    	}
    	
		mol5.addSecondaryStructure(thisBP);
		if (mol5i!=mol3i){
			mol3.addSecondaryStructure(thisBP);
		}
		
	}

	private void addHelix(Element helix, int molID ) {

        int mol5i = molID;
        int mol3i = molID;
    	Element base5 = helix.getChild("base-id-5p").getChild("base-id");
        Element base3 = helix.getChild("base-id-3p").getChild("base-id");
        int pos5i  = Integer.parseInt(base5.getChildText("position"));
        int pos3i  = Integer.parseInt(base3.getChildText("position"));
        int hLen  = Integer.parseInt(helix.getChildText("length"));

        if (molID == -1) {
        mol5i = Integer.parseInt(base5.getChild("molecule-id").getAttributeValue("ref"));
        mol3i = Integer.parseInt(base3.getChild("molecule-id").getAttributeValue("ref"));
        }
        
    	NucleicAcid mol5 = rnamlIndexMolecules.get(mol5i);
    	NucleicAcid mol3 = rnamlIndexMolecules.get(mol3i);
    	
    	List<Integer> nTable5 = resNumTables.get(mol5i);
    	List<Integer> nTable3 = resNumTables.get(mol3i);
    	Duplex dup = null;
    	
    	for (int idx=0; idx<hLen; idx++){
    		int PDBpos5 = ((Integer)nTable5.get(pos5i+idx-1));
    		int PDBpos3 = ((Integer)nTable3.get(pos3i-idx-1));
    		Residue r5 = mol5.getResidueByNumber(PDBpos5);
    		Residue r3 = mol3.getResidueByNumber(PDBpos3);
    		BasePair thisBP = BasePair.makeBasePair(r5, r3, "rnaml");
    		if (thisBP!=null) {
    			if (dup==null){
    				dup = new Duplex();
    		    	dup.setSource("rnaml");
    			}
    			dup.addBasePair(thisBP);
    		}
    	}
    	if (dup !=null){
    		mol5.addSecondaryStructure(dup);
    		if (mol5i!=mol3i){
    			mol3.addSecondaryStructure(dup);
    		}
    	}
    }
    
}
