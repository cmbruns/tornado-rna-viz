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
package org.simtk.molecularstructure;

import java.util.*;
import java.awt.Color;
import java.text.*;
import javax.naming.OperationNotSupportedException;

import org.simtk.molecularstructure.protein.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.geometry3d.*;
import org.simtk.util.*;

/** 
 * @author Christopher Bruns
 * 
 * \brief One monomer residue of a Biopolymer
 *
 */
public class ResidueClass extends MolecularClass implements Residue {
    public static List<String> modifiedAdenylates   = Arrays.asList("+A","1MA"); 
    public static List<String> modifiedCytidylates  = Arrays.asList("+C","5MC","OMC","DOC"); 
    public static List<String> modifiedGuanylates   = Arrays.asList("+G","2MG","7MG","M2G","YG","OMG"); 
    public static List<String> modifiedInositates   = Arrays.asList("+I"); 
    public static List<String> modifiedThymidylates = Arrays.asList("+T"); 
    public static List<String> modifiedUridylates   = Arrays.asList("+U","PSU","H2U","5MU","4SU"); 
    public static List<String> knownHetatms   		= new ArrayList<String>(Arrays.asList("BRO", "HOH","MG","MO3","NA","ZN")); 
    
    // Map<String, Set<String>> genericBonds = new HashMap<String, Set<String>>();
    char insertionCode = ' ';
    public Integer residueNumber = null;
    private Set<SecondaryStructure> secondaryStructures = new HashSet<SecondaryStructure>();
    private ResidueType residueType;
    private ResidueAtoms residueAtoms = new ResidueAtoms(this);

    // Even if there is a break in the sequence, the next residue train should probably jump over the gap
    protected Residue nextResidue;
    protected Residue previousResidue;

    public ResidueClass(ResidueType type) {
        this.residueType = type;
        createGenericBonds();
    }
    
    public ResidueClass(String PdbLine) throws ParseException {
        if ((PdbLine.substring(0,6).equals("ATOM  ")) || (PdbLine.substring(0,6).equals("HETATM"))) {        
            String residueName = PdbLine.substring(17,20);
            int residueNumber = new Integer(PdbLine.substring(22,26).trim()).intValue();
            char insertionCode = PdbLine.charAt(26);
            
            this.residueType = ResidueTypeClass.getType(residueName);
            this.residueNumber = residueNumber;
            this.insertionCode = insertionCode;
        }
        else throw new ParseException("Unrecognized PDB line", 0);
    }
    
    public boolean matchesPdbLine(String PdbLine) throws ParseException {
        if ((PdbLine.substring(0,6).equals("ATOM  ")) || (PdbLine.substring(0,6).equals("HETATM"))) {        
            String residueName = PdbLine.substring(17,20);
            int residueNumber = new Integer(PdbLine.substring(22,26).trim()).intValue();
            char insertionCode = PdbLine.charAt(26);
            
            if (insertionCode != this.insertionCode) return false;
            if (residueNumber != this.residueNumber) return false;
            return true;
        }
        else throw new ParseException("Unrecognized PDB line", 0);
    }
    
    /**
     * Override atoms() method to use a class that can index by atom name
     */
    @Override
    public Set<Atom> atoms() {
        return residueAtoms;
    }

    public void setNextResidue(Residue r) {this.nextResidue = r;}
    public void setPreviousResidue(Residue r) {this.previousResidue = r;}

    public Residue getNextResidue(){return this.nextResidue;}
    public Residue getPreviousResidue(){return this.previousResidue;}
    
    public ResidueType getResidueType() {return this.residueType;} 
    
    public Collection<SecondaryStructure> secondaryStructures() {return secondaryStructures;}

    public Set<Atom> getHydrogenBondDonors() {
        Set<Atom> answer = new HashSet<Atom>();
        for (String atomName : getHydrogenBondDonorAtomNames()) {
            Atom atom = getAtom(atomName);
            if (atom != null) answer.add(atom);
        }
        return answer;
    }
    public Set<Atom> getHydrogenBondAcceptors() {
        Set<Atom> answer = new HashSet<Atom>();
        for (String atomName : getHydrogenBondAcceptorAtomNames()) {
            Atom atom = getAtom(atomName);
            if (atom != null) answer.add(atom);
        }
        return answer;
    }

    public Set<String> getHydrogenBondDonorAtomNames() {
        return residueType.getHydrogenBondDonorAtomNames();
    }
    
    public Set<String> getHydrogenBondAcceptorAtomNames() {
        return residueType.getHydrogenBondAcceptorAtomNames();
    };

