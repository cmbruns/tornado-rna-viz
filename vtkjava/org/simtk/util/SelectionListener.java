/*
 * Created on Jun 30, 2005
 *
 */
package org.simtk.util;

/**
 *  
  * @author Christopher Bruns
  * 
  * Object with an interest in user interface objects that are selected via a SelectionBroadcaster
 */
public interface SelectionListener {
    abstract void select(Selectable s);
    abstract void unSelect();
    abstract void unSelect(Selectable s);
}
