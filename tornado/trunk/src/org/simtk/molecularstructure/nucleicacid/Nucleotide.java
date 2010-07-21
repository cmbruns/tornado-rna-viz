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

import java.util.HashSet;
import java.util.Set;

import org.simtk.molecularstructure.FunctionalGroup;
import org.simtk.molecularstructure.ResidueTypeClass;

/** 
 * @author Christopher Bruns
 * 
 * \brief One nucleotide base in a DNA or RNA molecule
 *
 */
public class Nucleotide extends ResidueTypeClass {
    static String[] baseGroupAtomNames = {
        " N1 ", " C2 ", " N3 ", " C4 ", " C5 ", " C6 ", " N7 ", " C8 ", " N9 ", // inner rings
        " O2 ", " N2 ", " O4 ", " N4 ", " O6 ", " N6 "}; // side atoms
    static String[] sugarGroupAtomNames = {" C1*", " C2*", " O2*", " C3*", " O3*", " C4*", " O4*", " C5*", " O5*"};
    static String[] phosphateGroupAtomNames = {" P  ", " OP1", " OP2", " O5*"};    

    static public FunctionalGroup baseGroup = new FunctionalGroup(baseGroupAtomNames);
    static public FunctionalGroup sugarGroup = new FunctionalGroup(sugarGroupAtomNames);
    static public FunctionalGroup phosphateGroup = new FunctionalGroup(phosphateGroupAtomNames);

    @Override
    public Set<String> getHydrogenBondDonorAtomNames() {
        Set<String> answer = new HashSet<String>();
        answer.addAll(super.getHydrogenBondDonorAtomNames());
        String[] donors = {" O2*", " N2 ", " N4 ", " N6 ", };
        for (String atomName : donors) answer.add(atomName);
        return answer;
    }

    @Override
    public Set<String> getHydrogenBondAcceptorAtomNames() {
        Set<String> answer = new HashSet<String>();
        answer.addAll(super.getHydrogenBondAcceptorAtomNames());
        String[] acceptors = {" O2*", " N2 ", " N4 ", " N6 ", };
        for (String atomName : acceptors) answer.add(atomName);
        return answer;        
    }

    @Override
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
    
    public Nucleotide(char olc, String tlc, String name) {
        super(olc, tlc, name);
    }
    
    // public Nucleotide() {
    //     super('N', "UNK", "(unknown nucleotide)");
    // }
}