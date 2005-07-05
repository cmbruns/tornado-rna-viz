/*
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.simtk.atomicstructure.Atom;
import org.simtk.atomicstructure.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 * 
 */
public class Uridylate extends Pyrimidine {
    public Uridylate() {}
    public Uridylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "uridylate";}

    public char getOneLetterCode() {return 'U';}

    public Collection getHydrogenBondAcceptors() {
        HashSet answer = new HashSet();

        for (Iterator i = super.getHydrogenBondAcceptors().iterator(); i.hasNext(); ) {
            Atom a = (Atom) i.next();
            answer.add(a);
        }

        String acceptorAtomNames[] = {" N3 "};
        for (int i = 0; i < acceptorAtomNames.length; i++) {
            String atomName = acceptorAtomNames[i];
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
}
