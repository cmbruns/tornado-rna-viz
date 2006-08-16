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
 * 
 * NOTE:  for mfold/UNAFold rnaml files, there is no preexisting protocol relating the rnaml
 * molecules to pdb molecules.  What is assumed here is that each of the n molecules in the rnaml
 * file correspond to the first n nucleic acid molecules in the pdb file.  So output from mfold 
 * (which only processes a single sequence at a time) will only work if it is for the first nucleic
 * acid chain in the file.  For UNAFold input, a group of sequences may be provided together, separated 
 * by semicolons; their secondary structures will then be reported in a single rnaml file.  If any of the
 * nucleic acid chains are not of interest, a short (one residue) dummy sequence can be provided that will 
 * be known to not generate any secondary structure, but which will serve to keep the molecules indexed
 * properly in the resulting rnaml file.
 * 
 * Also, with respect to UNAFold/mfold files, it should be noted that the rnaml files may contain multiple 
 * models of possible folding for each molecule, but tornado currently only uses the first such model. 
 */

/*
 * Created on Jun 12, 2006
 * Original author: Christopher Bruns and Eric Willgohs
 */
package org.simtk.rnaml;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.*;
import org.jdom.filter.*;
import org.jdom.input.SAXBuilder;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.SecondaryStructureClass.SourceType;
import org.simtk.molecularstructure.atom.Atom;
import org.simtk.molecularstructure.nucleicacid.*;

import java.io.File;

public class RnamlDocument {
    public Map<Integer, NucleicAcid> rnamlIndexMolecules = new HashMap<Integer, NucleicAcid>();
    org.jdom.Document rnamlDoc;
    Element rnamlEl = null;
    String source = "";
    Map<Integer, List<Integer> > resNumTables = new HashMap<Integer, List<Integer> >();
    
