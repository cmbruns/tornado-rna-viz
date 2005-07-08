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
 * Created on Apr 22, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.awt.Color;
import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

/** 
 * @author Christopher Bruns
 * 
 * \brief One nucleotide base in a DNA or RNA molecule
 *
 */
public class Nucleotide extends Residue {

    static private Color defaultColor = new Color(255,150,255); // magenta for unknown nucleotide
    public Color getDefaultColor() {return defaultColor;}
        
    static String[] baseGroupAtomNames = {
        " N1 ", " C2 ", " N3 ", " C4 ", " C5 ", " C6 ", " N7 ", " C8 ", " N9 ", // inner rings
        " O2 ", " N2 ", " O4 ", " N4 ", " O6 ", " N6 "}; // side atoms
    static String[] sugarGroupAtomNames = {" C1*", " C2*", " O2*", " C3*", " O3*", " C4*", " O4*", " C5*", " O5*"};
    static String[] phosphateGroupAtomNames = {" P  ", " OP1", " OP2", " O5*"};    

    static public FunctionalGroup baseGroup = new FunctionalGroup(baseGroupAtomNames);
    static public FunctionalGroup sugarGroup = new FunctionalGroup(sugarGroupAtomNames);
    static public FunctionalGroup phosphateGroup = new FunctionalGroup(phosphateGroupAtomNames);
    
    public Collection getHydrogenBondDonors() {
        HashSet answer = new HashSet();

        for (Iterator i = super.getHydrogenBondDonors().iterator(); i.hasNext(); ) {
            Atom a = (Atom) i.next();
            answer.add(a);
        }

        // Note: donor/acceptor status of N1,N3 depends upon exact base
        // So see those derived classes for additional atoms
        String donorAtomNames[] = {" O2*", " N2 ", " N4 ", " N6 ", };
        for (int i = 0; i < donorAtomNames.length; i++) {
            String atomName = donorAtomNames[i];
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
    public Collection getHydrogenBondAcceptors() {
        HashSet answer = new HashSet();

        for (Iterator i = super.getHydrogenBondAcceptors().iterator(); i.hasNext(); ) {
            Atom a = (Atom) i.next();
            answer.add(a);
        }

        String acceptorAtomNames[] = {" O2*", " N7 ", " O2 ", " O4 ", " O6 "};
        for (int i = 0; i < acceptorAtomNames.length; i++) {
            String atomName = acceptorAtomNames[i];
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
    
    public BaseVector3D getBackbonePosition() {
        Atom atom = getAtom(" C5*");
        if (atom == null) return null;
        return atom.getCoordinates();
    }

    public BaseVector3D getSideChainPosition() {
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
