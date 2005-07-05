/*
 * Created on Jun 30, 2005
 *
 */
package org.simtk.util;

import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * When an item is selected in the user interface, objects can register
  * to learn about the seletion through this object.
 */
public class SelectionBroadcaster {
    protected HashSet listeners = new HashSet();

    public void addSelectionListener(SelectionListener l) {listeners.add(l);}

    public void fireSelect(Selectable s) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l = (SelectionListener) i.next();
            l.select(s);
        }
    }
    public void fireUnSelect() {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l = (SelectionListener) i.next();
            l.unSelect();
        }
    }
    public void fireUnSelect(Selectable s) {
        for (Iterator i = listeners.iterator(); i.hasNext(); ) {
            SelectionListener l = (SelectionListener) i.next();
            l.unSelect(s);
        }
    }
}