    /**
     * Return preferred location of backbone trace in molecular representations
     */
    public Vector3D getBackbonePosition() 
    throws InsufficientAtomsException
    {
        String[] backboneAtoms = null;
        if (getResidueType() instanceof AminoAcid) {
            String[] bAtoms = {"CA","N","C"};
            backboneAtoms = bAtoms;
        }
        else if (getResidueType() instanceof Nucleotide) {
            String[] bAtoms = {"P","O5*","C5*","C4*","C3*","O3*"};
            backboneAtoms = bAtoms;
        }
        if (backboneAtoms != null) {
            for (String atomName : backboneAtoms) {
                Atom atom = getAtom(atomName);
                if (atom == null) continue;
                if (atom.getCoordinates() == null) continue;
                return atom.getCoordinates();
            }
            throw new InsufficientAtomsException("No backbone atoms found");   
        }
        throw new InsufficientAtomsException("Backbone not defined for this residue type");
    }

    public Vector3D getSideChainPosition() 
    throws InsufficientAtomsException
    {
        if (getResidueType() instanceof AminoAcid) {
            Molecular sideChain = get(AminoAcid.sideChainGroup);
            if (sideChain.atoms().size() >= 1)
                return get(AminoAcid.sideChainGroup).getCenterOfMass();
            // TODO - handle glycine and other no side chain cases
            //  by estimating position of fake CB
            else throw new InsufficientAtomsException("No side chain atoms found");
        }
        else if (getResidueType() instanceof Nucleotide) {
            Molecular base = get(Nucleotide.baseGroup);
            if (base != null)
                return base.getCenterOfMass();
            else throw new InsufficientAtomsException("No side chain atoms found");
        }
        throw new InsufficientAtomsException("Side chain not defined for this residue type");
    }
    
    /**
     * Create an empty Residue object with no atoms.
     */
    public final char getOneLetterCode() {return residueType.getOneLetterCode();}
    public final String getResidueName() {return residueType.getResidueName();}
    public Map<String, Set<String>> genericBonds() {return residueType.genericBonds();}
    public final String getThreeLetterCode() {return residueType.getThreeLetterCode();}
    
    /**
     * Creates a new residue object.
     * 
     * This method does NOT create objects of derived classes such as AminoAcid.
     * Use createFactoryResidue() for that purpose.
     * 
     * @param bagOfAtoms
     */

    public Molecular get(FunctionalGroup fg) 
    throws InsufficientAtomsException
    {
        String[] groupAtomNames = fg.getAtomNames();
        Molecular mol = new MolecularClass();
        for (int n = 0; n < groupAtomNames.length ; n ++) {
            String atomName = groupAtomNames[n];
            Atom atom = getAtom(atomName);
            if (atom != null) mol.atoms().add(atom);
        }
        if (mol.atoms().size() < 1)
            throw new InsufficientAtomsException();

        return mol;
    }
    
    /**
     * @return Returns the insertionCode.
     */
    public char getPdbInsertionCode() {
        return insertionCode;
    }
    /**
     * @param insertionCode The insertionCode to set.
     */
    public void setPdbInsertionCode(char insertionCode) {
        this.insertionCode = insertionCode;
    }
    /**
     * @return Returns the residueNumber.
     */
    public int getResidueNumber() {
        return residueNumber;
    }
    /**
     * @param residueNumber The residueNumber to set.
     */
    public void setResidueNumber(int residueNumber) {
        this.residueNumber = residueNumber;
    }
    /** 
     * Identify covalent bonds in the residue.
     * 
     * This must be performed after atoms have been loaded into the residue.
     * Only do this once for each residue.
     */
    void createGenericBonds() {
        ATOM1: for (Atom atom1 : atoms()) {
            // Assign bonds from residue dictionary
            if (genericBonds().containsKey(atom1.getAtomName())) {
                BOND: for (String a2Name : genericBonds().get(atom1.getAtomName())) {
                    ATOM2: for (Atom atom2 : residueAtoms.getAtoms(a2Name)) {
                        atom1.bonds().add(atom2);
                        atom2.bonds().add(atom1);
                    }
                }
            }
            
        }
    }
    
    /**
     * Get the atom with the given name.
     * 
     * If there are atoms with alternate locations, the first such atom is returned.
     * If the atom name argument includes an alternate location character, the atom at that particular
     * location is returned.
     * @param atomName
     * @return
     */
    public Atom getAtom(String atomName) {
        return residueAtoms.getAtom(atomName);
    }
    
