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
import org.simtk.molecularstructure.atom.Atom;
import org.simtk.molecularstructure.nucleicacid.*;

import java.io.File;

public class RnamlDocument {
    Map<Integer, NucleicAcid> rnamlIndexMolecules = new HashMap<Integer, NucleicAcid>();
    org.jdom.Document rnamlDoc;
    Element rnamlEl = null;
    String source = "";
    Map<Integer, List<Integer> > resNumTables = new HashMap<Integer, List<Integer> >();

    
    public RnamlDocument(File rnamlFile, MoleculeCollection molecules) 
    throws JDOMException, java.io.IOException
    {        
        // Read xml file
        SAXBuilder builder = new SAXBuilder();
        rnamlDoc = builder.build(rnamlFile);
        
        Iterator docIt = rnamlDoc.getDescendants(new ElementFilter("rnaml"));
        if (!docIt.hasNext()){
            System.err.println("xml file has no rnaml element!");
            return;
        }
        else {
        	rnamlEl = (Element) docIt.next();
        }
        
        source = determineSource();
        System.out.println("rnaml source type = "+source);
        
        // Find nucleic acid molecules from Tornado
        // and index them by the expected rnaml index
        int nucleicAcidIndex = 1;
        for (Molecule mol : molecules.molecules()) {
            if ( !(mol instanceof NucleicAcid)) continue;
            NucleicAcid rna = (NucleicAcid) mol;
            rnamlIndexMolecules.put(nucleicAcidIndex, rna);
            nucleicAcidIndex ++;
        }
        
        if (source.equals("rnaview")) {
        	checkRnaviewMols();
        }
    }
        
    private void checkRnaviewMols() {
    	int nRNAViewRNAs = rnamlEl.getChildren("molecule").size();
    	if (nRNAViewRNAs < rnamlIndexMolecules.keySet().size()){
    		Map<Integer, NucleicAcid> newIndexMolecules = new HashMap<Integer, NucleicAcid>();
    		int nucleicAcidIndex = 1;
    		for (int index = 1; index <= rnamlIndexMolecules.keySet().size(); index++) {
    			NucleicAcid rna = rnamlIndexMolecules.get(index);
    			if ( !isRnaviewNA(rna)) continue;
    			newIndexMolecules.put(nucleicAcidIndex, rna);
    			nucleicAcidIndex ++;
    		}
    		if (nRNAViewRNAs != newIndexMolecules.keySet().size()){
    			System.err.println("cannot reconcile with rnview reporting of rna chains.");
    		}
    		else {
    			rnamlIndexMolecules = newIndexMolecules;
    		}
    	}
    	else if (nRNAViewRNAs < rnamlIndexMolecules.keySet().size()){
    		System.err.println("rnaview reports too many rna chains.");
    	}
	}

	/**
     * 
     */
    @SuppressWarnings("unchecked")
	public void importSecondaryStructures() {
    	// Parse the xml file
    	
    	int molIndex = 0;
        for (Element rnamlMol: (List<Element>) rnamlEl.getChildren("molecule")){
        	molIndex++;
            int molID = getID(molIndex, rnamlMol.getAttributeValue("id"));
            
            if (source.equals("rnaview")){
            	Element sequence = rnamlMol.getChild("sequence");
            	/* nTable is retrieved via getDescendants because the standard DTD requires that the numbering 
            	 * table be a child of a numbering-system which in turn is a child of the sequence, although 
            	 * rnaview for some reason creates the numbering table as a direct child of the sequence. 
            	 */
            	Element nTableEl = (Element) sequence.getDescendants( new ElementFilter("numbering-table")).next();
            	String nTableString = nTableEl.getTextNormalize();
            	List<Integer> nTable = new ArrayList<Integer>();
            	Matcher toke = Pattern.compile("\\d+").matcher(nTableString);
            	while (toke.find()) {
            		nTable.add(Integer.parseInt(toke.group(0)));
            	}
            	resNumTables.put(molID, nTable);
            }
            
            // Parse intramolecular helices
            for (Element struc : (List<Element>) rnamlMol.getChildren("structure")){ // only one is made by rnaview
            	Element modl =  (Element) struc.getChild("model"); //UNAFold makes several, but we'll only use first
            	parseAnnotations(modl, molID);            	
            }
            
        }

        // Parse intermolecular secondary structure
        Element intrx =  (Element) rnamlEl.getChild("interactions");
    	parseAnnotations(intrx, -1);            	
    }

