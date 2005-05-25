/*
 * Created on May 17, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;

/** 
 *  
  * @author Christopher Bruns
  * 
  * AWT canvas with double buffering
 */
public class BufferedCanvas extends Canvas {
    public static final long serialVersionUID = 1L;
    
    Image offScreenImage;
    int width = -1;
    int height = -1;
    
    public void update(Graphics g) {
        checkOffScreen();
        if (offScreenImage == null) return;
        paint(offScreenImage.getGraphics());
        g.drawImage(offScreenImage, 0, 0, null);
    }
    
    public void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0,0,width,height);
        g.setColor(getForeground());
    }
    
    void checkOffScreen() {
        Dimension d = getSize();
        if ( (offScreenImage == null) ||
             (width != d.width) ||
             (height != d.height) ) {
            height = d.height;
            width = d.width;
            if ( (width > 0) && (height > 0) )
                offScreenImage = createImage(width, height);
            else offScreenImage = null;
        }
    }
    
    public void setSize(int width, int height) {
        super.setSize(width, height);
        checkOffScreen();
    }
    public void setSize(Dimension d) {
        super.setSize(d);
        checkOffScreen();
    }
}
