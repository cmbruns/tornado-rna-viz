/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Apr 24, 2005
 *
 */
package org.simtk.moleculargraphics;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import javax.jnlp.*;

import org.simtk.moleculargraphics.cartoon.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.pdb.*;
import org.simtk.geometry3d.*;
import org.simtk.util.*;

import edu.stanford.ejalbert.BrowserLauncher;

import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * RNA manipulation application
 *
 */
public class Tornado extends JFrame 
implements ResidueActionListener 
{
    // 
    // Public
    //
    
    public Color highlightColor = new Color(255, 240, 50); // Pale orange
    protected Tornado3DCanvas canvas;
    private LoadStructureDialog loadStructureDialog = new LoadStructureDialog(this);

    Tornado() {
        super("toRNAdo: (no structures currently loaded)");
        
        // loadNativeLibraries();
        
        classLoader = getClass().getClassLoader();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new Tornado3DCanvas(residueActionBroadcaster); // must create before menus
        createMenuBar();

        JPanel panel = new JPanel();
        this.setIconImage(new ImageIcon(classLoader.getResource("resources/images/tornado_icon.gif")).getImage());
        
        // With the addition of a separate sequence cartoon, it is probably time
        // for a fancy layout
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // each on its own row
        gbc.weightx = 1.0; // Everybody stretches horizontally
        gbc.fill = GridBagConstraints.HORIZONTAL; // stretch horizontally
        
        // 3D molecule canvas - must be created before menus are
        gbc.fill = GridBagConstraints.BOTH; // stretch horizontally and vertically
        gbc.weighty = 1.0;
        gridbag.setConstraints(canvas, gbc);

        // Secondary structure canvas
        SecondaryStructureCanvas canvas2D = null;
        if (drawSecondaryStructure) {
            canvas2D = new SecondaryStructureCanvas(residueActionBroadcaster);

            // canvas.setMinimumSize(new Dimension(10,10));
            // canvas2D.setMinimumSize(new Dimension(10, 10));
            
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    canvas, canvas2D);
            splitPane.setResizeWeight(0.67);
            splitPane.setDividerSize(4);
            splitPane.setContinuousLayout(true);
 
            panel.add(splitPane, gbc);
        }
        else
            panel.add(canvas, gbc);
        
        // Sequence area
        sequencePane = new SequencePane(residueActionBroadcaster);
        sequenceCartoonCanvas = new SequenceCartoonCanvas(residueActionBroadcaster, sequencePane.getSequenceCanvas());

        gbc.fill = GridBagConstraints.HORIZONTAL; // stretch horizontally only
        gbc.weighty = 0.0; // don't stretch vertically
        gridbag.setConstraints(sequenceCartoonCanvas, gbc);
        panel.add(sequenceCartoonCanvas, gbc);

        // The sequence pane is being very tricky in the layout
        // So try putting another panel behind it
        sequencePanel = new Panel();
        sequencePanel.setLayout(new BorderLayout());
        sequencePanel.add(sequencePane, BorderLayout.SOUTH);
        gridbag.setConstraints(sequencePanel, gbc);
        panel.add(sequencePanel, gbc);
        
		messageArea = new JLabel();
        gridbag.setConstraints(messageArea, gbc);
        panel.add(messageArea, gbc);
		
        getContentPane().add(panel, BorderLayout.CENTER);
        
        pack();
        
        setVisible(true);
        
        if (useRotationThread) {
            rotationThread = new InertialRotationThread(this);
            rotationThread.setPriority(Thread.MIN_PRIORITY);
            rotationThread.sitStill = true;
            rotationThread.start();
        }
                
        setMessage("No molecules are currently loaded");
        
        residueActionBroadcaster.addSelectionListener(sequencePane);
        residueActionBroadcaster.addSelectionListener(canvas);
        residueActionBroadcaster.addSelectionListener(sequenceCartoonCanvas);
        residueActionBroadcaster.addSelectionListener(this);
        if (drawSecondaryStructure)
            residueActionBroadcaster.addSelectionListener(canvas2D);            
        
        setSelectionColor(new Color(255,255,150));
    }
    
    public static void main(String[] args) {
        // Put the menu bar at the top of the screen on the mac
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        Tornado tornadoFrame = new Tornado();
    }
    
    public void setSelectionColor(Color c) {
        selectionColor = c;
        sequencePane.setSelectionColor(c);
        sequenceCartoonCanvas.setSelectionColor(c);
        canvas.setSelectionColor(c);
    }

    public void setBackgroundColor(Color c) {
        canvas.setBackgroundColor(c);
    }
    
    /**
     * compare base pairs in the external file to those in the molecule structre
     * @param fileName
     */
    public void compareHbonds(String fileName, RNA rna) {
        // HashSet<BasePair> loadedBasePairs = new HashSet<BasePair>();
        HashSet loadedBasePairs = new HashSet();
        
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
        catch (FileNotFoundException exc) {return;}
        String line;
        int lineCount = 0;
        try {
            boolean parsingBasePairs = false; // Are we in the base-pair stanza?
            LINE: while ((line = reader.readLine()) != null) {
                lineCount ++;
                if (line.indexOf("BEGIN_base-pair") >= 0) {
                    parsingBasePairs = true;
                    continue LINE;
                }
                if (!parsingBasePairs) continue LINE;
                if (line.indexOf ("END_base-pair") >= 0) {
                    parsingBasePairs = false;
                    break LINE;
                }
                
                //     1_182, B:    96 G-C   278 B: +/+ cis         XIX
                StringTokenizer tokenizer = new StringTokenizer(line);
                String token;
                token = tokenizer.nextToken(); // residue index
                token = tokenizer.nextToken(); // chain
                token = tokenizer.nextToken(); // first residue number
                int firstResidueNumber = (new Integer(token)).intValue();
                token = tokenizer.nextToken(); // one letter codes
                token = tokenizer.nextToken(); // second residue number
                int secondResidueNumber = (new Integer(token)).intValue();

                // Only care about distant ones
                if ( (secondResidueNumber - firstResidueNumber) < 3 ) continue LINE;
                
                Nucleotide residue1 = (Nucleotide) rna.getResidueByNumber(firstResidueNumber);
                Nucleotide residue2 = (Nucleotide) rna.getResidueByNumber(secondResidueNumber);
                if ( (residue1 != null) && (residue2 != null) ) {
                    BasePair bp = new BasePair(residue1, residue2);
                    loadedBasePairs.add(bp);
                }
                else System.out.println("!!!Could not find base pair " +firstResidueNumber+ " to " +secondResidueNumber);
            }
            reader.close();
        }
        catch (IOException exc) {}
        System.out.println("" + lineCount + " lines read.");

        // Compare both base pair computation methods
        // HashSet<BasePair> myBasePairs = new HashSet<BasePair>();
        HashSet myBasePairs = new HashSet();
        // for (BasePair bp : rna.identifyBasePairs()) myBasePairs.add(bp);
        for (Iterator i = rna.identifyBasePairs().iterator();
            i.hasNext();) {
            BasePair bp = (BasePair) (i.next());
            myBasePairs.add(bp);
        }
        
        // How many are in common?
        int commonCount = 0;
        int uniqueToSelfCount = 0;
        int uniqueToLoadedCount = 0;
        System.out.println("Unique to Tornado: ");
        reportBasePairGeometry(myBasePairs);
        // for (BasePair bp : myBasePairs) {
        for (Iterator i = myBasePairs.iterator(); i.hasNext();) {
            BasePair bp = (BasePair) i.next();
            if (loadedBasePairs.contains(bp)) commonCount++;
            else {
                uniqueToSelfCount ++;
            }
        }
        System.out.println("Unique to RNAMLView: ");
        reportBasePairGeometry(loadedBasePairs);
        // for (BasePair bp : loadedBasePairs) {
        for (Iterator i = loadedBasePairs.iterator(); i.hasNext();) {
            BasePair bp = (BasePair) i.next();
            if (myBasePairs.contains(bp)) {}
            else {
                uniqueToLoadedCount ++;
            }
        }
        
        System.out.println("" + commonCount + " base pairs in common.");
        System.out.println("" + uniqueToLoadedCount + " base pairs unique to RNAMLView.");
        System.out.println("" + uniqueToSelfCount + " base pairs unique to Tornado.");

    }
    void reportBasePairGeometry(HashSet basePairs) {
        TreeSet centroidDistances = new TreeSet();
        TreeSet planeAngles = new TreeSet();
        TreeSet planeDistances = new TreeSet();
        TreeSet atomDistances = new TreeSet();

        // for (BasePair bp : basePairs) {
        for (Iterator i = basePairs.iterator(); i.hasNext();) {
            BasePair bp = (BasePair) i.next();
            StructureMolecule base1 = bp.getResidue1().get(Nucleotide.baseGroup);
            StructureMolecule base2 = bp.getResidue2().get(Nucleotide.baseGroup);
            Vector3D centroid1 = base1.getCenterOfMass();
            Vector3D centroid2 = base2.getCenterOfMass();
            Plane3D plane1 = base1.bestPlane3D();
            Plane3D plane2 = base2.bestPlane3D();
            
            double distance = centroid1.distance(centroid2);
            centroidDistances.add(new Double(distance));
            System.out.println("   centroid distance = " + distance);
            
            double angle = Math.abs(Math.acos(plane1.getNormal().dot(plane2.getNormal())) * 180.0 / Math.PI);
            if (angle > 90) angle = 180 - angle;
            planeAngles.add(new Double(angle));
            System.out.println("   plane angle = " + angle + " degrees.");
            
            double planeDistance1 = plane1.distance(centroid2);
            double planeDistance2 = plane2.distance(centroid1);
            planeDistances.add(new Double(planeDistance1));
            planeDistances.add(new Double(planeDistance2));
            System.out.println("   plane distances are " + planeDistance1 + " and " + planeDistance2);
    
            // Touching criterion
            double minDistance = 1000;
            // for (Atom atom1 : bp.getResidue1().getAtoms()) {
            for (Iterator i1 = bp.getResidue1().getAtomIterator(); i1.hasNext();) {
                LocatedAtom atom1 = (LocatedAtom) i1.next();
                
                // if (! ((atom1 instanceof PDBOxygen) || (atom1 instanceof PDBNitrogen))) continue;
                if (! ((atom1.getElementName().equals("oxygen")) || (atom1.getElementName().equals("nitrogen")))) continue;

                // for (Atom atom2 : bp.getResidue2().getAtoms()) {
                for (Iterator i2 = bp.getResidue2().getAtomIterator(); i2.hasNext();) {
                    PDBAtom atom2 = (PDBAtom) i2.next();
                    if (! ((atom2.getElementName().equals("oxygen")) || (atom2.getElementName().equals("nitrogen")))) continue;
                    double testDistance = atom1.distance(atom2);
                    if (testDistance < minDistance) minDistance = testDistance;
                }
            }
            atomDistances.add(new Double(minDistance));
            System.out.println("   closest atomic distance = " + minDistance);
        }

        
        int cutoffIndex = (int)((centroidDistances.size() - 1.0) * 0.95);

        double cutoffDistance = ((Double) centroidDistances.toArray()[cutoffIndex]).doubleValue();
        System.out.println("Cutoff centroid distance = " + cutoffDistance);
        
        double cutoffAngle = ((Double) (planeAngles.toArray()[cutoffIndex])).doubleValue();
        System.out.println("Cutoff plane angle = " + cutoffAngle);

        double cutoffPlaneDistance = ((Double) (planeDistances.toArray()[2 * cutoffIndex])).doubleValue();
        System.out.println("Cutoff plane distance = " + cutoffPlaneDistance);

        double cutoffAtomDistance = ((Double) (atomDistances.toArray()[cutoffIndex])).doubleValue();
        System.out.println("Cutoff atom distance = " + cutoffAtomDistance);
    }
    
    // Show the user that some waiting time is needed
    public void setWait(String message) {
        setCursor(waitCursor);
        canvas.setCursor(waitCursor);
        // pauseRotation();
        if (message == null) setMessage("Please wait...");
        else setMessage(message + " "); // Extra space to preserve message area size
        
        // System.err.println(message);
    }
    
    // The wait is over
    public void unSetWait(String message) {
        if (message == null) setMessage("Done.");
        else setMessage(message + " "); // Extra space to preserve message area size
        setCursor(defaultCursor);
        canvas.setCursor(defaultCursor);
        // resumeRotation();
    }
    
    public void setMessage(String msg) {
        messageArea.setText(msg);
    }
    
    void createMenuBar() {
        
        // Prevent the vtkPanel from obscuring the JMenus
        // JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        JCheckBoxMenuItem checkItem;

//        menu = new JMenu("Tornado");
//        menuBar.add(menu);
//
//        menuItem = new JMenuItem("About Tornado");
//        menuItem.setEnabled(true);
//        menuItem.addActionListener(new AboutTornadoAction());
//        menu.add(menuItem);
//
//        menu.add(new JSeparator());
//
//        menuItem = new JMenuItem("Exit Tornado");
//        menuItem.addActionListener(new QuitAction());
//        menu.add(menuItem);


        menu = new JMenu("File");
        menuBar.add(menu);

        menuItem = new JMenuItem("Load PDB Molecule...");
        menuItem.addActionListener(new LoadPDBAction());
        menu.add(menuItem);

//        menuItem = new JMenuItem("Run script file...");
//        menuItem.addActionListener(new RunScriptAction());
//        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Save PNG Image...");
        menuItem.addActionListener(new SaveImageFileAction());
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Exit Tornado");
        menuItem.addActionListener(new QuitAction());
        menu.add(menuItem);


        menu = new JMenu("Edit");
        menuBar.add(menu);
        menuItem = new JMenuItem("Undo");
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menuItem = new JMenuItem("Redo");
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menuItem = new JMenuItem("Delete");
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menuItem = new JMenuItem("Relax molecule");
        menuItem.addActionListener(new RelaxCoordinatesAction());
        menu.add(menuItem);

//         menuItem = new JMenuItem("Move selection");
//         menuItem.addActionListener(new MoveSelectionAction());
//         menu.add(menuItem);
        
        viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        addCartoonSelection( MolecularCartoon.CartoonType.BALL_AND_STICK,
                "Ball and Stick",
                new ImageIcon(classLoader.getResource("resources/images/po4_stick_icon.png")) );

        addCartoonSelection( MolecularCartoon.CartoonType.SPACE_FILLING,
                "Space-filling Atoms",
                new ImageIcon(classLoader.getResource("resources/images/nuc_fill_icon.png")) );

        addCartoonSelection( MolecularCartoon.CartoonType.ROPE_AND_CYLINDER2,
                "Rope and Cylinder",
                new ImageIcon(classLoader.getResource("resources/images/cylinder_icon.png")) );

        addCartoonSelection( MolecularCartoon.CartoonType.RESIDUE_SPHERE,
                "Residue Spheres",
                null );

        addCartoonSelection( MolecularCartoon.CartoonType.BACKBONE_TRACE,
                "Backbone Trace",
                null );

        addCartoonSelection( MolecularCartoon.CartoonType.WIRE_FRAME,
                "Line Drawing",
                null );
        
        menu = new JMenu("Rotation");
        viewMenu.add(menu);

        ButtonGroup rotationGroup = new ButtonGroup();
        
        checkItem = new JCheckBoxMenuItem("None / Sit still");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateNoneAction());
        checkItem.setState((rotationThread == null) || rotationThread.sitStill);
        rotationGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Rock");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateRockAction());
        checkItem.setState((rotationThread != null) && rotationThread.doRock && (!rotationThread.sitStill));
        rotationGroup.add(checkItem);
        menu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("Spin");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateSpinAction());
        checkItem.setState((rotationThread != null) && (!rotationThread.doRock) && (!rotationThread.sitStill));
        rotationGroup.add(checkItem);
        menu.add(checkItem);
        
        menu = new JMenu("Background Color");
        viewMenu.add(menu);
        ButtonGroup backgroundGroup = new ButtonGroup();

        checkItem = new JCheckBoxMenuItem("Sky");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new BackgroundColorAction(new Color(0.92f, 0.96f, 1.0f)));
        checkItem.setState(true);
        backgroundGroup.add(checkItem);
        menu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("White");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new BackgroundColorAction(Color.white));
        checkItem.setState(false);
        backgroundGroup.add(checkItem);
        menu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("Black");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new BackgroundColorAction(Color.black));
        checkItem.setState(false);
        backgroundGroup.add(checkItem);
        menu.add(checkItem);
        
