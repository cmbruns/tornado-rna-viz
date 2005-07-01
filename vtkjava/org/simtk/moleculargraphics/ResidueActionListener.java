/*
 * Created on May 6, 2005
 *
 */
package org.simtk.moleculargraphics;

import org.simtk.molecularstructure.Residue;
import org.simtk.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Methods by which grapical representations of biopolymer sequences (e.g. DNA, protein) can
  * display sets of selected residues, and individual residues with the current focus.
 */
public interface ResidueActionListener extends SelectionListener {

    
    /**
     * Color one Residue yellow, or otherwise indicate that the residue has the current focus.
     * At most one residue at a time should be highlighted.  The suggested color is yellow.
     * @param r
     */
    public void highlight(Residue r);

    
    /**
     * Turn off the highlight of the highlighted residue, if any
     *
     */
    public void unHighlightResidue();

    
    public void add(Residue r);
    public void clearResidues();
    public void centerOn(Residue r);
}
