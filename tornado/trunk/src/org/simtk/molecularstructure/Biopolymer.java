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
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure;

import java.util.*;

import org.simtk.molecularstructure.atom.*;

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
                    LocatedAtom firstAtom = previousResidue.getAtom(firstAtomName);
                    if (firstAtom != null) {
                        for (Iterator s3 = ((HashSet)genericResidueBonds.get(firstAtomName)).iterator(); s3.hasNext(); ) {
                            String secondAtomName = (String) s3.next();
                            LocatedAtom secondAtom = residue.getAtom(secondAtomName);
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
