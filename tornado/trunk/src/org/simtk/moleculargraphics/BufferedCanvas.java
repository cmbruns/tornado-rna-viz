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
 * Created on May 17, 2005
 *
 */
package org.simtk.moleculargraphics;

// import java.awt.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Dimension;
import javax.swing.*;
import java.awt.event.*;

/** 
 *  
  * @author Christopher Bruns
  * 
  * AWT canvas with double buffering
 */
public class BufferedCanvas 
extends JPanel
implements ComponentListener
{
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
    
//    public void setSize(int width, int height) {
//        super.setSize(width, height);
//        checkOffScreen();
//    }
//    public void setSize(Dimension d) {
//        super.setSize(d);
//        checkOffScreen();
//    }
    
    public void componentResized(ComponentEvent event) {
        System.out.println("resize");
        checkOffScreen();
    }
    public void componentHidden(ComponentEvent event) {}
    public void componentMoved(ComponentEvent event) {}
    public void componentShown(ComponentEvent event) {}
}
