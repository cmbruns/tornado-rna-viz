/*
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.*;

import org.simtk.atomicstructure.Atom;
import org.simtk.atomicstructure.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 */
public class Guanylate extends Purine {
    public Guanylate() {}
    public Guanylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "guanylate";}

    public char getOneLetterCode() {return 'G';}

    public Collection getHydrogenBondDonors() {
        HashSet answer = new HashSet();

        for (Iterator i = super.getHydrogenBondDonors().iterator(); i.hasNext(); ) {
            Atom a = (Atom) i.next();
            answer.add(a);
        }

        // Note: donor/acceptor status of N1,N3 depends upon exact base
        // So see those derived classes for additional atoms
        String donorAtomNames[] = {" N1 "};
        for (int i = 0; i < donorAtomNames.length; i++) {
            String atomName = donorAtomNames[i];
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
}
