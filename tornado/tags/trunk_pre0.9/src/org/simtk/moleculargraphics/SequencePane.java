/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Apr 27, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.simtk.util.*;
import org.simtk.molecularstructure.*;


/** 
 * @author Christopher Bruns
 * 
 * Window area for displaying the molecular sequence of residues in Tornado application.
 */
public class SequencePane extends JScrollPane 
implements ResidueHighlightListener, ActiveMoleculeListener,
ResidueCenterListener
{
    public static final long serialVersionUID = 1L;
    
    // public boolean userIsInteracting = false;

    // Biopolymer molecule;
    Residue currentHighlightedResidue;
    Residue firstResidue;
    Residue finalResidue;
    
    // SequenceTextPane textPane;
    TornadoSequenceCanvas sequenceCanvas;
    // NumberPane numberPane;
    Color backgroundColor = Color.white;
    Tornado tornado;
    ResidueHighlightBroadcaster residueHighlightBroadcaster;
    ResidueCenterBroadcaster residueCenterBroadcaster;
    Panel contentPanel;
    
    SequencePane(ResidueHighlightBroadcaster b, 
            ResidueCenterBroadcaster c,
            Tornado parent) {
        residueHighlightBroadcaster = b;
        residueCenterBroadcaster = c;
        tornado = parent;
        
        // Only scroll horizontally
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        getViewport().setBackground(backgroundColor); // for when text panel does not fill viewport
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        sequenceCanvas = new TornadoSequenceCanvas(
                "This is a test", 
                this, 
                residueHighlightBroadcaster,
                residueCenterBroadcaster);

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

//    public void setSelectionColor(Color c) {
//        selectionColor = c;
//        sequenceCanvas.setSelectionColor(c);
//    }

    public void setActiveMolecule(Molecule molecule) {
        if (molecule instanceof Biopolymer) {
            clearResidues();
            for (Residue residue: ((Biopolymer)molecule).residues()) {
                add(residue);
            }
            repaint();
        }
    }
    
    public void lubricateUserInteraction() {tornado.lubricateUserInteraction();}
    
    Dimension maxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Dimension minSize = new Dimension(10,10);
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
    public Dimension getMinimumSize() {
        return new Dimension(0, getPreferredSize().height);
    }
    public void setMaximumSize(Dimension d) {maxSize = d;}
    public void setMinimumSize(Dimension d) {minSize = d;}
    
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
    
    public TornadoSequenceCanvas getSequenceCanvas() {return sequenceCanvas;}

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
    
    public void highlightResidue(Residue r, Color c) {
        sequenceCanvas.highlightResidue(r, c);
        currentHighlightedResidue = r;
    }
    public void unhighlightResidues() {
        sequenceCanvas.unhighlightResidues();
        currentHighlightedResidue = null;
    }
    public void unhighlightResidue(Residue r) {
        sequenceCanvas.unhighlightResidue(r);
        if (currentHighlightedResidue == r) currentHighlightedResidue = null;
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
            highlightNextResidue(Color.blue);
        }
    }
    class PreviousResidueAction extends AbstractAction {
        public static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent e) {
            highlightPreviousResidue(Color.blue);
        }
    }

    void highlightNextResidue(Color c) {
        Residue oldResidue = currentHighlightedResidue;
        Residue nextResidue = null;
        if (oldResidue == null) // Go to first residue
            nextResidue = firstResidue;
        else 
            nextResidue = currentHighlightedResidue.getNextResidue();

        if (oldResidue != null) residueHighlightBroadcaster.fireUnhighlightResidue(oldResidue);
        if (nextResidue != null) {
            residueHighlightBroadcaster.fireHighlight(nextResidue);
            // residueCenterBroadcaster.fireCenter(nextResidue);
        }
    }

    void highlightPreviousResidue(Color c) {
        Residue oldResidue = currentHighlightedResidue;
        Residue previousResidue = null;
        if (oldResidue == null) // Go to last residue
            previousResidue = finalResidue;
        else 
            previousResidue = currentHighlightedResidue.getPreviousResidue();

        if (oldResidue != null) residueHighlightBroadcaster.fireUnhighlightResidue(oldResidue);
        if (previousResidue != null) {
            residueHighlightBroadcaster.fireHighlight(previousResidue);
            // residueCenterBroadcaster.fireCenter(previousResidue);
        }
    }

    public void centerOnResidue(Residue residue) {
        sequenceCanvas.centerOnResidue(residue);
    }
}
