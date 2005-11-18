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
 * Created on Nov 17, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.gui;

import javax.swing.*;

public class Throbber extends JLabel {
    private Object[] icons;
    private int iconIndex = 0;
    
    Throbber(Object[] i) {
        icons = i;
        iconIndex = 0;
        setIcon((Icon)icons[iconIndex]);
    }
    
    /**
     * Change image to the next one in the animation sequence
     *
     */
    public void increment() {
        iconIndex ++;
        if (iconIndex >= icons.length) iconIndex = 0;
        setIcon((Icon)icons[iconIndex]);
        repaint();
    }
    
    static final long serialVersionUID = 01L;
}
