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
 * Created on Jul 20, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * JPanel containing controls for adjusting camera.
  * Intended to include controls for rotate, translate, and zoom.
 */
public class CameraPanel extends JPanel {
    CameraPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(new ZoomWidget());
        add(new ZoomWidget());
    }
    
    /**
     * Main method for testing the CameraPanel
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.add(new CameraPanel());
        frame.pack();
        frame.setVisible(true);
    }
    
    class ZoomWidget extends JPanel {
        ZoomWidget() {
            setBorder(BorderFactory.createRaisedBevelBorder());
            setLayout(new BoxLayout(ZoomWidget.this, BoxLayout.Y_AXIS));

            ClassLoader classLoader = getClass().getClassLoader();

            ImageIcon zoomInIcon = new ImageIcon(classLoader.getResource("images/UpPlusSmall.png"));
            JButton zoomInButton = new JButton(zoomInIcon);
            zoomInButton.addActionListener(new ZoomInAction());
            add(new CenteredPanel(zoomInButton));
            
            JLabel zoomLabel = new JLabel("Zoom");
            // zoomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(new CenteredPanel(zoomLabel));
            
            ImageIcon zoomOutIcon = new ImageIcon(classLoader.getResource("images/DownMinusSmall.png"));
            JButton zoomOutButton = new JButton(zoomOutIcon);
            zoomOutButton.addActionListener(new ZoomOutAction());
            add(new CenteredPanel(zoomOutButton));            
        }
    }
    
    class CenteredPanel extends JPanel {
        CenteredPanel(JComponent component) {
            setLayout(new BoxLayout(CenteredPanel.this, BoxLayout.X_AXIS)); 
            add(Box.createHorizontalGlue());
            add(component);
            add(Box.createHorizontalGlue());            
        }
    }
    
    class ZoomInAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("Zoom in...");
        }
    }

    class ZoomOutAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("Zoom out...");
        }
    }
}
