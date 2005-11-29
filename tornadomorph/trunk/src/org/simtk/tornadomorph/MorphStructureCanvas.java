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
 * Created on Nov 3, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.tornadomorph;

import org.simtk.moleculargraphics.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MorphStructureCanvas extends StructureCanvas 
{
    HashSet mouseSlaves = new HashSet();

    MorphStructureCanvas() {
        super();
        setBackgroundColor(Color.WHITE);
    }

    /**
     * Repeat mouse events in this panel in another panel
     *
     */
    void addMouseSlavePanel(MorphStructureCanvas p) {
        mouseSlaves.add(p);
    }
    void removeMouseSlavePanel(MorphStructureCanvas p) {
        mouseSlaves.remove(p);
    }
    
    /**
     * Send mouse events from this canvas to other canvases that ask for it
     * @param event
     */
    public void superMouseDragged(MouseEvent event) {
        super.mouseDragged(event);
    }    
    public void mouseDragged(MouseEvent event) {
        superMouseDragged(event);
        for (Iterator i = mouseSlaves.iterator(); i.hasNext();) {
            MorphStructureCanvas otherCanvas = (MorphStructureCanvas) i.next();
            if (otherCanvas != this)
                otherCanvas.superMouseDragged(event);
        }
    }
    
    public void superMousePressed(MouseEvent event) {
        super.mousePressed(event);
    }    
    public void mousePressed(MouseEvent event) {
        superMousePressed(event);
        for (Iterator i = mouseSlaves.iterator(); i.hasNext();) {
            MorphStructureCanvas otherCanvas = (MorphStructureCanvas) i.next();
            if (otherCanvas != this)
                otherCanvas.superMousePressed(event);
        }
    }    
    
    public void superMouseWheelMoved(MouseWheelEvent event) {
        super.mouseWheelMoved(event);
    }    
    public void mouseWheelMoved(MouseWheelEvent event) {
        superMouseWheelMoved(event);
        for (Iterator i = mouseSlaves.iterator(); i.hasNext();) {
            MorphStructureCanvas otherCanvas = (MorphStructureCanvas) i.next();
            if (otherCanvas != this)
                otherCanvas.superMouseWheelMoved(event);
        }
    }    
    
    static final long serialVersionUID = 01L;
}
