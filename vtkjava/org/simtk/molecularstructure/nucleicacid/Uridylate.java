/*
 * Created on Apr 22, 2005
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
public class Uridylate extends Pyrimidine {
    public Uridylate() {}
    public Uridylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "uridylate";}

    public char getOneLetterCode() {return 'U';}

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
