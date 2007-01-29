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
 * Created on Nov 7, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.console;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.text.ParseException;

/**
 * 
  * @author Christopher Bruns
  * 
  * Panel with a command line interface.  Previously entered text
  * and output appear above; the next line of input is entered below.
 */
public abstract class ConsolePanel 
extends JPanel 
implements MouseListener
{    
    private HistoryArea historyText = new HistoryArea();
    private Prompt prompt = new Prompt();
    private CommandField commandLine = new CommandField();

    ConsolePanel() {
        setBackground(Color.white);
        setFont(new Font("Monospaced", Font.PLAIN, 14));

        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JScrollPane historyScrollPane = new JScrollPane(historyText);
        historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(historyScrollPane); // Where output appears        
        
        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));        
        commandPanel.add(prompt);
        commandPanel.add(commandLine);
        
        add(commandPanel); // Where the user types text
        
        addMouseListener(this);
    }
    
    /**
     * Override this method to respond to commands typed by the user.
     * 
     * @param command
     * @throws ParseException
     */
    protected abstract void runCommandString(String command) throws ParseException;
    
    /**
     * Add a string to the message area of the console
     * @param msg
     */
    public void appendString(String msg) {
        historyText.append("\n");
        historyText.append(msg);
        // Make sure the last line is always visible
        historyText.setCaretPosition(historyText.getDocument().getLength());        
    }
    
    @Override
    public void setBackground(Color bgColor) {
        super.setBackground(bgColor);
        if (bgColor == null) return;
        if (historyText != null) historyText.setBackground(bgColor);
        if (prompt != null) prompt.setBackground(bgColor);
        if (commandLine != null) commandLine.setBackground(bgColor);
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (historyText != null) historyText.setFont(font);
        if (prompt != null) prompt.setFont(font);
        if (commandLine != null) commandLine.setFont(font);
    }
    
    public void setPrompt(String promptString) {
        prompt.setText(promptString);
        repaint();
    }
    
    // Make sure the command area gets input focus most of the time
    public void mouseEntered(MouseEvent event) {
        commandLine.requestFocusInWindow();
    }
    public void mouseClicked(MouseEvent event) {
        commandLine.requestFocusInWindow();
    }
    public void mouseExited(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    
    
    
    private class HistoryArea extends JTextArea {
        HistoryArea() {
            setEditable(false);

            // Enable line-wrapping
            setLineWrap(true);
            setWrapStyleWord(false);
            
            addMouseListener(ConsolePanel.this);
        }
    }
    
    private class Prompt extends JTextField {
        Prompt() {   
            setText("> ");
            setEditable(false);
            setBorder(BorderFactory.createEmptyBorder());

            addMouseListener(ConsolePanel.this);
        }
        
        @Override
        public Dimension getMinimumSize() {return getPreferredSize();}
        @Override
        public Dimension getMaximumSize() {return getPreferredSize();}
    }
    
    /**
     * 
      * @author Christopher Bruns
      * 
      * Component in which the user can type a command.
     */
    private class CommandField extends JTextField implements ActionListener {
        CommandField() {
            setEditable(true);
            setBorder(BorderFactory.createEmptyBorder());
            addActionListener(this);

            addMouseListener(ConsolePanel.this);
        }
        
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
        
        /**
         * Respond to command when user presses ENTER
         */
        public void actionPerformed(ActionEvent event) {
            String command = getText();
            if (command.length() == 0) return;
            
            appendString(prompt.getText() + command);
            
            // Actually send the command somewhere
            try {runCommandString(command);}
            catch (ParseException exc) {
                appendString("!!! Syntax Error !!!: " + exc);
            }
            
            setText(""); // Clear for next
            ConsolePanel.this.repaint();
        }
    }
}
