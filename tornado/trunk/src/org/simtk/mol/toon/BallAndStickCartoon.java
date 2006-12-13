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
 * Created on Apr 28, 2005
 *
 */
package org.simtk.mol.toon;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

/** 
 * @author Christopher Bruns
 * 
 * Small spheres on each atom with cylinders connecting bonded atoms
 */
public class BallAndStickCartoon extends CompositeCartoon {

    BondStickCartoon sticks = new BondStickCartoon(0.15);
    AtomSphereActor balls = new AtomSphereActor(0.25);
    // Lone metal atoms should be large
    AtomSphereActor largeBalls = new AtomSphereActor(0.80);

    public BallAndStickCartoon() {
        // Make spheres be caps for sticks
        // balls.setScale(0.15);
        // balls.scaleByAtom = false;
        
        addSubToon(sticks);
        addSubToon(balls);
        addSubToon(largeBalls);
    }
    
    public void addMolecule(Molecule m) {
        sticks.addMolecule(m);

        // Make lone atoms other than oxygen large
        for (Atom atom : m.atoms()) {
            if ( (atom.bonds().size() == 0) && (! atom.getElementSymbol().equals("O")) )
                largeBalls.addAtom(atom);
            else 
                balls.addAtom(atom);
        }
    }
}