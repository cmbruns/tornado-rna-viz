/*
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.protein;

import org.simtk.atomicstructure.PDBAtom;
import org.simtk.atomicstructure.PDBAtomSet;
import org.simtk.molecularstructure.*;

/** 
 * @author Christopher Bruns
 * 
 * One protein amino acid residue
 */
abstract public class AminoAcid extends Residue {
    public AminoAcid() {}
    public AminoAcid(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}

    abstract public char getOneLetterCode();
    abstract public String getThreeLetterCode();
    abstract public String getResidueName();

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
        PDBAtom atom = bagOfAtoms.get(0);
        String residueName = atom.getResidueName().trim().toUpperCase();
        
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