//        menuItem = new JMenuItem("Test Full Screen");
//        viewMenu.add(menuItem);
//        menuItem.setEnabled(true);
//        checkItem.addActionListener(new TestFullScreenAction());

        menu = new JMenu("Stereoscopic 3D");
        viewMenu.add(menu);

        ButtonGroup stereoscopicOptionsGroup = new ButtonGroup();
        
        checkItem = new JCheckBoxMenuItem("Off / Ordinary monoscopic");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new StereoOffAction());
        checkItem.setState(true);
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Red/Blue glasses", new ImageIcon(classLoader.getResource("resources/images/rbglasses.png")));
        checkItem.setEnabled(true);
        checkItem.addActionListener(new StereoRedBlueAction());
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Interlaced shutter glasses");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new StereoInterlacedAction());
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Scan doubled shutter glasses");
        checkItem.setEnabled(false);
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        
        menu = new JMenu("Help");
        menuBar.add(menu);

        menuItem = new JMenuItem("About Tornado");
        menuItem.setEnabled(true);
        menuItem.addActionListener(new AboutTornadoAction());
        menu.add(menuItem);

        menu.add(new JSeparator());
        menuItem = new JMenuItem("Web Links:");
        menuItem.setEnabled(false);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("  Report a program bug...");
        menuItem.addActionListener(new BrowserLaunchAction
                ("https://simtk.org/tracker/?func=add&group_id=12&atid=129"));
        menu.add(menuItem);

        menuItem = new JMenuItem("  Request a new program feature...");
        menuItem.addActionListener(new BrowserLaunchAction
                ("https://simtk.org/tracker/?func=add&group_id=12&atid=132"));
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }
    
    class QuitAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);  // terminate this program
        }
    }

    class RunScriptAction implements ActionListener {
        JFileChooser loadScriptFileChooser = new JFileChooser();
        MyJFileFilter scriptFilter = new MyJFileFilter();
        
        public RunScriptAction() {
            scriptFilter.setDescription("script files (*.scr,*.ras,*.rsc)");
            scriptFilter.addExtension("scr");
            scriptFilter.addExtension("ras");
            scriptFilter.addExtension("rsc");
            
            loadScriptFileChooser.setFileFilter(scriptFilter);
        }
        
        public void actionPerformed(ActionEvent e) {

            int returnVal = loadScriptFileChooser.showOpenDialog(Tornado.this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = loadScriptFileChooser.getSelectedFile();
                try {
                    setWait("Loading script file " + file.getCanonicalPath() + " ...");
                    FileInputStream inStream = new FileInputStream(file);

                    // TODO - parse script
                    // MoleculeCollection molecules = loadPDBFile(inStream);
                    
                    unSetWait("Script completed (" + file.getName() + ")");
                }                
                catch (FileNotFoundException exc) {
                    unSetWait("File not found. (" + file.getName() + ")");
                    String[] options = {"Bummer!"};
                    JOptionPane.showOptionDialog(null, "No such file: " + file, "script File Error!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, options, options[0]);
                }
                catch (IOException exc) {
                    unSetWait("File error. (" + file.getName() + ")");
                    String[] options = {"Bummer!"};
                    JOptionPane.showOptionDialog(null, "Problem reading file: " + file + ": " + exc, "script File Error!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, options, options[0]);
                }
            }
        }
    }

    class TestFullScreenAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.testFullScreen();
        }
    }

    class BackgroundColorAction implements ActionListener {
        Color color;
        BackgroundColorAction(Color c) {color = c;}
        public void actionPerformed(ActionEvent e) {
            setBackgroundColor(color);
        }
    }

