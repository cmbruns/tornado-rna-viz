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
 * Created on May 1, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.simtk.molecularstructure.atom.Atom;
import org.simtk.molecularstructure.atom.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 * 
 */
public class Pyrimidine extends Nucleotide {
    public Pyrimidine() {}
    public Pyrimidine(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public char getOneLetterCode() {return 'Y';}
    
    protected void addGenericBonds() {
        super.addGenericBonds();
        // Note - sugar-base linkage depends upon nucleotide type
        // addGenericBond(" C1*", " N9 ");
        addGenericBond(" C1*", " N1 ");
    }

    public Collection getHydrogenBondAcceptors() {
        HashSet answer = new HashSet();

        for (Iterator i = super.getHydrogenBondAcceptors().iterator(); i.hasNext(); ) {
            Atom a = (Atom) i.next();
            answer.add(a);
        }

        String acceptorAtomNames[] = {" N1 "};
        for (int i = 0; i < acceptorAtomNames.length; i++) {
            String atomName = acceptorAtomNames[i];
            Atom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
}