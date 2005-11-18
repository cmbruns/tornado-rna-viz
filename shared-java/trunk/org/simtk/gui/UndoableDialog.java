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
 * Created on Nov 10, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.gui;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * This dialog (UndoableDialog) permits the user to choose from a set of parameters.
  * Previous values entered into the dialog are stored, so the user may recall them by
  * pressing the "undo" button.
  * The set of parameters must be encapsulated in a object.
  * For complex parameter sets, it is recommended to override the "equals" method
  * of the parameters object.
 */
public abstract class UndoableDialog extends JDialog implements ActionListener {
    // Store previous choices in an array
    private Vector parameterHistory = new Vector();
    private int currentHistoryPosition = parameterHistory.size() - 1;
    private Object latestAcceptedParameters;

    private JButton undoButton = new JButton("Undo");
    private JButton redoButton = new JButton("Redo");
    private JButton okayButton = new JButton("Okay");
    private JButton cancelButton = new JButton("Cancel");
    
    private JPanel contentPane = new JPanel();
    
    protected UndoableDialog() {
        
        // Set up GUI
        
        // Buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(undoButton);
        buttonPane.add(redoButton);
        buttonPane.add(okayButton);
        buttonPane.add(cancelButton);
        
        undoButton.addActionListener(this);
        redoButton.addActionListener(this);
        okayButton.addActionListener(this);
        cancelButton.addActionListener(this);
        
        Container parentPane = super.getContentPane();
        parentPane.add(contentPane, BorderLayout.CENTER);
        parentPane.add(buttonPane, BorderLayout.SOUTH);

        checkButtons();
        
        pack();
    }

    public void setAcceptedParameters(Object parameters) {
        latestAcceptedParameters = parameters;
        displayNewParameters(parameters);
    }
    
    public Container getContentPane() {
        return contentPane;
    }
    
    // Update the current displayed parameter to a different set of parameters
    protected void displayNewParameters(Object parameters) {
        if ( parameters.equals(getCurrentParameters()) ) return; // No change? => do nothing

        // Put the new parameter into the stack
        currentHistoryPosition ++;
        parameterHistory.insertElementAt(parameters, currentHistoryPosition);

        // If we are only part way into the stack, remove above material
        // Remove old future elements
        while (parameterHistory.size() > (currentHistoryPosition + 1)) {
            // Remove terminal element
            parameterHistory.removeElementAt(parameterHistory.size() - 1); // pop
        }
        checkButtons();
        
        // System.out.println("currentHistoryPosition = " + currentHistoryPosition);
        
        updateParametersDisplay(parameters);
    }
    
    Object getCurrentParameters() {
        if (currentHistoryPosition < 0) return null;
        return parameterHistory.elementAt(currentHistoryPosition);
    }
    
    // Derived classes should override acceptParameters to actually respond to the user's choice
    private void fireAcceptParameters(Object parameters) {
        latestAcceptedParameters = parameters;
        acceptParameters(parameters); // derived class specific action
    }
    
    protected abstract void acceptParameters(Object parameters);
    
    // Derived classes should override updateParametersDisplay to update the display to the current provisional parameters
    /**
     * Update the dialog display to reflect selection of a particular set of parameters.
     * This must be overridden by derived classes.
     */
    protected abstract void updateParametersDisplay(Object parameters);

    public void actionPerformed(ActionEvent event) {

        // Okay button
        if (event.getSource() == okayButton) {
            System.out.println("okay");
            fireAcceptParameters(getCurrentParameters()); // set current parameters in stone
            setVisible(false);
        }

        // Cancel button
        else if (event.getSource() == cancelButton) {
            System.out.println("cancel");
            
            // I'm unsure if this is needed... TODO
            // displayNewParameters(latestAcceptedParameters); // revert display to actual used parameters

            setVisible(false);
        }
        
        else if (event.getSource() == undoButton) {
            System.out.println("undo");

            if (currentHistoryPosition > 0)
                currentHistoryPosition --;
            updateParametersDisplay(getCurrentParameters());

            checkButtons();
            // System.out.println("currentHistoryPosition = " + currentHistoryPosition);
        }

        else if (event.getSource() == redoButton) {
            System.out.println("redo");

            if (currentHistoryPosition < (parameterHistory.size() - 1) )
                currentHistoryPosition ++;
            updateParametersDisplay(getCurrentParameters());

            checkButtons();
            // System.out.println("currentHistoryPosition = " + currentHistoryPosition);
        }
    }
    

    void checkButtons() {
        if (currentHistoryPosition < 1) undoButton.setEnabled(false);
        else undoButton.setEnabled(true);

        if ( currentHistoryPosition < (parameterHistory.size() - 1) ) redoButton.setEnabled(true);
        else redoButton.setEnabled(false);
    }

    static final long serialVersionUID = 01L;
}
