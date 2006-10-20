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
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.ResidueClass;
import org.simtk.geometry3d.*;
import java.io.*;
import java.util.*;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Cursor;
import java.awt.BasicStroke;
import java.awt.geom.*;
import java.awt.RenderingHints;

import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import org.simtk.moleculargraphics.cartoon.BoundingBox;

public class SecondaryStructureCanvas 
extends JPanel
implements ResidueHighlightListener, MouseWheelListener, 
MouseMotionListener, MouseListener, ComponentListener
{
    protected ResidueHighlightBroadcaster residueHighlightBroadcaster;
    // protected java.util.List<BasePosition> bases = new Vector<BasePosition>();
    // protected double heightScale = 100;  // image units per screen height
    // protected double centerX = 50;
    // protected double centerY = 50;
    protected double characterDensity = 1.0;

    protected java.util.List<Font> fonts = new Vector<Font>();
    // fontHeights is initialized in the paint() method
    protected java.util.Map<Font, Double> fontHeights = null;
    protected java.util.Map<Font, Double> fontWidths = null;

    // protected double baseSpacing = 4.0; // TODO - measure this
    
    protected SecondaryStructureDiagram diagram = null;
    protected Transform2D transform = null;
    
    protected Set<BasePair> worstBasePairs;
    
    public SecondaryStructureCanvas(ResidueHighlightBroadcaster r) {
        this.residueHighlightBroadcaster = r;
        setBackground(Color.white);
        
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        addComponentListener(this);
        
        int fontStyle = Font.BOLD;
        String fontType = "Courier";
        
        fonts.add(new Font(fontType, fontStyle,  2));
        fonts.add(new Font(fontType, fontStyle,  3));
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
        fonts.add(new Font(fontType, fontStyle,  200));
        fonts.add(new Font(fontType, fontStyle,  300));
        fonts.add(new Font(fontType, fontStyle,  500));
        fonts.add(new Font(fontType, fontStyle,  800));
    }
    
    public void componentHidden(ComponentEvent event) {}
    public void componentShown(ComponentEvent event) {}
    public void componentMoved(ComponentEvent event) {}

    private double previousHeight = 0;
    private boolean finiteHeightWasSeen = false;
    public void componentResized(ComponentEvent event) {
        double height = getHeight();
        
        // The first time a size is seen, scale diagram to the window
        if ( (height > 0) && !finiteHeightWasSeen) {
            finiteHeightWasSeen = true;
            if (diagram != null) {
                BoundingBox boundingBox = diagram.getBoundingBox();
                transformToBoundingBox(boundingBox);
                // repaint();
            }
        }
        
        if (height != previousHeight) {
            if (previousHeight > 0) {
                transform.zoom(height / previousHeight);
                repaint();
            }            
            previousHeight = height;
        }
    }
    
    public void setDiagram(SecondaryStructureDiagram diagram) {
        this.diagram = diagram;

        BoundingBox boundingBox = diagram.getBoundingBox();
        transformToBoundingBox(boundingBox);
        
        // TODO - worst base pairs are for testing only
        Set<BasePair> allBasePairs = new HashSet<BasePair>();
        for (BasePairPosition bpp : diagram.basePairPositions()) {
            BasePair bp = 
                new BasePair(bpp.position1.getResidue(), bpp.position2.getResidue());
            allBasePairs.add(bp);
        }
        worstBasePairs = SecondaryStructureDiagramClass.findWorstPseudoknotBasePairs(allBasePairs);
        
        repaint();
    }
    
    protected void transformToBoundingBox(BoundingBox boundingBox) {
        // Scale based on height
        // image units per screen height
        double heightScale = boundingBox.yMax - boundingBox.yMin + 1;
        double scale = 200 / heightScale;
        if (getHeight() > 0) {
            scale = getHeight()/heightScale; 
            // System.out.println("Scale = "+scale);
        }
        
        double centerX = boundingBox.xMin + 0.5 * (boundingBox.xMax - boundingBox.xMin);
        double centerY = boundingBox.yMin + 0.5 * (boundingBox.yMax - boundingBox.yMin);

        transform = new Transform2D(centerX, centerY, scale);        
    }
   
    protected void loadSStructViewFile(InputStream inputStream) 
    throws IOException 
    {
        LineNumberReader reader = 
            new LineNumberReader(new InputStreamReader(inputStream));

        diagram = new SecondaryStructureDiagramClass();
        String fileLine;
        FILE_LINE: while ((fileLine = reader.readLine()) != null) {
            String[] result = fileLine.split("\\s");
            if (result.length < 1) continue;
            if (result[0].equals("BASE")) {
                int resNum = new Integer(result[1]);
                String baseChar = result[2];
                int x = new Integer(result[3]);
                int y = new Integer(result[4]);
                
                Residue residue = new ResidueClass(ResidueTypeClass.getType(baseChar));
                residue.setResidueNumber(resNum);
                diagram.basePositions().add(new BasePosition(residue, new Vector2DClass(x,y)));
            }
        }
        setDiagram(diagram);
    }

    public void paint(Graphics g) {
        if (diagram == null) return;
        if (transform == null) return;
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(),getHeight());
        
        // Scale by zoom
        double height = getSize().height;
        double width = getSize().width;
        // double scale = height / heightScale;
        double screenCenterX = width * 0.5;
        double screenCenterY = height * 0.5;
        double spacing = diagram.getConsecutiveBaseDistance() * transform.scale;
        
        // Just-in-time creation of font size lookup
        // Compute font heights only the first time they are needed
        if (fontHeights == null) {
            fontHeights = new HashMap<Font, Double>();
            fontWidths = new HashMap<Font, Double>();
            FontRenderContext frc = g2.getFontRenderContext();
            for (Font font : fonts) {
                fontHeights.put(font, (double) font.getLineMetrics("ACGT", frc).getHeight());
                fontWidths.put(font, (double) g.getFontMetrics(font).charWidth('G'));
            }
        }
        
        // Choose a font
        Font font = null;
        // Font majorTickFont = null;
        // Font minorTickFont = null;

        // Is the smallest font too big?
        if (fontHeights.get(fonts.get(0)) > spacing) 
            font = null;
        // Is the largest font too small? -- use it anyway
        else if (fontHeights.get(fonts.get(fonts.size() - 1)) < spacing) 
            font = fonts.get(fonts.size() - 1);
        // Choose a font based on scale
        else for (Font f : fonts) { 
            font = f;
            if (fontHeights.get(f) > spacing * characterDensity) break; // font is big enough
        }
        
        // Maximum space occupied by basePosition, for clipping
        double maxClipH = diagram.getConsecutiveBaseDistance() * transform.scale;
        double maxClipW = diagram.getConsecutiveBaseDistance() * transform.scale;
        
        // Letter size
        int maxFontH = 20;
        int maxFontW = 20;
        if (font != null) {
            g.setFont(font);
            maxFontH = (int)(fontHeights.get(font) + 1);
            maxFontW = (int)(fontWidths.get(font) + 1);
        }

        // 1) Draw polygon for RNA backbone
        g.setColor(Color.lightGray);
        
        boolean doDrawLetters = true;
        if (maxFontH < 1.5) doDrawLetters = false;
        if (font == null) doDrawLetters = false;
        
        // line width
        float lineWidth = (int)(1.0 + maxClipH * 0.1);
        if ( (g instanceof Graphics2D) && (lineWidth > 1.5) )
            ((Graphics2D)g).setStroke(new BasicStroke(lineWidth));
        
        // gap between line segments, to make room for characters
        // Specify the gap length rather than the segment length, because
        // there may be occasional consecutive bases with very large distances,
        // where we want a long segment with a still short gap at the end.
        double segmentGapLength = 0.4 * (maxFontW + maxFontH); 
        if (! doDrawLetters) segmentGapLength = 0;
        
        BasePosition previousBase = null;
        for (BasePosition base : diagram.basePositions()) {
            double x = (int)(screenCenterX + transform.x(base.getX()));
            double y = (int)(screenCenterY + transform.y(base.getY()));

            // Clipping
            boolean isOnScreen = true;
            if (x < -maxClipW) isOnScreen = false;
            if (y < -maxClipH) isOnScreen = false;
            if (x > getSize().width + maxClipW) isOnScreen = false;
            if (y > getSize().height + maxClipH) isOnScreen = false;
            
            if (isOnScreen) {
                // Draw connector between consecutive bases
                if (previousBase != null) {
                    double prevX = (int)(screenCenterX + transform.x(previousBase.getX()));
                    double prevY = (int)(screenCenterY + transform.y(previousBase.getY()));

                    double deltaX = x - prevX;
                    double deltaY = y - prevY;
                    
                    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    
                    // Draw nothing if the segment is shorter than its end gaps
                    if (segmentGapLength < length) {
                        double gapX = 0.5 * segmentGapLength * deltaX / length;
                        double gapY = 0.5 * segmentGapLength * deltaY / length;
                        g.drawLine((int)(x - gapX), (int)(y - gapY), 
                                (int)(prevX + gapX), (int)(prevY + gapY));                        
                    }                    
                }            
            }            
            previousBase = base;
        }

        // 2) Draw lines for base pair connections
        g.setColor(Color.RED);

        // line width
        lineWidth = (int)(1.0 + maxClipH * 0.05);
        if ( (g instanceof Graphics2D) && (lineWidth > 1.5) )
            ((Graphics2D)g).setStroke(new BasicStroke(lineWidth));

        for (BasePairPosition pair : diagram.basePairPositions()) {
            
            BasePair basePair = new BasePair(pair.position1.getResidue(), pair.position2.getResidue());
            if ( (worstBasePairs != null) && (worstBasePairs.contains(basePair)) )
                g.setColor(Color.RED);
            else 
                g.setColor(Color.BLUE);
            
            if (pair.straightLine) {
            // if (true) {
                double x1 = screenCenterX + transform.x(pair.position1.getX());
                double y1 = screenCenterY + transform.y(pair.position1.getY());
                double x2 = screenCenterX + transform.x(pair.position2.getX());
                double y2 = screenCenterY + transform.y(pair.position2.getY());

                g.drawLine((int)(x1), (int)(y1), 
                        (int)(x2), (int)(y2));
            }
            else { // Arc
                
                double x = screenCenterX + transform.x(pair.arcX);
                double y = screenCenterY + transform.y(pair.arcY);
                double w = transform.scale * pair.arcWidth;
                double h = transform.scale * pair.arcHeight;
                double start = pair.arcStart;
                double range = pair.arcRange;
                
                double radius = 0.5 * w;
                double arcGap = 0.5 * (segmentGapLength / radius) * (180.0 / Math.PI);
                
                Arc2D arc = new Arc2D.Double(x, y, w, h, start - arcGap, range + 2 * arcGap, Arc2D.OPEN);
                
                g2.draw(arc);
                // g.drawArc(x, y, w, h, start, range);
                
                // System.out.println(""+ x + "," + y + "," + w + "," + h + "," + start + "," + range);
                // System.out.println("  "+ pair.arcX + "," + pair.arcY + "," + pair.arcWidth + "," + pair.arcHeight + "," + pair.arcStart + "," + pair.arcRange);
            }
        }
        
        // 3) Draw residue numbers
        g.setColor(getForeground());
        double gap = 0.5 * diagram.getConsecutiveBaseDistance();
        FontRenderContext frc = g2.getFontRenderContext();
        Vector2D screenCenter = new Vector2DClass(screenCenterX, screenCenterY);
        
        // Major ticks
        for (NumberTick tick : diagram.majorTicks()) {
            if (spacing < 0.1) break;
            
            Vector2D direction = tick.labelDirection;
            
            Vector2D tickStart = screenCenter.plus(
                transform.transform(tick.basePosition.position.plus(direction.times(1.2 * gap))) );
            Vector2D tickEnd = screenCenter.plus(
                    transform.transform(tick.basePosition.position.plus(direction.times(2.2 * gap))) );
            Vector2D labelStart = screenCenter.plus(
                    transform.transform(tick.basePosition.position.plus(direction.times(3.2 * gap))) );

            Line2D segment = new Line2D.Double(
                    tickStart.x(), tickStart.y(),
                    tickEnd.x(), tickEnd.y());
            
            g2.draw(segment);
            
            if (font == null) continue;

            Vector2D labelPos = getLabelCorner(g2, tick.label, labelStart, direction);                        
            g.drawString(tick.label, (int)labelPos.x(), (int)labelPos.y());
        }
        
        // Minor ticks
        for (NumberTick tick : diagram.minorTicks()) {
            if (spacing < 1.0) break;

            Vector2D direction = tick.labelDirection;
            
            Vector2D tickStart = screenCenter.plus(
                transform.transform(tick.basePosition.position.plus(direction.times(1.2 * gap))) );
            Vector2D tickEnd = screenCenter.plus(
                    transform.transform(tick.basePosition.position.plus(direction.times(2.2 * gap))) );
            Vector2D labelStart = screenCenter.plus(
                    transform.transform(tick.basePosition.position.plus(direction.times(3.2 * gap))) );

            Line2D segment = new Line2D.Double(
                    tickStart.x(), tickStart.y(),
                    tickEnd.x(), tickEnd.y());
            
            g2.draw(segment);

            if (font == null) continue;
            Vector2D labelPos = getLabelCorner(g2, tick.label, labelStart, direction);                        
            g.drawString(tick.label, (int)labelPos.x(), (int)labelPos.y());
        }
        
        // 4) Draw characters on top of polygon
        // Font size
        
        // No characters, only polygon, below a certain size
        if (doDrawLetters) {
    
            g.setColor(getForeground());
            for (BasePosition base : diagram.basePositions()) {
                double x = screenCenterX + transform.x(base.getX());
                double y = screenCenterY + transform.y(base.getY());
    
                // Clipping
                if (x < -maxClipW) continue;
                if (y < -maxClipH) continue;
                if (x > getSize().width + maxClipW) continue;
                if (y > getSize().height + maxClipH) continue;
                
                // Draw a little square if the image is too zoomed out to draw characters
                if (font == null) {
                    if (spacing > 2.5) g.fillRect((int)x,(int)y,2,2);
                    else g.fillRect((int)x,(int)y,1,1);
                }
                else { // Draw a one letter code character
                    // Center the character on the basePosition
                    g.drawString(""+base.getResidue().getOneLetterCode(), 
                            (int)(x - (maxFontW/2.0)), (int)(y + (maxFontH/4.5)));
                }
            }
        }
    }
    
    /**
     * 
     * @param g2 Graphics context
     * @param label The text of the label
     * @param position The known position of one edge of the label
     * @param direction The general direction that the label should extend from the known edge
     * @return position for lower left corner of label as used by Graphics.drawString()
     */
    protected Vector2D getLabelCorner(Graphics2D g2, String label, Vector2D position, Vector2D direction) {
        FontRenderContext frc = g2.getFontRenderContext();

        Rectangle2D labelBounds =  g2.getFont().getStringBounds(label, frc);
        double labelHeight = labelBounds.getHeight();
        double labelWidth = labelBounds.getWidth();
        
        double x, y;
        
        // Four cases of label placement
        if (direction.x() > 0.707) { // label to right
            x = position.x();
            y = position.y() + labelHeight * 0.25;
        }
        else if (direction.x() < -0.707) { // label to left
            x = position.x() - labelWidth;
            y = position.y() + labelHeight * 0.25;
        }
        else if (direction.y() > 0.707) { // label above
            x = position.x() - labelWidth * 0.5;
            y = position.y();
        }
        else { // label below
            x = position.x() - labelWidth * 0.5;
            y = position.y() + labelHeight * 0.5;
        }
        
        return new Vector2DClass(x, y);
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
        if (transform == null) return;
        transform.zoom(zoomFactor);
        repaint();
    }
    
    protected void translate(double transX, double transY) {
        if (transform == null) return;
        transform.translate(transX, transY);
        repaint();
    }
    
    // MouseListener interface
    int oldMouseX = 0;
    int oldMouseY = 0;
    public void mouseEntered(MouseEvent event) {
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    public void mouseExited(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {
        oldMouseX = event.getX();
        oldMouseY = event.getY();
    }
    public void mouseReleased(MouseEvent event) {}
    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            double screenCenterX = getWidth() * 0.5;
            double screenCenterY = getHeight() * 0.5;
            transform.translate(screenCenterX - event.getX(), event.getY() - screenCenterY);
            repaint();
        }
    }
    
    // MouseWheelListener
    public void mouseWheelMoved(MouseWheelEvent event) {
        zoom(Math.pow(1.15, event.getWheelRotation()));
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

    class Transform2D {
        private double centerX = 0;
        private double centerY = 0;
        private double scale = 1.0;
        private boolean flipY = true;
        
        public Transform2D(double cenX, double cenY, double scale) {
            this.centerX = cenX;
            this.centerY = cenY;
            this.scale = scale;
        }
        
        public double x(double x) {
            return scale * (x + centerX);
        }
        
        public double y(double y) {
            double answer = scale * (y + centerY);
            if (flipY)
                return -answer;
            else
                return answer;
        }
        
        public Vector2D transform(Vector2D v) {
            return new Vector2DClass(x(v.x()), y(v.y()));
        }
        
        public void zoom(double zoomFactor) {
            scale = scale * zoomFactor;
        }
        
        protected void translate(double transX, double transY) {
            centerX += transX / scale;
            if (false)
                centerY -= transY / scale;
            else
                centerY += transY / scale;
        }

    }
}
