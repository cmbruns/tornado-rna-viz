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
public abstract class PDBResidueClass extends PDBMoleculeClass implements Selectable, MutablePDBResidue {
    private static Color defaultColor = new Color(255, 255, 255);
    
    public static List<String> modifiedAdenylates   = Arrays.asList("+A","1MA"); 
    public static List<String> modifiedCytidylates  = Arrays.asList("+C","5MC","OMC","DOC"); 
    public static List<String> modifiedGuanylates   = Arrays.asList("+G","2MG","7MG","M2G","YG","OMG"); 
    public static List<String> modifiedInositates   = Arrays.asList("+I"); 
    public static List<String> modifiedThymidylates = Arrays.asList("+T"); 
    public static List<String> modifiedUridylates   = Arrays.asList("+U","PSU","H2U","5MU","4SU"); 
    public static List<String> knownHetatms   		= new ArrayList(Arrays.asList("BRO", "HOH","MG","MO3","NA","ZN")); 
    
    Hashtable genericBonds = new Hashtable(); // maps atom names of bondable atoms
    char insertionCode = ' ';
    public int residueNumber = 0; // TODO create accessors
    Map<String, Set<Atom> > atomNames = new HashMap<String, Set<Atom> >();
    private Collection secondaryStructures = new HashSet();

    // Even if there is a break in the sequence, the next residue train should probably jump over the gap
    PDBResidueClass nextResidue;
    PDBResidueClass previousResidue;

    public Iterator getSecondaryStructureIterator() {return secondaryStructures.iterator();}
    public Collection<SecondaryStructure> secondaryStructures() {return secondaryStructures;}
    public void addSecondaryStructure(SecondaryStructure structure) {
        secondaryStructures.add(structure);
    }
        
    public Color getDefaultColor() {return defaultColor;}
    
    public Collection getHydrogenBondDonors() {
        return new HashSet();
    }
    public Collection getHydrogenBondAcceptors() {
        return new HashSet();        
    }
    
    /**
     * Return preferred location of backbone trace in molecular representations
     */
    public Vector3D getBackbonePosition() 
    throws InsufficientAtomsException
    {throw new InsufficientAtomsException("Backbone not defined for this residue type");}

    public Vector3D getSideChainPosition() 
    throws InsufficientAtomsException
    {throw new InsufficientAtomsException("Side chain not defined for this residue type");}
    
    /**
     * Create an empty Residue object with no atoms.
     */
    public PDBResidueClass() {
        addGenericBonds();
    }

    public void setNextResidue(PDBResidueClass r) {nextResidue = r;}
    public Residue getNextResidue(){return nextResidue;}
    public void setPreviousResidue(PDBResidueClass r) {previousResidue = r;}
    public Residue getPreviousResidue(){return previousResidue;}
    
    abstract public char getOneLetterCode();
    abstract public String getResidueName();
    