//    class MoveSelectionAction implements ActionListener {
//        public void actionPerformed(ActionEvent e) {
//            System.out.println("Hey, this isn't moving a residue!?!?!");
//        }
//    }

    class RelaxCoordinatesAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            moleculeCollection.relaxCoordinates();
            
            // TODO update cartoon
            canvas.currentCartoon.clear();
            canvas.currentCartoon.show(moleculeCollection);
            
            // System.out.println("Hey, this isn't relaxing the coordinates!?!?!");
        }
    }

    class CartoonAction implements ActionListener {
        MolecularCartoon.CartoonType type;
        CartoonAction(MolecularCartoon.CartoonType t) {
            type = t;
        }
        public void actionPerformed(ActionEvent e) {
            setWait("Calculating geometry...");
            
            canvas.setMolecules(moleculeCollection, null);
            
//            canvas.currentCartoonType = type;
//
//            canvas.currentCartoon = type.newInstance();
//            
//            canvas.currentCartoon.show(moleculeCollection);
//            vtkAssembly assembly = canvas.currentCartoon.getAssembly();
//            
//            if (assembly != null) {
//                canvas.Lock();
//                canvas.GetRenderer().RemoveAllProps();
//                
//                // AddProp deprecated in vtk 5.0
//                // try{canvas.GetRenderer().AddViewProp(assembly);}
//                // catch(NoSuchMethodError exc){canvas.GetRenderer().AddProp(assembly);}
//                canvas.GetRenderer().AddProp(assembly);
//
//                canvas.UnLock();
//            }
            
            // TODO loaded molecule does not paint
            // assembly.Modified();
            // canvas.repaint();

            // Update residue highlights
//            firstResidue = null;
//            finalResidue = null;
//            // for (Molecule molecule : moleculeCollection.molecules()) {
//            for (Iterator i = moleculeCollection.molecules().iterator(); i.hasNext();) {
//                Molecule molecule = (Molecule) i.next();
//                if (molecule instanceof Biopolymer) {
//                    Biopolymer bp = (Biopolymer) molecule;
//                    canvas.Lock();
//                    // canvas.clearResidueHighlights();
//                    boolean isFirstResidue = true;
//                    // for (Residue residue : bp.residues()) {
//                    for (Iterator i2 = bp.residues().iterator(); i2.hasNext();) {
//                        Residue residue = (Residue) i2.next();
//                        if (isFirstResidue)
//                            firstResidue = residue;
//                        // vtkProp highlight = canvas.currentCartoon.highlight(residue, highlightColor);
//                        // canvas.addResidueHighlight(residue, highlight);                                
//                        isFirstResidue = false;
//                        finalResidue = residue;
//                    }
//                    canvas.UnLock();
//                    break; // only put the sequence of the first molecule with a sequence
//                }
//            }
            
            if (currentHighlightedResidue != null)
                residueActionBroadcaster.fireHighlight(currentHighlightedResidue);

            unSetWait("Geometry computed.");
        }
    }

    class RotateNoneAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.sitStill = true;
            rotationThread.interrupt();
        }
    }

    class RotateRockAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.sitStill = false;
            rotationThread.pauseRotation = false;
            rotationThread.doRock = true;
            rotationThread.currentAngle = 0.0;
            rotationThread.interrupt();
        }
    }

    class RotateSpinAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.sitStill = false;
            rotationThread.pauseRotation = false;
            rotationThread.doRock = false;
            rotationThread.interrupt();
        }
    }

    class StereoOffAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.setStereoOff();
        }
    }

    class StereoRedBlueAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.setStereoRedBlue();
        }
    }

    class StereoInterlacedAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.setStereoInterlaced();
        }
    }

    class BrowserLaunchAction implements ActionListener {
        String urlString;
        BrowserLaunchAction(String u) {urlString = u;}
        public void actionPerformed(ActionEvent e) {
            
            // Show information dialog, so the savvy user will be able to
            // go to the url manually, in case the browser open fails.
            JOptionPane.showConfirmDialog(
                    null, 
                    "Your browser will open to page " + urlString + " in a moment\n", 
                    "Browse to SimTK.org",
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE
                    );

            // New way
            URL url;
            try {url = new URL(urlString);}            
            catch (MalformedURLException exc) {
                launchErrorConfirmDialog("Problem opening browser to page " + urlString + "\n" + exc,
                "Web URL error!");
                return;
            }
            try {
                // This only works when started in a web start application
                BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
                boolean result = bs.showDocument(url);
            } 
            catch (UnavailableServiceException exc) {
                // launchErrorConfirmDialog("Problem opening browser to page " + urlString + "\n" + exc,
                // "JNLP Error!");
                try {BrowserLauncher.openURL(urlString);}
                catch (IOException exc2) {
                    launchErrorConfirmDialog("Problem opening browser to page " + urlString + "\n" + exc2,
                                             "Web URL error!");
                }        
            }
        }
    }
    
    void launchErrorConfirmDialog(String msg, String title) {
        String[] options = {"Bummer!"};
        JOptionPane.showOptionDialog(
                null, 
                msg, 
                "Web URL error!",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]);        
    }

    class LoadStructureDialog extends MoleculeAcquisitionMethodDialog {
        LoadStructureDialog(JFrame f) {super(f);}

        public void readStructureFromMoleculeCollection(MoleculeCollection molecules)
        {
            loadPDBFile(molecules);
        }
        static final long serialVersionUID = 01L;
    }
    
    class LoadPDBAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            loadStructureDialog.show();
        }
    }
    
