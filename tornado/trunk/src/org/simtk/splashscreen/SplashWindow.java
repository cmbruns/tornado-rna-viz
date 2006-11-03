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
 * Created on Sep 11, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.splashscreen;

import java.net.URL;
import java.awt.*;
import java.awt.event.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Singleton class for fast-loading splash screen.
  * Based on description and code from 
  * http://www.randelshofer.ch/oop/javasplash/javasplash.html
 */
public class SplashWindow extends Window {
    private static SplashWindow instance;
    private static Image image;

    private SplashWindow(Frame parent, Image image) {
        super(parent);
        this.image = image;

        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image,0);
        try {
            mt.waitForID(0);
        } catch(InterruptedException ie){}

        // Users shall be able to close the splash window by
        // clicking on its display area. This mouse listener
        // listens for mouse clicks and disposes the splash window.
        MouseAdapter disposeOnClick = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                // Note: To avoid that method splash hangs, we
                // must set paintCalled to true and call notifyAll.
                // This is necessary because the mouse click may
                // occur before the contents of the window
                // has been painted.
                synchronized(SplashWindow.this) {
                    SplashWindow.this.paintCalled = true;
                    SplashWindow.this.notifyAll();
                }
                dispose();
            }
        };
        addMouseListener(disposeOnClick);

        setSize(image.getWidth(null), image.getHeight(null));
        setLocationRelativeTo(null);
    }

    
    public static void splash(URL imageURL) {
        if (imageURL != null) {
            splash(Toolkit.getDefaultToolkit().createImage(imageURL));
        }
    }

    public static void splash(Image image) {
        if (instance == null && image != null) {
            Frame f = new Frame();

            instance = new SplashWindow(f, image);
            instance.setVisible(true);

            if (! EventQueue.isDispatchThread()
            && Runtime.getRuntime().availableProcessors() == 1) {

                synchronized (instance) {
                    while (! instance.paintCalled) {
                        try {instance.wait();} 
                        catch (InterruptedException e) {}
                    }
                }
            }
        }
    }
    
    /**
     * Uses reflection to avoid loading many classes before
     * this routine is actually called, allowing rapid splash
     * screen display.
     * @param className
     * @param args
     */
    public static void invokeMain(String className, String[] args) {
        try {
            Class.forName(className)
                .getMethod("main", new Class[] {String[].class})
                .invoke(null, new Object[] {args});
        } catch (Exception e) {
            InternalError error =
                new InternalError("Failed to invoke main method");
            error.initCause(e);
            throw error;
        }
    }
    
    private boolean paintCalled = false;

    // Avoid drawing background color, since image should fill 
    // window.  This avoids flickering.
    public void update(Graphics g) {
        paint(g);
    }
    
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);

        if (! paintCalled) {
            paintCalled = true;
            synchronized (this) { notifyAll(); }
        }
    }
    
    public static void disposeSplash() {
        if (instance != null) {
            instance.getOwner().dispose();
            instance = null;
        }
    }
}
