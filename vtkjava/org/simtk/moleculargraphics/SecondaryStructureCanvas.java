/*
 * Created on Jun 7, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import org.simtk.molecularstructure.*;
import org.simtk.util.*;

public class SecondaryStructureCanvas extends BufferedCanvas 
implements ResidueActionListener
{
    static final long serialVersionUID = 1L;
    ResidueActionBroadcaster residueActionBroadcaster;
    
    public SecondaryStructureCanvas(ResidueActionBroadcaster b) {
        residueActionBroadcaster = b;
        setBackground(Color.white);
    }

    public void add(Residue residue) {}
    public void clearResidues() {}
    public void highlight(Residue residue) {}
    public void unHighlightResidue() {}
    public void select(Selectable s) {}
    public void unSelect(Selectable s) {}
    public void unSelect() {}
    public void centerOn(Residue residue) {}
    
    public void paint(Graphics g) {
        super.paint(g);
    }
}
