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
 * Created on Oct 24, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.tornadomorph;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import org.simtk.moleculargraphics.*;
import java.awt.event.*;

public class TornadoMorph extends JFrame {

    public static void main(String[] args) {
        TornadoMorph tornadoMorphFrame = new TornadoMorph();
    }
    
    TornadoMorph() {
        super("toRNAdoMorph: (no structures currently loaded)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ClassLoader classLoader = getClass().getClassLoader();
        
        layOutGUI();
        
        pack();
        setVisible(true);
    }

    void layOutGUI() {
        // Top level layout
        Container rootPanel = getContentPane();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS)); // vertical

        // Panel to hold structure windows and associated paraphenalia
        rootPanel.add(new StructurePanel());
        
        // Panel to hold sequence alignment of structures
        rootPanel.add(new AlignmentPanel());
        
        // Panel to hold Play button, Pause button, etc.
        rootPanel.add(new PlayerControlPanel());        
    }
    
    // Panel to hold Play button, Pause button, etc., and slider
    class PlayerControlPanel extends MinimizablePanel {
        PlayerControlPanel() {
            super("Animation Control Panel");

            Container panel = getContentPane();
            
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // vertical
            
            // Panel for buttons only
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            panel.add(buttonPanel);
    
            // The buttons themselves
            new PlayerButton(buttonPanel, "Slower");
            new PlayerButton(buttonPanel, "Reverse");
            new PlayerButton(buttonPanel, "Pause");
            new PlayerButton(buttonPanel, "Play");
            new PlayerButton(buttonPanel, "Faster");
            
            // Slider panel
            JSlider slider = new JSlider();
            panel.add(slider);
        }
        static final long serialVersionUID = 01L;

        // Button for movie playback: "Play", "Pause", etc.
        class PlayerButton extends JButton {
            PlayerButton(Container parent, String label) {
                super(label);
                parent.add(this);
            }
            static final long serialVersionUID = 01L;
        }
    }
    
    // Panel to hold Play button, Pause button, etc., and slider
    class StructurePanel extends MinimizablePanel {
        StructurePanel() {
            super("Structure Viewer");

            Container panel = getContentPane();
            
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); // horizontal
            
            // Starting structure
            panel.add(new SingleStructurePanel("Starting structure"));
            
            // Separator panel
            panel.add(new JPanel());
            
            // Target structure
            panel.add(new SingleStructurePanel("Target structure"));
        }
        
        // One structure window with a label
        class SingleStructurePanel extends BasePanel {
            SingleStructurePanel(String label) {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // vertical
                
                // Panel label and "Load" button
                JPanel labelPanel = new JPanel();
                labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
                labelPanel.add(new JLabel(label));
                labelPanel.add(new JPanel()); // Flexible filler
                labelPanel.add(new JButton("Load..."));
                add(labelPanel);
                
                // Editable text description field above the structure
                // TODO this is too vertically stretchy
                add(new JTextField("(no structure loaded)"));
                
                // Canvas for rendering the structure
                add(new StructureCanvas());
            }
            static final long serialVersionUID = 01L;
        }
        static final long serialVersionUID = 01L;
    }

    // Panel to hold Play button, Pause button, etc., and slider
    class AlignmentPanel extends MinimizablePanel {
        AlignmentPanel() {
            super("Sequence Alignment");
            
            Container panel = getContentPane();
            
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // vertical
            
            // place for the alignment
            panel.add(new AlignmentTextArea());
            
            // scrollbar
            panel.add(new JScrollBar(Adjustable.HORIZONTAL));
        }
        // Panel where the alignment text is shown
        class AlignmentTextArea extends JPanel {
            AlignmentTextArea() {
                setLayout(new GridLayout(2, 2)); // vertical

                // First sequence
                add(new JLabel("Starting:"));
                add(new SequenceTextArea());

                // Second sequence
                add(new JLabel("Target:"));
                add(new SequenceTextArea());
            }
            static final long serialVersionUID = 01L;

            // The actual sequence text
            class SequenceTextArea extends JTextArea {
                SequenceTextArea() {
                    setEditable(false);
                    setText("(no structure loaded)");
                }
                static final long serialVersionUID = 01L;
            }
        }
        static final long serialVersionUID = 01L;
    }
    
    // Panels that hold the main regions of the interface
    class BasePanel extends JPanel {
        // Nice border to set the core panels apart        
        Border panelBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);

        BasePanel() {
            setBorder(panelBorder);
        }
        static final long serialVersionUID = 01L;
    }
    
    // Panel that can fold up into a single label
    class MinimizablePanel extends BasePanel implements ActionListener {
        Container contentPane = new JPanel();
        Container togglePane = new JPanel();
        JButton toggleButton = new JButton("hide");
        boolean isMaximized = true;
        JLabel panelTitle = new JLabel();
        
        MinimizablePanel(String label) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            panelTitle.setText(label);
            togglePane.add(panelTitle);
            togglePane.add(toggleButton);
            togglePane.add(new JPanel()); // to take up space to the right
            toggleButton.addActionListener(this);

            add(togglePane);
            add(contentPane);
        }
        Container getContentPane() {
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
                repaint();
            }
            // If the pane is hidden, show it
            else {
                add(getContentPane());
                isMaximized = true;
                toggleButton.setLabel("hide");
                panelTitle.setEnabled(true);
                repaint();
            }
        }
        static final long serialVersionUID = 01L;
    }
    
    static final long serialVersionUID = 01L; // just to shut up the compiler warnings
}