    private int getID(int molIndex, String id){
    	if (source.equals("rnaview")){ 
    		return Integer.parseInt(id);
    	}
    	else return molIndex;
    }

    private Residue getRes(int molID, int localPos){
    	NucleicAcid mol = rnamlIndexMolecules.get(molID);
        if (mol == null) return null;
    	if (source.equals("rnaview")){
    		List<Integer> nTable = resNumTables.get(molID);
    		localPos = ((Integer)nTable.get(localPos));
        	return mol.getResidueByNumber(localPos);
    	}
    	else return mol.getResidue(localPos);
    }
    
    @SuppressWarnings({"unchecked", "unused"})
	private void parseAnnotations(Element modl, int molID){
    	if (modl == null) return;
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
    	
		Residue r5 = getRes(mol5i, pos5i-1);
		Residue r3 = getRes(mol3i, pos3i-1);
        
		// TODO throw errors here
        if (r5 == null) return;
        if (r3 == null) return;

        BasePair thisBP = BasePair.makeBasePair(r5, r3, source);
        if (thisBP == null) return;

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

    	Element bond = basepair.getChild("bond-orientation"); //should be zero or one of these
    	if (bond!=null){ 
    		try {
        		thisBP.setBond_orient(bond.getTextTrim());
    		}
    		catch (IllegalArgumentException e) {
                System.out.println("problem adding "+bond.getTextTrim()+" bond orientation to BP "+thisBP);
    		}
    	}
    	
    	Element strandO = basepair.getChild("strand-orientation"); //should be zero or one of these
    	if (strandO!=null){ 
    		try {
        		thisBP.setStrand_orient(strandO.getTextTrim());
    		}
    		catch (IllegalArgumentException e) {
                System.out.println("problem adding "+strandO.getTextTrim()+" strand orienation to BP "+thisBP);
    		}
    	}
    	
		mol5.secondaryStructures().add(thisBP);
		if (mol5i!=mol3i){
			mol3.secondaryStructures().add(thisBP);
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
    	Duplex dup = null;
    	
    	for (int idx=0; idx<hLen; idx++){
    		Residue r5 = getRes(mol5i, pos5i-1);
    		Residue r3 = getRes(mol3i, pos3i-1);
    		BasePair thisBP = BasePair.makeBasePair(r5, r3, source);
    		if (thisBP!=null) {
    			if (dup==null){
    				dup = new Duplex();
    		    	dup.setSource(source);
    			}
    			dup.addBasePair(thisBP);
    		}
    	}
    	if (dup !=null){
    		mol5.secondaryStructures().add(dup);
    		if (mol5i!=mol3i){
    			mol3.secondaryStructures().add(dup);
    		}
    	}
    }
    
	private String determineSource(){
        Iterator modelIt = rnamlDoc.getDescendants(new ElementFilter("model"));
        if (modelIt.hasNext()){
        	Element firstModel = (Element) modelIt.next();
        	if (firstModel.getAttributeValue("id").equals("?")) {
        		return "rnaview";  //only rnaview is known to specify the invalid character "?" for model id
        	}
        }
        
        Iterator analysisIt = rnamlDoc.getDescendants(new ElementFilter("analysis"));
        if (analysisIt.hasNext()){
            Element firstAnalysis = (Element) analysisIt.next();
            if (firstAnalysis.getAttributeValue("id").equals("mfold")) {
            	return "mfold";
            }
            else if (firstAnalysis.getAttributeValue("id").equals("UNAFold")) {
            	return "mfold";
            }
        }
        
		return "unknown";
	}

	public boolean isRnaviewNA(Biopolymer mol){
		int nNucBases = 0;
		for (Residue res: (mol.residues())){
			if (!(res instanceof Residue)) return false;
			if (isDeemedNucleotide((Residue) res, "rnaview")) {
				nNucBases++;
			}
		}
		if (nNucBases > 1){
			return true;
		}
		else {
			return false;
		}
			}

	public boolean isDeemedNucleotide(Residue res, String source){
		
		if (!source.equalsIgnoreCase("rnaview")){
			return (res instanceof Nucleotide);
		}
		/*	This routine is written to be consistent with the RNAVIEW resdue_ident function, found in 
		 *  RNAVIEW file "fpair_sub.c".  For temporary reference, the text of that routine follows below in this comment
		 * 		long residue_ident(char **AtomName, double **xyz, long ib, long ie)
				/*  identifying a residue as follows:
				 *  R-base  Y-base  amino-acid, others [default]
				 *   +1        0        -1        -2 [default]
				 
				{
				    double d1, d2, d3, dcrt = 2.0, dcrt2 = 3.0, temp[4];
				    long i, id = -2;
				    long CA, C, N1, C2, C6, N9;

				    N9 = find_1st_atom(" N9 ", AtomName, ib, ie, "");
				    N1 = find_1st_atom(" N1 ", AtomName, ib, ie, "");
				    C2 = find_1st_atom(" C2 ", AtomName, ib, ie, "");
				    C6 = find_1st_atom(" C6 ", AtomName, ib, ie, "");
				    if (N1 && C2 && C6) {
				        for (i = 1; i <= 3; i++)
				            temp[i] = xyz[N1][i] - xyz[C2][i];
				        d1 = veclen(temp);
				        for (i = 1; i <= 3; i++)
				            temp[i] = xyz[N1][i] - xyz[C6][i];
				        d2 = veclen(temp);
				        for (i = 1; i <= 3; i++)
				            temp[i] = xyz[C2][i] - xyz[C6][i];
				        d3 = veclen(temp);
				        if (d1 <= dcrt && d2 <= dcrt && d3 <= dcrt2) {
				            id = 0;
				            if (N9) {
				                for (i = 1; i <= 3; i++)
				                    temp[i] = xyz[N1][i] - xyz[N9][i];
				                d3 = veclen(temp);
				                if (d3 >= 3.5 && d3 <= 4.5)         ~4.0 
				                    id = 1;
				            }
				        }
				        return id;
				    }
				    CA = find_1st_atom(" CA ", AtomName, ib, ie, "");
				    C = find_1st_atom(" C  ", AtomName, ib, ie, "");
				    if (!C)                         if C does not exist, use N 
				        C = find_1st_atom(" N  ", AtomName, ib, ie, "");
				    if (CA && C) {
				        for (i = 1; i <= 3; i++)
				            temp[i] = xyz[CA][i] - xyz[C][i];
				        if (veclen(temp) <= dcrt)
				            id = -1;
				        return id;
				    }
				    return id;                         #other cases 
				}
		*/		
		Atom N1, C2, C6;
		double distN1C2, distN1C6, distC2C6;

		N1 = getNamedAtom(res, "N1");
		C2 = getNamedAtom(res, "C2");
		C6 = getNamedAtom(res, "C6");
		if ((N1 == null) || (C2 == null) || (C6 == null)) {
			return false;
		}
		distN1C2 = N1.distance(C2);
		distN1C6 = N1.distance(C6);
		distC2C6 = C2.distance(C6);

		if ((distN1C2 <= 2.0) && (distN1C6 <= 2.0) && (distC2C6 <= 3.0)) {
			return true;
		}
		return false;
	}
	
	private Atom getNamedAtom(Residue res, String name){
		Atom result = null;
		for (String tryName: (Arrays.asList(name, " "+name, " "+name+" "))){
			result = res.getAtom(tryName);
			if (result!=null) break;
		}
		return result;
	}
}
