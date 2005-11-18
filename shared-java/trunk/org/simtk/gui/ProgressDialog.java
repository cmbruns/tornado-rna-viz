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
 * Created on Nov 15, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.net.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Shows progress of a process running in the background
 */
public class ProgressDialog extends JDialog implements ActionListener {
    Throbber throbber;
    Date startTime = new Date();
    JLabel timeLabel = new JLabel();
    JButton cancelButton = new JButton("Cancel");
    
    ProgressDialog(String description) {
        startTime = new Date();
        
        initializeThrobber();

        Container parentPanel = getContentPane();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));

        int spacing = 5;

        // Top panel - description and throbber
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
        descriptionPanel.add(Box.createHorizontalStrut(spacing));
        descriptionPanel.add(new JLabel(description));
        descriptionPanel.add(Box.createHorizontalStrut(spacing));
        descriptionPanel.add(throbber);
        descriptionPanel.add(Box.createHorizontalStrut(spacing));

        // Elapsed time panel
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
        timePanel.add(Box.createHorizontalStrut(spacing));
        timePanel.add(new JLabel("Elapsed time: "));
        timePanel.add(timeLabel);
        timePanel.add(Box.createHorizontalGlue());
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(Box.createHorizontalGlue()); // Move buttons to right edge
        buttonPanel.add(cancelButton);
        cancelButton.addActionListener(this);
        
        parentPanel.add(descriptionPanel);
        parentPanel.add(timePanel);

        pack();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == cancelButton) {
            // TODO
        }
    }
    
    private void initializeThrobber() {
        // Get images for throbber
        Vector throbberIcons = new Vector();
        String[] throbberImageNames = {
                "resources/images/throbber/ThrobberSmall000.png",
                "resources/images/throbber/ThrobberSmall030.png",
                "resources/images/throbber/ThrobberSmall060.png",
                "resources/images/throbber/ThrobberSmall090.png",
                "resources/images/throbber/ThrobberSmall120.png",
                "resources/images/throbber/ThrobberSmall150.png",
                "resources/images/throbber/ThrobberSmall180.png",
                "resources/images/throbber/ThrobberSmall210.png",
                "resources/images/throbber/ThrobberSmall240.png",
                "resources/images/throbber/ThrobberSmall270.png",
                "resources/images/throbber/ThrobberSmall300.png",
                "resources/images/throbber/ThrobberSmall330.png"
        };
        for (int i = 0; i < throbberImageNames.length; i++) {
            String fileName = throbberImageNames[i];
            URL imageURL = getClass().getClassLoader().getResource(fileName);
            if (imageURL != null) {
                Icon icon = new ImageIcon(imageURL);
                if (icon != null) throbberIcons.add(icon);
            }
        }
        if (throbberIcons.size() > 0) {
            throbber = new Throbber(throbberIcons.toArray());
        }
        else throbber = new Throbber(null);
    }
    
    private void updateTimeLabel() {
        long milliseconds = (new Date()).getTime() - startTime.getTime();
        int seconds = (int)(milliseconds / 1000);
        int minutes = seconds/60;
        int hours = minutes/60;
        
        seconds = seconds - (60 * minutes);
        minutes = minutes - (60 * hours);

        String timeString = "";
        if (hours < 10) timeString += "0";
        timeString += hours + ":";
        if (minutes < 10) timeString += "0";
        timeString += minutes + ":";
        if (seconds < 10) timeString += "0";
        timeString += seconds;
        
        timeLabel.setText(timeString);
        timeLabel.repaint();
    }
    
    public void updateState() {
        // increment throbber
        throbber.increment();
        // increment time
        updateTimeLabel();
    }

    static final long serialVersionUID = 01L;
}
