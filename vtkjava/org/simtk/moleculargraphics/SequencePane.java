/*
 * Created on Apr 27, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.simtk.molecularstructure.*;


/** 
 * @author Christopher Bruns
 * 
 * Window area for displaying the molecular sequence of residues in Tornado application.
 */
public class SequencePane extends JScrollPane 
implements ResidueActionListener
{
    public static final long serialVersionUID = 1L;
    
    // public boolean userIsInteracting = false;

    // Biopolymer molecule;
    Residue currentHighlightedResidue;
    Residue firstResidue;
    Residue finalResidue;
    
    // SequenceTextPane textPane;
    SequenceCanvas sequenceCanvas;
    // NumberPane numberPane;
    Color backgroundColor = Color.white;
    // Tornado tornado;
    ResidueActionBroadcaster residueActionBroadcaster;
    Panel contentPanel;
    
    SequencePane(ResidueActionBroadcaster b) {
        residueActionBroadcaster = b;
        // tornado = parent;
        
        // Only scroll horizontally
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        getViewport().setBackground(backgroundColor); // for when text panel does not fill viewport
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        sequenceCanvas = new SequenceCanvas("This is a test", this, residueActionBroadcaster);

        contentPanel = new Panel();
        contentPanel.setBackground(Color.white);
        contentPanel.setLayout(new BorderLayout());
                
        contentPanel.add(sequenceCanvas, BorderLayout.SOUTH);

        setViewportView(contentPanel);
        checkSize();

        // Respond to space key
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                "nextResidue");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                "nextResidue");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0),
                "nextResidue");
        getActionMap().put("nextResidue", new NextResidueAction());


        // Respond to backspace key
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                "previousResidue");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                "previousResidue");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0),
                "previousResidue");
        getActionMap().put("previousResidue", new PreviousResidueAction());
       }

    public void paint(Graphics g) {
        checkSize();
        super.paint(g);
    }

    /**
     * Set the minimum size to show the entire height
     *
     */
    public void checkSize() {
        Dimension d = getSize();
        Dimension p = getPreferredSize();
        Dimension m = getMinimumSize();
        if (d.height != p.height) {
            setSize(d.width, p.height);
            setMinimumSize(new Dimension(m.width, p.height));
        }
    }
    
    public SequenceCanvas getSequenceCanvas() {return sequenceCanvas;}

    public void clearResidues() {
        sequenceCanvas.clearResidues();

        firstResidue = null;
        finalResidue = null;
        currentHighlightedResidue = null;
    }
    
    public void add(Residue r) {
        sequenceCanvas.add(r);

        finalResidue = r;
        if (firstResidue == null) firstResidue = r;
    }
    
    public void highlight(Residue r) {
        sequenceCanvas.highlight(r);
        currentHighlightedResidue = r;
    }
    public void unHighlightResidue() {
        sequenceCanvas.unHighlightResidue();
        currentHighlightedResidue = null;
    }
    public void select(Residue r) {
        sequenceCanvas.select(r);
    }
    public void unSelect(Residue r) {
        sequenceCanvas.unSelect(r);
    }
    public void centerOn(Residue r) {
        sequenceCanvas.centerOn(r);
    }

    /**
     *  
      * @author Christopher Bruns
      * 
      * Move highlight to the next residue when space bar is pressed
     */
    class NextResidueAction extends AbstractAction {
        public static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
            highlightNextResidue();
        }
    }
    class PreviousResidueAction extends AbstractAction {
        public static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
            highlightPreviousResidue();
        }
    }

    void highlightNextResidue() {
        Residue nextResidue = null;
        if (currentHighlightedResidue == null) // Go to first residue
            nextResidue = firstResidue;
        else 
            nextResidue = currentHighlightedResidue.getNextResidue();

        if (nextResidue == null) residueActionBroadcaster.fireUnHighlightResidue();
        else residueActionBroadcaster.fireHighlight(nextResidue);
    }

    void highlightPreviousResidue() {
        Residue previousResidue = null;
        if (currentHighlightedResidue == null) 
            previousResidue = finalResidue;
        else 
            previousResidue = currentHighlightedResidue.getPreviousResidue();

        if (previousResidue == null) residueActionBroadcaster.fireUnHighlightResidue();
        else residueActionBroadcaster.fireHighlight(previousResidue);
    }
}