//    class OldLoadPDBAction implements ActionListener {
//        JDialog dialog = null;
//        JButton loadFileButton = null;
//        JButton webPDBButton = null;
//        JButton cancelButton = null;
//        JTextField idField = null;
//        
//        // JCheckBoxMenuItem bioUnitCheckBox;
//        JComboBox bioUnitList;
//        
//        public void actionPerformed(ActionEvent e) {
//            // If this is the very first call to this action, create the dialog
//            if (dialog == null) {
//                dialog = new JDialog(Tornado.this, "Choose Molecule Source", false);
//                dialog.setLocationRelativeTo(Tornado.this);
//                JPanel contentPanel = new JPanel();
//                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
//                dialog.setContentPane(contentPanel);
//                
//                contentPanel.add(Box.createRigidArea(new Dimension(0,5)));
//                JLabel label = new JLabel("Choose molecule source:");
//                label.setAlignmentX(Component.CENTER_ALIGNMENT);
//                contentPanel.add(label);
//
//                contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
//                contentPanel.add(new JSeparator());
//                contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
//
//                loadFileButton = new JButton("From file...");
//                loadFileButton.addActionListener(this);
//                loadFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
//                contentPanel.add(loadFileButton);
//
//                contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
//                contentPanel.add(new JSeparator());
//                contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
//                
//                JPanel webPDBPanel = new JPanel();
//                webPDBPanel.setLayout(new BoxLayout(webPDBPanel, BoxLayout.X_AXIS));
//
//                label = new JLabel(" PDB ID (4 characters): ");
//                webPDBPanel.add(label);
//
//                idField = new JTextField("1GID", 4);
//                idField.addActionListener(this);
//                webPDBPanel.add(idField);
//
//                webPDBButton = new JButton("From web");
//                webPDBButton.addActionListener(this);
//                webPDBPanel.add(webPDBButton);
//
//                webPDBPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
//                contentPanel.add(webPDBPanel);
//
//                // contentPanel.add(Box.createRigidArea(new Dimension(0,5)));
//                
//                // bioUnitCheckBox = new JCheckBoxMenuItem("Biological Unit");
//                // bioUnitCheckBox.setState(true);
//                // contentPanel.add(bioUnitCheckBox);
//                
//                String unitOptions[] = {"Biological Unit", "Crystallographic Unit"};
//                bioUnitList = new JComboBox(unitOptions);
//                bioUnitList.setSelectedIndex(0); // biological unit
//                contentPanel.add(bioUnitList);
//                
//                contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
//                contentPanel.add(new JSeparator());
//                contentPanel.add(Box.createRigidArea(new Dimension(0,8)));
//
//                cancelButton = new JButton("Cancel");
//                cancelButton.addActionListener(this);
//                cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
//                contentPanel.add(cancelButton);
//                contentPanel.add(Box.createRigidArea(new Dimension(0,5)));
//
//                dialog.pack();
//            }
//
//            else if ( e.getSource() == loadFileButton ) {
//                // Load molecule from file using file browser dialog
//                dialog.setVisible(false); // temporarily hide the dialog
//                if (loadPDBFileAction.pdbFileLoaded())
//                    // success
//                    return;
//                else
//                    dialog.setVisible(true);  // Give the user another chance if something went wrong
//            }
//
//            if ( (e.getSource() == webPDBButton) ||
//                 (e.getSource() == idField) ) {
//                // Load PDB molecule from the internet
//                String pdbId = idField.getText().trim().toLowerCase();
//
//                // Force ID to be 4 characters
//                if (pdbId.length() != 4) return;
//
//                String urlBase;
//                String extension;
//                String filePrefix = "";
//                // if (bioUnitCheckBox.getState()) {
//                if (bioUnitList.getSelectedIndex() == 0) { // biological unit
//                    urlBase = "ftp://ftp.rcsb.org/pub/pdb/data/biounit/coordinates/divided/";
//                    extension = "pdb1.gz";
//                }
//                else {
//                    urlBase = "ftp://ftp.rcsb.org/pub/pdb/data/structures/divided/pdb/";
//                    extension = "ent.Z";
//                    filePrefix = "pdb";
//                }
//                
//                String division = pdbId.substring(1, 3);
//                String fullURLString = urlBase + division + "/" + filePrefix + pdbId + "." + extension;
//
//                InputStream inStream;
//                try {
//                    setWait("Loading remote PDB structure...");
//                    dialog.setCursor(waitCursor);
//
//                    setWait("Connecting to the PDB ftp site...");
//                    URLConnection urlConnection = (new URL(fullURLString)).openConnection();
//                    inStream = urlConnection.getInputStream();
//                    int fileSize = urlConnection.getContentLength();
//                    
//                    if ( (fullURLString.endsWith(".gz")) )
//                        inStream = new GZIPInputStream(inStream);
//                    if ( (fullURLString.endsWith(".Z")) )
//                        inStream = new UncompressInputStream(inStream);
//
//                    // Monitor load progress
//                    // TODO - this does not appear to work
//                    setWait("Reading structure file...");
//                    ProgressMonitorInputStream progressStream = 
//                            new ProgressMonitorInputStream(
//                                    Tornado.this,
//                                    "Reading " + fullURLString,
//                                    inStream);
//                    ProgressMonitor pm = progressStream.getProgressMonitor(); 
//                    pm.setMaximum(fileSize);
//                    pm.setMinimum(0);
//                    pm.setProgress(10);
//                    pm.setMillisToDecideToPopup(500);
//                    pm.setMillisToPopup(2000);
//                    setWait("Structure File size = " + fileSize + "...");
//                    
//                    MoleculeCollection molecules = loadPDBFile(progressStream);
//                    updateTitleBar();
//                    
//                    dialog.setCursor(defaultCursor);
//                    dialog.setVisible(false); // success, so close the dialog
//                    return;
//                } catch (IOException exc) {
//                    unSetWait("Remote PDB load error. (" + fullURLString + ")");
//                    String[] options = {"Bummer!"};
//                    JOptionPane.showOptionDialog(null, "Problem reading structure: " + pdbId + ": " + exc, "PDB File Error!",
//                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
//                            null, options, options[0]);
//                    dialog.setCursor(defaultCursor);
//                }
//                
//                
//                dialog.setCursor(defaultCursor);
//                dialog.setVisible(true); // reopen dialog so user can try again
//                return;
//            }
//            
//            if ( e.getSource() == cancelButton ) {
//                dialog.setCursor(defaultCursor);
//                dialog.setVisible(false);
//                return;
//            }
//
//            // User selected load molecule menu item
//            else {
//                dialog.setVisible(true);
//                dialog.setCursor(defaultCursor);
//            }
//        }
//    }

