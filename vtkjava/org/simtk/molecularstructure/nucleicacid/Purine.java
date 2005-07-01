/*
 * Created on May 1, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.Collection;
import java.util.HashSet;

import org.simtk.atomicstructure.Atom;
import org.simtk.atomicstructure.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 * 
 */
public class Purine extends Nucleotide {
    public Purine() {}
    public Purine(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public char getOneLetterCode() {return 'R';}
    
    protected void addGenericBonds() {
        super.addGenericBonds();
        // Note - sugar-base linkage depends upon nucleotide type
        addGenericBond(" C1*", " N9 ");
        // addGenericBond(" C1*", " N1 ");
    }

    @Override
    public Collection<Atom> getHydrogenBondAcceptors() {
        HashSet<Atom> answer = new HashSet<Atom>();
        for (Atom a : super.getHydrogenBondAcceptors())
            answer.add(a);

        String acceptorAtomNames[] = {" N3 "};
        for (String atomName : acceptorAtomNames) {
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
    
}
