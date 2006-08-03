/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Jul 31, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.toon.secstruct;

import org.simtk.moleculargraphics.*;
import org.simtk.molecularstructure.*;
import java.io.*;
import org.simtk.util.Selectable;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import org.simtk.moleculargraphics.cartoon.BoundingBox;

public class SecondaryStructureCanvas 
extends JPanel
implements ResidueHighlightListener, MouseWheelListener, MouseMotionListener, MouseListener

{
    protected ResidueHighlightBroadcaster residueHighlightBroadcaster;
    protected Set<Base> bases = new HashSet<Base>();
    protected BoundingBox boundingBox = null;
    protected double heightScale = 100;  // image units per screen height
    protected double centerX = 50;
    protected double centerY = 50;

    protected java.util.List<Font> fonts = new Vector<Font>();
    // fontHeights is initialized in the paint() method
    protected java.util.Map<Font, Double> fontHeights = null;

    protected double baseSpacing = 4.0; // TODO - measure this
    
    public SecondaryStructureCanvas(ResidueHighlightBroadcaster r) {
        this.residueHighlightBroadcaster = r;
        setBackground(Color.white);
        
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        
        int fontStyle = Font.BOLD;
        String fontType = "Courier";
        
        fonts.add(new Font(fontType, fontStyle,  5));
        fonts.add(new Font(fontType, fontStyle,  8));
        fonts.add(new Font(fontType, fontStyle,  12));
        fonts.add(new Font(fontType, fontStyle,  18));
        fonts.add(new Font(fontType, fontStyle,  24));
        fonts.add(new Font(fontType, fontStyle,  36));
        fonts.add(new Font(fontType, fontStyle,  48));
        fonts.add(new Font(fontType, fontStyle,  72));
        fonts.add(new Font(fontType, fontStyle,  96));
        fonts.add(new Font(fontType, fontStyle,  120));
    }
    
    protected void loadSStructViewFile(InputStream inputStream) 
    throws IOException 
    {
        LineNumberReader reader = 
            new LineNumberReader(new InputStreamReader(inputStream));

        bases.clear();
        String fileLine;
        FILE_LINE: while ((fileLine = reader.readLine()) != null) {
            String[] result = fileLine.split("\\s");
            if (result.length < 1) continue;
            if (result[0].equals("BASE")) {
                int resNum = new Integer(result[1]);
                String baseChar = result[2];
                int x = new Integer(result[3]);
                int y = new Integer(result[4]);
                
                bases.add(new Base(baseChar, resNum, x, y));
                
                double[] bounds = {x,x,y,y+20,0,0};
                if (boundingBox == null) boundingBox = new BoundingBox(bounds);
                else boundingBox.add(new BoundingBox(bounds));
            }
            
            // Scale based on height
            // image units per screen height
            heightScale = boundingBox.yMax - boundingBox.yMin + 1;
            centerX = 0.5 * (boundingBox.xMax - boundingBox.xMin);
            centerY = 0.5 * (boundingBox.yMax - boundingBox.yMin);
        }
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(getForeground());
        
        // Scale by zoom
        double height = getSize().height;
        double width = getSize().width;
        double scale = height / heightScale;
        double screenCenterX = width / 2.0;
        double screenCenterY = height / 2.0;
        double spacing = baseSpacing * scale;
        
        // Just-in-time creation of font size lookup
        // Compute font heights only the first time they are needed
        if (fontHeights == null) {
            fontHeights = new HashMap<Font, Double>();
            FontRenderContext frc = g2.getFontRenderContext();
            for (Font font : fonts) {
                fontHeights.put(font, (double) font.getLineMetrics("ACGT", frc).getHeight());
            }
        }
        
        // Choose a font
        Font font = null;
        // Is the smallest font too big?
        if (fontHeights.get(fonts.get(0)) > spacing) 
            font = null;
        // Is the largest font too small? -- use it anyway
        else if (fontHeights.get(fonts.get(fonts.size() - 1)) < spacing) 
            font = fonts.get(fonts.size() - 1);
        // Choose a font based on scale
        else for (Font f : fonts) { 
            font = f;
            if (fontHeights.get(f) > spacing * 1.0) break; // font is big enough
        }
        if (font != null) g.setFont(font);
        
        for (Base base : bases) {
            int x = (int)(screenCenterX + scale * (base.getPosX() - centerX));
            int y = (int)(screenCenterY - scale * (base.getPosY() - centerY));

            if (x < 0 ) continue;
            if (y < 0 ) continue;
            if (x > getSize().width) continue;
            if (y > getSize().height) continue;
            
            // Draw a little square if the image is too zoomed out to draw characters
            if (font == null) {
                if (spacing > 3) g.fillRect(x,y,2,2);
                else g.fillRect(x,y,1,1);
            }
            else g.drawString(base.getType(), x, y);
        }
    }
    
    public void highlightResidue(Residue r, Color c) {}
    public void unhighlightResidue(Residue r) {}
    public void unhighlightResidues() {}

    Dimension maxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Dimension minSize = new Dimension(10,10);
    Dimension prefSize = new Dimension(640,480);
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
    
    protected void zoom(double zoomFactor) {
        heightScale = heightScale * zoomFactor;
        repaint();
    }
    
    protected void translate(double transX, double transY) {
        double scale = getSize().height / heightScale;
        centerX -= transX / scale;
        centerY -= transY / scale;
        repaint();
    }
    
    // MouseListener interface
    int oldMouseX = 0;
    int oldMouseY = 0;
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {
        oldMouseX = event.getX();
        oldMouseY = event.getY();
    }
    public void mouseReleased(MouseEvent event) {}
    public void mouseClicked(MouseEvent event) {}
    
    // MouseWheelListener
    public void mouseWheelMoved(MouseWheelEvent event) {
        zoom(Math.pow(1.30, event.getWheelRotation()));
    }
    
    // MouseMotionListener
    public void mouseMoved(MouseEvent event) {}
    public void mouseDragged(MouseEvent event) {
        int deltaX = event.getX() - oldMouseX;
        int deltaY = oldMouseY - event.getY();
        
        translate(deltaX, deltaY);
        
        oldMouseX = event.getX();
        oldMouseY = event.getY();
    }
}

class Base {
    protected String baseType;
    protected int resNum;
    protected double posX;
    protected double posY;
    
    Base(String baseType, int resNum, double posX, double posY) {
        this.baseType = baseType;
        this.resNum = resNum;
        this.posX = posX;
        this.posY = posY;
    }
    
    public String getType() {return baseType;}
    public int getResNum() {return resNum;}
    public double getPosX() {return posX;}
    public double getPosY() {return posY;}
    
    public String toString() {
        return ""+getType()+getResNum()+" ("+getPosX()+","+getPosY()+")";
    }
}
