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
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.atomicstructure.Atom;
import org.simtk.atomicstructure.PDBNitrogen;
import org.simtk.atomicstructure.PDBOxygen;
import org.simtk.geometry3d.*;

import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * RNA manipulation application
 *
 */
public class Tornado extends JFrame 
implements ResidueSelector 
{
    public static final long serialVersionUID = 1L;
    Tornado3DCanvas canvas;
    SequencePane sequencePane;
    SequenceCartoonCanvas sequenceCartoonCanvas;
    Panel sequencePanel;
    JLabel messageArea;

    // Stop auto rotation if the user is trying to do something
    volatile boolean userIsInteracting = false;
    
    public Color highlightColor = new Color(255, 240, 50); // Pale orange
    
    // Tables for helping sequence window talk to structure window
    Hashtable<Residue, vtkProp> residueHighlightProps = new Hashtable<Residue, vtkProp>();
    // Hashtable<Integer, Residue> positionResidues = new Hashtable<Integer, Residue>();
    vtkProp currentHighlight;

    boolean useRotationThread = true;
    InertialRotationThread rotationThread;
    
    // TODO - move all 3D cartoon work to Tornado3DCanvas
    enum CartoonType {
        SPACE_FILLING,
        BALL_AND_STICK,
        ROPE_AND_CYLINDER
    };
    CartoonType currentCartoonType = CartoonType.ROPE_AND_CYLINDER;
    MolecularCartoon currentCartoon = new RopeAndCylinderCartoon();
    MoleculeCollection moleculeCollection = new MoleculeCollection();

    Cursor handCursor = new Cursor (Cursor.HAND_CURSOR);
    Cursor defaultCursor = new Cursor (Cursor.DEFAULT_CURSOR);
    Cursor waitCursor = new Cursor (Cursor.WAIT_CURSOR);

    Tornado() {
        super("ToRNAdo from SimTK.org");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMenuBar();

        JPanel panel = new JPanel();
        
        // With the addition of a separate sequence cartoon, it is probably time
        // for a fancy layout
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // each on its own row
        gbc.weightx = 1.0; // Everybody stretches horizontally
        gbc.fill = GridBagConstraints.HORIZONTAL; // stretch horizontally
        
        // 3D molecule canvas
        canvas = new Tornado3DCanvas(this);
        gbc.fill = GridBagConstraints.BOTH; // stretch horizontally and vertically
        gbc.weighty = 1.0;
        gridbag.setConstraints(canvas, gbc);
        panel.add(canvas, gbc);

        // Sequence area
        sequencePane = new SequencePane(this);
        sequenceCartoonCanvas = new SequenceCartoonCanvas(this, sequencePane.getSequenceCanvas());

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
		
        add(panel, BorderLayout.CENTER);
        
        pack();
        
        setVisible(true);
        
        if (useRotationThread) {
            rotationThread = new InertialRotationThread(this);
            rotationThread.setPriority(Thread.MIN_PRIORITY);
            rotationThread.start();
        }
                
        setMessage("No molecules are currently loaded");
        
    }
    
    public static void main(String[] args) {
        Tornado tornadoFrame = new Tornado();
    }
    
    /**
     * compare base pairs in the external file to those in the molecule structre
     * @param fileName
     */
    public void compareHbonds(String fileName, RNA rna) {
        HashSet<BasePair> loadedBasePairs = new HashSet<BasePair>();
        
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
                if (line.contains("BEGIN_base-pair")) {
                    parsingBasePairs = true;
                    continue LINE;
                }
                if (!parsingBasePairs) continue LINE;
                if (line.contains("END_base-pair")) {
                    parsingBasePairs = false;
                    break LINE;
                }
                
                //     1_182, B:    96 G-C   278 B: +/+ cis         XIX
                StringTokenizer tokenizer = new StringTokenizer(line);
                String token;
                token = tokenizer.nextToken(); // residue index
                token = tokenizer.nextToken(); // chain
                token = tokenizer.nextToken(); // first residue number
                int firstResidueNumber = new Integer(token);
                token = tokenizer.nextToken(); // one letter codes
                token = tokenizer.nextToken(); // second residue number
                int secondResidueNumber = new Integer(token);

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

        // TODO - do something with the information
        HashSet<BasePair> myBasePairs = new HashSet<BasePair>();
        for (BasePair bp : rna.identifyBasePairs()) myBasePairs.add(bp);
        
        // How many are in common?
        int commonCount = 0;
        int uniqueToSelfCount = 0;
        int uniqueToLoadedCount = 0;
        System.out.println("Unique to Tornado: ");
        reportBasePairGeometry(myBasePairs);
        for (BasePair bp : myBasePairs) {
            if (loadedBasePairs.contains(bp)) commonCount++;
            else {
                uniqueToSelfCount ++;
            }
        }
        System.out.println("Unique to RNAMLView: ");
        reportBasePairGeometry(loadedBasePairs);
        for (BasePair bp : loadedBasePairs) {
            if (myBasePairs.contains(bp)) {}
            else {
                uniqueToLoadedCount ++;
            }
        }
        
        System.out.println("" + commonCount + " base pairs in common.");
        System.out.println("" + uniqueToLoadedCount + " base pairs unique to RNAMLView.");
        System.out.println("" + uniqueToSelfCount + " base pairs unique to Tornado.");

    }
    void reportBasePairGeometry(HashSet<BasePair> basePairs) {
        TreeSet<Double> centroidDistances = new TreeSet<Double>();
        TreeSet<Double> planeAngles = new TreeSet<Double>();
        TreeSet<Double> planeDistances = new TreeSet<Double>();
        TreeSet<Double> atomDistances = new TreeSet<Double>();

        for (BasePair bp : basePairs) {
            Molecule base1 = bp.getResidue1().get(Nucleotide.baseGroup);
            Molecule base2 = bp.getResidue2().get(Nucleotide.baseGroup);
            Vector3D centroid1 = base1.getCenterOfMass();
            Vector3D centroid2 = base2.getCenterOfMass();
            Plane3D plane1 = base1.bestPlane3D();
            Plane3D plane2 = base2.bestPlane3D();
            
            double distance = centroid1.distance(centroid2);
            centroidDistances.add(distance);
            System.out.println("   centroid distance = " + distance);
            
            double angle = Math.abs(Math.acos(plane1.getNormal().dot(plane2.getNormal())) * 180.0 / Math.PI);
            if (angle > 90) angle = 180 - angle;
            planeAngles.add(angle);
            System.out.println("   plane angle = " + angle + " degrees.");
            
            double planeDistance1 = plane1.distance(centroid2);
            double planeDistance2 = plane2.distance(centroid1);
            planeDistances.add(planeDistance1);
            planeDistances.add(planeDistance2);
            System.out.println("   plane distances are " + planeDistance1 + " and " + planeDistance2);
    
            // Touching criterion
            double minDistance = 1000;
            for (Atom atom1 : bp.getResidue1().getAtoms()) {
                if (! ((atom1 instanceof PDBOxygen) || (atom1 instanceof PDBNitrogen))) continue;
                for (Atom atom2 : bp.getResidue2().getAtoms()) {
                    if (! ((atom2 instanceof PDBOxygen) || (atom2 instanceof PDBNitrogen))) continue;
                    double testDistance = atom1.distance(atom2);
                    if (testDistance < minDistance) minDistance = testDistance;
                }
            }
            atomDistances.add(minDistance);
            System.out.println("   closest atomic distance = " + minDistance);
        }

        
        int cutoffIndex = (int)((centroidDistances.size() - 1.0) * 0.95);

        double cutoffDistance = (Double) centroidDistances.toArray()[cutoffIndex];
        System.out.println("Cutoff centroid distance = " + cutoffDistance);
        
        double cutoffAngle = (Double) (planeAngles.toArray()[cutoffIndex]);
        System.out.println("Cutoff plane angle = " + cutoffAngle);

        double cutoffPlaneDistance = (Double) (planeDistances.toArray()[2 * cutoffIndex]);
        System.out.println("Cutoff plane distance = " + cutoffPlaneDistance);

        double cutoffAtomDistance = (Double) (atomDistances.toArray()[cutoffIndex]);
        System.out.println("Cutoff atom distance = " + cutoffAtomDistance);
    }
    
    // Show the user that some waiting time is needed
    public void setWait(String message) {
        setCursor(waitCursor);
        canvas.setCursor(waitCursor);
        // pauseRotation();
        if (message == null) setMessage("Please wait...");
        else setMessage(message + " "); // Extra space to preserve message area size
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
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        JCheckBoxMenuItem checkItem;

        menu = new JMenu("Tornado");
        menuBar.add(menu);
        menuItem = new JMenuItem("Exit Tornado");
        menuItem.addActionListener(new QuitAction());
        menu.add(menuItem);


        menu = new JMenu("File");
        menuBar.add(menu);
        menuItem = new JMenuItem("Load PDB Molecule...");
        menuItem.addActionListener(new LoadPDBFileAction());
        menu.add(menuItem);

        menuItem = new JMenuItem("Save PNG Image...");
        menuItem.addActionListener(new SaveImageFileAction());
        menu.add(menuItem);

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

        
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        menu = new JMenu("Molecule Style");
        viewMenu.add(menu);

        ButtonGroup cartoonGroup = new ButtonGroup();

        checkItem = new JCheckBoxMenuItem("Ball and Stick");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new CartoonAction(CartoonType.BALL_AND_STICK));
        checkItem.setState(currentCartoonType == CartoonType.BALL_AND_STICK);
        cartoonGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Space-filling Atoms");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new CartoonAction(CartoonType.SPACE_FILLING));
        checkItem.setState(currentCartoonType == CartoonType.SPACE_FILLING);
        cartoonGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Rope and Cylinder");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new CartoonAction(CartoonType.ROPE_AND_CYLINDER));
        checkItem.setState(currentCartoonType == CartoonType.ROPE_AND_CYLINDER);
        cartoonGroup.add(checkItem);
        menu.add(checkItem);

        menu = new JMenu("Rotation");
        viewMenu.add(menu);

        ButtonGroup rotationGroup = new ButtonGroup();
        
        checkItem = new JCheckBoxMenuItem("None / Sit still");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateNoneAction());
        checkItem.setState(false);
        rotationGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Rock");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateRockAction());
        checkItem.setState(true);
        rotationGroup.add(checkItem);
        menu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("Spin");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new RotateSpinAction());
        checkItem.setState(false);
        rotationGroup.add(checkItem);
        menu.add(checkItem);
        
        menu = new JMenu("Stereoscopic 3D");
        viewMenu.add(menu);

        ButtonGroup stereoscopicOptionsGroup = new ButtonGroup();
        
        checkItem = new JCheckBoxMenuItem("Off / Ordinary monoscopic");
        checkItem.setEnabled(true);
        checkItem.addActionListener(new StereoOffAction());
        checkItem.setState(true);
        stereoscopicOptionsGroup.add(checkItem);
        menu.add(checkItem);

        checkItem = new JCheckBoxMenuItem("Red/Blue glasses", new ImageIcon("rbglasses.GIF"));
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
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.add(new JSeparator());
        menuItem = new JMenuItem("Web Links:");
        menuItem.setEnabled(false);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("  Report a program bug...");
        menuItem.addActionListener(new BrowserLaunchAction
                // Doesn't work with those "&" characters in the URL...
                // TODO - get a better url for this
                ("https://simtk.org/tracker/?group_id=12"));
        menu.add(menuItem);

        menuItem = new JMenuItem("  Request a new program feature...");
        menuItem.addActionListener(new BrowserLaunchAction
                ("https://simtk.org/tracker/?group_id=12"));
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }
    
    class QuitAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);  // terminate this program
        }
    }

    class CartoonAction implements ActionListener {
        CartoonType type;
        CartoonAction(CartoonType t) {
            type = t;
        }
        public void actionPerformed(ActionEvent e) {
            setWait("Calculating geometry...");
            
            currentCartoonType = type;
            switch (type) {
                case BALL_AND_STICK:
                    currentCartoon = new BallAndStickCartoon();
                    break;
                case SPACE_FILLING:
                    currentCartoon = new AtomSphereCartoon();
                    break;
                case ROPE_AND_CYLINDER:
                    currentCartoon = new RopeAndCylinderCartoon();
                    break;
                default:
                    currentCartoon = new BallAndStickCartoon();
                    break;
            }
            vtkAssembly assembly = currentCartoon.represent(moleculeCollection, 1.0, null);
            if (assembly != null) {
                canvas.Lock();
                canvas.GetRenderer().RemoveAllProps();
                
                // AddProp deprecated in vtk 5.0
                // try{canvas.GetRenderer().AddViewProp(assembly);}
                // catch(NoSuchMethodError exc){canvas.GetRenderer().AddProp(assembly);}
                canvas.GetRenderer().AddProp(assembly);

                canvas.UnLock();
            }

            // Update residue highlights
            for (Molecule molecule : moleculeCollection.molecules()) {
                if (molecule instanceof Biopolymer) {
                    Biopolymer bp = (Biopolymer) molecule;
                    canvas.Lock();
                    canvas.clearResidueHighlights();
                    for (Residue residue : bp.residues()) {
                        vtkProp highlight = currentCartoon.highlight(residue, highlightColor);
                        canvas.addResidueHighlight(residue, highlight);                                
                    }
                    canvas.UnLock();
                    break; // only put the sequence of the first molecule with a sequence
                }
            }
            
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
            rotationThread.rock = true;
            rotationThread.currentAngle = 0.0;
            rotationThread.interrupt();
        }
    }

    class RotateSpinAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rotationThread.sitStill = false;
            rotationThread.pauseRotation = false;
            rotationThread.rock = false;
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
        String url;
        BrowserLaunchAction(String u) {url = u;}
        public void actionPerformed(ActionEvent e) {
            // TODO - this will only work for windows, make it work for others too
            try {Runtime.getRuntime().exec("cmd /c start " + url);} 
            catch (IOException exc) {                
                String[] options = {"Bummer!"};
                JOptionPane.showOptionDialog(
                        null, 
                        "Problem opening browser to page " + url + "\n" + exc, 
                        "Web URL error!",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                        null, options, options[0]);
            }
        }
    }

    class LoadPDBFileAction implements ActionListener {
        JFileChooser loadPDBFileChooser;

        public class PDBFilter extends javax.swing.filechooser.FileFilter {
            public String getExtension(File f) {
                if(f != null) {
                    String filename = f.getName();
                    int i = filename.lastIndexOf('.');
                    if(i>0 && i<filename.length()-1) {
                    return filename.substring(i+1).toLowerCase();
                    };
                }
                return null;
           }
           public boolean accept(File f) {
                if (f.isDirectory()) {return true;}
                String extension = getExtension(f);
                if (extension == null) return false;
                extension = extension.toLowerCase();
                if (extension != null)
                    if (
                        extension.equals("pdb") ||
                        extension.equals("pdb1") ||
                        extension.equals("pdb2") 
                        )
                            return true;
                return false;
            }

            public String getDescription() {return "Protein Data Bank (PDB) structure files";}
        }
        
        public void actionPerformed(ActionEvent e) {
            if (loadPDBFileChooser == null) {
                loadPDBFileChooser = new JFileChooser();
                PDBFilter filter = new PDBFilter();
                loadPDBFileChooser.setFileFilter(filter);
            }
            
            // canvas.Lock();
            
            int returnVal = loadPDBFileChooser.showOpenDialog(Tornado.this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = loadPDBFileChooser.getSelectedFile();
                try {
                    setWait("Loading file " + file.getCanonicalPath() + " ...");
                    FileInputStream inStream = new FileInputStream(file);
                    MoleculeCollection molecules = new MoleculeCollection();
                    molecules.loadPDBFormat(inStream);

                    setMessage("Read " + molecules.getAtomCount() + " atoms, in " +
                            molecules.getMoleculeCount() + " molecules, from file " +
                            file.getCanonicalPath());
                    
                    moleculeCollection = molecules;
                    
                    // Create graphical representation of the molecule
                    (new CartoonAction(currentCartoonType)).actionPerformed(new ActionEvent(this, 0, ""));

                    // Center camera on new molecule
                    Vector3D com = molecules.getCenterOfMass();
                    canvas.GetRenderer().GetActiveCamera().SetFocalPoint(com.getX(), com.getY(), com.getZ());

                    // Display sequence of first molecule that has a sequence
                    clearResidues();
                    Biopolymer bp = null;
                    for (Molecule molecule : molecules.molecules()) {
                        if (molecule instanceof Biopolymer) {
                            bp = (Biopolymer) molecule;
                            
                            for (Residue residue : bp.residues())
                                addResidue(residue);
                            
                            break; // only put the sequence of the first molecule with a sequence
                        }
                    }
                    unSetWait("Molecule loaded (" + file.getName() + ")");
                    // TODO - create one subroutine for updating the sequences
                    // maybe in the ResidueSelector interface
                    canvas.repaint();
                    sequencePane.repaint();
                    sequenceCartoonCanvas.repaint();
                    repaint();
                    
                    // This is temporary
                    // if ( (bp != null) && file.getName().contains("1x8w") ) {
                    //     compareHbonds("1x8w.pdb2.out", (RNA) bp);
                    // }
                }
                
                catch (FileNotFoundException exc) {
                    unSetWait("File not found. (" + file.getName() + ")");
                    String[] options = {"Bummer!"};
                    JOptionPane.showOptionDialog(null, "No such file: " + file, "PDB File Error!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, options, options[0]);
                }
                catch (IOException exc) {
                    unSetWait("File error. (" + file.getName() + ")");
                    String[] options = {"Bummer!"};
                    JOptionPane.showOptionDialog(null, "Problem reading file: " + file + ": " + exc, "PDB File Error!",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, options, options[0]);
                }

            }

            // canvas.UnLock();
        }
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
    
    Residue currentHighlightedResidue;
    public void highlight(Residue residue) {
        currentHighlightedResidue = residue;
        if (residue == null) return;
        sequencePane.highlight(residue);
        sequenceCartoonCanvas.highlight(residue);
        canvas.highlight(residue);
        setMessage("Residue " + residue.getResidueName() + 
                " (" + residue.getOneLetterCode() + ") " + residue.getResidueNumber());
    }
    public void unHighlight() {
        currentHighlightedResidue = null;
        sequencePane.unHighlight();
        canvas.unHighlight();
        sequenceCartoonCanvas.unHighlight();
        setMessage(" "); // Use a space, otherwise message panel collapses
    }
    public void select(Residue r) {
        if (r == null) return;
        sequencePane.select(r);
        sequenceCartoonCanvas.select(r);
        canvas.select(r);        
    }
    public void unSelect(Residue r) {
        if (r == null) return;
        sequencePane.unSelect(r);
        sequenceCartoonCanvas.unSelect(r);
        canvas.unSelect(r);        
    }
    
    public void addResidue(Residue r) {
        if (r == null) return;
        sequencePane.addResidue(r);
        sequenceCartoonCanvas.addResidue(r);
        canvas.addResidue(r);
    }
    
    public void clearResidues() {
        sequencePane.clearResidues();
        sequenceCartoonCanvas.clearResidues();
        canvas.clearResidues();
    }
    public void centerOnResidue(Residue r) {
        sequencePane.centerOnResidue(r);
        sequenceCartoonCanvas.centerOnResidue(r);
        canvas.centerOnResidue(r);
    }
    
    void highlightNextResidue() {
        Residue nextResidue = null;
        if (currentHighlightedResidue == null) { // TODO go to first residue
        }
        else 
            nextResidue = currentHighlightedResidue.getNextResidue();

        if (nextResidue == null) unHighlight();
        else highlight(nextResidue);
    }
}
