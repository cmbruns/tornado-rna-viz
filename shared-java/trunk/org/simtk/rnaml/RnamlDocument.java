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
    
    // Populate duplexes of RNA molecules
    public void importDuplexes() {
        // Parse the xml file
        Element rnamlEl;
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
//            PDBMolecule thisMol = (PDBMolecule) naChains.get(molID-1);
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
            List<Integer> nTable = new ArrayList<Integer>();
            Matcher toke = Pattern.compile("\\d+").matcher(nTableString);
            while (toke.find()) {
                nTable.add(Integer.parseInt(toke.group(0)));
            }
            resNumTables.put(molID, nTable);
            
            // Parse intramolecular helices
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

        // Parse intermolecular helices
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

    private void addHelix(int mol5i, int pos5i, int mol3i, int pos3i, int hlen) {
        NucleicAcid mol5 = rnamlIndexMolecules.get(mol5i);
        NucleicAcid mol3 = rnamlIndexMolecules.get(mol3i);
        
        List<Integer> nTable5 = resNumTables.get(mol5i);
        List<Integer> nTable3 = resNumTables.get(mol3i);
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
                        Collection<LocatedAtom> r5atoms = ((PDBMoleculeClass) r5).getAtoms();
                        Object[] r5atomsArr = r5atoms.toArray();
                        PDBAtom r5a0 = (PDBAtom) r5atomsArr[0];
                        System.out.println("unrecognized residue reported in rnaml as basepaired:");
                        System.out.println("mol5 id "+mol5i+", resno "+PDBpos5+", res name "+r5a0.getPDBResidueName()+", res "+r5);
                    }
                    if (!(r3 instanceof Nucleotide)){
                        Collection<LocatedAtom> r3atoms = ((PDBMoleculeClass) r3).getAtoms();
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
