/* Portions copyright (c) 2007 Stanford University and Christopher Bruns
 * Contributors:
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on May 1, 2007
 * Original author: Christopher Bruns
 */
package org.simtk.tornado;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.simtk.molecularstructure.*;

public class CenterOnMoleculeDialog extends JDialog {
    boolean dialogHasBeenDrawnAtLeastOnce = false;
    JFrame parent;
    JPanel moleculePanel = new JPanel();
    
    public CenterOnMoleculeDialog(JFrame parent) {
        super(parent, "SimTK Tornado: Center on molecule");
        this.parent = parent;
        setModal(false);
        
        moleculePanel.setLayout( new BoxLayout(moleculePanel, BoxLayout.Y_AXIS) );
        // TODO
        
        JScrollPane scrollPane = new JScrollPane(moleculePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().setPreferredSize( new Dimension(200, 200) );
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new BoxLayout(buttonPanel, BoxLayout.X_AXIS) );
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add( new JButton(new CloseDialogAction()) );
        
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }
    
    @Override
    public void setVisible(boolean b) {
        if (b) {
            if (! dialogHasBeenDrawnAtLeastOnce) {
                dialogHasBeenDrawnAtLeastOnce = true;
                setLocationRelativeTo(parent);
            }
        }
        super.setVisible(b);
    }
    
    public void updateMoleculeList(MoleculeCollection molecules) {
        moleculePanel.removeAll();
        
        int numberOfMolecules = 0;
        for (Molecule molecule : molecules) {
            moleculePanel.add( new SingleMoleculePanel(molecule) );
            ++numberOfMolecules;
        }
        
        if (numberOfMolecules == 0) {
            moleculePanel.add( new JLabel("(no molecules found)") );
        }
    }

    class SingleMoleculePanel extends JPanel {
        Molecule molecule;
        
        SingleMoleculePanel(Molecule mol) {
            this.molecule = mol;
            setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );
            add(new JButton("Test"));
        }        
    }
    
    class CloseDialogAction extends AbstractAction {
        CloseDialogAction() {
            super("Close");
        }
        
        public void actionPerformed(ActionEvent event) {
            setVisible(false);
        }
    }
}
