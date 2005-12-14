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
import org.simtk.moleculargraphics.cartoon.*;
import org.simtk.molecularstructure.*;
import org.simtk.mvc.ObservableInterface;
import org.simtk.pdb.*;
import org.simtk.gui.*;
import java.awt.event.*;
import java.util.*;

public class TornadoMorph extends JFrame {
    static Color panelColor = new Color(240, 240, 240); // light grey
    static Color buttonColor = new Color(190, 210, 255); // pale blue (not used)
    
    Color originColor = Color.red;
    Color goalColor = Color.blue;
    
    MoleculeLoadBroadcaster startingMoleculeLoadBroadcaster = new MoleculeLoadBroadcaster();
    MoleculeLoadBroadcaster targetMoleculeLoadBroadcaster = new MoleculeLoadBroadcaster();
    
    private final String startingMoleculeLabel = "Origin";
    private final String finalMoleculeLabel = "Goal";

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
        nonStructurePanel.add(new AlignmentPanel(
                startingMoleculeLabel,
                finalMoleculeLabel,
                startingMoleculeLoadBroadcaster,
                targetMoleculeLoadBroadcaster,
                originColor,
                goalColor
                ));
        
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
            
            /* There was a horizontal layout problem with the 
             * structure panels.  Making the window narrower obscured the window
             * on the right at the expense of the window on the right.  Using
             * gridbaglayout instead of boxlayout fixed this -CMB
             */
            
            // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); // horizontal
            GridBagLayout gridbag = new GridBagLayout();
            panel.setLayout(gridbag);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = 1; // all in one row
            gbc.weightx = 1.0; // Everybody stretches horizontally
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH; // stretch           
            
            // Starting structure
            SingleStructurePanel startingStructurePanel = 
                new SingleStructurePanel(
                        startingMoleculeLabel, 
                        "1D9V", 
                        startingMoleculeLoadBroadcaster,
                        originColor);
            startingMoleculeLoadBroadcaster.addObserver(startingStructurePanel);
            gridbag.setConstraints(startingStructurePanel, gbc);
            panel.add(startingStructurePanel);
            
            // Separator panel
            JPanel separatorPanel = new WhitePanel();
            separatorPanel.add(Box.createHorizontalStrut(2));
            gbc.weightx = 0; // don't stretch
            gridbag.setConstraints(separatorPanel, gbc);
            panel.add(separatorPanel);
            
            // Target structure
            SingleStructurePanel targetStructurePanel = 
                new SingleStructurePanel(
                        finalMoleculeLabel, 
                        "1MRP", 
                        targetMoleculeLoadBroadcaster,
                        goalColor);
            targetMoleculeLoadBroadcaster.addObserver(targetStructurePanel);
            gbc.weightx = 1.0; // stretch
            gbc.gridwidth = GridBagConstraints.REMAINDER; // finish row
            gridbag.setConstraints(targetStructurePanel, gbc);
            panel.add(targetStructurePanel);
            
