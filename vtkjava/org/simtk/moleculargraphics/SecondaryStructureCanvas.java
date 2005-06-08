/*
 * Created on Jun 7, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import org.simtk.molecularstructure.*;

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
    public void select(Residue residue) {}
    public void unSelect(Residue residue) {}
    public void centerOn(Residue residue) {}
    
    public void paint(Graphics g) {
        super.paint(g);
    }
}
