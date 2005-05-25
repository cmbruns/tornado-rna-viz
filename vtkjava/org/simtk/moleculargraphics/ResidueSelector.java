/*
 * Created on May 6, 2005
 *
 */
package org.simtk.moleculargraphics;

import org.simtk.molecularstructure.Residue;

/**
 *  
  * @author Christopher Bruns
  * 
  * Methods by which grapical representations of biopolymer sequences (e.g. DNA, protein) can
  * display sets of selected residues, and individual residues with the current focus.
 */
public interface ResidueSelector {

    
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
    public void unHighlight();

    
    /**
     * Color or otherwise indicate that the selected residue has been added to the selection list.
     * The suggested color is light blue.
     * @param r
     */
    public void select(Residue r);

    
    /**
     * Remove coloring or other indication that the residue is in the selection list.
     * @param r
     */
    public void unSelect(Residue r);
    
    public void addResidue(Residue r);
    public void clearResidues();
    public void centerOnResidue(Residue r);
}
