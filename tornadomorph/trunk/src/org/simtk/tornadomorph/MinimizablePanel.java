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
 * Created on Dec 1, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.tornadomorph;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Panel that can fold up into a single label
class MinimizablePanel extends BasePanel implements ActionListener {
    JPanel contentPane = new WhitePanel();
    Container togglePane = new WhitePanel();
    JButton toggleButton = new JButton("hide");
    boolean isMaximized = true;
    JLabel panelTitle = new JLabel();
    
    MinimizablePanel(String label) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        panelTitle.setText(label);
        togglePane.add(panelTitle);
        togglePane.add(toggleButton);
        togglePane.add(new WhitePanel()); // to take up space to the right
        toggleButton.addActionListener(this);

        add(togglePane);
        add(contentPane);
    }
    JPanel getContentPane() {
        return contentPane;
    }
    
    // Toggle button depressed
    public void actionPerformed(ActionEvent event) {
        // If the pane is shown, hide it
        if (isMaximized) {
            remove(getContentPane());
            isMaximized = false;
            toggleButton.setLabel("show");
            panelTitle.setEnabled(false);
            revalidate();
            repaint();
        }
        // If the pane is hidden, show it
        else {
            add(getContentPane());
            isMaximized = true;
            toggleButton.setLabel("hide");
            panelTitle.setEnabled(true);
            getContentPane().revalidate();
            revalidate();
            repaint();
        }
    }
    static final long serialVersionUID = 01L;
}
