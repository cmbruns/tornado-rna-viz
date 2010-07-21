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
 * Created on Jul 27, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.pdb;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class JLinkLabel extends JLabel {

    final Color COLOR_NORMAL = Color.BLUE;
    final Color COLOR_HOVER = Color.RED;
    final Color COLOR_ACTIVE = COLOR_NORMAL;
    final Color COLOR_BG_NORMAL = Color.LIGHT_GRAY;
    final Color COLOR_BG_ACTIVE = Color.LIGHT_GRAY;

    Color mouseOutDefault;

    public JLinkLabel(String text) {
        super(text);
        initialize();
    }

    public JLinkLabel() {
        initialize();
    }
    
    private void initialize() {
        addMouseListener();
        setForeground(COLOR_NORMAL);
        setBackground(COLOR_BG_NORMAL);
        mouseOutDefault = COLOR_NORMAL;
        this.setSize(
                (int)this.getPreferredSize().getWidth(), 
                (int)this.getPreferredSize().getHeight());
        // this.setOpaque(true);
    }
    
    protected void respondToClick() {
        System.out.println("Click...");
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.drawLine(2, getHeight() - 1, (int) getPreferredSize().getWidth() - 2,
                getHeight() - 1);
    }

    public void addMouseListener() {

        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent me) {
                setForeground(COLOR_ACTIVE);
                mouseOutDefault = COLOR_ACTIVE;
                // do something here
                respondToClick();
            }

            public void mouseReleased(MouseEvent me) {}

            public void mousePressed(MouseEvent me) {
                mouseOutDefault = COLOR_ACTIVE;
            }

            public void mouseEntered(MouseEvent me) {
                setForeground(COLOR_HOVER);
                setBackground(COLOR_BG_ACTIVE);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent me) {
                setForeground(mouseOutDefault);
                setBackground(COLOR_BG_NORMAL);
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

}