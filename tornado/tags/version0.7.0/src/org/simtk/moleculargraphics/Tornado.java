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
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.pdb.*;
import org.simtk.rnaml.RnamlDocument;
import org.simtk.util.*;

import edu.stanford.ejalbert.BrowserLauncher;
import org.simtk.rnaml.RnamlDocument;

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
    private String currentPath = ".";
    private String saveImagePath = ".";
    private LoadStructureDialog loadStructureDialog = new LoadStructureDialog(this);
    
    private MolecularCartoonClass.CartoonType cartoonType = 
        MolecularCartoonClass.CartoonType.WIRE_FRAME;
    private MutableMolecularCartoon currentCartoon = cartoonType.newInstance();

    Tornado() {
        super("toRNAdo: (no structures currently loaded)");
        
        // Avoid upper left sucky Java window location
        setLocationRelativeTo(null);
        
        // loadNativeLibraries();
        
        classLoader = getClass().getClassLoader();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new Tornado3DCanvas(); // must create before menus
        createMenuBar();

        JPanel panel = new JPanel();
        this.setIconImage(new ImageIcon(classLoader.getResource("images/tornado_icon.gif")).getImage());
        
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
        // residueActionBroadcaster.addSelectionListener(canvas);
        residueActionBroadcaster.addSelectionListener(sequenceCartoonCanvas);
        residueActionBroadcaster.addSelectionListener(this);
        if (drawSecondaryStructure)
            residueActionBroadcaster.addSelectionListener(canvas2D);
        
        setBackgroundColor(Color.white);
    }
    
    public static void main(String[] args) {

        // Put the menu bar at the top of the screen on the mac
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        VTKLibraries.load();

        new Tornado();
    }
    
    public void setBackgroundColor(Color c) {
        canvas.setBackgroundColor(c);
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

        viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        addCartoonSelection( MolecularCartoonClass.CartoonType.WIRE_FRAME,
                "Line Drawing",
                null );
        
        addCartoonSelection( MolecularCartoonClass.CartoonType.BALL_AND_STICK,
                "Ball and Stick",
                new ImageIcon(classLoader.getResource("images/po4_stick_icon.png")) );

        addCartoonSelection( MolecularCartoonClass.CartoonType.SPACE_FILLING,
                "Space-filling Atoms",
                new ImageIcon(classLoader.getResource("images/nuc_fill_icon.png")) );

        addCartoonSelection( MolecularCartoonClass.CartoonType.RESIDUE_SPHERE,
                "Residue Spheres",
                null );

        addCartoonSelection( MolecularCartoonClass.CartoonType.ROPE_AND_CYLINDER2,
                "Rope and Cylinder",
                new ImageIcon(classLoader.getResource("images/cylinder_icon.png")) );

        addCartoonSelection( BasePairRibbon.class, 
                "Base Pair Rods", null );
        
        addCartoonSelection( OvalBasePairPlus.class, 
                "Base Pairs Ovals", null );
        
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

        checkItem = new JCheckBoxMenuItem("White");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new BackgroundColorAction(Color.white));
        checkItem.setState(true);
        backgroundGroup.add(checkItem);
        menu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("Sky");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new BackgroundColorAction(new Color(0.92f, 0.96f, 1.0f)));
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

        checkItem = new JCheckBoxMenuItem("Red/Blue glasses", new ImageIcon(classLoader.getResource("images/rbglasses.png")));
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