    @SuppressWarnings("unchecked") 
    List<Integer> latestMolIDs = new ArrayList();

    
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
    			System.err.println("cannot reconcile with rnview reporting of rna chains.");//hopefully never again
    		}
    		else {
    			rnamlIndexMolecules = newIndexMolecules;
    		}
    	}
    	else if (nRNAViewRNAs < rnamlIndexMolecules.keySet().size()){
    		System.err.println("rnaview reports too many rna chains.");//hopefully never again
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
            	 * 
            	 * This routine currently does require that there be a numbering table, which is fine for the
            	 * existing sources; if need be the code could be rewritten to assume 1..n numbering if no 
            	 * numbering table is provided.
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
    	boolean debug = false;
    	int bpsFound = 0;
    	
    	if (modl == null) return;
    	for (Element annot : (List<Element>) modl.getChildren("str-annotation")){
    		for (Element baseconf: (List<Element>) annot.getChildren("base-conformation")){
    			// TODO add support for rnaml structural annotation
    		}  
    		for (Element basepair: (List<Element>) annot.getChildren("base-pair")){
    			addBasePair(basepair, molID);
    			bpsFound += 1;
    		} 
    		for (Element basetriple: (List<Element>) annot.getChildren("base-triple")){
    			addBaseTriple(basetriple, molID);
    		}
    		for (Element basestack: (List<Element>) annot.getChildren("base-stack")){
    			addBaseStack(basestack, molID);
    		}
    		for (Element helix : (List<Element>) annot.getChildren("helix")){
    			addHelix(helix, molID);
    		}
    		for (Element pseudoknot: (List<Element>) annot.getChildren("pseudoknot")){
    			addPseudoknot(pseudoknot, molID);
    		}
    		for (Element strand: (List<Element>) annot.getChildren("single-strand")){
    			// TODO add support for rnaml secondary structure
    		}
    		for (Element dist_const: (List<Element>) annot.getChildren("distance-constraint")){
    			// TODO add support for rnaml secondary structure
    		}
    		for (Element surface_const: (List<Element>) annot.getChildren("surface-constraint")){
    			// TODO add support for rnaml secondary structure
    		}
    	}
    	if (debug){
    		System.out.println(""+bpsFound+" basepairs in molecule # "+molID);
    	}
    }
        
    private BasePair addBasePair(Element basepair, int molID) {
    	//caveat: if rnaml has two (or more) models with same source, and a given basepair betwteen two
    	//given residues appears in both models, only a single basepair object will be created & used, 
    	//even if the basepairs in the two models have different secondary chars. such as base conf.
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
        latestMolIDs = Arrays.asList(mol5i, mol3i);
        
    	NucleicAcid mol5 = rnamlIndexMolecules.get(mol5i);
    	NucleicAcid mol3 = rnamlIndexMolecules.get(mol3i);
    	
		Residue r5 = getRes(mol5i, pos5i-1);
		Residue r3 = getRes(mol3i, pos3i-1);
        
		// TODO throw errors here
        if (r5 == null) 
        	return null;
        if (r3 == null) 
        	return null;

        BasePair thisBP = BasePair.makeBasePair(r5, r3, source);
        if ((thisBP == null) ||(SourceType.getSourceType(source)!=thisBP.getSource())){
        	System.err.println("anomolous duplicate basepair "+thisBP+" in chain "+mol5.getPdbChainId());
        	return null;
        }

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
    	for (Biopolymer mol : Arrays.asList(mol5, mol3)){
    		thisBP.addMolecule(mol);
    		mol.secondaryStructures().add(thisBP);
    	}
		return thisBP;
	}
    
    @SuppressWarnings("unchecked")
	private void addBaseTriple(Element basetriple, int molID) {
    	Set<Integer> parentMols = new LinkedHashSet<Integer>();
    	List<BasePair> basepairs = new ArrayList<BasePair>(3);

    	for (Element basepair: (List<Element>) basetriple.getChildren("base-pair")){
			BasePair newBP = addBasePair(basepair, molID);
			if (newBP!=null) {
				basepairs.add(newBP);
				parentMols.addAll(latestMolIDs);
			}
		} 
    	for (Element basepairID: (List<Element>) basetriple.getChildren("base-pair-id")){
    		String bpID = basepairID.getAttributeValue("ref");
    		Element basepair = getElementByID(rnamlEl.getDescendants(new ElementFilter("base-pair")), bpID);
			if (basepair != null) {
				BasePair newBP =  addBasePair(basepair, molID);
				if (newBP!=null) {
					basepairs.add(newBP);
					parentMols.addAll(latestMolIDs);
				}
			}
		} 
    	if ((basepairs.size()!=3)&&(basepairs.size()!=2)){
    		//error
    		System.err.println("Invalid basetriple specification in rnaml.");
    		return;
    	}
    	
    	BaseTriple thisBT = new BaseTriple(basepairs, source);
    		
    	for (Integer thisMolID : parentMols){
        	NucleicAcid mol = rnamlIndexMolecules.get(thisMolID);
        	thisBT.addMolecule(mol);
        	mol.secondaryStructures().add(thisBT);
    	}
		
	}

    private void addBaseStack(Element basestack, int molID) {
        int mol1ID = molID;
        int mol2ID = molID;
    	Element base1 = (Element) basestack.getChildren("base-id").get(0);
    	Element base2 = (Element) basestack.getChildren("base-id").get(1);
        int pos1  = Integer.parseInt(base1.getChildText("position"));
        int pos2  = Integer.parseInt(base2.getChildText("position"));

        if (molID == -1) {
        mol1ID = Integer.parseInt(base1.getChild("molecule-id").getAttributeValue("ref"));
        mol2ID = Integer.parseInt(base2.getChild("molecule-id").getAttributeValue("ref"));
        }
        
    	NucleicAcid mol1 = rnamlIndexMolecules.get(molID);
    	NucleicAcid mol2 = rnamlIndexMolecules.get(molID);
    	
		Residue r1 = getRes(mol1ID, pos1-1);
		Residue r2 = getRes(mol2ID, pos2-1);
        
		// TODO throw errors here
        if (r1 == null) return;
        if (r2 == null) return;

        BaseStack thisBS = new BaseStack(Arrays.asList(r1, r2), source);
    	
    	for (Biopolymer mol : Arrays.asList(mol1, mol2)){
    		thisBS.addMolecule(mol);
    		mol.secondaryStructures().add(thisBS);
    	}
		return;
	}
    
	private Duplex addHelix(Element helix, int molID ) {

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
        latestMolIDs = Arrays.asList(mol5i, mol3i);
        
    	NucleicAcid mol5 = rnamlIndexMolecules.get(mol5i);
    	NucleicAcid mol3 = rnamlIndexMolecules.get(mol3i);
    	Duplex dup = null;
    	
    	for (int idx=0; idx<hLen; idx++){
    		Residue r5 = getRes(mol5i, pos5i-1+idx);
    		Residue r3 = getRes(mol3i, pos3i-1-idx);
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
        	for (Biopolymer mol : Arrays.asList(mol5, mol3)){
        		dup.addMolecule(mol);
        		mol.secondaryStructures().add(dup);
        	}
    	}

    	return dup;
    }

    @SuppressWarnings("unchecked")
	private void addPseudoknot(Element pseudoknot, int molID) {
    	Set<Integer> parentMols = new LinkedHashSet<Integer>();
    	List<Duplex> helices = new ArrayList<Duplex>(2);

    	for (Element helixID: (List<Element>) pseudoknot.getChildren("helix-id")){
    		String hID = helixID.getAttributeValue("ref");
    		Element helix = getElementByID(rnamlEl.getDescendants(new ElementFilter("helix")), hID);
			if (helix != null) {
				Duplex dup =  addHelix(helix, molID);//TODO *usually* results in duplicate duplex, could fix as with BasePairs
				if (dup!=null) {
					helices.add(dup);
					parentMols.addAll(latestMolIDs);
				}
			}
		} 
    	if ((helices.size()!=2)){
    		//error
    		System.err.println("Invalid pseudoknot specification in rnaml.");
    		return;
    	}
    	
    	Pseudoknot thisKnot = new Pseudoknot(helices.get(0), helices.get(1), source);
    		
    	for (Integer thisMolID : parentMols){
        	NucleicAcid mol = rnamlIndexMolecules.get(thisMolID);
        	thisKnot.addMolecule(mol);
        	mol.secondaryStructures().add(thisKnot);
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
        
		return "other";
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

	private Element getElementByID(Iterator elementIt, String id){
        while (elementIt.hasNext()) {
            Element element = (Element) elementIt.next();
			if (element.getAttributeValue("id")==id) return element;
		}
		return null;
	}

}
