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
 * Created on May 19, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.simtk.util.*;

import org.simtk.molecularstructure.Residue;

/**
 *  
  * @author Christopher Bruns
  * 
  * Simple rectangular cartoon representation of a macromolecular sequence
 */
public class SequenceCartoonCanvas extends BufferedCanvas 
implements MouseMotionListener, ResidueActionListener, AdjustmentListener, MouseListener
{
    public static final long serialVersionUID = 1L;
    // Tornado tornado;
    ResidueActionBroadcaster residueActionBroadcaster;
    // boolean userIsInteracting = false;
    SequenceCanvas sequenceCanvas;

    Hashtable residuePositions = new Hashtable();
    Hashtable positionResidues = new Hashtable();
    int highlight = -1;
    int residueCount = 0;

    Color cartoonBackgroundColor = new Color(200, 200, 200);
    Color cartoonVisibleColor    = new Color(220, 220, 220);

    int cartoonHeight = 15;
    int cartoonTop = (int) (0);
    int cartoonBottom = cartoonTop + cartoonHeight - 1;
    int cartoonLeft = 0;
    int cartoonRight = 0;            
    int cartoonMargin = 16; // Same as the hard coded size of arrow buttons in JScrollBar

    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    Cursor leftRightCursor = new Cursor(Cursor.E_RESIZE_CURSOR);

    private Color selectionColor;

    public SequenceCartoonCanvas(ResidueActionBroadcaster b, SequenceCanvas s) {
        super();
        sequenceCanvas = s;
        // tornado = t;
        residueActionBroadcaster = b;
        setBackground(Color.white);
        checkSize();
        addMouseMotionListener(this);
        addMouseListener(this);
        sequenceCanvas.parent.getHorizontalScrollBar().addAdjustmentListener(this);
    }

    public void setSelectionColor(Color c) {
        selectionColor = c;
    }

    public void checkSize() {
        Dimension preferredSize = new Dimension(cartoonMargin * 2 + cartoonRight - cartoonLeft + 1, cartoonHeight);
        setSize(preferredSize);
    }
    
    public void paint(Graphics g) {

        // Clear background
        g.setColor(getBackground());
        Dimension d = getSize();
        g.fillRect(0, 0, d.width, d.height);

        // Draw sequence cartoon
        if (residueCount <= 0) return;
        
        cartoonLeft = cartoonMargin;

        // int cartoonWidth = d.width - 2 * cartoonMargin;
        
        // if (cartoonWidth <= 0) return;
        
        // Notice if sequence does not fill its viewport window
        // double sequenceWidth = sequenceCanvas.getSequenceWidth();
        // double windowWidth = sequenceCanvas.getViewportWidth();
        // double visibleRatio = windowWidth / sequenceWidth;
        // if (visibleRatio > 1.0) { // Entire sequence is shown
        //     cartoonWidth = (int) (cartoonWidth / visibleRatio);
        //     visibleRatio = 1.0;
        // }
        
        // Draw rectangular background of sequence cartoon
        cartoonRight = cartoonLeft + cartoonWidth() - 1;
        g.setColor(cartoonBackgroundColor);
        g.fillRect(cartoonLeft, cartoonTop, cartoonWidth(), cartoonHeight - 2);

        // Draw lighter rectangle where sequence is visible
        g.setColor(cartoonVisibleColor);
        Residue leftResidue = sequenceCanvas.getFirstVisibleResidue();
        Residue rightResidue = sequenceCanvas.getFinalVisibleResidue();
        if (    (leftResidue != null) && 
                (residuePositions.containsKey(leftResidue)) &&
                (rightResidue != null) && 
                (residuePositions.containsKey(rightResidue))
             ) {
            int leftPosition = ((Integer)residuePositions.get(leftResidue)).intValue();
            int rightPosition = ((Integer)residuePositions.get(rightResidue)).intValue();
            
            // double leftFraction = leftPosition/(residueCount - 1.0);

            int leftPixel = positionPixel(leftPosition - 0.5);
            int rightPixel = positionPixel(rightPosition + 0.5);

            g.fillRect(leftPixel, cartoonTop, rightPixel - leftPixel + 1, cartoonHeight - 2);            
        }
        
        // Draw highlight
        if (highlight >= 0) {
            int highlightLeft = positionPixel(highlight - 0.5);
            int highlightRight = positionPixel(highlight + 0.5);
            int highlightWidth = highlightRight - highlightLeft + 1;
            if (highlightWidth < 1) highlightWidth = 1;
            
            g.setColor(Color.yellow);
            g.fillRect(highlightLeft, cartoonTop, highlightWidth, cartoonHeight - 2);
        }
        
        // Draw selection
        // Sort selected residues into contiguous ranges
        HashSet processedSelections = new HashSet();
        Vector selectStarts = new Vector();
        Vector selectEnds = new Vector();
        for (Iterator i = residueActionBroadcaster.getSelected().iterator(); i.hasNext(); ) {
            Residue r = (Residue) i.next();
            if (processedSelections.contains(r)) continue; // skip residues we already saw
            processedSelections.add(r);

            // Find start of this selected range
            Residue f = r;
            while (true) {
                if (f.getPreviousResidue() == null) break;
                if ( ! residueActionBroadcaster.getSelected().contains(f.getPreviousResidue())) break;
                f = f.getPreviousResidue();
                processedSelections.add(f);
            }

            // Find end of this selected range
            Residue e = r;
            while (true) {
                if (e.getNextResidue() == null) break;
                if ( ! residueActionBroadcaster.getSelected().contains(e.getNextResidue())) break;
                e = e.getNextResidue();
                processedSelections.add(e);
            }
            
            Integer startPosition = (Integer) residuePositions.get(e);
            Integer endPosition = (Integer) residuePositions.get(f);
            if ( (startPosition != null) && (endPosition != null) ) {
                selectEnds.add(startPosition);
                selectStarts.add(endPosition);
            }
        }

        // Paint a rectangle for each selection
        g.setColor(selectionColor);
        for (int i = 0; i < selectStarts.size(); i++) {
            int start = ((Integer)selectStarts.get(i)).intValue();
            int end = ((Integer)selectEnds.get(i)).intValue();
            int startPixel = positionPixel(start - 0.5);
            int endPixel = positionPixel(end + 0.5);
            g.fillRect(startPixel, cartoonTop, (endPixel - startPixel + 1), cartoonHeight - 2);            
        }
        
        // Finally draw cartoon outline
        g.setColor(getForeground());
        g.drawRect(cartoonLeft, cartoonTop, cartoonWidth(), cartoonHeight - 2);

    }
    
    int cartoonWidth() {
        Dimension d = getSize();
        double cartoonWidth = d.width - 2 * cartoonMargin;
        double sequenceWidth = sequenceCanvas.getSequenceWidth();
        double windowWidth = sequenceCanvas.getViewportWidth();
        double visibleRatio = windowWidth / sequenceWidth;
        if (visibleRatio > 1.0) { // Entire sequence is shown
            cartoonWidth = (int) (cartoonWidth / visibleRatio);
            visibleRatio = 1.0;
        }
        return (int)cartoonWidth;
    }
    int positionPixel(double position) {
        return (int)(cartoonLeft + ((position + 0.5) * cartoonWidth() / residueCount));
    }

    public void mouseClicked(MouseEvent e) {
        if (mouseIsInCartoon(e)) {
            if (e.getClickCount() == 2) {
                // TODO double click should center on position
                residueActionBroadcaster.fireCenterOn(mouseResidue(e));
            }
            else mouseDragged(e);
        }
    }
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
        setCursor(defaultCursor);
    }
    
    boolean mousePressedInCartoon = false;
    public void mousePressed(MouseEvent e) {
        if (mouseIsInCartoon(e)) {
            mousePressedInCartoon = true;

            // Center on selected residue, just as in MouseDragged
            mouseDragged(e);
        }
        else {
            mousePressedInCartoon = false;
            setCursor(defaultCursor);
        }
    }
    public void mouseReleased(MouseEvent e) {
        setCursor(defaultCursor);
    }
    
    boolean mouseMoveHighlights = false;
    public void mouseMoved(MouseEvent e) {
        if (mouseMoveHighlights) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            // Is it in the cartoon area?
            if (mouseIsInCartoon(e)) {
                residueActionBroadcaster.lubricateUserInteraction();
                setCursor(leftRightCursor);
                Residue residue = mouseResidue(e);
                if (residue != null) {
                    residueActionBroadcaster.fireHighlight(residue);
                    repaint();
                }
            }
            else 
                setCursor(defaultCursor);
        }
    }

    public void mouseDragged(MouseEvent e) {
        // Drag on cartoon acts like a scroll bar

        // Did this drag begin in the cartoon area?
        if (mousePressedInCartoon) {
            residueActionBroadcaster.lubricateUserInteraction();
            setCursor(leftRightCursor);
            Residue residue = mouseResidue(e);
            if (residue != null) {
                sequenceCanvas.centerOn(residue);
                residueActionBroadcaster.fireHighlight(residue);
            }
        }
    }
    
    boolean mouseIsInCartoon(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();
        return ( (mouseX >= cartoonLeft) &&
                 (mouseX <= cartoonRight) &&
                 (mouseY >= cartoonTop) &&
                 (mouseY <= cartoonBottom) );         
    }
    
    Residue mouseResidue(MouseEvent e) {
        // Return an end residue if the mouse is outside of the cartoon to the left or right
        int mouseX = e.getX();
        int position = (int) (residueCount * (mouseX - cartoonLeft) / (cartoonRight - cartoonLeft));
        if (position < 0) position = 0;
        if (position >= residueCount) position = residueCount - 1;
        Residue residue = null;
        if (positionResidues.containsKey(new Integer(position)));
            residue = (Residue) positionResidues.get(new Integer(position));
        return residue;
    }

    public void clearResidues() {
        residuePositions.clear();
        positionResidues.clear();
        highlight = -1;
        residueCount = 0;
    }    
    public void add(Residue r) {
        residuePositions.put(r, new Integer(residueCount));
        positionResidues.put(new Integer(residueCount), r);
        residueCount ++;
    }    

    public void highlight(Residue r) {
        if (residuePositions.containsKey(r)) {
            highlight = ((Integer)residuePositions.get(r)).intValue();
            repaint();
        }
        else unHighlightResidue();
    }
    public void unHighlightResidue() {
        highlight = -1;
        repaint();
    }
    public void select(Selectable s) {
        repaint();
    }
    public void unSelect(Selectable s) {
        repaint();
    }
    public void unSelect() {
        repaint();
    }
    public void centerOn(Residue r) {} // This sequence does not move

    // Respond to sequence scroll bar event - redraw
    public void adjustmentValueChanged(AdjustmentEvent e) {
        repaint();
    }
}