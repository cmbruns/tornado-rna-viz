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
implements ResidueActionListener
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
    // Tornado tornado;
    ResidueActionBroadcaster residueActionBroadcaster;
    Panel contentPanel;
    private Color selectionColor;
    
    SequencePane(ResidueActionBroadcaster b) {
        residueActionBroadcaster = b;
        // tornado = parent;
        
        // Only scroll horizontally
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        getViewport().setBackground(backgroundColor); // for when text panel does not fill viewport
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        
        sequenceCanvas = new TornadoSequenceCanvas("This is a test", this, residueActionBroadcaster);

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

    public void setSelectionColor(Color c) {
        selectionColor = c;
        sequenceCanvas.setSelectionColor(c);
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
    
    public void highlight(Residue r) {
        sequenceCanvas.highlight(r);
        currentHighlightedResidue = r;
    }
    public void unHighlightResidue() {
        sequenceCanvas.unHighlightResidue();
        currentHighlightedResidue = null;
    }
    public void select(Selectable r) {
        sequenceCanvas.select(r);
    }
    public void unSelect(Selectable r) {
        sequenceCanvas.unSelect(r);
    }
    public void unSelect() {
        sequenceCanvas.unSelect();
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
