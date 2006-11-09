/* Copyright (c) 2005 Stanford University and Christopher Bruns
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
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Nov 8, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.simtk.mol.color.BlockComplementaryBaseColorScheme;
import org.simtk.molecularstructure.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Canvas that displays a portion of a molecular sequence
 */
public class SequenceCanvas extends BufferedCanvas implements Observer {
    int numberOfResidues = 0; // number of residues in the sequence
    int minHeight = 10;
    int minWidth = 100;
    
    protected Graphics myGraphics = null; // Notice when Graphics object is available
    JComponent parentContainer = null;

    // Parameters describing character size
    protected Font font;
    double symbolWidth = 25;
    double symbolHeight = 30;
    double characterSpacing = 5; // pixels between symbols
    int baseLine = (int) symbolHeight; // y coordinate of character baseline
    // including little residue numbers below the residues
    protected Font numberFont;
    int numberHeight = 5;
    int numberBaseLine = 5;
    
    // Parameters describing the region of sequence being shown
    private int leftEdgeVirtualPixel = 0;
    
    private Biopolymer sequenceMolecule = null;

    // Track mapping of string positions to residue objects
    protected Map<Residue, Integer> residuePositions = new LinkedHashMap<Residue, Integer>();
    protected Map<Integer, Residue> positionResidues = new LinkedHashMap<Integer, Residue>();
    Vector residueSymbols = new Vector();

    public SequenceCanvas() {
        setBackground(Color.white);
        font = new Font("Monospaced", Font.BOLD, 20);
        numberFont = new Font("SanSerif", Font.PLAIN, 9);
        // setSize(minWidth, minHeight); // This is needed to make the container exist...
    }
    