    public static boolean isSolvent(String residueName) {
        String trimmedName = residueName.trim().toUpperCase(); // remove spaces

        // Water
        if (trimmedName.equals("HOH")) return true;
        if (trimmedName.equals("WAT")) return true;
        if (trimmedName.equals("H2O")) return true;
        if (trimmedName.equals("SOL")) return true;
        if (trimmedName.equals("TIP")) return true;

        // Deuterated water
        if (trimmedName.equals("DOD")) return true;
        if (trimmedName.equals("D2O")) return true;

        // Sulfate
        if (trimmedName.equals("SO4")) return true;
        if (trimmedName.equals("SUL")) return true;

        // Phosphate
        if (trimmedName.equals("PO4")) return true;

        return false;
    }
    
    public static boolean isProtein(String residueName) {
        String trimmedName = residueName.trim().toUpperCase(); // remove spaces

        if (trimmedName.equals("ALA")) return true;
        if (trimmedName.equals("CYS")) return true;
        if (trimmedName.equals("ASP")) return true;
        if (trimmedName.equals("GLU")) return true;
        if (trimmedName.equals("PHE")) return true;
        if (trimmedName.equals("GLY")) return true;
        if (trimmedName.equals("HIS")) return true;
        if (trimmedName.equals("ILE")) return true;
        if (trimmedName.equals("LYS")) return true;
        if (trimmedName.equals("LEU")) return true;
        if (trimmedName.equals("MET")) return true;
        if (trimmedName.equals("ASN")) return true;
        if (trimmedName.equals("PRO")) return true;
        if (trimmedName.equals("GLN")) return true;
        if (trimmedName.equals("ARG")) return true;
        if (trimmedName.equals("SER")) return true;
        if (trimmedName.equals("THR")) return true;
        if (trimmedName.equals("VAL")) return true;
        if (trimmedName.equals("TRP")) return true;
        if (trimmedName.equals("TYR")) return true;

        if (trimmedName.equals("ASX")) return true;
        if (trimmedName.equals("GLX")) return true;
                
        return false;
    }

    public static boolean isNucleicAcid(String residueName) {
        String trimmedName = residueName.trim().toUpperCase(); // remove spaces

        if (trimmedName.equals("A")) return true;
        if (trimmedName.equals("C")) return true;
        if (trimmedName.equals("G")) return true;
        if (trimmedName.equals("I")) return true;
        if (trimmedName.equals("T")) return true;
        if (trimmedName.equals("U")) return true;
        if (modifiedAdenylates.contains(trimmedName)) return true;
        if (modifiedCytidylates.contains(trimmedName)) return true;
        if (modifiedGuanylates.contains(trimmedName)) return true;
        if (modifiedInositates.contains(trimmedName)) return true;
        if (modifiedThymidylates.contains(trimmedName)) return true;
        if (modifiedUridylates.contains(trimmedName)) return true;
                    
        return false;
    }

    public static boolean isKnownHetatm(String residueName) {
        String trimmedName = residueName.trim().toUpperCase(); // remove spaces

        if (knownHetatms.contains(trimmedName)) return true;
                    
        return false;
    }

    
    public static boolean isDNA(String residueName) {
        String trimmedName = residueName.trim().toUpperCase(); // remove spaces

        if (trimmedName.equals("A")) return true;
        if (trimmedName.equals("C")) return true;
        if (trimmedName.equals("G")) return true;
        if (trimmedName.equals("T")) return true;
        if (modifiedAdenylates.contains(trimmedName)) return true;
        if (modifiedCytidylates.contains(trimmedName)) return true;
        if (modifiedGuanylates.contains(trimmedName)) return true;
//TODO        if (ModifiedInositates.contains(trimmedName)) return true;
        if (modifiedThymidylates.contains(trimmedName)) return true;
                 
        return false;
    }

    public static boolean isRNA(String residueName) {
        String trimmedName = residueName.trim().toUpperCase(); // remove spaces

        if (trimmedName.equals("A")) return true;
        if (trimmedName.equals("C")) return true;
        if (trimmedName.equals("G")) return true;
        if (trimmedName.equals("U")) return true;
        if (modifiedAdenylates.contains(trimmedName)) return true;
        if (modifiedCytidylates.contains(trimmedName)) return true;
        if (modifiedGuanylates.contains(trimmedName)) return true;
//TODO:Inositates?        if (ModifiedInositates.contains(trimmedName)) return true;
        if (modifiedUridylates.contains(trimmedName)) return true;
                 
        return false;
    }

