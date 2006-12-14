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
 * Created on Jun 7, 2005
 *
 */
package org.simtk.moleculargraphics;

import org.simtk.molecularstructure.Residue;
import java.util.*;
import java.awt.Color;

public class ResidueHighlightBroadcaster {
    protected Set<ResidueHighlightListener> listeners = 
        new LinkedHashSet<ResidueHighlightListener>();
    // protected Color color = new Color(100, 120, 255);
    protected Color color = new Color(100, 255, 100);

    public void addResidueHighlightListener(ResidueHighlightListener l) {
        listeners.add(l);
    }
    
    public void removeResidueHighlightListener(ResidueHighlightListener l) {
        listeners.remove(l);
    }
    
    public void fireHighlight(Residue r) {
        for (ResidueHighlightListener listener : listeners)
            listener.highlightResidue(r, color);
    }

    public void fireUnhighlightResidue(Residue r) {
        for (ResidueHighlightListener listener : listeners)
            listener.unhighlightResidue(r);
    }
    
    public void fireUnhighlightResidues() {
        for (ResidueHighlightListener listener : listeners)
            listener.unhighlightResidues();
    }    

    public void setHighlightColor(Color color) {
        this.color = color;
    }

    public Color getHighlightColor() {return color;}
}
