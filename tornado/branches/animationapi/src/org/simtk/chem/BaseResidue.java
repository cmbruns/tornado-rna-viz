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
 * Created on Apr 26, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import java.util.*;

public class BaseResidue extends BaseMolecular implements Residue {
    private Residue previousResidue;
    private Residue nextResidue;
    private String threeLetterCode = "UNK";
    private String residueName = "(Unknown name)";
    private int residueNumber = 0;
    private ResidueType residueType;
    
    // Atom name/altloc combo should be unique within a residue
    private Map<String, Collection<Atom> > atomNames = new Hashtable<String, Collection<Atom> >();
    private GenericBondCollection genericBonds = new GenericBondCollection();
    
    public static Residue createResidue(AtomCollection atoms) {
        Residue answer = null;

        BaseResidue baseResidue = new BaseResidue();
        baseResidue.initializeFromAtoms(atoms);
        answer = baseResidue;
        
        return answer;
    }
    
    public String getThreeLetterCode() {return threeLetterCode;}
    public Residue getPreviousResidue() {return previousResidue;}
    public Residue getNextResidue() {return nextResidue;}
    public String getResidueName() {return residueName;}
    public int getResidueNumber() {return residueNumber;}

    public Atom getAtom(String atomName) {
        if (!atomNames.containsKey(atomName)) return null;
        return (PDBAtom) ((Vector)atomNames.get(atomName)).firstElement();
    }    

    public Collection<GenericBond> genericBonds() {return genericBonds;}
    
    public ResidueType getResidueType() {return residueType;}
    protected void setResidueType(ResidueType type) {residueType = type;}
    
    protected void initializeFromAtoms(Collection<Atom> atoms) {
        super.initializeFromAtoms(atoms);

        setThreeLetterCode(guessThreeLetterCode(atoms));
        
        PDBAtom atom = getOnePDBAtom(atoms);
        if (atom != null) {
            setResidueNumber(atom.getResidueNumber());
            setResidueType(BaseResidueType.getType(atom));
        }
        
        indexAtoms();
    }
    
    protected static String guessThreeLetterCode(Collection<Atom> atoms) {

        String threeLetterCode = "UNK";
        
        PDBAtom atom = getOnePDBAtom(atoms);
        if (atom != null) {
            threeLetterCode = atom.getPDBResidueName();
        }
        
        return threeLetterCode;        
    }
    
    protected static PDBAtom getOnePDBAtom(Collection<Atom> atoms) {
        PDBAtom answer = null;
        for (Atom atom : atoms) {
            if (atom instanceof PDBAtom) {
                answer = (PDBAtom) atom;
            }
        } 
        return answer;
    }
    
    protected void setThreeLetterCode(String threeLetterCode) {
        this.threeLetterCode = threeLetterCode;
    }
    protected void setPreviousResidue(Residue residue) {
        this.previousResidue = residue;
    }
    protected void setNextResidue(Residue residue) {
        this.nextResidue = residue;
    }
    protected void setResidueName(String name) {
        this.residueName = name;
    }
    protected void setResidueNumber(int number) {
        this.residueNumber = number;
    }

    /** 
     * Identify covalent bonds in the residue.
     * 
     * This must be performed after atoms have been loaded into the residue.
     * Only do this once for each residue.
     */
    void createGenericBonds() {
        ATOM1: for (Atom atom1 : atoms()) {
            String atomName1 = atom1.getAtomName();
            NAME2: for (String atomName2 : genericBonds.getPartners(atomName1)) {            
                if (! atomNames.containsKey(atomName2)) continue NAME2;
                ATOM2: for (Atom atom2 : atomNames.get(atomName2)) {

                    // PDB atoms should not bond if they have separate altloc values
                    if ((atom1 instanceof PDBAtom) && (atom2 instanceof PDBAtom)) {
                        if ( ((PDBAtom)atom1).getAlternateLocationIndicator() != ((PDBAtom)atom2).getAlternateLocationIndicator() )
                            continue ATOM2;
                    }
                    
                    Bond bond = new CovalentBond(atom1, atom2);
                    atom1.bonds().add(bond);
                    atom2.bonds().add(bond);
                    bonds().add(bond);
                }
            }
        }
            
    }
    
    private void indexAtoms() {
        for (Atom atom : atoms()) {

            // Short atom name
            String atomName = atom.getAtomName();
            
            if (! atomNames.containsKey(atomName)) atomNames.put(atomName, new Vector<Atom>());
            atomNames.get(atomName).add(atom);
            
            if (atom instanceof PDBAtom) { // Include altloc field for PDB atoms
                PDBAtom pdbAtom = (PDBAtom) (atom);
    
                // Insertion code qualified atom name
                char altLoc = pdbAtom.getAlternateLocationIndicator();
                String fullAtomName = atomName + ":";
                if (altLoc != ' ') {fullAtomName = fullAtomName + altLoc;}
                
                if (! atomNames.containsKey(fullAtomName)) atomNames.put(fullAtomName, new Vector<Atom>());
                atomNames.get(fullAtomName).add(atom);
            }
        }
    }    

}
