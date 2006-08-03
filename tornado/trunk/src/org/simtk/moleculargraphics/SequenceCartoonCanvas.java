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

// import java.awt.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.*;
import org.simtk.util.*;
import org.simtk.molecularstructure.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Simple rectangular cartoon representation of a macromolecular sequence
 */
public class SequenceCartoonCanvas extends BufferedCanvas 
implements MouseMotionListener, ResidueHighlightListener, 
AdjustmentListener, MouseListener, ActiveMoleculeListener
{
    public static final long serialVersionUID = 1L;
    // Tornado tornado;
    ResidueHighlightBroadcaster residueHighlightBroadcaster;
    // boolean userIsInteracting = false;
    TornadoSequenceCanvas sequenceCanvas;
    
    protected Map<Residue, Color> highlightedResidues = new LinkedHashMap<Residue, Color>();

    Map<Residue, Integer> residuePositions = new LinkedHashMap<Residue, Integer>();
    Hashtable positionResidues = new Hashtable();
    // int highlight = -1;
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

    public SequenceCartoonCanvas(ResidueHighlightBroadcaster b, TornadoSequenceCanvas s) {
        super();
        sequenceCanvas = s;
        // tornado = t;
        residueHighlightBroadcaster = b;
        setBackground(Color.white);
        checkSize();
        addMouseMotionListener(this);
        addMouseListener(this);
        sequenceCanvas.parent.getHorizontalScrollBar().addAdjustmentListener(this);
    }

    private int preferredHeight() {return cartoonHeight;}
    
    Dimension maxSize = new Dimension(Integer.MAX_VALUE, preferredHeight());
    Dimension minSize = new Dimension(0, preferredHeight());
    Dimension prefSize = new Dimension(100, preferredHeight());
    public Dimension getMaximumSize() {
        return maxSize;
    }
    public Dimension getMinimumSize() {
        return minSize;
    }
    public Dimension getPreferredSize() {
        return prefSize;
    }
    public void setMaximumSize(Dimension d) {maxSize = d;}
    public void setMinimumSize(Dimension d) {minSize = d;}
    public void setPreferredSize(Dimension d) {prefSize = d;}
    

    public void checkSize() {
        Dimension preferredSize = new Dimension(cartoonMargin * 2 + cartoonRight - cartoonLeft + 1, cartoonHeight);
        setSize(preferredSize);
    }
    
    class HighlightSegment {
        int start;
        int end;
        Color color;
        HighlightSegment(int start, int end, Color color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }
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
        
        // Draw highlights
        // Sort selected residues into contiguous ranges
        HashSet<Residue> processedSelections = new HashSet<Residue>();
        // Map<Color, Integer> selectStarts = new LinkedHashMap<Color, Integer>();
        // Map<Color, Integer> selectEnds = new LinkedHashMap<Color, Integer>();
        List<HighlightSegment> highlightSegments = new Vector<HighlightSegment>();
        
        for (Residue r : highlightedResidues.keySet()) {
            if (processedSelections.contains(r)) continue; // skip residues we already saw
            processedSelections.add(r);

            Color color = highlightedResidues.get(r);
            
            // Find start of this selected range
            Residue f = r;
            while (true) {
                Residue prev = f.getPreviousResidue();
                if (prev == null) break; // start of chain
                if ( ! highlightedResidues.containsKey(prev)) break; // not highlighted
                if ( ! highlightedResidues.get(prev).equals(color)) break; // not same color
                f = prev;
                processedSelections.add(f);
            }

            // Find end of this selected range
            Residue e = r;
            while (true) {
                Residue next = e.getNextResidue();
                if (next == null) break;
                if ( ! highlightedResidues.containsKey(next)) break;
                if ( ! highlightedResidues.get(next).equals(color)) break;
                e = next;
                processedSelections.add(e);
            }
            
            
            Integer startPosition = residuePositions.get(f);
            Integer endPosition = residuePositions.get(e);
            if ( (startPosition != null) && (endPosition != null) ) {
                highlightSegments.add(new HighlightSegment(startPosition, endPosition, color));
            }
        }

        // Paint a rectangle for each selection
        for (HighlightSegment segment : highlightSegments) {
            g.setColor(segment.color);
            int start = segment.start;
            int end = segment.end;
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
                // residueHighlightBroadcaster.fireCenterOn(mouseResidue(e));
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
                // residueHighlightBroadcaster.lubricateUserInteraction();
                setCursor(leftRightCursor);
                Residue residue = mouseResidue(e);
                if (residue != null) {
                    // residueHighlightBroadcaster.fireHighlight(residue);
                    // repaint();
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
            sequenceCanvas.lubricateUserInteraction();
            setCursor(leftRightCursor);
            Residue residue = mouseResidue(e);
            if (residue != null) {
                sequenceCanvas.centerOnResidue(residue);
                // residueHighlightBroadcaster.fireHighlight(residue);
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
        highlightedResidues.clear();
        residueCount = 0;
    }    
    public void add(Residue r) {
        residuePositions.put(r, new Integer(residueCount));
        positionResidues.put(new Integer(residueCount), r);
        residueCount ++;
    }    

    public void highlightResidue(Residue r, Color c) {
        if (residuePositions.containsKey(r)) {
            highlightedResidues.put(r, c);
            repaint();
        }
    }
    
    
    public void unhighlightResidue(Residue r) {
        highlightedResidues.remove(r);
        repaint();
    }

    public void unhighlightResidues() {
        highlightedResidues.clear();
        repaint();
    }

    // Respond to sequence scroll bar event - redraw
    public void adjustmentValueChanged(AdjustmentEvent e) {
        repaint();
    }

    public void setActiveMolecule(Molecule molecule) {
        if (molecule instanceof Biopolymer) {
            clearResidues();
            for (Residue residue : ((Biopolymer)molecule).residues()) {
                add(residue);
            }
        }
    }
}