//        menu = new JMenu("Secondary Structure");
//        menuBar.add(menu);
//        
//        menuItem = new JMenuItem("Use RNAML File...");
//        menuItem.setEnabled(true);
//        menuItem.addActionListener(new ApplyRnamlAction());
//        menu.add(menuItem);
//        
//        menuItem = new JMenuItem("Compute from structure");
//        menuItem.setEnabled(true);
//        menuItem.addActionListener(new ComputeSecondaryStructureAction());
//        menu.add(menuItem);
        
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
    
    // For me the programmer to use when creating new actions
    class TemplateAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        }
    }

    // For me the programmer to use when creating new actions
    class ApplyRnamlAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // TODO
        }
    }

    class ComputeSecondaryStructureAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (Molecule molecule : Tornado.this.moleculeCollection.molecules()) {
                if (!(molecule instanceof NucleicAcid)) continue;
                NucleicAcid nucleicAcid = (NucleicAcid) molecule;
                
                // Remove preexisting secondary structures
                Collection<SecondaryStructure> structs = nucleicAcid.secondaryStructures();
                for (SecondaryStructure structure : structs) {
                    if (structure instanceof BasePair)
                        structs.remove(structure);
                    if (structure instanceof Duplex)
                        structs.remove(structure);
                }
                
                for (BasePair basePair : nucleicAcid.identifyBasePairs())
                    structs.add(basePair);
                
                for (Duplex duplex : nucleicAcid.identifyHairpins())
                    structs.add(duplex);
            }
        }
    }

    class QuitAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);  // terminate this program
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

    class CartoonAction implements ActionListener {
        MolecularCartoonClass.CartoonType type = null;
        Class cartoonClass = null;

        CartoonAction(MolecularCartoonClass.CartoonType t) {
            type = t;
        }
        
        CartoonAction(Class cartoonClass) {
            this.cartoonClass = cartoonClass;
        }

        public void actionPerformed(ActionEvent e) {
            setWait("Calculating geometry...");
            
            canvas.clear();
            
            if (type != null)
                currentCartoon = type.newInstance();
            else if (cartoonClass != null) {
                try {
                    currentCartoon = (MutableMolecularCartoon) cartoonClass.newInstance();
                } 
                catch (InstantiationException exc) {exc.printStackTrace();}
                catch (IllegalAccessException exc) {exc.printStackTrace();}
            }

            for (Iterator iterMolecule = moleculeCollection.molecules().iterator(); iterMolecule.hasNext(); ) {
                Object o = iterMolecule.next();
                if (o instanceof LocatedMolecule)
                    currentCartoon.add((LocatedMolecule)o);
            }

            canvas.add(currentCartoon);
            
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
                bs.showDocument(url);
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
        LoadStructureDialog(JFrame f) {
            super(f, null, Tornado.this.currentPath);
            setLocationRelativeTo(Tornado.this);
            setDefaultPdbId("1GRZ");
        }

        public void readStructureFromMoleculeCollection(MoleculeCollection molecules)
        {
            loadPDBFile(molecules);
                        
            // Check for RNAML secondary structures
            // needs to be made more robust & expansive
            // looks in local direrctory, doesn't know loc of PDB
            boolean haveNucleic = false;
            for (Molecule m : molecules.molecules()) 
                if (m instanceof NucleicAcid) haveNucleic = true;

            boolean foundRnaml = false;
            if ( haveNucleic && (molecules.getPdbId() != null) )  {
                String rnamlFileName = molecules.getPdbId()+".pdb.xml";
                
                // File curDir = new File(".");
                File curDir = new File(Tornado.this.currentPath);
                java.util.List dirList = Arrays.asList(curDir.list());
                if (dirList.contains(rnamlFileName)){
                    // System.out.println("processing xml file");

                    try {
                        File rnamlFile = new File(curDir, rnamlFileName);
                        RnamlDocument rnamlDoc = new RnamlDocument(rnamlFile, molecules);
                        rnamlDoc.importSecondaryStructures();
                        foundRnaml = true;
                    } 
                    catch (org.jdom.JDOMException exc) {
                        exc.printStackTrace();
                    }
                    catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }
                else {
                    // System.out.println("can't find xml file "+rnamlFileName);
                    // System.out.println("directory listing includes "+dirList);
                }
            }
            
            if (haveNucleic && !foundRnaml) {                    
                // Compute secondary structure using Tornado methods
                for (Molecule molecule : molecules.molecules()) {
                    if (!(molecule instanceof NucleicAcid)) continue;
                    NucleicAcid nucleicAcid = (NucleicAcid) molecule;
                    Collection<SecondaryStructure> structs = nucleicAcid.secondaryStructures();
                    
                    for (BasePair basePair : nucleicAcid.identifyBasePairs())
                        structs.add(basePair);
                    
                    for (Duplex duplex : nucleicAcid.identifyHairpins())
                        structs.add(duplex);
                }
            }

        }
        
        protected void currentPathUpdated(){
            Tornado.this.currentPath = loadStructureDialog.getCurrentPath();
            // System.out.println("current path now equals " + Tornado.this.currentPath);
            if (Tornado.this.saveImagePath.equals(".")){
            	Tornado.this.saveImagePath = Tornado.this.currentPath; 
            }

        }
    }
    
    class LoadPDBAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            loadStructureDialog.setVisible(true);
        }
    }
    
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
        (new CartoonAction(cartoonType)).actionPerformed(new ActionEvent(this, 0, ""));

        // Center camera on new molecule
        // Vector3D com = molecules.getCenterOfMass();
        // canvas.GetRenderer().GetActiveCamera().SetFocalPoint(com.getX(), com.getY(), com.getZ());
        canvas.centerByBoundingBox();

        // Display sequence of first molecule that has a sequence
        residueActionBroadcaster.fireClearResidues();
        BiopolymerClass bp = null;
        // for (Molecule molecule : molecules.molecules()) {
        for (Iterator i1 = molecules.molecules().iterator(); i1.hasNext();) {
            LocatedMolecule molecule = (LocatedMolecule) i1.next();
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
        String saveImagePath = ".";

        public void actionPerformed(ActionEvent e) {
            if ((saveImageFileChooser == null)||(!saveImagePath.equals(Tornado.this.saveImagePath))){
            	saveImagePath = Tornado.this.saveImagePath;
                saveImageFileChooser = new JFileChooser(Tornado.this.saveImagePath);            
            }
            int returnVal = saveImageFileChooser.showSaveDialog(Tornado.this);

    	    String savePath = saveImageFileChooser.getCurrentDirectory().getPath();
            String FS = System.getProperty("file.separator");
	        int index = savePath.lastIndexOf(FS);
	        Tornado.this.saveImagePath = savePath.substring(0, index);
        	saveImagePath = Tornado.this.saveImagePath;

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
            String pdbId = moleculeCollection.getPdbId();
            if ( (pdbId == null) || (pdbId.equals("")) || pdbId.equals("XXXX") ) pdbId = "";
            else pdbId = "("+pdbId+") ";
            
            if (moleculeCollection.getTitle().length() > 0)
                Tornado.this.setTitle(titleBase + ": " + pdbId + moleculeCollection.getTitle());
            else
                Tornado.this.setTitle(titleBase + ": " + pdbId + "(unknown molecule)");
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
    private void addCartoonSelection(MolecularCartoonClass.CartoonType cartoonType, 
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
        checkItem.setState(cartoonType == cartoonType);
        cartoonGroup.add(checkItem);
        cartoonMenu.add(checkItem);        
    }

    /**
     * Put all hooks for adding a new molecule representation here
     * Call this from createMenuBar()
     */
    private void addCartoonSelection(Class cartoonType, 
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
        checkItem.setState(cartoonType == cartoonType);
        cartoonGroup.add(checkItem);
        cartoonMenu.add(checkItem);        
    }
        
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
    
    private boolean useRotationThread = true;
    private InertialRotationThread rotationThread;
    
    private boolean drawSecondaryStructure = false;
    
    private MoleculeCollection moleculeCollection = new MoleculeCollection();

    private Cursor handCursor = new Cursor (Cursor.HAND_CURSOR);
    private Cursor defaultCursor = new Cursor (Cursor.DEFAULT_CURSOR);
    private Cursor waitCursor = new Cursor (Cursor.WAIT_CURSOR);

    private ResidueActionBroadcaster residueActionBroadcaster = new ResidueActionBroadcaster();
    
    private String titleBase = "toRNAdo";

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

}