    /**
     * Creates a new residue object.
     * 
     * This method does NOT create objects of derived classes such as AminoAcid.
     * Use createFactoryResidue() for that purpose.
     * 
     * @param bagOfAtoms
     */
    public PDBResidueClass(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms);
        addGenericBonds();
        indexAtoms(bagOfAtoms);
        createGenericBonds();
    }
    
    protected void indexAtoms(PDBAtomSet bagOfAtoms) {
        if (bagOfAtoms == null) return;
        
        // Inherit residue properties from the first atom in the list
        if (bagOfAtoms.size() > 0) {
            PDBAtom atom = (PDBAtom) bagOfAtoms.get(0);
            insertionCode = atom.getInsertionCode();
            residueNumber = atom.getResidueNumber();
        }
        
        for (int a = 0; a < bagOfAtoms.size(); a++) {
            PDBAtom atom = (PDBAtom) bagOfAtoms.get(a);
            // atoms.add(atom);
            
            // Create index for lookup by atom name
            // Short atom name
            String atomName = atom.getPDBAtomName();
            
            // Insertion code qualified atom name
            char insertionCode = atom.getInsertionCode();
            String fullAtomName = atomName + ":";
            if (insertionCode != ' ') {fullAtomName = fullAtomName + insertionCode;}
            
            String shortAtomName = atomName.trim();
            
            String[] names = {atomName, fullAtomName, shortAtomName};
            for (String name : names) {
                if (! atomNames.containsKey(name)) atomNames.put(name, new LinkedHashSet<Atom>());
                atomNames.get(name).add(atom);                
            }
        }
    }
    
    protected void addGenericBonds() {
    }
    
    public void addGenericBond(String atom1, String atom2) {
        if (!genericBonds.containsKey(atom1))
            genericBonds.put(atom1, new HashSet());
        if (!genericBonds.containsKey(atom2))
            genericBonds.put(atom2, new HashSet());

        ((HashSet)genericBonds.get(atom1)).add(atom2);
        ((HashSet)genericBonds.get(atom2)).add(atom1);
    }
    
    public LocatedMolecule get(FunctionalGroup fg) 
    throws InsufficientAtomsException
    {
        String[] groupAtomNames = fg.getAtomNames();
        MutableLocatedMolecule mol = new PDBMoleculeClass();
        for (int n = 0; n < groupAtomNames.length ; n ++) {
            String atomName = groupAtomNames[n];
            PDBAtom atom = getAtom(atomName);
            if (atom != null) mol.addAtom(atom);
        }
        if (mol.getAtomCount() < 1)
            throw new InsufficientAtomsException();

        return mol;
    }
    
    /**
     * @return Returns the insertionCode.
     */
    public char getInsertionCode() {
        return insertionCode;
    }
    /**
     * @param insertionCode The insertionCode to set.
     */
    public void setInsertionCode(char insertionCode) {
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
    // TODO - this only works for PDB atoms right now
    void createGenericBonds() {
        ATOM1: for (Iterator a1 = getAtomIterator(); a1.hasNext(); ) {
            LocatedAtom atom = (LocatedAtom) a1.next();
            if (! (atom instanceof PDBAtomClass)) continue ATOM1;
            MutablePDBAtom atom1 = (MutablePDBAtom) atom;
            
            // Assign bonds from residue dictionary
            if (genericBonds.containsKey(atom1.getPDBAtomName())) {
                BOND: for (Iterator b2 = ((HashSet)genericBonds.get(atom1.getPDBAtomName())).iterator(); b2.hasNext(); ) {
                    String a2Name = (String) b2.next();
                    if (! atomNames.containsKey(a2Name)) continue BOND;
                    ATOM2: for (Atom a2 : atomNames.get(a2Name)) {
                    // ATOM2: for (Iterator a2 = ((Vector)atomNames.get(a2Name)).iterator(); a2.hasNext(); ) {
                        MutablePDBAtom atom2 = (MutablePDBAtom) a2;
                        if (! (atom2 instanceof PDBAtom)) continue ATOM2;
    
                        PDBAtom pdbAtom2 = (PDBAtom) atom2;
                        if (! (atom1.getAlternateLocationIndicator() == pdbAtom2.getAlternateLocationIndicator()))
                            continue ATOM2;
                        
                        atom1.addBond(atom2);
                        atom2.addBond(atom1);
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
    public PDBAtom getAtom(String atomName) {
        if (!atomNames.containsKey(atomName)) return null;
        return (PDBAtom) atomNames.get(atomName).iterator().next();
    }
    
    
    /**
     * Create a new Residue of the correct type, e.g. AminoAcid, Adenosine, etc.
     * @return Returns a Residue object of the correct subtype
     */
    static PDBResidue createFactoryResidue(PDBAtomSet bagOfAtoms) {
        if (bagOfAtoms == null) return null;
        if (bagOfAtoms.size() == 0) return null;
        PDBAtom atom = (PDBAtom) bagOfAtoms.get(0);
        if (isProtein(atom.getPDBResidueName())) return AminoAcid.createFactoryAminoAcid(bagOfAtoms);
        else if (isNucleicAcid(atom.getPDBResidueName())) return Nucleotide.createFactoryNucleotide(bagOfAtoms);
        else if (isKnownHetatm(atom.getPDBResidueName())) return new UnknownResidue(bagOfAtoms);
        
        else {
			System.out.println("unknown residue: name "+atom.getPDBResidueName()+", record name: "+atom.getPDBRecordName());
        	return new UnknownResidue(bagOfAtoms); // default to base class
        }
    }
    
    public String toString() {
    	if (getOneLetterCode()!='?'){
    		return "Chain: "+getChainID()+", res: "+getOneLetterCode() + " " + getResidueNumber();
    	}
    	else {
    		return "Chain: "+getChainID()+", res: "+getResidueName() + " " + getResidueNumber();
    	}
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

}
