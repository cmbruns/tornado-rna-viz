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
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

/** 
 * @author Christopher Bruns
 * 
 * One protein amino acid residue
 */
abstract public class AminoAcid extends PDBResidueClass {
    public AminoAcid() {}
    public AminoAcid(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}

    abstract public char getOneLetterCode();
    abstract public String getThreeLetterCode();
    abstract public String getResidueName();

    static String[] sideChainAtomNames = {
        " CA ", " CB ", " CG ", " CG1", " CG2", " OG ", " OG1", " SG ", 
        " CD ", " CD1", " CD2", " OD ", " OD1", " OD2", " ND ", " ND1", " ND2", " SD ",
        " CE ", " CE1", " CE2", " OE ", " OE1", " OE2", " NE ", " NE1", " NE2",
        " NZ ", " CZ ", " OH ", " NH1", " NH2"};
    static String[] backboneAtomNames = {" N  ", " CA ", " C  ", " O  ", " OXT"};

    static public FunctionalGroup sideChainGroup = new FunctionalGroup(sideChainAtomNames);
    static public FunctionalGroup backboneGroup = new FunctionalGroup(backboneAtomNames);

    public Vector3D getBackbonePosition() {
        LocatedAtom atom = getAtom(" CA ");
        if (atom == null) return null;
        return atom.getCoordinates();
    }

    public Vector3D getSideChainPosition() {
        StructureMolecule sideChain = get(sideChainGroup);
        if (sideChain.getAtomCount() >= 1)
            return get(sideChainGroup).getCenterOfMass();
        // TODO - handle glycine and other no side chain cases
        //  by estimating position of fake CA
        else return null;
    }
    
    protected void addGenericBonds() {
        super.addGenericBonds();
        // Backbone
        addGenericBond(" N  ", " CA ");
        addGenericBond(" CA ", " C  ");
        addGenericBond(" C  ", " O  ");
        
        // C-terminus
        addGenericBond(" C  ", " OXT");        
        
        // Side chain
        addGenericBond(" CA ", " CB ");        

        addGenericBond(" CB ", " CG ");        
        addGenericBond(" CB ", " CG1"); // ILE, VAL
        addGenericBond(" CB ", " CG2"); // ILE, VAL, THR
        addGenericBond(" CB ", " OG "); // SER 
        addGenericBond(" CB ", " OG1"); // THR       
        addGenericBond(" CB ", " SG "); // CYS

        addGenericBond(" CG ", " CD ");        
        addGenericBond(" CG ", " CD1");        
        addGenericBond(" CG ", " CD2");        
        addGenericBond(" CG1", " CD1"); // ILE     
        addGenericBond(" CG ", " OD ");        
        addGenericBond(" CG ", " OD1");        
        addGenericBond(" CG ", " OD2");        
        addGenericBond(" CG ", " ND ");        
        addGenericBond(" CG ", " ND1");        
        addGenericBond(" CG ", " ND2"); // ASN
        addGenericBond(" CG ", " SD "); // MET       

        addGenericBond(" CD ", " CE ");        
        addGenericBond(" CD ", " OE ");        
        addGenericBond(" CD ", " OE1");        
        addGenericBond(" CD ", " OE2");        
        addGenericBond(" CD ", " NE ");        
        addGenericBond(" CD ", " NE2"); // GLN
        addGenericBond(" CD1", " CE1");
        addGenericBond(" CD2", " CE2");
        addGenericBond(" CD2", " NE2"); // HIS
        addGenericBond(" ND1", " CE1"); // HIS
        addGenericBond(" NE2", " CE1"); // HIS
        addGenericBond(" SD ", " CE "); // MET
        
        addGenericBond(" CE ", " NZ ");
        addGenericBond(" CE1", " CZ ");
        addGenericBond(" CE2", " CZ ");
        addGenericBond(" NE ", " CZ "); // ARG

        addGenericBond(" CZ ", " OH "); // TYR
        addGenericBond(" CZ ", " NH1"); // ARG
        addGenericBond(" CZ ", " NH2"); // ARG
        
    }
    
    static public AminoAcid createFactoryAminoAcid(PDBAtomSet bagOfAtoms) {
        // Distinguish among subclasses
        PDBAtom atom = (PDBAtom) bagOfAtoms.get(0);
        String residueName = atom.getPDBResidueName().trim().toUpperCase();
        
        if (residueName.equals("ALA")) {return new Alanine(bagOfAtoms);}
        if (residueName.equals("CYS")) {return new Cysteine(bagOfAtoms);}
        if (residueName.equals("ASP")) {return new Aspartate(bagOfAtoms);}
        if (residueName.equals("GLU")) {return new Glutamate(bagOfAtoms);}
        if (residueName.equals("PHE")) {return new Phenylalanine(bagOfAtoms);}
        if (residueName.equals("GLY")) {return new Glycine(bagOfAtoms);}
        if (residueName.equals("HIS")) {return new Histidine(bagOfAtoms);}
        if (residueName.equals("ILE")) {return new Isoleucine(bagOfAtoms);}
        if (residueName.equals("LYS")) {return new Lysine(bagOfAtoms);}
        if (residueName.equals("LEU")) {return new Leucine(bagOfAtoms);}
        if (residueName.equals("MET")) {return new Methionine(bagOfAtoms);}
        if (residueName.equals("ASN")) {return new Asparagine(bagOfAtoms);}
        if (residueName.equals("PRO")) {return new Proline(bagOfAtoms);}
        if (residueName.equals("GLN")) {return new Glutamine(bagOfAtoms);}
        if (residueName.equals("ARG")) {return new Arginine(bagOfAtoms);}
        if (residueName.equals("SER")) {return new Serine(bagOfAtoms);}
        if (residueName.equals("THR")) {return new Threonine(bagOfAtoms);}
        if (residueName.equals("VAL")) {return new Valine(bagOfAtoms);}
        if (residueName.equals("TRP")) {return new Tryptophan(bagOfAtoms);}
        if (residueName.equals("TYR")) {return new Tyrosine(bagOfAtoms);}

        return new UnknownAminoAcid(bagOfAtoms);
    }
}
