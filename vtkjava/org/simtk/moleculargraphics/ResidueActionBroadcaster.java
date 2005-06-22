/*
 * Created on Jun 7, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.util.*;
import org.simtk.molecularstructure.*;

public class ResidueActionBroadcaster {
    HashSet<ResidueActionListener> listeners = new HashSet<ResidueActionListener>();
    volatile boolean userIsInteracting = false;

    public void fireHighlight(Residue r) {
        for (ResidueActionListener l : listeners)
            l.highlight(r);
    }

    public void fireUnHighlightResidue() {
        for (ResidueActionListener l : listeners)
            l.unHighlightResidue();
    }
    
    public void fireAdd(Residue r) {
        for (ResidueActionListener l : listeners)
            l.add(r);
    }
    
    public void fireCenterOn(Residue r) {
        for (ResidueActionListener l : listeners)
            l.centerOn(r);
    }
    
    public void fireClearResidues() {
        for (ResidueActionListener l : listeners)
            l.clearResidues();
    }
    
    public void addListener(ResidueActionListener l) {
        listeners.add(l);
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
