/*
 * Created on Jun 7, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import org.simtk.molecularstructure.*;

public class SecondaryStructureCanvas extends BufferedCanvas 
implements ResidueSelector
{
    static final long serialVersionUID = 1L;
    
    public SecondaryStructureCanvas() {
        setBackground(Color.white);
    }
    
    public void addResidue(Residue residue) {}
    public void clearResidues() {}
    public void highlight(Residue residue) {}
    public void unHighlight() {}
    public void select(Residue residue) {}
    public void unSelect(Residue residue) {}
    public void centerOnResidue(Residue residue) {}
    
    public void paint(Graphics g) {
        super.paint(g);
    }
}
