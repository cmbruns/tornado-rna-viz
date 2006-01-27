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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.simtk.molecularstructure.atom.LocatedMoleculeAtom;
import org.simtk.molecularstructure.atom.PDBAtomSet;

/** 
 * @author Christopher Bruns
 * 
 * 
 */
public class Uridylate extends Pyrimidine {
    public Uridylate() {}
    public Uridylate(PDBAtomSet bagOfAtoms) {super(bagOfAtoms);}
    public String getResidueName() {return "uridylate";}

    static private Color defaultColor = new Color(255,90,130); // darker red
    public Color getDefaultColor() {return defaultColor;}


    public char getOneLetterCode() {return 'U';}

    public Collection getHydrogenBondAcceptors() {
        HashSet answer = new HashSet();

        for (Iterator i = super.getHydrogenBondAcceptors().iterator(); i.hasNext(); ) {
            LocatedMoleculeAtom a = (LocatedMoleculeAtom) i.next();
            answer.add(a);
        }

        String acceptorAtomNames[] = {" N3 "};
        for (int i = 0; i < acceptorAtomNames.length; i++) {
            String atomName = acceptorAtomNames[i];
            LocatedMoleculeAtom a = getAtom(atomName);
            if (a != null) answer.add(a);
        }
        
        return answer;
    }
}