    public boolean isStrand() {
        for (Iterator i = secondaryStructures().iterator(); i.hasNext();) {
            SecondaryStructure structure = (SecondaryStructure) i.next();
            if (structure instanceof BetaStrand)
                return true;
        }
        return false;        
    }
    public boolean isHelix() {
        for (Iterator i = secondaryStructures().iterator(); i.hasNext();) {
            SecondaryStructure structure = (SecondaryStructure) i.next();
            if (structure instanceof Helix)
                return true;
        }
        return false;        
    }
    public boolean isAlphaHelix() {
        for (Iterator i = secondaryStructures().iterator(); i.hasNext();) {
            SecondaryStructure structure = (SecondaryStructure) i.next();
            if ((structure instanceof Helix) && 
                    (((Helix)structure).getHelixType() == Helix.ALPHA) )
                return true;
        }
        return false;
    }

    /**
     *  
      * @author Christopher Bruns
      * 
      * An atom container that keeps an index of atoms by name
     */
    class ResidueAtoms extends LinkedHashSet<Atom> {
        private Residue residue;
        private Map<String, Set<Atom> > nameAtoms = new HashMap<String, Set<Atom> >();
        
        ResidueAtoms(Residue residue) {
            this.residue = residue;
        }
        
        public Atom getAtom(String atomName) {
            if (! (nameAtoms.containsKey(atomName))) return null;
            return nameAtoms.get(atomName).iterator().next();
        }

        public Set<Atom> getAtoms(String atomName) {
            return nameAtoms.get(atomName);
        }
        
        public boolean add(Atom atom) {
            boolean answer = super.add(atom);
            if (answer) addAtomName(atom);
            return answer;
        }
        
        public boolean addAll(Collection<? extends Atom> c) {
            boolean answer = super.addAll(c);
            if (answer) {
                for (Atom atom : c)
                    addAtomName(atom);
            }
            return answer;
        }
        
        public void clear() {
            super.clear();
            nameAtoms.clear();
        }
        
        public boolean remove(Object atom) {
            boolean answer = super.remove(atom);
            if (answer) {
                if (atom instanceof Atom) removeAtomName((Atom)atom);
            }
            return answer;
        }
        
        public boolean removeAll(Collection<?> c) {
            boolean answer = super.removeAll(c);
            if (answer) {
                for (Object atom : c)
                    if (atom instanceof Atom) removeAtomName((Atom)atom);
            }
            return answer;
        }        
        
        public boolean retainAll(Collection<?> c) {
            boolean answer = super.removeAll(c);
            if (answer) {
                nameAtoms.clear();
                for (Atom atom : this)
                    addAtomName(atom);
            }
            return answer;
        }        
        
        private void removeAtomName(Atom atom) {
            for (String name : atomNames(atom)) {
                nameAtoms.get(name).remove(atom);
                if (nameAtoms.get(name).size() == 0) nameAtoms.remove(name);
            }
        }
        
        private void addAtomName(Atom atom) {
            // Create index for lookup by atom name
            for (String name : atomNames(atom)) {
                if (! nameAtoms.containsKey(name)) nameAtoms.put(name, new LinkedHashSet<Atom>());
                nameAtoms.get(name).add(atom);                
            }
            
            // Create bonds between atoms
            // 1) Look for generic bonds
            String atomName1 = atom.getAtomName();
            Map<String, Set<String>> gBonds = residue.getResidueType().genericBonds();
            if (gBonds.containsKey(atom.getAtomName())) {
                for (String atomName2 : gBonds.get(atomName1)) {
                    Atom atom2 = residue.getAtom(atomName2);
                    if (atom2 == null) continue;
                    atom.bonds().add(atom2);
                    atom2.bonds().add(atom);
                }
            }
            else { // No generic bonds? bond by distance
                // TODO
            }
        }
        
        private Set<String> atomNames(Atom atom) {
            Set<String> answer = new HashSet<String>();
            // Create index for lookup by atom name
            // Short atom name
            String atomName = atom.getAtomName();
            
            String shortAtomName = atomName.trim();
            
            // Insertion code qualified atom name
//            char insertionCode = atom.getInsertionCode();
//            String fullAtomName = atomName + ":";
//            if (insertionCode != ' ') {fullAtomName = fullAtomName + insertionCode;}

            String[] names = {atomName, shortAtomName};
            for (String name : names) answer.add(name);

            return answer;            
        }
    }
}