//    class LoadPDBFileAction implements ActionListener {
//        JFileChooser loadPDBFileChooser;
//
//        public class PDBFilter extends javax.swing.filechooser.FileFilter {
//            public String getExtension(File f) {
//                if(f != null) {
//                    String filename = f.getName();
//                    int i = filename.lastIndexOf('.');
//                    if(i>0 && i<filename.length()-1) {
//                    return filename.substring(i+1).toLowerCase();
//                    };
//                }
//                return null;
//           }
//           public boolean accept(File f) {
//                if (f.isDirectory()) {return true;}
//                String extension = getExtension(f);
//                if (extension == null) return false;
//                extension = extension.toLowerCase();
//                if (extension != null)
//                    if (
//                        extension.equals("pdb") ||
//                        extension.equals("pdb1") ||
//                        extension.equals("pdb2") 
//                        )
//                            return true;
//                return false;
//            }
//
//            public String getDescription() {return "Protein Data Bank (PDB) structure files";}
//        }
//        
//        public void actionPerformed(ActionEvent e) {
//            pdbFileLoaded();
//        }
//        
//        boolean pdbFileLoaded() { 
//            boolean answer = false;  // start pessimistic
//            
//            if (loadPDBFileChooser == null) {
//                loadPDBFileChooser = new JFileChooser();
//                PDBFilter filter = new PDBFilter();
//                loadPDBFileChooser.setFileFilter(filter);
//            }
//            
//            int returnVal = loadPDBFileChooser.showOpenDialog(Tornado.this);
//            if(returnVal == JFileChooser.APPROVE_OPTION) {
//                File file = loadPDBFileChooser.getSelectedFile();
//                try {
//                    setWait("Loading file " + file.getCanonicalPath() + " ...");
//                    FileInputStream inStream = new FileInputStream(file);
//                    
//                    MoleculeCollection molecules = loadPDBFile(inStream);
//                                        
//                    unSetWait("Molecule loaded (" + file.getName() + ")");
//                    answer = true;
//                    
//                    updateTitleBar();
//                    
//                    // This is temporary
//                    // if ( (bp != null) && file.getName().contains("1x8w") ) {
//                    //     compareHbonds("1x8w.pdb2.out", (RNA) bp);
//                    // }
//                }                
//                catch (FileNotFoundException exc) {
//                    unSetWait("File not found. (" + file.getName() + ")");
//                    String[] options = {"Bummer!"};
//                    JOptionPane.showOptionDialog(null, "No such file: " + file, "PDB File Error!",
//                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
//                            null, options, options[0]);
//                }
//                catch (IOException exc) {
//                    unSetWait("File error. (" + file.getName() + ")");
//                    String[] options = {"Bummer!"};
//                    JOptionPane.showOptionDialog(null, "Problem reading file: " + file + ": " + exc, "PDB File Error!",
//                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
//                            null, options, options[0]);
//                }
//    
//            }
//            return answer;
//        }
//    }
    
    MoleculeCollection loadPDBFile(InputStream inStream) throws IOException, InterruptedException {
        MoleculeCollection molecules = new MoleculeCollection();
        molecules.loadPDBFormat(inStream);
        loadPDBFile(molecules);
        return molecules;
    }

    void loadPDBFile(MoleculeCollection molecules) {

        setMessage("Read " + molecules.getAtomCount() + " atoms, in " +
                molecules.getMoleculeCount() + " molecules");
        
        moleculeCollection = molecules;
        
        updateTitleBar();
        
        // Create graphical representation of the molecule
        (new CartoonAction(canvas.currentCartoonType)).actionPerformed(new ActionEvent(this, 0, ""));

        // Center camera on new molecule
        Vector3D com = molecules.getCenterOfMass();
        canvas.GetRenderer().GetActiveCamera().SetFocalPoint(com.getX(), com.getY(), com.getZ());

        // Display sequence of first molecule that has a sequence
        residueActionBroadcaster.fireClearResidues();
        BiopolymerClass bp = null;
        // for (Molecule molecule : molecules.molecules()) {
        for (Iterator i1 = molecules.molecules().iterator(); i1.hasNext();) {
            StructureMolecule molecule = (StructureMolecule) i1.next();
            if (molecule instanceof BiopolymerClass) {
                bp = (BiopolymerClass) molecule;
                
                // for (Residue residue : bp.residues())
                for (Iterator i2 = bp.getResidueIterator(); i2.hasNext(); ) {
                    Residue residue = (Residue) i2.next();
                    residueActionBroadcaster.fireAdd(residue);
                }
                
                break; // only put the sequence of the first molecule with a sequence
            }
        }

        // TODO - create one subroutine for updating the sequences
        // maybe in the ResidueSelector interface
        canvas.repaint();
        sequencePane.repaint();
        sequenceCartoonCanvas.repaint();
        repaint();
    }

    class SaveImageFileAction implements ActionListener {
        JFileChooser saveImageFileChooser;

        public void actionPerformed(ActionEvent e) {
            if (saveImageFileChooser == null)
                saveImageFileChooser = new JFileChooser();            
            int returnVal = saveImageFileChooser.showSaveDialog(Tornado.this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = saveImageFileChooser.getSelectedFile();
                setWait("Saving image file...");
                // Warn if file exists
                if (file.exists()) {
                    int response = JOptionPane.showConfirmDialog(null, "" + file + "\nFile exists.  Overwrite?");
                    if ( (response == JOptionPane.CANCEL_OPTION) ||
                         (response == JOptionPane.NO_OPTION)) {
                        unSetWait("File not overwritten. (" + file.getName() + ")");
                        return;
                    }
                }
                vtkWindowToImageFilter wti = new vtkWindowToImageFilter();
                wti.SetInput(canvas.GetRenderWindow());
                canvas.Lock();
                wti.Update();
                canvas.UnLock();

                vtkPNGWriter writer = new vtkPNGWriter();
                try {
                    writer.SetFileName(file.getCanonicalPath());
                    writer.SetInput(wti.GetOutput());
                    writer.Write();

                    JOptionPane.showMessageDialog(null, "Image file save complete.", 
                            "Image File Write Completed", JOptionPane.INFORMATION_MESSAGE);
                    unSetWait("Wrote image to file " + file.getCanonicalPath());
                }
                catch (IOException exc) {
                    JOptionPane.showMessageDialog(null, "ERROR! Unable to write to file " + file, 
                            "Image File Write Error", JOptionPane.ERROR_MESSAGE);
                    unSetWait("Error writing image file " + file.getName());
                    return;
                }
            }
        }
    }
    
    class AboutTornadoAction implements ActionListener {
        String aboutString = 
            " toRNAdo version " + tornadoVersion + "\n"+
             " Copyright (c) 2005, Stanford University. All rights reserved. \n"+
             " Redistribution and use in source and binary forms, with or without \n"+
             " modification, are permitted provided that the following conditions\n"+
             " are met: \n"+
             "  - Redistributions of source code must retain the above copyright \n"+
             "    notice, this list of conditions and the following disclaimer. \n"+
             "  - Redistributions in binary form must reproduce the above copyright \n"+
             "    notice, this list of conditions and the following disclaimer in the \n"+
             "    documentation and/or other materials provided with the distribution. \n"+
             "  - Neither the name of the Stanford University nor the names of its \n"+
             "    contributors may be used to endorse or promote products derived \n"+
             "    from this software without specific prior written permission. \n"+
             " THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \n"+
             " \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT\n"+
             " LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS \n"+
             " FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE \n"+
             " COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,\n"+
             " INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, \n"+
             " BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; \n"+
             " LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER \n"+
             " CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT\n"+
             " LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN \n"+
             " ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE \n"+
             " POSSIBILITY OF SUCH DAMAGE.";
            
        
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, aboutString, "About toRNAdo", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void highlight(Residue residue) {
        if (residue == null) setMessage(" ");
        else setMessage("Residue " + residue.getResidueName() + 
                " (" + residue.getOneLetterCode() + ") " + residue.getResidueNumber());
        currentHighlightedResidue = residue;
    }
    public void unHighlightResidue() {
        setMessage(" "); // Use a space, otherwise message panel collapses
        currentHighlightedResidue = null;
    }
    public void select(Selectable r) {}
    public void unSelect(Selectable r) {}    
    public void unSelect() {}    
    public void add(Residue r) {}    
    public void clearResidues() {
        currentHighlightedResidue = null;
    }
    public void centerOn(Residue r) {}
    
    public synchronized boolean userIsInteracting() {
        return residueActionBroadcaster.userIsInteracting();
    }
    
    public synchronized void flushUserIsInteracting() {
        residueActionBroadcaster.flushUserInteraction();
    }
    
    public void updateTitleBar() {
        // Set title
        if ( (moleculeCollection != null) && (moleculeCollection.molecules().size() > 0) ) {
            if (moleculeCollection.getTitle().length() > 0)
                Tornado.this.setTitle(titleBase + ": "+ moleculeCollection.getTitle());
            else
                Tornado.this.setTitle(titleBase + ": (unknown molecule)");
        }
        else 
            Tornado.this.setTitle(titleBase + ": (no molecules currently loaded)");
        Tornado.this.repaint();
    }

    public static final long serialVersionUID = 1L;

    //
    // Private
    // 

    /**
     * Put all hooks for adding a new molecule representation here
     * Call this from createMenuBar()
     */
    private void addCartoonSelection(MolecularCartoon.CartoonType cartoonType, 
                                     String description,
                                     ImageIcon imageIcon) {
        if (viewMenu == null)
            throw new RuntimeException("No View menu in which to add a Cartoon style");
        if ( (cartoonGroup == null) || (cartoonMenu == null) ) {
            cartoonMenu = new JMenu("Molecule Style");
            viewMenu.add(cartoonMenu);
            cartoonGroup = new ButtonGroup();
        }
        
        JCheckBoxMenuItem checkItem;
        if (imageIcon != null)
            checkItem = new JCheckBoxMenuItem(description, imageIcon);
        else
            checkItem = new JCheckBoxMenuItem(description);
        
        checkItem.setEnabled(true);
        checkItem.addActionListener(new CartoonAction(cartoonType));
        checkItem.setState(canvas.currentCartoonType == cartoonType);
        cartoonGroup.add(checkItem);
        cartoonMenu.add(checkItem);        
    }
    
//    private static void loadNativeLibraries() {
//        // To supplement those libraries loaded by vtkPanel
//        // when in Java Web Start mode
//        loadOneNativeLibrary("vtkfreetype"); 
//        loadOneNativeLibrary("vtkexpat"); 
//        loadOneNativeLibrary("vtkjpeg"); 
//        loadOneNativeLibrary("vtkzlib"); 
//        loadOneNativeLibrary("vtktiff"); 
//        loadOneNativeLibrary("vtkpng"); 
//        loadOneNativeLibrary("vtkftgl"); 
//        loadOneNativeLibrary("vtkCommon"); 
//        loadOneNativeLibrary("vtkFiltering"); 
//        loadOneNativeLibrary("vtkDICOMParser"); 
//        loadOneNativeLibrary("vtkIO"); 
//        loadOneNativeLibrary("vtkImaging"); 
//        loadOneNativeLibrary("vtkGraphics"); 
//        loadOneNativeLibrary("vtkRendering"); 
//        loadOneNativeLibrary("vtkHybrid"); 
//        loadOneNativeLibrary("jogl"); 
//        // loadOneNativeLibrary("jogl_cg"); 
//
//        
//        loadOneNativeLibrary("vtkCommonJava"); 
//        loadOneNativeLibrary("vtkFilteringJava"); 
//        loadOneNativeLibrary("vtkIOJava"); 
//        loadOneNativeLibrary("vtkImagingJava"); 
//        loadOneNativeLibrary("vtkGraphicsJava"); 
//        loadOneNativeLibrary("vtkRenderingJava"); 
//        loadOneNativeLibrary("vtkHybridJava");
//    }
//    
//    private static void loadOneNativeLibrary(String libName) {
//        try {System.loadLibrary(libName);}
//        catch (UnsatisfiedLinkError exc) {
//            System.err.println("Failed to load native library " + libName + " : " + exc);
//        }
//    }
    
    static {
        VTKLibraries.load();
        // loadNativeLibraries();
        
        // Keep vtk canvas from obscuring swing widgets
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
    
    private ButtonGroup cartoonGroup = null;
    private JMenu cartoonMenu = null;
    private JMenu viewMenu = null;

    private static String tornadoVersion = "0.50";
    
    private ClassLoader classLoader;
    private SequencePane sequencePane;
    private SequenceCartoonCanvas sequenceCartoonCanvas;
    private Panel sequencePanel;
    private JLabel messageArea;

    // private LoadPDBFileAction loadPDBFileAction = new LoadPDBFileAction();
    
    private Residue currentHighlightedResidue = null;
    
    private PDBResidue firstResidue = null;
    private PDBResidue finalResidue = null;
    private vtkProp currentHighlight;

    private boolean useRotationThread = true;
    private InertialRotationThread rotationThread;
    
    private boolean drawSecondaryStructure = false;
    
    private MoleculeCollection moleculeCollection = new MoleculeCollection();

    private Cursor handCursor = new Cursor (Cursor.HAND_CURSOR);
    private Cursor defaultCursor = new Cursor (Cursor.DEFAULT_CURSOR);
    private Cursor waitCursor = new Cursor (Cursor.WAIT_CURSOR);

    private ResidueActionBroadcaster residueActionBroadcaster = new ResidueActionBroadcaster();
    
    private Color selectionColor;
    
    private String titleBase = "toRNAdo";

}
