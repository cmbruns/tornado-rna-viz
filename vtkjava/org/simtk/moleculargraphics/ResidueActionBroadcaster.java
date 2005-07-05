/*
 * Created on Jun 7, 2005
 *
 */
package org.simtk.moleculargraphics;

import org.simtk.util.*;
import org.simtk.molecularstructure.*;
import java.util.*;

public class ResidueActionBroadcaster extends SelectionBroadcaster {
    volatile boolean userIsInteracting = false;

    public void fireHighlight(Residue r) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l1 = (SelectionListener) i.next();
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.highlight(r);
        }
    }

    public void fireUnHighlightResidue() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l1 = (SelectionListener) i.next();
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.unHighlightResidue();
        }
    }
    
    public void fireAdd(Residue r) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l1 = (SelectionListener) i.next();
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.add(r);
        }
    }
    
    public void fireCenterOn(Residue r) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l1 = (SelectionListener) i.next();
            if (! (l1 instanceof ResidueActionListener)) continue;
            ResidueActionListener l = (ResidueActionListener) l1;
            l.centerOn(r);
        }
    }
    
    public void fireClearResidues() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l1 = (SelectionListener) i.next();
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
