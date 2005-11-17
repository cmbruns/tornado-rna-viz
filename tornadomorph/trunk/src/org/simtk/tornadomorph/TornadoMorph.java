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
import org.simtk.molecularstructure.*;
import org.simtk.pdb.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TornadoMorph extends JFrame {
    static Color panelColor = new Color(240, 240, 240); // light grey
    static Color buttonColor = new Color(190, 210, 255); // pale blue (not used)
    
    MoleculeLoadBroadcaster startingMoleculeLoadBroadcaster = new MoleculeLoadBroadcaster();
    MoleculeLoadBroadcaster targetMoleculeLoadBroadcaster = new MoleculeLoadBroadcaster();

    // Need to load vtk libraries in the correct order before vtkPanel gets a chance to do it wrong
    static {
        VTKLibraries.load();
    }
    
    public static void main(String[] args) {
        TornadoMorph tornadoMorphFrame = new TornadoMorph();
    }
    
    TornadoMorph() {
        super("toRNAdoMorph: (no molecules currently loaded)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        layOutGUI();
        
        pack();
        setVisible(true);
    }

    void layOutGUI() {
        // Top level layout
        Container rootPanel = getContentPane();
        rootPanel.setLayout(new BorderLayout());

        // Make the structure panel absorb all extra space, and give up all needed space
        
        // Panel to hold structure windows and associated paraphenalia
        rootPanel.add(new StructurePanel(), BorderLayout.CENTER);

        // Put all other panels in the south side of a borderlayout
        JPanel nonStructurePanel = new JPanel();
        nonStructurePanel.setLayout(new BoxLayout(nonStructurePanel, BoxLayout.Y_AXIS));
        
        // Panel to hold sequence alignment of structures
        nonStructurePanel.add(new AlignmentPanel());
        
        // Panel to hold Play button, Pause button, etc.
        nonStructurePanel.add(new PlayerControlPanel());
        
        rootPanel.add(nonStructurePanel, BorderLayout.SOUTH);
    }
    
    // Panel to hold Play button, Pause button, etc., and slider
    class PlayerControlPanel extends MinimizablePanel {
        PlayerControlPanel() {
            super("Animation Control Panel");

            Container panel = getContentPane();
            
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // vertical
            
            // Panel for buttons only
            WhitePanel buttonPanel = new WhitePanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            panel.add(buttonPanel);
    
            PlayerButtonManager buttonManager = new PlayerButtonManager(buttonPanel);
            
            // Slider panel
            JSlider slider = new JSlider();
            slider.setBackground(TornadoMorph.panelColor);
            slider.setValue(0); // start at left
            panel.add(slider);
        }
        static final long serialVersionUID = 01L;

        // Handle complex interactions among player buttons
        class PlayerButtonManager implements ActionListener {
            // Button objects
            PlayerButton playButton;
            PlayerButton pauseButton;
            PlayerButton stepButton;
            PlayerButton reverseButton;
            PlayerButton fasterButton;
            PlayerButton slowerButton;
            PlayerButton beginButton;
            PlayerButton endButton;
            
            PlayerButtonManager(Container container) {

                // Load button icons
                ClassLoader classLoader = getClass().getClassLoader();
                ImageIcon playIcon = new ImageIcon( classLoader.getResource("resources/play_icon.png") );
                ImageIcon pauseIcon = new ImageIcon( classLoader.getResource("resources/pause_icon.png") );
                ImageIcon reverseIcon = new ImageIcon( classLoader.getResource("resources/reverse_icon.png") );
                ImageIcon fasterIcon = new ImageIcon( classLoader.getResource("resources/faster_icon.png") );
                ImageIcon slowerIcon = new ImageIcon( classLoader.getResource("resources/slower_icon.png") );
                ImageIcon beginIcon = new ImageIcon( classLoader.getResource("resources/begin_icon.png") );
                ImageIcon endIcon = new ImageIcon( classLoader.getResource("resources/end_icon.png") );
                           
                // Create buttons
                beginButton = new PlayerButton("Begin", beginIcon, this);
                slowerButton = new PlayerButton("Slower", slowerIcon, this);
                reverseButton = new PlayerButton("Reverse", reverseIcon, this);
                pauseButton = new PlayerButton("Pause", pauseIcon, this);
                playButton = new PlayerButton("Play", playIcon, this);
                fasterButton = new PlayerButton("Faster", fasterIcon, this);
                endButton = new PlayerButton("End", endIcon, this);                

                // Lay buttons out in container
                Dimension buttonSpacerSize = new Dimension(5,5);
                container.add(beginButton);
                container.add(Box.createRigidArea(buttonSpacerSize)); // space between buttons
                container.add(slowerButton);
                container.add(Box.createRigidArea(buttonSpacerSize)); // space between buttons
                container.add(reverseButton);
                container.add(Box.createRigidArea(buttonSpacerSize)); // space between buttons
                container.add(pauseButton);
                container.add(Box.createRigidArea(buttonSpacerSize)); // space between buttons
                container.add(playButton);
                container.add(Box.createRigidArea(buttonSpacerSize)); // space between buttons
                container.add(fasterButton);
                container.add(Box.createRigidArea(buttonSpacerSize)); // space between buttons
                container.add(endButton);
                
                setPausedMode();
            }
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == playButton) {
                    setPlayingMode();
                }
                else if (event.getSource() == pauseButton) {
                    setPausedMode();
                }
            }
            void setPlayingMode() {
                pauseButton.setEnabled(true);
                playButton.setEnabled(false);
                // System.out.println("Playing");
            }
            void setPausedMode() {
                pauseButton.setEnabled(false);
                playButton.setEnabled(true);
                // System.out.println("Paused");
            }
        }
        
        // Button for movie playback: "Play", "Pause", etc.
        class PlayerButton extends JButton {
            PlayerButton(String label, Icon icon, PlayerButtonManager buttonManager) {
                super(label);
                initializePlayerButton();
                this.setIcon(icon);
                addActionListener(buttonManager);
            }
            
            // common to all constructors
            protected void initializePlayerButton() {
                // Color the player buttons
                // setBackground(TornadoMorph.buttonColor);
                
                // Place button icon above button text
                setHorizontalTextPosition(SwingConstants.CENTER);
                setVerticalTextPosition(SwingConstants.BOTTOM);                
            }
            
            static final long serialVersionUID = 01L;
        }
        
        /**
         *  
          * @author Christopher Bruns
          * 
          * Button that shows border response to mouse-over
         */
        class HoverButton extends JButton  implements MouseListener {
            private Border empty = null;
            private Border raised = null;
            private Border lowered = null;
            private Insets inset = null;
            
            HoverButton(String label) {
                super(label);
                initializeHoverButton();
            }

            void initializeHoverButton() {
                // Create various borders for hover effect
                setBorderValues();
                
                addMouseListener(this);                
            }

            private void setBorderValues()
            {
                if(inset == null)
                    inset = getBorder().getBorderInsets(this);
                Border decreasedMargin = new EmptyBorder(new Insets(inset.top-2, inset.left-2, inset.bottom-2, inset.right-2));
                Border originalMargin = new EmptyBorder(inset);
                empty = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(), originalMargin);
                           raised = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), decreasedMargin);
                lowered = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), decreasedMargin);
                setBorder(empty);
            }
            
            // Mouse actions for hover effect
            public void mouseEntered(MouseEvent e)
            {
                if(isEnabled())
                    setBorder(raised);
                repaint();
            }
         
            public void mouseExited(MouseEvent e)
            {
                if(isEnabled())
                    setBorder(empty);
                repaint();
            }
         
            public void mouseClicked(MouseEvent e){}
            public void mousePressed(MouseEvent e)
            {
                if(isEnabled())
                    setBorder(lowered);
                else
                    setBorder(empty);
                repaint();
            }
         
            public void mouseReleased(MouseEvent e)
            {
                if(getBorder().equals(empty))
                    return;
         
                if(isEnabled())
                    setBorder(raised);
                else
                    setBorder(empty);
                repaint();
            }
            public void setEnabled(boolean activate)
            {
                super.setEnabled(activate);
                setBorder(empty);
                repaint();
            }
            
            public void setMargin(Insets inset)
            {
                super.setMargin(inset);
                this.inset = inset;
                this.setBorderValues();
                repaint();
            }

            static final long serialVersionUID = 01L;            
        }
    }
    
    // Panel to hold 3D structure views
    class StructurePanel extends BasePanel {
        StructurePanel() {
            // super("Structure Viewer");

            JComponent panel = getContentPane();
            
            // Make the structure panel the most springy
            // panel.setMinimumSize(new Dimension(10,10));
            // panel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); // horizontal
            
            // Starting structure
            SingleStructurePanel startingStructurePanel = 
                new SingleStructurePanel("Starting molecule", "1D9V", startingMoleculeLoadBroadcaster);
            startingMoleculeLoadBroadcaster.addObserver(startingStructurePanel);
            panel.add(startingStructurePanel);
            
            // Separator panel
            panel.add(new WhitePanel());
            
            // Target structure
            SingleStructurePanel targetStructurePanel = 
                new SingleStructurePanel("Target molecule", "1MRP", targetMoleculeLoadBroadcaster);
            targetMoleculeLoadBroadcaster.addObserver(targetStructurePanel);
            panel.add(targetStructurePanel);
        }
        
        // One structure window with a label
        class SingleStructurePanel extends BasePanel implements ActionListener, Observer {
            Observable moleculeLoadBroadcaster;
            MorphStructureCanvas structureCanvas = new MorphStructureCanvas();
            MoleculeAcquisitionMethodDialog loadDialog;
            JLabel structurePanelLabel = new JLabel("Structure: ?");

            // Editable text description field above the structure
            JTextField userLabelField = new JTextField("(no molecule loaded)");

            SingleStructurePanel(String label, String pdbId, MoleculeLoadBroadcaster broadcaster) {
                initializeStructurePanel();
                structurePanelLabel.setText(label);

                loadDialog = new MorphStructDialog(TornadoMorph.this, broadcaster);
                loadDialog.setDefaultPdbId(pdbId);
            }
            
            void initializeStructurePanel() {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // vertical
                
                // Panel label and "Load" button
                WhitePanel labelPanel = new WhitePanel();
                labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
                labelPanel.add(structurePanelLabel);
                labelPanel.add(new WhitePanel()); // Flexible filler

                JButton loadButton = new JButton("Load molecule...");
                loadButton.addActionListener(this);
                labelPanel.add(loadButton);

                add(labelPanel);
                
                // Don't allow this label to get tall
                Dimension labelSize = new Dimension(Integer.MAX_VALUE, userLabelField.getPreferredSize().height);
                userLabelField.setMaximumSize(labelSize);
                add(userLabelField);
                
                // Canvas for rendering the structure
                add(structureCanvas);
            }

            /**
             * Respond to loading of a new molecule
             */
            public void update(Observable observable, Object object) {
                if ( (object != null) && (object instanceof MoleculeCollection) ) {
                    MoleculeCollection molecules = (MoleculeCollection) object;
                    setMolecules(molecules);
                }
            }
            
            private void setMolecules(MoleculeCollection molecules) {
                structureCanvas.setMolecules(molecules);

                String pdbId = molecules.getPdbId();
                
                String pdbTitle = molecules.getTitle();
                String structureFileName = molecules.getInputStructureFileName();

                String structureDescription = new String("");
                if (pdbId != null) structureDescription += pdbId + ": ";
                if (pdbTitle != null) structureDescription += pdbTitle + " ";
                if (structureFileName != null) structureDescription += structureFileName;
                if (structureDescription.equals("")) structureDescription = new String("(type structure description here)");
                userLabelField.setText(structureDescription);
                userLabelField.setCaretPosition(0);                
            }
            
            // Load structure button was pressed
            public void actionPerformed(ActionEvent event) {
                // This set location works, why not the one in the constructor?
                loadDialog.setLocationRelativeTo(SingleStructurePanel.this);
                loadDialog.show();
            }
            
            class MorphStructDialog extends MoleculeAcquisitionMethodDialog {
                // StructureCanvas dialogTargetCanvas;
                MoleculeLoadBroadcaster broadcaster;
                
                MorphStructDialog(JFrame f, MoleculeLoadBroadcaster b) {
                    super(f);
                    // System.out.println("MorphStructDialog constructor");
                    // dialogTargetCanvas = c;
                    broadcaster = b;
                }
                
                protected void readStructureFromStream(InputStream structureStream) throws IOException {
                    // System.out.println("Reading structure...");
                    
                    super.readStructureFromStream(structureStream);
                    
                    MoleculeCollection molecules = new MoleculeCollection();
                    molecules.loadPDBFormat(structureStream);
                    
                    // System.out.println("Number of molecules = " + molecules.molecules().size());
                    
                    // Set PDB Id to what the user said
                    if (getPdbId() != null) molecules.setPdbId(getPdbId());
                    
                    if (getStructureFileName() != null) molecules.setInputStructureFileName(getStructureFileName());
                    
                    // Send molecule through MoleculeLoadBroadcaster interface
                    broadcaster.setChanged();
                    broadcaster.notifyObservers(molecules);
                }
                static final long serialVersionUID = 01L;
            }
            static final long serialVersionUID = 01L;
        }
        static final long serialVersionUID = 01L;
    }

    class AlignmentPanel extends MinimizablePanel {
        AlignmentTextArea alignmentTextArea;
        AlignmentScrollBar alignmentScrollBar;
        AlignmentPanel() {
            super("Sequence Alignment");
            
            Container panel = getContentPane();
            
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // vertical
            
            // place for the alignment
            alignmentTextArea = new AlignmentTextArea();
            panel.add(alignmentTextArea);
            
            // scrollbar
            alignmentScrollBar = new AlignmentScrollBar(Adjustable.HORIZONTAL);
            alignmentScrollBar.addAdjustmentListener(alignmentTextArea);
            panel.add(alignmentScrollBar);
            
            // updateScrollBarParameters();
        }
        
        /**
         * Make sure scroll bar geometry matches the loaded sequences
         */
        private void updateScrollBarParameters() {
            // TODO this hasn't worked yet
            // Use pixel units for scrollbar
            
            // Minimum is always zero
            alignmentScrollBar.setMinimum(0);
            
            int sequenceWidth = alignmentTextArea.getTotalSequenceWidth();
            int windowWidth = alignmentTextArea.getVisibleSequenceWidth();
            
            alignmentScrollBar.setMaximum(Math.max(sequenceWidth, windowWidth));
            alignmentScrollBar.setVisibleAmount(windowWidth);
            alignmentScrollBar.setBlockIncrement( (int)(0.95 * windowWidth) );
            alignmentScrollBar.setUnitIncrement(alignmentTextArea.getResidueWidth());
        }
        
        // Panel where the alignment text is shown
        class AlignmentTextArea extends WhitePanel implements AdjustmentListener {
            private SequenceTextPanel startingSequencePanel;
            private SequenceTextPanel targetSequencePanel;
            
            AlignmentTextArea() {
                GridBagLayout gridBag = new GridBagLayout();
                setLayout(gridBag);
                
                startingSequencePanel = new SequenceTextPanel(gridBag);
                targetSequencePanel = new SequenceTextPanel(gridBag);
                
                // First sequence
                add(new SequenceLabel("Starting: ", gridBag));
                startingMoleculeLoadBroadcaster.addObserver(startingSequencePanel);
                add(startingSequencePanel);

                // Second sequence
                add(new SequenceLabel("Target: ", gridBag));
                targetMoleculeLoadBroadcaster.addObserver(targetSequencePanel);
                add(targetSequencePanel);
            }
            
            private int getResidueWidth() {
                return startingSequencePanel.getResidueWidth();
            }
            
            private int getTotalSequenceWidth() {
                return Math.max(startingSequencePanel.getTotalSequenceWidth(),
                        targetSequencePanel.getTotalSequenceWidth());
            }
            
            private int getVisibleSequenceWidth() {
                return Math.max(startingSequencePanel.getWidth(),
                        targetSequencePanel.getWidth());
            }
            
            public void adjustmentValueChanged(AdjustmentEvent event) {
                JScrollBar scrollBar = (JScrollBar) event.getAdjustable();
                
                // Scroll the sequences to where the scrollbar tells them
                int pixelValue = scrollBar.getValue();
                startingSequencePanel.setLeftEdgePixel(pixelValue);
                targetSequencePanel.setLeftEdgePixel(pixelValue);
            }

            static final long serialVersionUID = 01L;

            // The actual sequence text
            class SequenceTextPanel extends BasePanel implements Observer {
                SequenceCanvas sequenceCanvas = new SequenceCanvas();
                
                SequenceTextPanel(GridBagLayout gb) {
                    GridBagConstraints gc = new GridBagConstraints();
                    gc.fill = GridBagConstraints.HORIZONTAL; // stretch
                    gc.weightx = 1.0; // stretch
                    gc.gridwidth = GridBagConstraints.REMAINDER; // finish line
                    gb.setConstraints(this, gc);

                    // BoxLayout does cause child to stretch to fill
                    // default layout (BorderLayout?) does not cause child to stretch
                    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                    
                    sequenceCanvas.setParentContainer(this);

                    add(sequenceCanvas);
                    add(Box.createHorizontalGlue()); // Keep alignment on the left side

                    setBackground(Color.white);
                }

                public void setBackground(Color c) {
                    super.setBackground(c);
                    if (sequenceCanvas != null) sequenceCanvas.setBackground(c);
                }
                
                public void setLeftEdgePixel(int p) {
                    sequenceCanvas.setLeftEdgePixel(p);
                }
                
                public int getLeftEdgePixel() {return sequenceCanvas.getLeftEdgePixel();}

                /**
                 * Respond to loading a new molecule
                 * @param observable
                 * @param object the new MoleculeCollection that has been loaded
                 */
                public void update(Observable observable, Object object) {
                    if ( (object != null) && (object instanceof MoleculeCollection) ) {
                        MoleculeCollection molecules = (MoleculeCollection) object;
                        setMolecules(molecules);
                    }
                }
                
                private void setMolecules(MoleculeCollection molecules) {
                    Biopolymer singleMolecule = null;
                    for (Iterator molIter = molecules.molecules().iterator(); molIter.hasNext();) {
                        Molecule molecule = (Molecule) molIter.next();
                        if (molecule instanceof Biopolymer) {
                            singleMolecule = (Biopolymer) molecule;
                            break;
                        }
                    }
                    
                    if ((sequenceCanvas != null) && (singleMolecule != null)) 
                        sequenceCanvas.setMolecule(singleMolecule);
                    
                    // New sequence may cause change in scroll bar geometry
                    AlignmentPanel.this.updateScrollBarParameters();
                }
                
                int getResidueWidth() {
                    return sequenceCanvas.getResidueWidth();
                }
                
                int getTotalSequenceWidth() {
                    return sequenceCanvas.getTotalSequenceWidth();
                }
                
                static final long serialVersionUID = 01L;
            }
            
            class SequenceLabel extends JLabel {
                SequenceLabel(String text, GridBagLayout gb) {
                    super(text);
                    setMaximumSize(getPreferredSize()); // don't grow
                    GridBagConstraints gc = new GridBagConstraints();
                    gc.fill = GridBagConstraints.NONE; // don't expand
                    gc.gridx = 0; // put on left
                    gb.setConstraints(this, gc);
                }
                static final long serialVersionUID = 01L;                
            }
        }
        class AlignmentScrollBar extends JScrollBar {
            AlignmentScrollBar(int p) {
                super(p);
                
                int initialVisibleWidth = 200;
                
                setValue(0); // start at left
                setUnitIncrement(10);
                setBlockIncrement((int)(initialVisibleWidth * 0.95));
                setMinimum(0);
                setMaximum(initialVisibleWidth);
                setVisibleAmount(initialVisibleWidth);
            }
            static final long serialVersionUID = 01L;            
        }
        static final long serialVersionUID = 01L;
    }

    class WhitePanel extends JPanel {
        WhitePanel() {
            setBackground(TornadoMorph.panelColor);            
        }
        static final long serialVersionUID = 01L;
    }
    
    // Panels that hold the main regions of the interface
    class BasePanel extends WhitePanel {
        // Nice border to set the core panels apart        
        Border panelBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);

        BasePanel() {
            setBorder(panelBorder);
        }
        JPanel getContentPane() {
            return this;
        }
        static final long serialVersionUID = 01L;
    }
    
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
    
    /** 
     *  
      * @author Christopher Bruns
      * 
      * Model wrapper to notify sequence panel and structure panel when molecule has changed
     */
    class MoleculeLoadBroadcaster extends Observable {
        // Make setChanged public
        public void setChanged() {super.setChanged();}
    }
    
    static final long serialVersionUID = 01L; // just to shut up the compiler warnings
}
