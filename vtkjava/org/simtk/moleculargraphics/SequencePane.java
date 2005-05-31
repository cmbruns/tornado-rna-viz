/*
 * Created on Apr 27, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.simtk.molecularstructure.Residue;


/** 
 * @author Christopher Bruns
 * 
 * Window area for displaying the molecular sequence of residues in Tornado application.
 */
public class SequencePane extends JScrollPane 
implements ResidueSelector
{
    public static final long serialVersionUID = 1L;
    // SequenceTextPane textPane;
    SequenceCanvas sequenceCanvas;
    // NumberPane numberPane;
    Color backgroundColor = Color.white;
    Tornado tornado;
    Panel contentPanel;
    
    SequencePane(Tornado parent) {
        tornado = parent;
        
        // Only scroll horizontally
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        getViewport().setBackground(backgroundColor); // for when text panel does not fill viewport
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        sequenceCanvas = new SequenceCanvas("This is a test", this, tornado);

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
    }
    
    public void addResidue(Residue r) {
        sequenceCanvas.addResidue(r);
    }
    
    public void highlight(Residue r) {
        sequenceCanvas.highlight(r);
    }
    public void unHighlight() {
        sequenceCanvas.unHighlight();
    }
    public void select(Residue r) {
        sequenceCanvas.select(r);
    }
    public void unSelect(Residue r) {
        sequenceCanvas.unSelect(r);
    }
    public void centerOnResidue(Residue r) {
        sequenceCanvas.centerOnResidue(r);
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
            tornado.highlightNextResidue();
        }
    }
    class PreviousResidueAction extends AbstractAction {
        public static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
            tornado.highlightPreviousResidue();
        }
    }
}
