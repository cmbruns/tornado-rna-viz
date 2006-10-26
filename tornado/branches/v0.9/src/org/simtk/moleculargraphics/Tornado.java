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
import javax.swing.undo.*;
import javax.swing.filechooser.FileFilter;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import org.simtk.gui.UndoRedoMechanism;
import org.simtk.moleculargraphics.cartoon.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.pdb.*;
import org.simtk.rnaml.RnamlDocument;
import org.simtk.rnaml.RnamlExportDocument;
import vtk.*;
import org.simtk.toon.secstruct.*;

/** 
 * @author Christopher Bruns
 * 
 * RNA manipulation application
 *
 */
public class Tornado extends MolApp 
// implements ResidueHighlightListener
{
    // private ResidueActionBroadcaster residueActionBroadcaster = new ResidueActionBroadcaster();
    protected ResidueHighlightBroadcaster residueHighlightBroadcaster = new ResidueHighlightBroadcaster();
    protected ResidueCenterBroadcaster residueCenterBroadcaster = new ResidueCenterBroadcaster();
    protected ActiveMoleculeBroadcaster activeMoleculeBroadcaster = new ActiveMoleculeBroadcaster();

    protected Color initialBackgroundColor = Color.white;
    // 
    // Public
    //
    
    public Color highlightColor = new Color(255, 240, 50); // Pale orange
    protected JPanel initialLoadStructurePanel;
    private String currentPath = ".";
    private String saveImagePath = ".";
    private LoadStructureDialog loadStructureDialog = new LoadStructureDialog(this);
    
    // protected ColorScheme yellowColorScheme = new ConstantColor(Color.yellow);
    // protected ColorScheme cyanColorScheme = new ConstantColor(Color.cyan);
    protected ColorScheme colorScheme = DefaultColorScheme.DEFAULT_COLOR_SCHEME;
    // protected ColorScheme highlightColorScheme = 
    //     new HighlightColorScheme(Color.yellow, colorScheme);

    // private MolecularCartoonClass.CartoonType cartoonType = 
    //     MolecularCartoonClass.CartoonType.WIRE_FRAME;
    // Class initialCartoonType = ResidueSpheres.class;
    private MoleculeCartoon currentCartoon;
    
    protected ToonRange toonRange = new ToonRange();
    protected List<SecondaryStructureClass.SourceType>  selectedSourceTypes = Arrays.asList(SecondaryStructureClass.SourceType.values());
    public Collection<SecondaryStructureClass.SourceType> getSelectedSourceTypes(){
    	if (selectedSourceTypes==null) return new ArrayList<SecondaryStructureClass.SourceType>();
    	else return selectedSourceTypes;
    }
    private UndoRedoMechanism undoRedoMechanism = new UndoRedoMechanism();
    
    Tornado() {
        setTitle("SimTK ToRNAdo: (no structures currently loaded)");
        
        // Test layered Pane
        // Result - awt Canvas can occlude vtkPanel, but cannot be transparent
        // java.awt.Canvas testPanel = new java.awt.Canvas();
        // testPanel.setBackground(Color.blue);
        // testPanel.setBounds(10,10,100,100);
        // getLayeredPane().add(testPanel, new Integer(2), 0);
        
        // try {currentCartoon = (MoleculeCartoon) initialCartoonType.newInstance();} 
        // catch (InstantiationException exc) {System.err.println(exc);}
        // catch (IllegalAccessException exc) {System.err.println(exc);}

        // loadNativeLibraries();
        
        // canvas = new Tornado3DCanvas(); // must create before menus

        initializeCartoonTypes();

        createMenuBar();

        JPanel panel = new JPanel();
        this.setIconImage(new ImageIcon(classLoader.getResource("images/tornado_icon.gif")).getImage());
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // 3D molecule canvas - must be created before menus are

        // Secondary structure canvas
        SecondaryStructureCanvas canvas2D = null;
        if (drawSecondaryStructure) {
            canvas2D = new SecondaryStructureCanvas(residueHighlightBroadcaster);
            residueHighlightBroadcaster.addResidueHighlightListener(canvas2D);

            // canvas.setMinimumSize(new Dimension(10,10));
            // canvas2D.setMinimumSize(new Dimension(10, 10));
            
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    canvas, canvas2D);
            splitPane.setResizeWeight(0.67);
            splitPane.setDividerSize(4);
            splitPane.setContinuousLayout(true);
 
            panel.add(splitPane);
        }
        else
            panel.add(canvas);
        
        // For the initial start up, Prompt the user to Load a structure 
        // from the main panel
        initialLoadStructurePanel = new JPanel();
        initialLoadStructurePanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        initialLoadStructurePanel.setLayout(new BoxLayout(initialLoadStructurePanel, BoxLayout.Y_AXIS));
        initialLoadStructurePanel.add(new JLabel("Click button below to choose a molecule structure:"));
        JButton loadButton = new JButton("Choose Molecule...");
        loadButton.addActionListener(new InitialLoadMoleculeAction());
        initialLoadStructurePanel.add(loadButton);
        initialLoadStructurePanel.setPreferredSize(new Dimension(500, 500));
        initialLoadStructurePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        canvas.setVisible(false);
        initialLoadStructurePanel.setVisible(true);
        panel.add(initialLoadStructurePanel);
        
        // Sequence area
        sequencePane = new SequencePane(residueHighlightBroadcaster, residueCenterBroadcaster, this);
        sequenceCartoonCanvas = new SequenceCartoonCanvas(residueHighlightBroadcaster, sequencePane.getSequenceCanvas());

        panel.add(sequenceCartoonCanvas);

        // The sequence pane is being very tricky in the layout
        // So try putting another panel behind it
        // sequencePanel = new Panel();
        // sequencePanel.setLayout(new BorderLayout());
        // sequencePanel.add(sequencePane, BorderLayout.SOUTH);
        // panel.add(sequencePanel);
        panel.add(sequencePane);
        
		messageArea = new JLabel();
        panel.add(messageArea);
		
        getContentPane().add(panel, BorderLayout.CENTER);
        
        pack();
        setSize(new Dimension(300,300));
        
        setVisible(true);
        
        if (useRotationThread) {
            rotationThread = new InertialRotationThread(this);
            rotationThread.setPriority(Thread.MIN_PRIORITY);
            rotationThread.rotationStyle = RotationStyle.NONE;
            rotationThread.start();
        }
                
        setMessage("No molecules are currently loaded");
        
        
        // Set up various event listeners
        residueHighlightBroadcaster.addResidueHighlightListener(sequencePane);
        // residueHighlightBroadcaster.addSelectionListener(canvas);
        residueHighlightBroadcaster.addResidueHighlightListener(sequenceCartoonCanvas);
        residueHighlightBroadcaster.addResidueHighlightListener((Tornado3DCanvas)canvas);
        // residueHighlightBroadcaster.addResidueHighlightListener(this);
        if (drawSecondaryStructure)
            residueHighlightBroadcaster.addResidueHighlightListener(canvas2D);
        
        activeMoleculeBroadcaster.addActiveMoleculeListener(sequenceCartoonCanvas);
        activeMoleculeBroadcaster.addActiveMoleculeListener(sequencePane);
        
        ((Tornado3DCanvas)canvas).setResidueHighlightBroadcaster(residueHighlightBroadcaster);
        ((Tornado3DCanvas)canvas).setResidueCenterBroadcaster(residueCenterBroadcaster);
        residueCenterBroadcaster.addResidueCenterListener((Tornado3DCanvas)canvas);
        residueCenterBroadcaster.addResidueCenterListener(sequencePane);
    }
    
    public static void main(String [] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {new Tornado();}
        });
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
    
    /** 
     * This must be done before createMenuBar()
     *
     */
    void initializeCartoonTypes() {
        // Add these in order of Decreasing coarseness        
        toonRange.add(new RangedToonType(
                "Molecule Ellipsoids (Coarsest)",
                MoleculeTensorCartoon.class,
                null));
        toonRange.add(new RangedToonType(
                "Molecule Blobs",
                MoleculeBlobs.class,
                null));
        toonRange.add(new RangedToonType(
                "Secondary Structures",
                RopeAndCylinder.class,
                new ImageIcon(classLoader.getResource("images/cylinder_icon.png")) ));
        toonRange.add(new RangedToonType(
                "Residue Balls",
                ResidueSpheres.class,
                null ));        
        toonRange.add(new RangedToonType(
                "Ribbons",                
                FineRibbonCartoon.class, 
                null ));        
        toonRange.add(new RangedToonType(
                "Bond Lines",
                WireFrame.class,
                null ));        
        toonRange.add(new RangedToonType(
                "Atom Balls",
                AtomSpheres.class,
                new ImageIcon(classLoader.getResource("images/nuc_fill_icon.png")) ));
        toonRange.add(new RangedToonType(
                "Bonds and Atoms (Finest)",
                BallAndStickCartoon.class,
                new ImageIcon(classLoader.getResource("images/po4_stick_icon.png")) ));
        
        // Set initial representation
        toonRange.setCurrentType(ResidueSpheres.class);
    }
    
    void createMenuBar() {
        
        // Prevent the vtkPanel from obscuring the JMenus
        // JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        JCheckBoxMenuItem checkItem;

        menu = new JMenu("File");
        menuBar.add(menu);

        menuItem = new JMenuItem("Load PDB Molecule...");
        menuItem.addActionListener(new LoadPDBAction());
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Save PNG Image...");
        menuItem.addActionListener(new SaveImageFileAction());
        // menuItem.addActionListener(new TestHighResOutputAction());
              menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Exit Tornado");
        menuItem.addActionListener(new QuitAction());
        menu.add(menuItem);


        menu = new JMenu("Edit");
        menuBar.add(menu);
        menu.add(new JMenuItem(undoRedoMechanism.getUndoAction()));
        menu.add(new JMenuItem(undoRedoMechanism.getRedoAction()));

        menuItem = new JMenuItem("Delete");
        menuItem.setEnabled(false);
        menu.add(menuItem);

        viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        
        JMenu cartoonMenu = new JMenu("Molecule Style");
        viewMenu.add(cartoonMenu);

        toonRange.createCartoonMenu(cartoonMenu);
        
        menu = new JMenu("Rotation");
        viewMenu.add(menu);

        ButtonGroup rotationGroup = new ButtonGroup();
        
        checkItem = new JCheckBoxMenuItem("Sit still (Stop)");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateNoneAction());
        checkItem.setState((rotationThread == null) || (rotationThread.rotationStyle == RotationStyle.NONE));
        rotationGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Gyrate (Wiggle)");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateNutateAction());
        checkItem.setState((rotationThread != null) && (rotationThread.rotationStyle == RotationStyle.NUTATE));
        rotationGroup.add(checkItem);
        menu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("Oscillate (Wag)");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateRockAction());
        checkItem.setState((rotationThread != null) && (rotationThread.rotationStyle == RotationStyle.ROCK));
        rotationGroup.add(checkItem);
        menu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("Rotate (Spin)");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateSpinAction());
        checkItem.setState((rotationThread != null) && (rotationThread.rotationStyle == RotationStyle.ROTATE));
        rotationGroup.add(checkItem);
        menu.add(checkItem);
        
        menu = new JMenu("Background Color");
        viewMenu.add(menu);
        ButtonGroup backgroundGroup = new ButtonGroup();

        Map<Color, String> bgColorNames = new LinkedHashMap<Color, String>();
        bgColorNames.put(Color.white, "White");
        bgColorNames.put(new Color(0.85f, 0.93f, 1.0f), "Sky");
        bgColorNames.put(Color.black, "Black");
        for (Color color : bgColorNames.keySet()) {
            String colorName = bgColorNames.get(color);

            checkItem = new JCheckBoxMenuItem(colorName);
            checkItem.setEnabled(true);
            checkItem.addActionListener(new BackgroundColorAction(color, colorName));
            checkItem.setState(color == initialBackgroundColor);
            backgroundGroup.add(checkItem);
            menu.add(checkItem);
        }

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

        checkItem = new JCheckBoxMenuItem("Left Eye");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new StereoLeftEyeAction());
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Right Eye");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new StereoRightEyeAction());
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Scan doubled shutter glasses");
        checkItem.setEnabled(false);
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        menu = new JMenu("Secondary Structure");
        menuBar.add(menu);
        
        menuItem = new JMenuItem("Load Secondary Structure File...");
        menuItem.setEnabled(true);
        menuItem.addActionListener(new LoadRnamlAction());
        menu.add(menuItem);

        menuItem = new JMenuItem("Write Secondary Structure File...");
        menuItem.setEnabled(true);
        menuItem.addActionListener(new WriteRnamlAction());
        menu.add(menuItem);
        
        //        
        menuItem = new JMenuItem("Compute Secondary Structure");
        menuItem.setEnabled(true);
        menuItem.addActionListener(new ComputeSecondaryStructureAction());
        menu.add(menuItem);     
        
        menuItem = new JMenu("Secondary Structure Source");
        menuItem.setEnabled(true);
        menu.add(menuItem);     
        
        ButtonGroup sourceGroup = new ButtonGroup();
    	JCheckBoxMenuItem sourceItem = new JCheckBoxMenuItem("All");        
    	sourceItem.setEnabled(true);
    	sourceItem.addActionListener(new SourceAction());
    	sourceItem.setState(true);
    	sourceGroup.add(sourceItem);
    	menuItem.add(sourceItem);
        menuItem.add(new JSeparator());
        for (SecondaryStructureClass.SourceType source: SecondaryStructureClass.SourceType.values()) {                
        	sourceItem = new JCheckBoxMenuItem(source.toString());
        	sourceItem.setEnabled(true);
        	sourceItem.addActionListener(new SourceAction(source));
        	sourceItem.setState(false);
        	sourceGroup.add(sourceItem);
        	menuItem.add(sourceItem);
        }

        
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

    class TestHighResOutputAction implements ActionListener {
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

                vtkRenderWindow bigWindow = new vtkRenderWindow();
                bigWindow.SetSize(10,10);
                vtkRendererCollection renderers = canvas.GetRenderWindow().GetRenderers();
                canvas.Lock();
                renderers.InitTraversal();
                for (int r = 0; r < renderers.GetNumberOfItems(); ++r) {
                    vtkRenderer renderer = renderers.GetNextItem();
                    bigWindow.AddRenderer(renderer);
                }
                wti.SetInput(bigWindow);
                wti.Update();
                renderers.InitTraversal();
                for (int r = 0; r < renderers.GetNumberOfItems(); ++r) {
                    vtkRenderer renderer = renderers.GetNextItem();
                    bigWindow.AddRenderer(renderer);
                }
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

    class InitialLoadMoleculeAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            initialLoadStructurePanel.setVisible(false);
            canvas.setVisible(true);
            (new LoadPDBAction()).actionPerformed(e);
        }
    }

    // 
    class GranularityAction implements ActionListener {
        protected int increment; // how much to change the Granularity
        GranularityAction(int increment) {this.increment = increment;}
        public void actionPerformed(ActionEvent e) {
            if (increment > 0) {
                if (toonRange.hasCoarser()) {
                    toonRange.stepCoarser();
                    Tornado.this.redrawCartoon();
                }
            }
            else if (increment < 0) {
                if (toonRange.hasFiner()) {
                    toonRange.stepFiner();
                    Tornado.this.redrawCartoon();
                }
            }
        }
    }

    // 
    class LoadRnamlAction implements ActionListener {
        JFileChooser rnamlFileChooser;
        String rnamlPath = "";

        public void actionPerformed(ActionEvent e) {
            // Check for RNAML secondary structures
            // needs to be made more robust & expansive
            // looks in local direrctory, doesn't know loc of PDB
        	MoleculeCollection molecules = Tornado.this.moleculeCollection;
            boolean haveNucleic = false;
            for (Molecule m : molecules.molecules()) 
                if (m instanceof NucleicAcid) haveNucleic = true;
            if (!haveNucleic){
//            	TODO should add an error alert here as well
            	return;
            }
            
            if ((rnamlFileChooser == null)){
            	rnamlPath = Tornado.this.currentPath;
            	rnamlFileChooser = new JFileChooser(Tornado.this.currentPath);            
                rnamlFileChooser.setFileFilter(new RnamlFilter());
            }
		    
            if ((molecules.getPdbId() != null) )  {
                String rnamlFileName = molecules.getPdbId()+".pdb.xml";
                
                File curDir = rnamlFileChooser.getCurrentDirectory();
                java.util.List dirList = Arrays.asList(curDir.list());
                if (dirList.contains(rnamlFileName)){
                	rnamlFileChooser.setSelectedFile(new File(rnamlFileName));
                }
            }

            
            int returnVal = rnamlFileChooser.showOpenDialog(Tornado.this);
		    if (returnVal != JFileChooser.APPROVE_OPTION){
		    	return;//user canceled
		    }
            try {
                File rnamlFile = rnamlFileChooser.getSelectedFile();
                RnamlDocument rnamlDoc = new RnamlDocument(rnamlFile, molecules);
                rnamlDoc.importSecondaryStructures();
                Tornado.this.redrawCartoon();
            } 
            catch (org.jdom.JDOMException exc) {
                exc.printStackTrace();
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
            catch (NullPointerException exc) {
                exc.printStackTrace();
            }

        }
    }

    // 
    class WriteRnamlAction implements ActionListener {
        JFileChooser rnamlFileChooser;
        String rnamlPath = "";
        
        public void actionPerformed(ActionEvent e) {
            RnamlExportDocument rnamlDoc;
            // Check for RNAML secondary structures
            // needs to be made more robust & expansive
            // looks in local direrctory, doesn't know loc of PDB
        	MoleculeCollection molecules = Tornado.this.moleculeCollection;
            boolean haveNucleic = false;
            for (Molecule m : molecules.molecules()) 
                if (m instanceof NucleicAcid) haveNucleic = true;
            if (!haveNucleic){
//            	TODO should add an error alert here as well
            	return;
            }
            
            rnamlDoc = new RnamlExportDocument("toRNAdo", tornadoVersion, Tornado.this.moleculeCollection);
            
            if ((rnamlFileChooser == null)){
            	rnamlPath = Tornado.this.currentPath;
            	rnamlFileChooser = new JFileChooser(Tornado.this.currentPath);            
                rnamlFileChooser.setFileFilter(new RnamlFilter());
            }
		    
            if ((molecules.getPdbId() != null) )  {
                String rnamlFileName = molecules.getPdbId()+".pdb.tornado.xml";
                rnamlFileChooser.setSelectedFile(new File(rnamlFileName));
            }

            
            int returnVal = rnamlFileChooser.showSaveDialog(Tornado.this);
		    if (returnVal != JFileChooser.APPROVE_OPTION){
		    	return;//user canceled
		    }
            File rnamlFile = rnamlFileChooser.getSelectedFile();

		    if (rnamlFile.exists()) {
                int response = JOptionPane.showConfirmDialog(null, "" + rnamlFile + "\nFile exists.  Overwrite?");
                if ( (response == JOptionPane.CANCEL_OPTION) ||
                     (response == JOptionPane.NO_OPTION)) {
                    return;
                }
            }

		    
		    
		    
		    try {
                rnamlDoc.writeTo(rnamlFile);
            }
            catch (NullPointerException exc) {
                exc.printStackTrace();
            }

        }
    }

    
	private class RnamlFilter extends FileFilter implements FilenameFilter{
		public boolean accept(File dir, String name) {
	        String suffix = getExtension(name);
	        if (suffix.equals("xml") ||
	               suffix.equals("rnaml")) return true;
	        else return false;
		}
		
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
	        String path = f.getPath();
	        String FS = System.getProperty("file.separator");
	        int index = path.lastIndexOf(FS);
	        currentPath = path.substring(0, index);
	        String name = path.substring(index+1);
	        return accept(new File(currentPath), name);
		}
		
		public String getDescription() {
			return "rnaml (and other xml) files";
		}
		
		private String getExtension(String path) {
			String suffix = "";
			int index = path.lastIndexOf(".");
			if (index != -1) {
				suffix = path.substring(index+1).toLowerCase();
			}
			return suffix;
		}

	}
	
    class ComputeSecondaryStructureAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (Molecule molecule : Tornado.this.moleculeCollection.molecules()) {
                if (!(molecule instanceof NucleicAcid)) continue;
                NucleicAcid nucleicAcid = (NucleicAcid) molecule;
                
                Collection<SecondaryStructure> structs = nucleicAcid.secondaryStructures();
                
                for (BasePair basePair : nucleicAcid.identifyBasePairs())
                    structs.add(basePair);
                
                for (Duplex duplex : nucleicAcid.identifyHairpins())
                    structs.add(duplex);
            }
            Tornado.this.redrawCartoon();
        }
    }

    class SourceAction implements ActionListener {
        protected List<SecondaryStructureClass.SourceType> mySources; 
        protected SourceAction() {mySources = Arrays.asList(SecondaryStructureClass.SourceType.values());}
        protected SourceAction(SecondaryStructureClass.SourceType mySource) {mySources = Arrays.asList(mySource);}
        public void actionPerformed(ActionEvent e) {
        	Tornado.this.selectedSourceTypes = mySources;
        	for (Molecule molecule : Tornado.this.moleculeCollection.molecules()){
        		molecule.setDisplaySourceTypes(Tornado.this.getSelectedSourceTypes());
        	}
        	Tornado.this.redrawCartoon();
        }
    }
    
    class BackgroundColorAction extends AbstractAction {
        Color color;
        String colorName;
        BackgroundColorAction(Color c, String colorName) {
            color = c;
            this.colorName = colorName;
        }
        public void actionPerformed(ActionEvent e) {
            Color oldColor = getBackgroundColor();
            Color newColor = color;
            
            if (oldColor.equals(newColor)) return;
            
            setBackgroundColor(newColor);

            // Permit "undo" of background color selection
            UndoableEdit edit = new BackgroundColorEdit(oldColor, newColor, colorName);
            undoRedoMechanism.addEdit(edit);
        }
        
        // Undoable actions need to create UndoableEdit objects
        class BackgroundColorEdit extends AbstractUndoableEdit {
            private Color startColor;
            private Color endColor;
            private String newColorName;
            BackgroundColorEdit(Color startColor, Color endColor, String newColorName) {
                this.startColor = startColor;
                this.endColor = endColor;
                this.newColorName = newColorName;
            }
            
            public void redo() {
                super.redo();
                setBackgroundColor(endColor);
            }
            public void undo() {
                super.undo();
                setBackgroundColor(startColor);
            }
            public String getPresentationName() {
                if (newColorName == null)
                    return "Set background color";
                else
                    return "Set background to " + newColorName;
            }
            public String getRedoPresentationName() {
                return "Redo " + getPresentationName();
            }
            public String getUndoPresentationName() {
                return "Undo " + getPresentationName();
            }
        }
    }

    class CartoonAction implements ActionListener {
        Class cartoonClass = null;

        CartoonAction(Class cartoonClass) {
            this.cartoonClass = cartoonClass;
        }

        public void actionPerformed(ActionEvent e) {
            setWait("Calculating geometry...");
            
            
            toonRange.setCurrentType(cartoonClass);
            currentCartoon = toonRange.createCartoon();

            currentCartoon.add(moleculeCollection);
            currentCartoon.colorToon(colorScheme);

            if (currentCartoon.vtkActors().size() > 0) {
                canvas.clear();
                canvas.add(currentCartoon);
            }
            else {
                // TODO - alert user that there was no geometry
            }

            unSetWait("Geometry computed.");
        }
    }

    void redrawCartoon(){
    	(new CartoonAction(toonRange.currentToonType.toonClass)).actionPerformed(new ActionEvent(this, 0, ""));
    }
    
    class RotateNoneAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.rotationStyle = RotationStyle.NONE;
            rotationThread.interrupt();
        }
    }

    class RotateNutateAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.rotationStyle = RotationStyle.NUTATE;
            rotationThread.pauseRotation = false;
            rotationThread.initialize();
            rotationThread.interrupt();
        }
    }

    class RotateRockAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.rotationStyle = RotationStyle.ROCK;
            rotationThread.pauseRotation = false;
            rotationThread.initialize();
            rotationThread.interrupt();
        }
    }

    class RotateSpinAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.rotationStyle = RotationStyle.ROTATE;
            rotationThread.pauseRotation = false;
            rotationThread.initialize();
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

    class StereoLeftEyeAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.setStereoLeftEye();
        }
    }

    class StereoRightEyeAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            canvas.setStereoRightEye();
        }
    }

    class LoadStructureDialog extends MoleculeAcquisitionMethodDialog {
        /**
		 * 
		 */

		LoadStructureDialog(JFrame f) {
            super(f, null, Tornado.this.currentPath);
            setLocationRelativeTo(f);
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

            if (haveNucleic) {                    
                // Compute secondary structure using Tornado methods
            	//new ComputeSecondaryStructureAction().actionPerformed(new ActionEvent(this, 0, ""));
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
            loadStructureDialog.setLocationRelativeTo(Tornado.this);
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

        setMessage("Read " + molecules.atoms().size() + " atoms, in " +
                molecules.molecules().size() + " molecules");
        
        moleculeCollection = molecules;
        
        updateTitleBar();

        for (Molecule molecule : molecules.molecules()) {
            molecule.setDisplaySourceTypes(Tornado.this.getSelectedSourceTypes());
        }
        
        
        // Create graphical representation of the molecule
        Tornado.this.redrawCartoon();

        // Display sequence of first molecule that has a sequence
        residueHighlightBroadcaster.fireUnhighlightResidues();
        BiopolymerClass bp = null;
        // for (Molecule molecule : molecules.molecules()) {
        for (Iterator<Molecule> i1 = molecules.molecules().iterator(); i1.hasNext();) {
            Molecule molecule = i1.next();
            if (molecule instanceof BiopolymerClass) {
                bp = (BiopolymerClass) molecule;
                
                activeMoleculeBroadcaster.fireSetActiveMolecle(molecule);
                
                break; // only put the sequence of the first molecule with a sequence
            }
        }

        // TODO - create one subroutine for updating the sequences
        // maybe in the ResidueSelector interface
        
        // Center camera on new molecule
        // Vector3D com = molecules.getCenterOfMass();
        // canvas.GetRenderer().GetActiveCamera().SetFocalPoint(com.getX(), com.getY(), com.getZ());
        canvas.centerByBoundingBox();
        canvas.scaleByBoundingBox();

        // Helps display of second and subsequent structure loads
        // TODO (maybe repaint() needs to be called first?)
        canvas.resetCameraClippingRange();

        sequencePane.repaint();
        sequenceCartoonCanvas.repaint();
        repaint();
        canvas.repaint();
    }

    class SaveImageFileAction implements ActionListener {
        JFileChooser saveImageFileChooser;
        String saveImagePath = ".";

        public void actionPerformed(ActionEvent e) {
            if ((saveImageFileChooser == null)||(!saveImagePath.equals(Tornado.this.saveImagePath))){
            	saveImagePath = Tornado.this.saveImagePath;
                saveImageFileChooser = new JFileChooser(Tornado.this.saveImagePath);
                saveImageFileChooser.addChoosableFileFilter(new PngFileFilter());
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
                wti.SetMagnification(3);
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
        
        class PngFileFilter extends FileFilter {
            private String getExtension(File f) {
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');

                if (i > 0 &&  i < s.length() - 1) {
                    ext = s.substring(i+1).toLowerCase();
                }
                return ext;
            }
            public boolean accept(File f) {
                if (f.isDirectory()) return true;

                String extension = getExtension(f);
                if (extension == null) return false;
                if (extension.equals("png")) return true;
                
                return false;
            }
            public String getDescription() {return "PNG Image files";}
        }
    }
    
    class AboutTornadoAction implements ActionListener {
        String aboutString = 
             " SimTK ToRNAdo version " + version.TornadoVersion.VERSION + "\n"+
             " (using SimTK shared Java version " + version.SimtkSharedJavaVersion.VERSION + ")\n"+
             " Copyright (c) 2005-2006, Christopher M. Bruns and Stanford University. \n"+
             " All rights reserved. \n"+
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
            JOptionPane.showMessageDialog(null, aboutString, "About SimTK ToRNAdo", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public void highlightResidue(Residue residue, Color color) {
        if (residue == null) setMessage(" ");
        else {
            // currentCartoon.colorToon(currentHighlightedResidue, colorScheme);
            
            // currentCartoon.colorToon(residue, highlightColorScheme);
            // for (Atom atom : residue.)
            
            // canvas.repaint();

            setMessage("Residue " + residue.getResidueName() + 
                " (" + residue.getOneLetterCode() + ") " + residue.getResidueNumber());
            currentHighlightedResidue = residue;
            
        }
    }
    
    public void unhighlightResidues() {
        setMessage(" "); // Use a space, otherwise message panel collapses
        currentHighlightedResidue = null;
    }
    public void unhighlightResidue(Residue r) {
        if (currentHighlightedResidue == r) {
            setMessage(" "); // Use a space, otherwise message panel collapses
            currentHighlightedResidue = null;
        }
    }
    public void clearResidues() {
        currentHighlightedResidue = null;
    }
    
    protected volatile boolean userIsInteracting = false;
    public synchronized boolean getUserIsInteracting() {
        return userIsInteracting;
    }    
    public synchronized void flushUserIsInteracting() {
        userIsInteracting = false;
    }
    public synchronized void lubricateUserInteraction() {
        userIsInteracting = true;
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

    // private JMenu cartoonMenu = null;
    private JMenu viewMenu = null;

    private static String tornadoVersion = version.TornadoVersion.VERSION;
    
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

    
    private String titleBase = "SimTK ToRNAdo";

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

    class RangedToonType {
        private String name;
        private Class toonClass;
        private ImageIcon icon;
        private JCheckBoxMenuItem checkItem;
        
        RangedToonType(String name, Class toonClass, ImageIcon icon) {
            this.name = name;
            this.toonClass = toonClass;
            this.icon = icon;
        }
        
        void setCheckItem(JCheckBoxMenuItem item) {
            checkItem = item;
        }
    }
    
    /**
     *  
      * @author Christopher Bruns
      * 
      * ToonRange manages an ordered list of molecule
      * representations.  The order of representations must
      * be from most coarse to most fine-grained.  ToonRange
      * also manages the status of a pull down menu by which
      * representations may be selected.
     */
    class ToonRange {
        private java.util.List<RangedToonType> toons = new Vector<RangedToonType>();
        private RangedToonType currentToonType = null;
        private Map<Class, RangedToonType> classTypes = new HashMap<Class, RangedToonType>();
        private Map<RangedToonType, Integer> typeIndices = new HashMap<RangedToonType, Integer>();
        private JMenuItem coarserMenu = new JMenuItem("Coarser");
        private JMenuItem finerMenu = new JMenuItem("Finer");

        public MoleculeCartoon createCartoon() {
            MoleculeCartoon answer = null;

            try {
                answer = (MoleculeCartoon) currentToonType.toonClass.newInstance();
            }
            catch (InstantiationException exc) {exc.printStackTrace();}
            catch (IllegalAccessException exc) {exc.printStackTrace();}

            return answer;
        }
        
        boolean hasCoarser() {
            int index = typeIndices.get(currentToonType);
            return (index > 0);
        }
        
        boolean hasFiner() {
            int index = typeIndices.get(currentToonType);
            return (index < (toons.size() - 1));
        }

        public void updateMenus() {
            if (hasCoarser()) coarserMenu.setEnabled(true);
            else coarserMenu.setEnabled(false);
            
            if (hasFiner()) finerMenu.setEnabled(true);
            else finerMenu.setEnabled(false);
            
            currentToonType.checkItem.setSelected(true);
        }
        
        public void stepCoarser() {
            if (hasCoarser()) {
                int index = typeIndices.get(currentToonType);
                currentToonType = toons.get(index - 1);
            }
            
            updateMenus();
        }
        
        public void stepFiner() {
            if (hasFiner()) {
                int index = typeIndices.get(currentToonType);
                currentToonType = toons.get(index + 1);                
            }

            updateMenus();
        }
        
        public void add(RangedToonType toon) {
            toons.add(toon);

            classTypes.put(toon.toonClass, toon);
            typeIndices.put(toon, toons.size() - 1);

            if (currentToonType == null) currentToonType = toon;
        }
        
        public void setCurrentType(Class cartoonType) {
            RangedToonType type = classTypes.get(cartoonType);
            if (type != null)
                currentToonType = type;
        }
        
        public void createCartoonMenu(JMenu parent) {
            coarserMenu.setEnabled(true);
            coarserMenu.addActionListener(new GranularityAction(1));
            parent.add(coarserMenu);
            
            finerMenu = new JMenuItem("Finer");
            finerMenu.setEnabled(true);
            finerMenu.addActionListener(new GranularityAction(-1));
            parent.add(finerMenu);
            
            parent.add(new JSeparator());

            ButtonGroup cartoonGroup = new ButtonGroup();
            
            for (RangedToonType toon : toons()) {                
                String description = toon.name;
                ImageIcon imageIcon = toon.icon;
                Class cartoonType = toon.toonClass;

                JCheckBoxMenuItem checkItem;

                if (imageIcon != null)
                    checkItem = new JCheckBoxMenuItem(description, imageIcon);
                else
                    checkItem = new JCheckBoxMenuItem(description);
                
                toon.setCheckItem(checkItem);
                
                checkItem.setEnabled(true);
                checkItem.addActionListener(new CartoonAction(cartoonType));
                checkItem.setState(toon == currentToonType);
                cartoonGroup.add(checkItem);
                parent.add(checkItem);
            }
        }
        
        public java.util.List<RangedToonType> toons() {return toons;}
    }
    
}
