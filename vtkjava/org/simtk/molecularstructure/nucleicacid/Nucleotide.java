/*
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.*;
import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;

/** 
 * @author Christopher Bruns
 * 
 * \brief One nucleotide base in a DNA or RNA molecule
 *
 */
public class Nucleotide extends Residue {

    
    static String[] baseGroupAtomNames = {
        " N1 ", " C2 ", " N3 ", " C4 ", " C5 ", " C6 ", " N7 ", " C8 ", " N9 ", // inner rings
        " O2 ", " N2 ", " O4 ", " N4 ", " O6 ", " N6 "}; // side atoms
    static String[] sugarGroupAtomNames = {" C1*", " C2*", " O2*", " C3*", " O3*", " C4*", " O4*", " C5*", " O5*"};
    static String[] phosphateGroupAtomNames = {" P  ", " OP1", " OP2", " O5*"};    

    static public FunctionalGroup baseGroup = new FunctionalGroup(baseGroupAtomNames);
    static public FunctionalGroup sugarGroup = new FunctionalGroup(sugarGroupAtomNames);
    static public FunctionalGroup phosphateGroup = new FunctionalGroup(phosphateGroupAtomNames);
    
    @Override
    public Collection<Atom> getHydrogenBondDonors() {
        HashSet<Atom> answer = new HashSet<Atom>();
        for (Atom a : super.getHydrogenBondDonors())
            answer.add(a);

        // Note: donor/acceptor status of N1,N3 depends upon exact base
        // So see those derived classes for additional atoms
        String donorAtomNames[] = {" O2*", " N2 ", " N4 ", " N6 ", };
        for (String atomName : donorAtomNames) {
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
    @Override
    public Collection<Atom> getHydrogenBondAcceptors() {
        HashSet<Atom> answer = new HashSet<Atom>();
        for (Atom a : super.getHydrogenBondAcceptors())
            answer.add(a);

        String acceptorAtomNames[] = {" O2*", " N7 ", " O2 ", " O4 ", " O6 "};
        for (String atomName : acceptorAtomNames) {
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
    
    @Override
    public BaseVector3D getBackbonePosition() {
        Atom atom = getAtom(" C5*");
        if (atom == null) return null;
        return atom.getCoordinates();
    }

    @Override
    public Vector3D getSideChainPosition() {
        return get(baseGroup).getCenterOfMass();
    }
    
    public String getResidueName() {return "(unknown nucleotide type)";}

    protected void addGenericBonds() {
        super.addGenericBonds();
        // Ribose carbon carbon bonds
        addGenericBond(" C1*", " C2*");
        addGenericBond(" C2*", " C3*");
        addGenericBond(" C3*", " C4*");
        addGenericBond(" C4*", " C5*");
        // Ribose carbon oxygen bonds
        addGenericBond(" C1*", " O4*");
        addGenericBond(" C2*", " O2*");
        addGenericBond(" C3*", " O3*");
        addGenericBond(" C4*", " O4*");
        addGenericBond(" C5*", " O5*");
        
        // 5' phosphate
        addGenericBond(" O5*", " P  ");
        addGenericBond(" O1P", " P  ");
        addGenericBond(" O2P", " P  ");

        // Base ring(s)
        addGenericBond(" N1 ", " C2 ");
        addGenericBond(" C2 ", " N3 ");
        addGenericBond(" N3 ", " C4 ");
        addGenericBond(" C4 ", " C5 ");
        addGenericBond(" C5 ", " C6 ");
        addGenericBond(" C6 ", " N1 ");
        addGenericBond(" C5 ", " N7 ");
        addGenericBond(" N7 ", " C8 ");
        addGenericBond(" C8 ", " N9 ");
        addGenericBond(" N9 ", " C4 ");

        // Note - sugar-base linkage depends upon nucleotide type
        // addGenericBond(" C1*", " N9 ");
        // addGenericBond(" C1*", " N1 ");

        // Base side groups
        addGenericBond(" C2 ", " O2 ");
        addGenericBond(" C2 ", " N2 ");
        addGenericBond(" C4 ", " N4 ");
        addGenericBond(" C4 ", " O4 ");
        addGenericBond(" C6 ", " N6 ");
        addGenericBond(" C6 ", " O6 ");
    }
    
    public Nucleotide() {}
    public Nucleotide(PDBAtomSet bagOfAtoms) {
        super(bagOfAtoms); 
    }
    
    public char getOneLetterCode() {return 'N';}

    static public Nucleotide createFactoryNucleotide(PDBAtomSet bagOfAtoms) {
        if (bagOfAtoms == null) return null;
        if (bagOfAtoms.size() == 0) return null;

        // Distinguish among subclasses
        PDBAtom atom = (PDBAtom) bagOfAtoms.get(0);
        String residueName = atom.getResidueName().trim().toUpperCase();

        if (residueName.equals("A")) return new Adenylate(bagOfAtoms);
        if (residueName.equals("C")) return new Cytidylate(bagOfAtoms);
        if (residueName.equals("G")) return new Guanylate(bagOfAtoms);
        if (residueName.equals("I")) return new Inositate(bagOfAtoms);
        if (residueName.equals("T")) return new Thymidylate(bagOfAtoms);
        if (residueName.equals("U")) return new Uridylate(bagOfAtoms);

        // Modified version of nucleotides
        if (residueName.equals("+A")) return new Adenylate(bagOfAtoms);
        if (residueName.equals("+C")) return new Cytidylate(bagOfAtoms);
        if (residueName.equals("+G")) return new Guanylate(bagOfAtoms);
        if (residueName.equals("+I")) return new Inositate(bagOfAtoms);
        if (residueName.equals("+T")) return new Thymidylate(bagOfAtoms);
        if (residueName.equals("+U")) return new Uridylate(bagOfAtoms);

        return new Nucleotide(bagOfAtoms); // default
    }
}
