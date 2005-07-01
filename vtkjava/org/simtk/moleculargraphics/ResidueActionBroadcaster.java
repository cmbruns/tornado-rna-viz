/*
 * Created on Jun 7, 2005
 *
 */
package org.simtk.moleculargraphics;

import org.simtk.util.*;
import org.simtk.molecularstructure.*;

public class ResidueActionBroadcaster extends SelectionBroadcaster {
    volatile boolean userIsInteracting = false;

    public void fireHighlight(Residue r) {
        for (SelectionListener l1 : listeners) {
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.highlight(r);
        }
    }

    public void fireUnHighlightResidue() {
        for (SelectionListener l1 : listeners) {
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.unHighlightResidue();
        }
    }
    
    public void fireAdd(Residue r) {
        for (SelectionListener l1 : listeners) {
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.add(r);
        }
    }
    
    public void fireCenterOn(Residue r) {
        for (SelectionListener l1 : listeners) {
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.centerOn(r);
        }
    }
    
    public void fireClearResidues() {
        for (SelectionListener l1 : listeners) {
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.clearResidues();
        }
    }
    
    synchronized public void lubricateUserInteraction() {
        userIsInteracting = true;
    }
    synchronized public void flushUserInteraction() {
        userIsInteracting = false;
    }
    synchronized public boolean userIsInteracting() {
        return userIsInteracting;
    }
}
