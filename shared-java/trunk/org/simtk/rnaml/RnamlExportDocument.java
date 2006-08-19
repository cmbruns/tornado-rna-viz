/* Copyright (c) 2005 Stanford University and Eric Willgohs
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
 */

/*
 * Created on Aug 18, 2006
 * Original author: Eric Willgohs
 */
package org.simtk.rnaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;

public class RnamlExportDocument {

	Element rnamlEl = new Element("rnaml").setAttribute("version", "1.1");
	org.jdom.Document rnamlDoc  = new org.jdom.Document(rnamlEl, new DocType("rnaml", "rnaml.dtd"));
    public List<NucleicAcid> rnamlMolecules = new Vector<NucleicAcid>();
    String source = "";

    public XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat() );
    
	public RnamlExportDocument(String program, String version) {
		Element analysis = new Element("analysis").setAttribute("id", program);
		Element programEl = new Element("program");
		programEl.addContent(new Element("prog-name").setText(program));
		programEl.addContent(new Element("prog-version").setText(version));
		analysis.addContent(programEl);
		rnamlEl.addContent(analysis);
		source = program;
	}
	
	public RnamlExportDocument(String program, String version, MoleculeCollection molecules) {
		this(program, version);
        for (Molecule mol : molecules.molecules()) {
            if ( !(mol instanceof NucleicAcid)) continue;
            rnamlMolecules.add((NucleicAcid) mol);
        }
        generateElements();
	}

	private void generateElements() {
		int modelIndx = 1;
		List<SecondaryStructure> interactionList = new ArrayList<SecondaryStructure>();
		for (NucleicAcid mol : rnamlMolecules){
			Element molElem = new Element("molecule");
			molElem.setAttribute("id", getName(mol));
			molElem.setAttribute("type", (mol instanceof RNA)?"rna":"dna");
			Element structure = new Element("structure");
			Element model = new Element("model").setAttribute("id", "model_"+modelIndx);
			modelIndx++;
			Element strAnnot = new Element("str-annotation");
			for (SecondaryStructure struct : mol.secondaryStructures()){
				if (struct.getSource()!=SecondaryStructureClass.SourceType.getSourceType(source)) continue;
				if (!(struct instanceof BasePair)) continue;
				if (struct.getMolecules().size()>1) {
					interactionList.add(struct);
					continue;
				}
				strAnnot.addContent(getBPElem((BasePair)struct, mol));
				
			}
			for (SecondaryStructure struct : mol.secondaryStructures()){
				if (struct.getSource()!=SecondaryStructureClass.SourceType.getSourceType(source)) continue;
				if (!(struct instanceof Duplex)) continue;
				if (struct.getMolecules().size()>1) {
					interactionList.add(struct);
					continue;
				}
				strAnnot.addContent(getHelixElem((Duplex)struct, mol));
				
			}
			model.addContent(strAnnot);
			structure.addContent(model);
			molElem.addContent(structure);
			rnamlEl.addContent(molElem);
		}
		
		Element strAnnot = new Element("str-annotation");
		for (SecondaryStructure struct : interactionList){
			if (struct.getSource()!=SecondaryStructureClass.SourceType.getSourceType(source)) continue;
			if (!(struct instanceof BasePair)) continue;
			strAnnot.addContent(getBPElem((BasePair)struct, null));
			
		}
		for (SecondaryStructure struct : interactionList){
			if (struct.getSource()!=SecondaryStructureClass.SourceType.getSourceType(source)) continue;
			if (!(struct instanceof Duplex)) continue;
			strAnnot.addContent(getHelixElem((Duplex)struct, null));
			
		}
		rnamlEl.addContent(new Element("interactions").addContent(strAnnot));
		
	}

	private String getName(NucleicAcid mol) {
		String molID = mol.getPdbChainId();
		return "Chain_"+((molID.equals(" "))?"SPACE":molID);
	}

	private Element getBPElem(BasePair bp, NucleicAcid mol){

		Element bpEl = new Element("base-pair");
		Element base5 = new Element("base-id");
		Element base3 = new Element("base-id");
		Residue res1 = bp.getResidue1();
		Residue res2 = bp.getResidue2();
		NucleicAcid mol1 = mol;
		NucleicAcid mol2 = mol;

		if (mol==null){
			NucleicAcid molA = (NucleicAcid)(bp.getMolecules().toArray()[0]);
			NucleicAcid molB = (NucleicAcid)(bp.getMolecules().toArray()[1]);
			mol1 = (molA.residues().contains(res1))? molA : molB;
			mol2 = (molB.residues().contains(res2))? molB : molA;
			base5.addContent(new Element("molecule-id").setAttribute("ref", getName(mol1)));
			base3.addContent(new Element("molecule-id").setAttribute("ref", getName(mol2)));
		}
		base5.addContent(new Element("position").setText(""+(mol1.residues().indexOf(res1)+1)));
		base3.addContent(new Element("position").setText(""+(mol2.residues().indexOf(res2)+1)));
		bpEl.addContent(new Element("base-id-5p").addContent(base5));
		bpEl.addContent(new Element("base-id-3p").addContent(base3));
		return bpEl;
	}
	
	private Element getHelixElem(Duplex dup, NucleicAcid mol){

				Element helixEl = new Element("helix");
				Element base5 = new Element("base-id");
				Element base3 = new Element("base-id");
				NucleicAcid mol1 = mol;
				NucleicAcid mol2 = mol;

				if (mol==null){
					NucleicAcid molA = (NucleicAcid)(dup.getMolecules().toArray()[0]);
					NucleicAcid molB = (NucleicAcid)(dup.getMolecules().toArray()[1]);
					mol1 = (rnamlMolecules.indexOf(molA)<rnamlMolecules.indexOf(molB))? molA : molB;
					mol2 = (rnamlMolecules.indexOf(molA)<rnamlMolecules.indexOf(molB))? molB : molA;
					base5.addContent(new Element("molecule-id").setAttribute("ref", getName(mol1)));
					base3.addContent(new Element("molecule-id").setAttribute("ref", getName(mol2)));
				}
				ArrayList<Integer> mol1Residues = new ArrayList<Integer>();
				ArrayList<Integer> mol2Residues = new ArrayList<Integer>();
				for (BasePair bp : dup.basePairs()){
					for (Residue res : bp.residues()){
						if (mol1.residues().contains(res)) mol1Residues.add(mol1.residues().indexOf(res));
						if (mol2.residues().contains(res)) mol2Residues.add(mol2.residues().indexOf(res));
					}
				}
				
				base5.addContent(new Element("position").setText(""+(Collections.min(mol1Residues)+1)));
				base3.addContent(new Element("position").setText(""+(Collections.max(mol2Residues)+1)));
				helixEl.addContent(new Element("base-id-5p").addContent(base5));
				helixEl.addContent(new Element("base-id-3p").addContent(base3));
				helixEl.addContent(new Element("length").setText(""+dup.basePairs().size()));
				return helixEl;
			}

	
	public void writeTo(OutputStream file){
		try {
			outputter.output(this.rnamlDoc, file );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeTo(File file){
		try {
			outputter.output(this.rnamlDoc, new FileWriter(file) );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
