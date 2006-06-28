/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Jun 23, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import java.util.Iterator;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.atom.*;

public class OvalBasePairPlus extends CompositeCartoon {
    private BasePairOval ovalToon = new BasePairOval();
    private BallAndStickCartoon stickToon = new BallAndStickCartoon();
    private BackboneCurveCartoon backboneRibbon = new BackboneCurveCartoon();
    private BackboneStick backboneStick = new BackboneStick();
    private BasePairConnectorStick pairStick = new BasePairConnectorStick();
    
    public OvalBasePairPlus() {
        addSubToon(ovalToon);
        // addSubToon(wireToon);
        addSubToon(stickToon);
        addSubToon(backboneRibbon);
        addSubToon(backboneStick);
        addSubToon(pairStick);
    }

    public void add(LocatedMolecule m) {
        massBody.add(m);
        // wireToon.add(m);
        if (m instanceof NucleicAcid) {
            ovalToon.add(m);
            backboneRibbon.add(m);
            pairStick.add(m);
        }
        else if (m instanceof Biopolymer) {
            backboneStick.add(m);
        }
        else {
            // Skip solvent
        	Iterator<LocatedAtom> atomIt = m.getAtomIterator();
        	if (atomIt.hasNext()) {
        		LocatedAtom atom = atomIt.next();
        		if ((atom instanceof PDBAtom)&& PDBResidueClass.isSolvent(((PDBAtom)atom).getPDBResidueName())){
        			return;
        		}
        	}
            stickToon.add(m);
        }
    }
    
}
