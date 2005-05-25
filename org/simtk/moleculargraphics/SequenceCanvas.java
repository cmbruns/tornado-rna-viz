/*
 * Created on May 17, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JScrollBar;

import org.simtk.molecularstructure.Residue;

public class SequenceCanvas extends BufferedCanvas 
implements ResidueSelector, MouseMotionListener, AdjustmentListener, MouseListener
{
    public static final long serialVersionUID = 1L;

    SequencePane parent;
    Tornado tornado;

    Vector<String> residueSymbols = new Vector<String>();
    int columnCount = 0;
    Hashtable<Residue, Integer> residuePositions = new Hashtable<Residue, Integer>();
    Hashtable<Integer, Residue> positionResidues = new Hashtable<Integer, Residue>();
    Graphics myGraphics = null; // Notice when Graphics object is available
    int highlight = -1;

    double symbolWidth = 25;
    double symbolHeight = 30;
    double characterSpacing = 5; // pixels between symbols
    Font font;
    int baseLine = (int) symbolHeight; // y coordinate of character baseline
    
    Font numberFont;
    int numberHeight = 5;
    int numberBaseLine = 5;
    
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);
    Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    Cursor textCursor = new Cursor(Cursor.TEXT_CURSOR);
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    Cursor leftRightCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
    Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

    public SequenceCanvas(String initialSequence, SequencePane p, Tornado t) {
        super();
        parent = p;
        tornado = t;
        setBackground(Color.white);
        columnCount = initialSequence.length();
        clearResidues();
        for (int r = 0; r < columnCount; r++)
            residueSymbols.add("" + initialSequence.charAt(r));
        font = new Font("Monospaced", Font.BOLD, 20);
        numberFont = new Font("SanSerif", Font.PLAIN, 9);
        checkSize(getGraphics()); // getGraphics returns null here, but I had to try...
        addMouseMotionListener(this);
        addMouseListener(this);
        parent.getHorizontalScrollBar().addAdjustmentListener(this);
    }

    public void update(Graphics g) {
        paint(g);
    }
    
    public void paint(Graphics onScreenGraphics) {
        checkOffScreen();
        if (offScreenImage == null) return;
        Graphics g = offScreenImage.getGraphics();
        checkSize(g);
        
        // Check bounds of viewport
        Rectangle viewRect = parent.getViewport().getViewRect();
        int leftPixel = (int) viewRect.getMinX();
        int rightPixel = (int) viewRect.getMaxX();
        int leftPosition = (int)((leftPixel - characterSpacing/2.0) / symbolWidth);
        int rightPosition = (int)((rightPixel - characterSpacing/2.0) / symbolWidth);
        if (leftPosition < 0) leftPosition = 0;
        if (rightPosition >= columnCount) rightPosition = columnCount - 1;
        
        // Clear background
        g.setColor(getBackground());
        g.fillRect(leftPixel, 0, getViewportWidth(), height);

        // Draw highlight
        if (highlight >= 0) {
            if ( (highlight >= leftPosition) && (highlight <= rightPosition) ) {
                int leftX = (int) (characterSpacing / 2.0 + highlight * symbolWidth);
                int rightX = (int) ((highlight + 1) * symbolWidth);
                int bottomY = (int) (baseLine);
                int topY = (int) (bottomY - symbolHeight);
                g.setColor(Color.yellow);
                g.fillRect(leftX, topY, (rightX - leftX + 1), (bottomY - topY + 1));
            }
        }

        // Draw sequence
        g.setFont(font);
        g.setColor(getForeground());
        for (int r = leftPosition; r <= rightPosition; r++) {
            g.drawString(residueSymbols.get(r), (int)(characterSpacing + r * symbolWidth), baseLine);
        }
        
        // Draw numbers
        g.setFont(numberFont);
        g.setColor(getForeground());
        for (int r = leftPosition; r <= rightPosition; r++) {
            Residue res = positionResidues.get(r);
            if (res != null) {
                int residueNumber = res.getResidueNumber();
                
                // Choose which numbers to show
                if ( ((residueNumber % 5) == 0) || // show round multiples of 10
                     (r == 0) || // show first residue number
                     (r == (columnCount - 1)) // show final residue number
                     ) {
                    // Don't number the ones right next to the numbered ends
                    if ( (r==1) || (r == (columnCount - 2)) ) continue;
                    g.drawString("" + residueNumber, (int)(characterSpacing + r * symbolWidth), numberBaseLine);
                }
            }
        
        }

        int height = getSize().height;
        int width = rightPixel - leftPixel + 1;
        onScreenGraphics.drawImage(offScreenImage, 
                leftPixel, 0, 
                leftPixel + width - 1, height - 1,
                leftPixel, 0,
                leftPixel + width - 1, height - 1,
                null);
    }
    
    void checkSize(Graphics g) {
        boolean haveNewGraphics = false;
        if ( (myGraphics == null) && (g != null) ) haveNewGraphics = true;

        // Set font sizes once, once a Graphics object is available
        // Many important things are initialized in this block
        if (haveNewGraphics) { // we have Graphics for the first time
            myGraphics = g;
            FontMetrics fm;

            fm = g.getFontMetrics(font);
            symbolHeight = fm.getAscent() + 1;
            symbolWidth = fm.charWidth('W') + characterSpacing;
            baseLine = fm.getAscent();

            fm = g.getFontMetrics(numberFont);
            numberHeight = fm.getAscent();
            numberBaseLine = numberHeight + baseLine;

            parent.getHorizontalScrollBar().setUnitIncrement((int)symbolWidth);
        }
        int desiredHeight = (int) numberBaseLine + 1;
        if (desiredHeight <= 0) desiredHeight = 1;

        int desiredWidth = (int) (characterSpacing + columnCount * symbolWidth);
        if (desiredWidth <= 0) desiredWidth = 1;
        if (desiredWidth < parent.getViewport().getWidth()) desiredWidth = parent.getViewport().getWidth();

        Dimension d = getSize();
        if ( ((d.height != desiredHeight) ||
             (d.width != desiredWidth)) ) {
            Dimension preferredSize = new Dimension(desiredWidth, desiredHeight);
            setSize(preferredSize);
            setPreferredSize(preferredSize);
            setMinimumSize(preferredSize);
            setMaximumSize(preferredSize);
            if (parent.contentPanel != null) {
                
                // Test kludge to make that dumb sequence pane the right size
                Panel p = tornado.sequencePanel;
                int preferredPanelHeight = p.getPreferredSize().height;
                p.setSize(p.getSize().width, preferredPanelHeight);

                // parent.contentPanel.revalidate(); // so that container gets resized
                // tornado.sequencePanel.revalidate();
                parent.checkSize();
            }
        }
    }

    public void clearResidues() {
        columnCount = 0;
        residueSymbols.clear();
        residuePositions.clear();
        positionResidues.clear();
        highlight = -1;
        checkSize(getGraphics());
    }    
    public void addResidue(Residue r) {
        residuePositions.put(r, residueSymbols.size());
        positionResidues.put(residueSymbols.size(), r);
        residueSymbols.add("" + r.getOneLetterCode());
        columnCount ++;        
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
    public void centerOnResidue(Residue r) {
        if (residuePositions.containsKey(r)) {
            int position = residuePositions.get(r);
            int pixel = (int)(symbolWidth * position + characterSpacing);

            JScrollBar bar = parent.getHorizontalScrollBar();

            // middle of scrollbar, not beginning
            pixel -= (bar.getVisibleAmount() / 2);
            if (pixel < bar.getMinimum()) pixel = bar.getMinimum();
            if (pixel > bar.getMaximum()) pixel = bar.getMaximum();            
            
            bar.setValue(pixel);
        }        
    }

    int oldMouseViewportX = 0; // mouse coordinate relative to the viewport
    boolean mouseIsDragging = false;
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
        mouseIsDragging = true;
        int leftPixel = (int) parent.getViewport().getViewRect().getMinX();
        oldMouseViewportX = e.getX() - leftPixel;
    }
    public void mouseReleased(MouseEvent e) {
        mouseIsDragging = false;
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {
        // Highlight residue under the pointer
        int mouseX = e.getX();
        int mouseY = e.getY();
        // Is the mouse in the sequence area?
        if ( (mouseY >= (baseLine - symbolHeight)) && 
             (mouseY <= baseLine) &&
             (mouseX >= (int) (characterSpacing/2.0)) &&
             (mouseX <= (int) (characterSpacing/2.0 + (columnCount) * symbolWidth)) ) 
        {
            setCursor(textCursor);
            
            int sequenceIndex = (int)( (mouseX - characterSpacing/2.0)/symbolWidth );
            if ( (sequenceIndex != highlight ) && (positionResidues.containsKey(sequenceIndex)) ) {
                tornado.userIsInteracting = true;
                Residue r = positionResidues.get(sequenceIndex);
                tornado.highlight(r);
                repaint();
            }
        }
        
        // Is the pointer in the numbering area?
        else if ( (mouseY > baseLine) &&
                  (mouseY <= numberBaseLine) ) {
            setCursor(leftRightCursor);            
        }
        
        else {
            setCursor(defaultCursor);
        }
    }

    public void mouseDragged(MouseEvent e) {
        // TODO - drag on sequence selects a range (click selects one residue)

        int mouseX = e.getX();
        int mouseY = e.getY();

        // Compute mouse position relative to viewport
        int leftPixel = (int) parent.getViewport().getViewRect().getMinX();
        int mouseViewportX = mouseX - leftPixel;

        // TODO - drag on numbers drags sequence? (maybe numbers section needs to be bigger?)
        // Is the pointer in the numbering area?
        if ( (mouseY > baseLine) &&
             (mouseY <= numberBaseLine) &&
             (mouseIsDragging)) {

            int delta = mouseViewportX - oldMouseViewportX;
            if (delta != 0) {
                JScrollBar bar = parent.getHorizontalScrollBar();
    
                // middle of scrollbar, not beginning
                int pixel = bar.getValue() - delta;
                if (pixel < bar.getMinimum()) pixel = bar.getMinimum();
                if (pixel > bar.getMaximum()) pixel = bar.getMaximum();            
                
                bar.setValue(pixel);
            }
        }  
        oldMouseViewportX = mouseViewportX;
    }
    
    // Respond to sequence scroll bar event - redraw
    public void adjustmentValueChanged(AdjustmentEvent e) {
        tornado.userIsInteracting = true;
        repaint();
    }
    
    /**
     * How wide is the viewport in which the sequence is shown?
     */
    public int getViewportWidth() {
        int leftPixel = (int) parent.getViewport().getViewRect().getMinX();
        int rightPixel = (int) parent.getViewport().getViewRect().getMaxX();
        return rightPixel - leftPixel + 1;        
    }
    /**
     * What is the total width in pixels of the entire sequence?
     * @return
     */
    public int getSequenceWidth() {
        checkSize(getGraphics());
        return (int) (characterSpacing + columnCount * symbolWidth);
    }
    
    /** 
     * 
     * @return
     */
    public Residue getFirstVisibleResidue() {
        int leftPixel = (int) parent.getViewport().getViewRect().getMinX();        
        int leftPosition = (int)((leftPixel - characterSpacing/2.0) / symbolWidth);
        if (positionResidues.containsKey(leftPosition))
            return positionResidues.get(leftPosition);
        else return null;
    }
}
