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
 * Created on Jul 7, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.util.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.Residue;

/** 
 *  
  * @author Christopher Bruns
  * 
  * A transparent blue cylinder around each duplex
 */
public class DuplexCylinderCartoon extends MoleculeCartoonClass 
{

    // vtkAssembly assembly = new vtkAssembly();
    
    public DuplexCylinderCartoon() {
    }

    public void updateCoordinates() {
        // TODO
    }

    public void addMolecule(Molecule molecule) {
        if (! (molecule instanceof NucleicAcid)) return;
        addNucleicAcid((NucleicAcid) molecule);
    }
    public void hide(Molecule molecule) {} // TODO
    public void hide() {} // TODO
    public void show(Molecule molecule) {} // TODO
    public void show() {} // TODO
    public void clear() {} // TODO

    public void addNucleicAcid(NucleicAcid nucleicAcid) {
//        Collection<Duplex> hairpins = nucleicAcid.identifyHairpins();
//        for (Iterator iterHairpin = hairpins.iterator(); iterHairpin.hasNext(); ) {
//            Duplex duplex = (Duplex) iterHairpin.next();
//            addDuplex(duplex);
//        }
        
        // Add duplexes (perhaps should restrict to source = rnaml only?
        for (SecondaryStructure structure : nucleicAcid.secondaryStructures()) {
            if (structure instanceof Duplex)  {
            	Duplex dup = (Duplex) structure;
            	List<BasePair> dupBPs = dup.basePairs();
            	BasePair firstBP = dupBPs.get(0);
            	BasePair lastBP = dupBPs.get(dupBPs.size()-1);
                
                // Only create duplexes for the molecule in which the
                // first residue appears.
            	Residue res5 = firstBP.getResidue1();
            	// if (res5.getPdbChainId().equals(nucleicAcid.getPdbChainId())){
                if (res5.equals(nucleicAcid.getResidueByNumber(res5.getResidueNumber()))){
                    
	                // System.out.println("Duplex found: "+dup.helixString());
	                try {addDuplex(dup);}
	                catch (InsufficientPointsException exc) {}
            	}
            }
        }
        

    }
    public void addDuplex(Duplex duplex) throws InsufficientPointsException {
        ActorCartoon actor = new DuplexCylinderActor(duplex);
        if (actor.isPopulated()) {
            subToons.add(actor);
            vtkActors().add(actor);
        }
    }
}
