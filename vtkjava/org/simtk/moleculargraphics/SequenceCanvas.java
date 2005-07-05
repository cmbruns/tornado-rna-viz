/*
 * Created on May 17, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JScrollBar;
import org.simtk.molecularstructure.*;
import org.simtk.util.*;

public class SequenceCanvas extends BufferedCanvas 
implements ResidueActionListener, MouseMotionListener, AdjustmentListener, MouseListener
{
    public static final long serialVersionUID = 1L;

    SequencePane parent;
    // Tornado tornado;
    ResidueActionBroadcaster residueActionBroadcaster;
    // boolean userIsInteracting = false;

    Vector residueSymbols = new Vector();
    int columnCount = 0;
    Hashtable residuePositions = new Hashtable();
    Hashtable positionResidues = new Hashtable();
    Graphics myGraphics = null; // Notice when Graphics object is available
    Residue highlightResidue = null;
    int highlightPosition = -1;

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

    // Keep track of selection insertion point
    Residue insertionResidue = null;
    boolean insertionResidueRightSide;
    HashSet selectedResidues = new HashSet();
    Color selectionColor = new Color(50, 50, 255);
    Color highlightColor = new Color(255, 255, 100);
    
    public SequenceCanvas(
            String initialSequence, 
            SequencePane p, 
            ResidueActionBroadcaster b)
    {
        super();
        parent = p;
        
        // tornado = t;
        residueActionBroadcaster = b;
        
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
    
    public void highlightPosition(Graphics graphics, int position, Color color) {
        int leftX = (int) (characterSpacing / 2.0 + position * symbolWidth);
        int rightX = (int) (leftX + symbolWidth);
        int bottomY = (int) (baseLine) + 2;
        int topY = (int) (bottomY - symbolHeight) + 2;
        graphics.setColor(color);
        graphics.fillRect(leftX, topY, (rightX - leftX + 1), (bottomY - topY + 1));        
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
        if (highlightPosition >= 0) {
            if ( (highlightPosition >= leftPosition) && (highlightPosition <= rightPosition) ) {
                highlightPosition(g, highlightPosition, highlightColor);
            }
        }
        
        // Draw sequence
        g.setFont(font);
        g.setColor(getForeground());
        for (int r = leftPosition; r <= rightPosition; r++) {
            // Is it selected?
            Residue residue = (Residue) positionResidues.get(new Integer(r));
            if (selectedResidues.contains(residue)) {
                highlightPosition(g, r, selectionColor);
                g.setColor(getBackground()); // Inverse text color for selected residues
            }
            else g.setColor(getForeground()); // Normal text
            
            g.drawString((String) residueSymbols.get(r), (int)(characterSpacing + r * symbolWidth), baseLine);
        }
        
        // Draw numbers
        g.setFont(numberFont);
        g.setColor(getForeground());
        for (int r = leftPosition; r <= rightPosition; r++) {
            Residue res = (Residue) positionResidues.get(new Integer(r));
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
                // Panel p = tornado.sequencePanel;
                // int preferredPanelHeight = p.getPreferredSize().height;
                // p.setSize(p.getSize().width, preferredPanelHeight);

                // parent.contentPanel.revalidate(); // so that container gets resized
                // tornado.sequencePanel.revalidate();
                parent.checkSize();
            }
        }
    }

    public void clearResidues() {
        highlightResidue = null;
        columnCount = 0;
        residueSymbols.clear();
        residuePositions.clear();
        positionResidues.clear();
        selectedResidues.clear();
        highlightPosition = -1;
        checkSize(getGraphics());
    }    
    public void add(Residue r) {
        residuePositions.put(r, new Integer(residueSymbols.size()));
        positionResidues.put(new Integer(residueSymbols.size()), r);
        residueSymbols.add("" + r.getOneLetterCode());
        columnCount ++;        
    }    

    public void highlight(Residue r) {
        highlightResidue = r;
        if (residuePositions.containsKey(r)) {
            highlightPosition = ((Integer)residuePositions.get(r)).intValue();
            repaint();
        }
        else unHighlightResidue();
    }
    public void unHighlightResidue() {
        highlightResidue = null;
        highlightPosition = -1;
        repaint();
    }
    public void select(Selectable s) {
        if (! (s instanceof Residue) ) return;
        selectedResidues.add((Residue)s);
    }
    public void unSelect(Selectable s) {
        selectedResidues.remove(s);
    }
    public void unSelect() {
        selectedResidues.clear();
    }
    public void centerOn(Residue r) {
        
        // don't center if sequence canvas was the source of the center command
        if (! yesActuallyCenterOnResidue) return;
        
        if (residuePositions.containsKey(r)) {
            int position = ((Integer)residuePositions.get(r)).intValue();
            int pixel = (int)(symbolWidth * position + characterSpacing);

            JScrollBar bar = parent.getHorizontalScrollBar();

            // middle of scrollbar, not beginning
            pixel -= (bar.getVisibleAmount() / 2);
            if (pixel < bar.getMinimum()) pixel = bar.getMinimum();
            if (pixel > bar.getMaximum()) pixel = bar.getMaximum();            
            
            bar.setValue(pixel);
        }        
    }

    boolean mousePressedInSequenceArea = false;
    boolean mousePressedInNumberArea = false;
    boolean yesActuallyCenterOnResidue = true;

    public void mouseClicked(MouseEvent e) {
        mousePressedInSequenceArea = false;
        mousePressedInNumberArea = false;
        
        Residue clickedResidue = mouseResidue(e);

        if (e.getClickCount() == 2) {
            // Double click should center on position
            yesActuallyCenterOnResidue = false;
            residueActionBroadcaster.fireCenterOn(clickedResidue);
            // But in this one case, don't actually center in the sequence window
            yesActuallyCenterOnResidue = true;
        }

        if (e.isControlDown()) { // Control click preserves other selections
            if (selectedResidues.contains(clickedResidue))
                residueActionBroadcaster.fireUnSelect(clickedResidue);
            else
                residueActionBroadcaster.fireSelect(clickedResidue);
        }
        else { // Normal click - unselect all
            residueActionBroadcaster.fireUnSelect();
        }
        
        // Set insertion point
        if (clickedResidue != null) {
            insertionResidue = clickedResidue;
            insertionResidueRightSide = mouseResidueRightSide(e);
        }
        
        repaint();
    }

    int mousePressedViewportX = -1;
    int mousePressedBarCenter = -1;
    public void mousePressed(MouseEvent e) {
        mousePressedInSequenceArea = false;
        mousePressedInNumberArea = false;

        if (mouseIsInSequenceArea(e)) mousePressedInSequenceArea = true;
        if (mouseIsInNumberArea(e)) mousePressedInNumberArea = true;

        int leftPixel = viewportLeftPixel();
        mousePressedViewportX = e.getX() - leftPixel;
        mousePressedBarCenter = parent.getHorizontalScrollBar().getValue();
    }
    public void mouseReleased(MouseEvent e) {
        mousePressedInSequenceArea = false;
        mousePressedInNumberArea = false;
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {
        // Highlight residue under the pointer
        int mouseX = e.getX();
        int mouseY = e.getY();
        // Is the mouse in the sequence area?
        if (mouseIsInSequenceArea(e)) {
            setCursor(textCursor);

//            Residue residue = mouseResidue(e);
//            if ( (residue != null) && (residue != highlightResidue) ) {
//                residueActionBroadcaster.lubricateUserInteraction();
//                residueActionBroadcaster.fireHighlight(residue);
//                repaint();
//            }
        }
        
        // Is the pointer in the numbering area?
        else if (mouseIsInNumberArea(e))
            setCursor(leftRightCursor); 
        
        else
            setCursor(defaultCursor);
    }

    // keep track of how far past the edge we are
    public void mouseDragged(MouseEvent e) {
        // TODO - drag on sequence selects a range (click selects one residue)

        int mouseX = e.getX();
        int mouseY = e.getY();

        // Compute mouse position relative to viewport
        int mouseViewportX = mouseX - viewportLeftPixel();

        // Drag on numbers drags sequence. (maybe numbers section needs to be bigger?)
        if (mousePressedInNumberArea || mousePressedInSequenceArea) {
            residueActionBroadcaster.lubricateUserInteraction();

            // Apply overdrag logic
            // When the user drags farther than the sequence will go, 
            //  going back in the other direction should not move things until
            //  the mouse is back where the last motion occured
            int delta = mousePressedViewportX - mouseViewportX;            
            int pixel = mousePressedBarCenter + delta;
            
            JScrollBar bar = parent.getHorizontalScrollBar();
            int oldPixel = bar.getValue();
            if (pixel < bar.getMinimum()) pixel = bar.getMinimum();
            else if (pixel > bar.getMaximum()) pixel = bar.getMaximum(); 
            if (pixel != oldPixel) {
                bar.setValue(pixel);
            }
            // repaint happens automatically in response to scroll change, if any
        }  
    }

    int viewportLeftPixel() {
        return (int) parent.getViewport().getViewRect().getMinX();
    }

    boolean mouseIsInSequenceArea(MouseEvent e) {
        // Is the mouse in the sequence area?
        int mouseX = e.getX();
        int mouseY = e.getY();
        return ( (mouseY >= (baseLine - symbolHeight)) && 
                 (mouseY <= baseLine) &&
                 (mouseX >= (int) (characterSpacing/2.0)) &&
                 (mouseX <= (int) (characterSpacing/2.0 + (columnCount) * symbolWidth)) 
                );
    }
    
    boolean mouseIsInNumberArea(MouseEvent e) {
        // Is the mouse in the sequence area?
        int mouseX = e.getX();
        int mouseY = e.getY();
        return ( (mouseY > baseLine) &&
                 (mouseY <= numberBaseLine) );
    }
    
    /**
     * Which residue is the mouse over?
     * @param e
     * @return
     */
    Residue mouseResidue(MouseEvent e) {
        int sequenceIndex = (int)( (e.getX() - characterSpacing/2.0)/symbolWidth );
        return (Residue) positionResidues.get(new Integer(sequenceIndex));
    }
    
    /**
     * Which side of the residue is the mouse near?
     * @param e
     * @return
     */
    boolean mouseResidueRightSide(MouseEvent e) {
        double pos = (e.getX() - characterSpacing/2.0)/symbolWidth;
        double remainder = pos - (int) pos;
        if (pos >= 0.5) return true;
        if (pos < 0) return true;
        return false;
    }
    
    // Respond to sequence scroll bar event - redraw
    public void adjustmentValueChanged(AdjustmentEvent e) {
        residueActionBroadcaster.lubricateUserInteraction();
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
        if (positionResidues.containsKey(new Integer(leftPosition)))
            return (Residue) positionResidues.get(new Integer(leftPosition));
        else return null;
    }
}