    private double luminosity(Color c) {
        return 1.0/255.0 * (0.30 * c.getRed() + 0.59 *  c.getGreen() + 0.11 * c.getBlue());
    }
    
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        if (luminosity(c) < 0.5) setForeground(Color.white);
        else setForeground(Color.black);
    }
    
    public void setParentContainer(JComponent parent) {
        parentContainer = parent;
    }

    // This is how to specify sizes for non-swing components
    public Dimension getMaximumSize() {
        Dimension size = new Dimension(getPreferredSize());
        size.width = Short.MAX_VALUE;
        return size;
    }
    
    // This is how to specify sizes for non-swing components
    public Dimension getMinimumSize() {
        Dimension size = new Dimension(getPreferredSize());
        size.width = minWidth;
        return size;
    }
    
    public Dimension getPreferredSize() {
        Dimension size = new Dimension();

        // height
        int desiredHeight = (int) numberBaseLine + 1;
        if (desiredHeight <= minHeight) desiredHeight = minHeight;
        size.height = desiredHeight;

        // width
        int desiredWidth = getTotalSequenceWidth();
        if (desiredWidth <= minWidth) desiredWidth = minWidth;
        size.width = desiredWidth;
        
        return size;
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    void checkSize(Graphics g) {
        boolean haveNewGraphics = false;
        if ( (myGraphics == null) && (g != null) ) haveNewGraphics = true;

        // Set font sizes once, once a Graphics object is available
        // Many important things are initialized in this block
        if (haveNewGraphics) { // we have an actual Graphics object for the first time
            myGraphics = g;
            FontMetrics fm;

            fm = g.getFontMetrics(font);
            symbolHeight = fm.getAscent() + 1;
            symbolWidth = fm.charWidth('W') + characterSpacing;
            baseLine = fm.getAscent();

            fm = g.getFontMetrics(numberFont);
            numberHeight = fm.getAscent();
            numberBaseLine = numberHeight + baseLine;

            // parent.getHorizontalScrollBar().setUnitIncrement((int)symbolWidth);

            // TODO the size is not changing appropriately
            // validate();
            // invalidate();
            // repaint();
            if (parentContainer != null) parentContainer.revalidate();
        }
    }

    public void paint(Graphics onScreenGraphics) {
        checkOffScreen();
        if (offScreenImage == null) return;
        Graphics g = offScreenImage.getGraphics();
        checkSize(g);
        
        // Check bounds of viewport
        // Rectangle viewRect = parent.getViewport().getViewRect();
        // int leftPixel = (int) viewRect.getMinX();
        // int rightPixel = (int) viewRect.getMaxX();
        
        int rightPixel = leftEdgeVirtualPixel + getWidth() - 1;
        
        int leftPosition = (int)((leftEdgeVirtualPixel - characterSpacing/2.0) / symbolWidth);
        int rightPosition = (int)((rightPixel - characterSpacing/2.0) / symbolWidth);
        if (leftPosition < 0) leftPosition = 0;
        if (rightPosition >= numberOfResidues) rightPosition = numberOfResidues - 1;
        
        Dimension dim = getSize();
        
        // Clear background
        g.setColor(getBackground());
        g.fillRect(0, 0, dim.width, dim.height);

        // If there is no sequence, put a default message
        if (numberOfResidues < 1) {
            g.drawString((String) "(no molecule sequence loaded)", 0, baseLine);
        }
        else {
            // Draw sequence
            g.setFont(font);

            for (int r = leftPosition; r <= rightPosition; r++) {
                
                // Set residue color 
                try {
                    Residue res = positionResidues.get(r);
                    Color resColor = BlockComplementaryBaseColorScheme.SCHEME.colorOf(res);
                    g.setColor(resColor);
                } catch (org.simtk.mol.color.UnknownObjectColorException exc) {
                    g.setColor(getForeground());
                }
                
                g.drawString((String) residueSymbols.get(r), positionPixel(r), baseLine);
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
                            (r == (numberOfResidues - 1)) // show final residue number
                    ) {
                        // Don't number the ones right next to the numbered ends
                        if ( (r==1) || (r == (numberOfResidues - 2)) ) continue;
                        g.drawString("" + residueNumber, positionPixel(r), numberBaseLine);
                    }
                }
                
            }
        }

        // Copy offscreen image onto screen
        // int height = getSize().height;
        // int width = rightPixel - leftEdgeVirtualPixel + 1;
        onScreenGraphics.drawImage(offScreenImage, 
                0, 0, 
                getWidth(), getHeight(),
                0, 0,
                getWidth(), getHeight(),
                null);
    }
    
    private int positionPixel(int pos) {
        return (int) (characterSpacing + pos * symbolWidth - leftEdgeVirtualPixel);
    }
    
    public void update(Observable observable, Object object) {
        if (!(observable instanceof Biopolymer)) return;
        // TODO respond to molecule change, but only if it's a sequence change
    }

    public Biopolymer getMolecule() {
        return sequenceMolecule;
    }

    public void setMolecule(Biopolymer molecule) {
        if (molecule == sequenceMolecule) return; // no change

        // Remove interest in old molecule
        // if (sequenceMolecule != null) sequenceMolecule.deleteObserver(this);

        // Change molecules
        sequenceMolecule = molecule;
        // Register for updates
        // sequenceMolecule.addObserver(this);

        // Import sequence
        numberOfResidues = 0;
        residuePositions.clear();
        positionResidues.clear();
        residueSymbols.clear();
        for (Iterator iterResidue = molecule.residues().iterator(); iterResidue.hasNext();) {
            Residue residue = (Residue) iterResidue.next();
            Integer indexInteger = new Integer(numberOfResidues);
            residuePositions.put(residue, indexInteger);
            positionResidues.put(indexInteger, residue);
            residueSymbols.add("" + residue.getOneLetterCode());
            numberOfResidues ++;        
        }
        
        if (parentContainer != null) parentContainer.revalidate();
        // repaint();
    }
    
    /**
     * Get width of entire sequence, not just the visible canvas
     * @return width of entire sequence in pixels
     */
    public int getTotalSequenceWidth() {
        return (int)(2 * characterSpacing + numberOfResidues * symbolWidth);
    }

    public int getResidueWidth() {
        return (int) symbolWidth;
    }
    
    public void setLeftEdgePixel(int p) {
        if (p != leftEdgeVirtualPixel) {
            leftEdgeVirtualPixel = p;
            repaint();
        }
    }
    
    public int getLeftEdgePixel() {return leftEdgeVirtualPixel;}
    public int getRightEdgePixel() {
        Rectangle viewRect = getVisibleRect();
        
        int answer = leftEdgeVirtualPixel + viewRect.width - 1;
        return answer;
    }
    
    static final long serialVersionUID = 01L;
}
