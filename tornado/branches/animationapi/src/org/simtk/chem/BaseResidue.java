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

import org.simtk.chem.pdb.PdbAtom;

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
    
    public String getThreeLetterCode() {return threeLetterCode;}
    public Residue getPreviousResidue() {return previousResidue;}
    public Residue getNextResidue() {return nextResidue;}
    public String getResidueName() {return residueName;}
    public int getResidueNumber() {return residueNumber;}

    public Atom getAtomByName(String atomName) {
        if (!atomNames.containsKey(atomName)) return null;
        return (PdbAtom) ((Vector)atomNames.get(atomName)).firstElement();
    }    

    public Collection<GenericBond> genericBonds() {return genericBonds;}
    
    public ResidueType getResidueType() {return residueType;}
    protected void setResidueType(ResidueType type) {residueType = type;}
    
    protected static String guessThreeLetterCode(Collection<Atom> atoms) {

        String threeLetterCode = "UNK";
        
        PdbAtom atom = getOnePdbAtom(atoms);
        if (atom != null) {
            threeLetterCode = atom.getPdbResidueName();
        }
        
        return threeLetterCode;        
    }
    
    protected static PdbAtom getOnePdbAtom(Collection<Atom> atoms) {
        PdbAtom answer = null;
        for (Atom atom : atoms) {
            if (atom instanceof PdbAtom) {
                answer = (PdbAtom) atom;
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

                    // Pdb atoms should not bond if they have separate altloc values
                    if ((atom1 instanceof PdbAtom) && (atom2 instanceof PdbAtom)) {
                        if ( ((PdbAtom)atom1).getAlternateLocationIndicator() != ((PdbAtom)atom2).getAlternateLocationIndicator() )
                            continue ATOM2;
                    }
                    
                    Bond bond = new CovalentBond(atom1, atom2);
                    addBond(bond);
                }
            }
        }
            
    }
    
}
