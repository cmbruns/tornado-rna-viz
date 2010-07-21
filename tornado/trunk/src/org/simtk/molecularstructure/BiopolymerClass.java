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
public class BiopolymerClass 
extends MoleculeClass 
implements Biopolymer 
{
    // maps atom names of bondable atoms that bond one residue to the next
    private Map<String, Set<String>> genericResidueBonds = new HashMap<String, Set<String>>(); 
    private Set<SecondaryStructure> secondaryStructure = new HashSet<SecondaryStructure>();
    private NumberedResidues residues = new NumberedResidues();
    
    public List<Residue> residues() {return residues;}
    
	// Distinguish between array index of residues and their sequence "number"
	public Residue getResidue(int i) {return residues.get(i);} // array index

    // sequence number
    public Residue getResidueByNumber(int i) {return getResidueByNumber(new Integer(i).toString());}
    public Residue getResidueByNumber(int i, char insertionCode) {return getResidueByNumber(new Integer(i).toString() + insertionCode);}
    public Residue getResidueByNumber(String n) {return residues.getResidueByNumber(n);}

    // public int getResidueCount() {return residues.size();}
	
	public BiopolymerClass(char chainId) {
        super(chainId);
        addGenericResidueBonds();
    } // Empty molecule

//    public BiopolymerClass(PDBAtomSet atomSet) {
//        super(atomSet); // fills atoms array
//
//        // Parse into residues
//        // Each residue should have a unique index/insertionCode combination
//        // TODO But what if the residue name (pathologically) changes within an index/insertionCode?
//        String previousResidueKey = "Hey, this isn't a reasonable residue key!!!";
//        PDBAtomSet newResidueAtoms = new PDBAtomSet();
//        for (int a = 0; a < atomSet.size(); a++) {
//            Atom atom = (Atom) atomSet.get(a);
//            String residueKey = "" + atom.getResidueNumber() + atom.getInsertionCode();
//            if (!residueKey.equals(previousResidueKey)) { // Start a new residue, flush the old one
//                if (newResidueAtoms.size() > 0) {
//                    Residue residue = ResidueClass.createFactoryResidue(newResidueAtoms);
//                    residues.add(residue);
//                }
//                newResidueAtoms = new PDBAtomSet();
//            }
//            newResidueAtoms.addElement(atom);
//            
//            previousResidueKey = residueKey;
//        }
//        // Flush final set of atoms
//        if (newResidueAtoms.size() > 0) {
//            Residue residue = ResidueClass.createFactoryResidue(newResidueAtoms);
//            residues.add(residue);
//        }
//
//        addGenericResidueBonds();
//        createResidueBonds();
//        
//        // Connect residues in a doubly linked list
//        Residue previousResidue = null;
//        for (Iterator i = residues.iterator(); i.hasNext(); ) {
//            Residue residue = (Residue) i.next();
//        // for (Residue residue : residues) {
//            if (previousResidue != null) {
//                residue.setPreviousResidue(previousResidue);
//                previousResidue.setNextResidue(residue);
//            }
//            previousResidue = residue;
//        }
//    }

    public void addSecondaryStructure(SecondaryStructure ss) {
        this.secondaryStructure.add(ss);
    }
    
    // public Iterator secondaryStructures().iterator() {return this.secondaryStructure.iterator();}

    public Set<SecondaryStructure> secondaryStructures() {return this.secondaryStructure;}
    public Set<SecondaryStructure> displayableStructures() {
        Set<SecondaryStructure> answer = new LinkedHashSet<SecondaryStructure>();
    	for (SecondaryStructure structure: this.secondaryStructure){
    		if ((this.displaySourceTypes==null)||(this.displaySourceTypes.contains(structure.getSource()))){
    			answer.add(structure);
    		}
    	}
    	return answer;
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
     *  
      * @author Christopher Bruns
      * 
      * Container that maintains a mapping of residue number to residue
     */
    class NumberedResidues extends Vector<Residue> {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Map<String, Residue> residueNumbers = new HashMap<String, Residue>();
        
        public Residue getResidueByNumber(String n) {return residueNumbers.get(n);}
        
        public boolean add(Residue residue) {
            Residue prev = null;
            if (size() > 0) prev = get(size() - 1);
            boolean answer = super.add(residue);
            if (answer) {
                if (prev != null) linkResidues(prev, residue);
                addNumberResidue(residue);
            }
            return answer;
        }
        
        public boolean addAll(Collection<? extends Residue> c) {
            Residue prev = null;
            if (size() > 0) prev = get(size() - 1);
            boolean answer = super.addAll(c);
            if (answer) {
                for (Residue residue : c) {
                    if (prev != null) linkResidues(prev, residue);
                    addNumberResidue(residue);
                    prev = residue;
                }
            }
            return answer;
        }
        
        public void clear() {
            super.clear();
            residueNumbers.clear();
        }
        
        public boolean remove(Object residue) {
            boolean answer = super.remove(residue);
            if (answer) {
                if (residue instanceof Residue) removeNumberResidue((Residue)residue);
            }
            return answer;
        }
        
        public boolean removeAll(Collection<?> c) {
            boolean answer = super.removeAll(c);
            if (answer) {
                for (Object residue : c)
                    if (residue instanceof Residue) removeNumberResidue((Residue)residue);
            }
            return answer;
        }        
        
        public boolean retainAll(Collection<?> c) {
            boolean answer = super.removeAll(c);
            if (answer) {
                residueNumbers.clear();
                for (Residue residue : this)
                    addNumberResidue(residue);
            }
            return answer;
        }        
        
        private void linkResidues(Residue res1, Residue res2) {
            res2.setPreviousResidue(res1);
            res1.setNextResidue(res2);                        

            for (String firstAtomName : genericResidueBonds.keySet()) {
                Atom firstAtom = res1.getAtom(firstAtomName);
                if (firstAtom == null) continue;
                for (String secondAtomName : genericResidueBonds.get(firstAtomName)) {
                    Atom secondAtom =  res2.getAtom(secondAtomName);
                    if (secondAtom == null) continue;

                    // Check distance
                    double d = firstAtom.distance(secondAtom);
                    if (d > 4.0) continue;
                    
                    firstAtom.bonds().add(secondAtom);
                    secondAtom.bonds().add(firstAtom);
                }
            }
        }
        
        private void addNumberResidue(Residue residue) {
            // TODO - set next and previous residue pointers
            
            String numberString = "" + residue.getResidueNumber();
            String fullString = numberString + residue.getPdbInsertionCode();
            residueNumbers.put(fullString, residue); // number plus insertion code
            // Only the first residue with a particular number gets to be invoked by that number alone
            if (!residueNumbers.containsKey(numberString)) 
                residueNumbers.put(numberString, residue); // number 
        }
        
        private void removeNumberResidue(Residue residue) {
            String numberString = "" + residue.getResidueNumber();
            String fullString = numberString + residue.getPdbInsertionCode();
            
            String[] numbers = {numberString, fullString};
            for (String number : numbers) {
                if (residue == residueNumbers.get(number))
                    residueNumbers.remove(number);
            }
        }

    }
}
