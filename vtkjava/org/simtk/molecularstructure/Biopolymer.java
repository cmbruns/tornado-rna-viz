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
	Vector residues = new Vector();
    Hashtable residueNumbers = new Hashtable();
    // maps atom names of bondable atoms that bond one residue to the next
    Hashtable genericResidueBonds = new Hashtable(); 

	// Distinguish between array index of residues and their sequence "number"
	public Residue getResidue(int i) {return (Residue) residues.get(i);} // array index
    public Vector residues() {return residues;}

    // sequence number
    public Residue getResidueByNumber(int i) {return getResidueByNumber(new Integer(i).toString());}
    public Residue getResidueByNumber(int i, char insertionCode) {return getResidueByNumber(new Integer(i).toString() + insertionCode);}
    public Residue getResidueByNumber(String n) {return (Residue) residueNumbers.get(n);}

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
        for (Iterator i = residues.iterator(); i.hasNext(); ) {
            Residue residue = (Residue) i.next();
        // for (Residue residue : residues) {
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
            genericResidueBonds.put(atom1, new HashSet());
        ((HashSet)genericResidueBonds.get(atom1)).add(atom2);
    }
    
    protected void addGenericResidueBonds() {
    }
    
    /** 
     * Identify covalent bonds connecting residues.
     * 
     */
    protected void createResidueBonds() {
        Residue previousResidue = null;
        for (Iterator r1 = residues.iterator(); r1.hasNext(); ) {
            Residue residue = (Residue) r1.next();
            if (previousResidue != null) {
                for (Iterator s2 = genericResidueBonds.keySet().iterator(); s2.hasNext(); ) {
                    String firstAtomName = (String) s2.next();
                    Atom firstAtom = previousResidue.getAtom(firstAtomName);
                    if (firstAtom != null) {
                        for (Iterator s3 = ((HashSet)genericResidueBonds.get(firstAtomName)).iterator(); s3.hasNext(); ) {
                            String secondAtomName = (String) s3.next();
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