            /**
             * Tie mouse events between the two structures
             */
            startingStructurePanel.addMouseSlavePanel(targetStructurePanel);
            targetStructurePanel.addMouseSlavePanel(startingStructurePanel);
        }
        
        // One structure window with a label
        class SingleStructurePanel extends BasePanel implements ActionListener, Observer {
            Observable moleculeLoadBroadcaster;
            MorphStructureCanvas structureCanvas = new MorphStructureCanvas();
            MoleculeAcquisitionMethodDialog loadDialog;
            JLabel structurePanelLabel = new JLabel("Structure: ?");

            JButton loadButton = new JButton("Load Molecule...");
            Container loadPanel = new WhitePanel();
            LoadProgressPanel progressPanel = new LoadProgressPanel();

            // Editable text description field above the structure
            JTextField userLabelField = new JTextField("(no molecule loaded)");

            SingleStructurePanel(
                    String label, 
                    String pdbId, 
                    MoleculeLoadBroadcaster broadcaster,
                    Color moleculeColor
                    ) {
                initializeStructurePanel();
                structurePanelLabel.setForeground(moleculeColor);
                structurePanelLabel.setText(label);

                loadDialog = new MorphStructDialog(
                        TornadoMorph.this, 
                        broadcaster,
                        progressPanel);
                loadDialog.setDefaultPdbId(pdbId);
            }
            
            /**
             * Delegate addMouseSlavePanel to internal canvas
             * @param p
             */
            public void addMouseSlavePanel(SingleStructurePanel p) {
                structureCanvas.addMouseSlavePanel(p.structureCanvas);
            }
            
            void initializeStructurePanel() {
                // Result: Gridbag layout resizes structureCanvas properly
                // Boxlayout does not

                GridBagLayout gridbag = new GridBagLayout();
                setLayout(gridbag);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = GridBagConstraints.REMAINDER; // each on its own row
                gbc.weightx = 1.0; // Everybody stretches horizontally
                gbc.fill = GridBagConstraints.HORIZONTAL; // stretch horizontally

                // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // vertical
                
                // Panel label and description
                JPanel titlePanel = new WhitePanel();
                titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
                titlePanel.add(structurePanelLabel);
                
                // Don't allow this label to get tall
                Dimension labelSize = new Dimension(Integer.MAX_VALUE, userLabelField.getPreferredSize().height);
                userLabelField.setMaximumSize(labelSize);
                gridbag.setConstraints(userLabelField, gbc);
                titlePanel.add(userLabelField);
                gridbag.setConstraints(titlePanel, gbc);                
                add(titlePanel);

                // Panel with "Load" button
                loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.X_AXIS));
                loadButton.addActionListener(this);
                loadPanel.add(loadButton);
                loadPanel.add(Box.createHorizontalGlue());
                gridbag.setConstraints(loadPanel, gbc);                
                add(loadPanel);
                
                // (initially hidden) panel with load progress
                gridbag.setConstraints(progressPanel, gbc);                
                add(progressPanel);                
                progressPanel.setVisible(false);
                
                // Canvas for rendering the structure
                gbc.fill = GridBagConstraints.BOTH; // stretch horizontally and vertically
                gbc.weighty = 1.0;
                gridbag.setConstraints(structureCanvas, gbc);
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
                // MutableMolecularCartoon cartoon = new AtomSphereCartoon(); // This works
                // MutableMolecularCartoon cartoon = new WireFrameCartoon(); // works
                MutableMolecularCartoon cartoon = new ProteinRibbon();
                // MutableMolecularCartoon cartoon = new BackboneStick(); // works

                // Doesn't help
                // MutableMolecularCartoon cartoon = MolecularCartoonClass.CartoonType.BACKBONE_STICK.newInstance();
                
                System.out.println("Number of Paths = " + cartoon.getAssembly().GetNumberOfPaths());

                for (Iterator i = molecules.molecules().iterator(); i.hasNext();) {
                    Object o = i.next();
                    if (o instanceof LocatedMolecule) {
                        cartoon.add((LocatedMolecule)o);
                        break; // TODO this is just for debugging
                    }
                }

                System.out.println("Number of Paths = "+cartoon.getAssembly().GetNumberOfPaths());

                // cartoon.add(molecules);
                // structureCanvas.setMolecules(molecules, MolecularCartoonClass.CartoonType.BACKBONE_STICK);
                structureCanvas.add(cartoon);

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
                if (event.getSource() == loadButton) {
                    // This set location works, why not the one in the constructor?
                    loadDialog.setLocationRelativeTo(SingleStructurePanel.this);
                    loadDialog.show();

                    loadPanel.setVisible(false);
                    progressPanel.setVisible(true);
                    repaint();
                }
            }
            
            class LoadProgressPanel extends ProgressPanel {
                LoadProgressPanel() {
                    super("Loading...");
                }
                public void hide() {
                    super.hide();
                    setVisible(false);
                    loadPanel.setVisible(true);
                }
                static final long serialVersionUID = 01L;
            }
            
            class MorphStructDialog extends MoleculeAcquisitionMethodDialog {
                // StructureCanvas dialogTargetCanvas;
                MoleculeLoadBroadcaster broadcaster;
                
                MorphStructDialog(JFrame f, 
                        MoleculeLoadBroadcaster b,
                        ProgressPanel p
                        ) {
                    super(f, p);
                    broadcaster = b;
                }
                
                protected void readStructureFromMoleculeCollection(MoleculeCollection molecules) {
                    broadcaster.setChanged();
                    broadcaster.notifyObservers(molecules);                    
                }
                
                static final long serialVersionUID = 01L;
            }
            static final long serialVersionUID = 01L;
        }
        static final long serialVersionUID = 01L;
    }

    /** 
     *  
      * @author Christopher Bruns
      * 
      * Model wrapper to notify sequence panel and structure panel when molecule has changed
     */
    class MoleculeLoadBroadcaster extends Observable implements ObservableInterface {
        // Make setChanged public, so I can declare the change from another object
        public void setChanged() {super.setChanged();}
    }
    
    static final long serialVersionUID = 01L; // just to shut up the compiler warnings
}
