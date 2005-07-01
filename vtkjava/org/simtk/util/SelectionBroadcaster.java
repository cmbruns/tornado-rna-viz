/*
 * Created on Jun 30, 2005
 *
 */
package org.simtk.util;

import java.util.HashSet;

/**
 *  
  * @author Christopher Bruns
  * 
  * When an item is selected in the user interface, objects can register
  * to learn about the seletion through this object.
 */
public class SelectionBroadcaster {
    protected HashSet<SelectionListener> listeners = new HashSet<SelectionListener>();

    public void addSelectionListener(SelectionListener l) {listeners.add(l);}

    public void fireSelect(Selectable s) {for (SelectionListener l : listeners) l.select(s);}
    public void fireUnSelect() {for (SelectionListener l : listeners) l.unSelect();}
    public void fireUnSelect(Selectable s) {for (SelectionListener l : listeners) l.unSelect(s);}
}
