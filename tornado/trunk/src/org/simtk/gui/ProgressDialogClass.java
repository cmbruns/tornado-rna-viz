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
import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Shows progress of a process running in the background
 */
public class ProgressDialogClass extends JDialog implements ProgressDialog {
    ProgressPanel progressPanel;

    ProgressDialogClass(JFrame frame, String description) {
        super(frame, description);
        progressPanel = new ProgressPanel(description);
        getContentPane().add(progressPanel);
        pack();
    }
    
    public ProgressPanel getProgressPanel() {return progressPanel;}
    
    // Delegate ProgressDialog interface to ProgressPanel
    public boolean isCancelled() {return progressPanel.isCancelled();}
    public void setCancelled(boolean isCancelled) {progressPanel.setCancelled(isCancelled);}
    public void setStartTime(Date startTime) {progressPanel.setStartTime(startTime);}
    public void updateState() {progressPanel.updateState();}
    
    static final long serialVersionUID = 01L;
}
