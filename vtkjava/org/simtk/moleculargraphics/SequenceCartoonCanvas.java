/*
 * Created on May 19, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

import org.simtk.molecularstructure.Residue;

/**
 *  
  * @author Christopher Bruns
  * 
  * Simple rectangular cartoon representation of a macromolecular sequence
 */
public class SequenceCartoonCanvas extends BufferedCanvas 
implements MouseMotionListener, ResidueSelector, AdjustmentListener, MouseListener
{
    public static final long serialVersionUID = 1L;
    Tornado tornado;
    SequenceCanvas sequenceCanvas;

    Hashtable<Residue, Integer> residuePositions = new Hashtable<Residue, Integer>();
    Hashtable<Integer, Residue> positionResidues = new Hashtable<Integer, Residue>();
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

    public SequenceCartoonCanvas(Tornado t, SequenceCanvas s) {
        super();
        sequenceCanvas = s;
        tornado = t;
        setBackground(Color.white);
        checkSize();
        addMouseMotionListener(this);
        addMouseListener(this);
        sequenceCanvas.parent.getHorizontalScrollBar().addAdjustmentListener(this);
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
        int cartoonWidth = d.width - 2 * cartoonMargin;
        
        if (cartoonWidth <= 0) return;
        
        // Notice if sequence does not fill its viewport window
        double sequenceWidth = sequenceCanvas.getSequenceWidth();
        double windowWidth = sequenceCanvas.getViewportWidth();
        double visibleRatio = windowWidth / sequenceWidth;
        if (visibleRatio > 1.0) { // Entire sequence is shown
            cartoonWidth = (int) (cartoonWidth / visibleRatio);
            visibleRatio = 1.0;
        }
        
        // Draw rectangular background of sequence cartoon
        cartoonRight = cartoonLeft + cartoonWidth - 1;
        g.setColor(cartoonBackgroundColor);
        g.fillRect(cartoonLeft, cartoonTop, cartoonWidth, cartoonHeight - 2);

        // TODO - draw lighter rectangle where sequence is visible
        g.setColor(cartoonVisibleColor);
        Residue leftResidue = sequenceCanvas.getFirstVisibleResidue();
        if ( (leftResidue != null) && (residuePositions.containsKey(leftResidue)) ) {
            int leftPosition = residuePositions.get(leftResidue);
            double leftFraction = leftPosition/(residueCount - 1.0);
            int leftPixel = cartoonLeft + (int)(leftFraction * cartoonWidth);
            g.fillRect(leftPixel, cartoonTop, (int)(cartoonWidth * visibleRatio + 0.5), cartoonHeight - 2);            
        }
        
        // Draw highlight
        if (highlight >= 0) {
            int highlightLeft = cartoonLeft + (int)((highlight) * cartoonWidth / residueCount);
            int highlightWidth = cartoonWidth / residueCount;
            if (highlightWidth < 1) highlightWidth = 1;
            
            g.setColor(Color.yellow);
            g.fillRect(highlightLeft, cartoonTop, highlightWidth, cartoonHeight - 2);
        }
        
        // Finally draw cartoon outline
        g.setColor(getForeground());
        g.drawRect(cartoonLeft, cartoonTop, cartoonWidth, cartoonHeight - 2);

    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
        setCursor(defaultCursor);
    }
    public void mousePressed(MouseEvent e) {
        // Center on selected residue, just as in MouseDragged
        mouseDragged(e);
    }
    public void mouseReleased(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();
        // Is it in the cartoon area?
        if ( (mouseX >= cartoonLeft) &&
             (mouseX <= cartoonRight) &&
             (mouseY >= cartoonTop) &&
             (mouseY <= cartoonBottom) ) 
        {
            setCursor(leftRightCursor);
            
            int position = (int) (residueCount * (mouseX - cartoonLeft) / (cartoonRight - cartoonLeft));
            if (positionResidues.containsKey(position)) {
                tornado.userIsInteracting = true;
                Residue residue = positionResidues.get(position);
                tornado.highlight(residue);
                repaint();
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        // Drag on cartoon acts like a scroll bar
        int mouseX = e.getX();
        int mouseY = e.getY();
        // Is it in the cartoon area?
        if ( (mouseX >= cartoonLeft) &&
             (mouseX <= cartoonRight) &&
             (mouseY >= cartoonTop) &&
             (mouseY <= cartoonBottom) ) 
        {
            setCursor(leftRightCursor);
            
            int position = (int) (residueCount * (mouseX - cartoonLeft) / (cartoonRight - cartoonLeft));
            if (positionResidues.containsKey(position)) {
                tornado.userIsInteracting = true;
                Residue residue = positionResidues.get(position);
                sequenceCanvas.centerOnResidue(residue);
            }
        }
    }

    public void clearResidues() {
        residuePositions.clear();
        positionResidues.clear();
        highlight = -1;
        residueCount = 0;
    }    
    public void addResidue(Residue r) {
        residuePositions.put(r, residueCount);
        positionResidues.put(residueCount, r);
        residueCount ++;
    }    

    public void highlight(Residue r) {
        if (residuePositions.containsKey(r)) {
            highlight = residuePositions.get(r);
            repaint();
        }
        else unHighlight();
    }
    public void unHighlight() {
        highlight = -1;
        repaint();
    }
    public void select(Residue r) {
    }
    public void unSelect(Residue r) {
    }
    public void centerOnResidue(Residue r) {} // This sequence does not move

    // Respond to sequence scroll bar event - redraw
    public void adjustmentValueChanged(AdjustmentEvent e) {
        repaint();
    }
}