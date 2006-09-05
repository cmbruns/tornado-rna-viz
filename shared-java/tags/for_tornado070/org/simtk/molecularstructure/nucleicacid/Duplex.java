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
 * Created on May 3, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import org.simtk.molecularstructure.*;
import java.util.*;

/** 
 *  
  * @author Christopher Bruns
  * 
  * Double helix of RNA or DNA
 */
public class Duplex extends SecondaryStructureClass {
    static final long serialVersionUID = 1L;
    // private HashSet residues = new HashSet();
    private List<BasePair> basePairs = new Vector<BasePair>();
    
    // TODO these should be const
    // public Collection residues() {return residues;}
    public List<BasePair> basePairs() {return basePairs;}
    
    public void addBasePair(BasePair bp) {
        basePairs.add(bp);
        this.addResidue(bp.getResidue1());
        this.addResidue(bp.getResidue2());
    }
    
    public String helixString() {
    	Nucleotide res5 = ((BasePair) basePairs.get(0)).getResidue1();
       	Nucleotide res3 = ((BasePair) basePairs.get(basePairs.size()-1)).getResidue2();
       	String mol5 = res5.getChainID();
       	String mol3 = res3.getChainID();
       	return "(("+mol5+", "+res5+"), ("+mol3+", "+res3+"), "+basePairs.size()+")";
    }
}
