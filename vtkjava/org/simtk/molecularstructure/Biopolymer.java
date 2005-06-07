/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure;

import java.util.*;

import org.simtk.atomicstructure.*;

/**
 * @author Christopher Bruns
 *
 * \brief A macromolecular heteropolymer, such as protein or DNA
 */
public class Biopolymer extends Molecule {
	Vector<Residue> residues = new Vector<Residue>();
    Hashtable<String, Residue> residueNumbers = new Hashtable<String, Residue>();
    // maps atom names of bondable atoms that bond one residue to the next
    Hashtable<String, HashSet<String> > genericResidueBonds = new Hashtable<String, HashSet<String> >(); 

	// Distinguish between array index of residues and their sequence "number"
	public Residue getResidue(int i) {return residues.get(i);} // array index
    public Vector<Residue> residues() {return residues;}

    // sequence number
    public Residue getResidueByNumber(int i) {return getResidueByNumber(new Integer(i).toString());}
    public Residue getResidueByNumber(int i, char insertionCode) {return getResidueByNumber(new Integer(i).toString() + insertionCode);}
    public Residue getResidueByNumber(String n) {return residueNumbers.get(n);}

    public int getResidueCount() {return residues.size();}
	
	public Biopolymer() {
        super();
        addGenericResidueBonds();
        createResidueBonds();
    } // Empty molecule
    public Biopolymer(PDBAtomSet atomSet) {
        super(atomSet); // fills atoms array

        // Parse into residues
        // Each residue should have a unique index/insertionCode combination
        // TODO But what if the residue name (pathologically) changes within an index/insertionCode?
        String previousResidueKey = "Hey, this isn't a reasonable residue key!!!";
        PDBAtomSet newResidueAtoms = new PDBAtomSet();
        for (int a = 0; a < atomSet.size(); a++) {
            PDBAtom atom = (PDBAtom) atomSet.get(a);
            String residueKey = "" + atom.getResidueIndex() + atom.getInsertionCode();
            if (!residueKey.equals(previousResidueKey)) { // Start a new residue, flush the old one
                if (newResidueAtoms.size() > 0) {
                    Residue residue = Residue.createFactoryResidue(newResidueAtoms);
                    residues.addElement(residue);
                    String numberString = "" + residue.getResidueNumber();
                    String fullString = numberString + residue.getInsertionCode();
                    residueNumbers.put(fullString, residue); // number plus insertion code
                    // Only the first residue with a particular number gets to be invoked by that number alone
                    if (!residueNumbers.containsKey(numberString)) 
                        residueNumbers.put(numberString, residue); // number 
                }
                newResidueAtoms = new PDBAtomSet();
            }
            newResidueAtoms.addElement(atom);
            
            previousResidueKey = residueKey;
        }
        // Flush final set of atoms
        if (newResidueAtoms.size() > 0) {
            Residue residue = Residue.createFactoryResidue(newResidueAtoms);
            residues.addElement(residue);
            String numberString = "" + residue.getResidueNumber();
            String fullString = numberString + residue.getInsertionCode();
            residueNumbers.put(fullString, residue); // number plus insertion code
            // Only the first residue with a particular number gets to be invoked by that number alone
            if (!residueNumbers.containsKey(numberString)) 
                residueNumbers.put(numberString, residue); // number 
        }

        addGenericResidueBonds();
        createResidueBonds();
        
        // Connect residues in a doubly linked list
        Residue previousResidue = null;
        for (Residue residue : residues) {
            if (previousResidue != null) {
                residue.setPreviousResidue(previousResidue);
                previousResidue.setNextResidue(residue);
            }
            previousResidue = residue;
        }
    }

    protected void addGenericResidueBond(String atom1, String atom2) {
        // Don't add bond in both directions; these bonds have a direction
        if (!genericResidueBonds.containsKey(atom1))
            genericResidueBonds.put(atom1, new HashSet<String>());
        genericResidueBonds.get(atom1).add(atom2);
    }
    
    protected void addGenericResidueBonds() {
    }
    
    /** 
     * Identify covalent bonds connecting residues.
     * 
     */
    protected void createResidueBonds() {
        Residue previousResidue = null;
        for (Residue residue : residues) {
            if (previousResidue != null) {
                for (String firstAtomName : genericResidueBonds.keySet()) {
                    Atom firstAtom = previousResidue.getAtom(firstAtomName);
                    if (firstAtom != null) {
                        for (String secondAtomName : genericResidueBonds.get(firstAtomName)) {
                            Atom secondAtom = residue.getAtom(secondAtomName);
                            if (secondAtom != null) {
                                // TODO check distance
                                firstAtom.addBond(secondAtom);
                                secondAtom.addBond(firstAtom);
                            }
                        }
                    }
                }
            }
            
            previousResidue = residue;
        }
    }

}
