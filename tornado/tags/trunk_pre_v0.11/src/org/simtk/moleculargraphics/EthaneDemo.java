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
 * Created on Sep 6, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;

import java.text.DecimalFormat;
import java.net.URL;
import vtk.vtkActor2D;

import org.simtk.mol.color.DefaultColorScheme;
import org.simtk.mol.toon.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import java.util.*;
import org.simtk.geometry3d.*;

import org.simtk.tornado.command.*;

public class EthaneDemo
extends MolApp 
implements ChangeListener
{
    // TODO - Set look and feel to represent dev status
    static {
        try {
            UIManager.setLookAndFeel("net.sourceforge.napkinlaf.NapkinLookAndFeel");
            // UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {System.err.println(e);}
    }
    
    protected String userAppName = "Ethane Demo";
    private AtomTorsion ethaneTorsion;
    private CappedBondActor actorCartoon;   
    private Molecule ethane;
    
    public static void main(String [] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {new EthaneDemo();}
        });
    }
    
    public EthaneDemo() {
        setTitle(userAppName);
        this.setIconImage(new ImageIcon(classLoader.getResource("images/ethane_icon48.png")).getImage());

        setUpMenuBar();        

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(canvas);
        
        JSlider slider = new JSlider();
        slider.setMinimum(0);
        slider.setMaximum(360);

        // Tick marks
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(30);
        slider.setMinorTickSpacing(10);

        // Tick labels
        Dictionary<Integer, JComponent> tickLabels = new Hashtable<Integer, JComponent>();
        tickLabels.put(0, new JLabel("0\u00B0"));
        tickLabels.put(60, new JLabel("60\u00B0"));
        tickLabels.put(120, new JLabel("120\u00B0"));
        tickLabels.put(180, new JLabel("180\u00B0"));
        tickLabels.put(240, new JLabel("240\u00B0"));
        tickLabels.put(300, new JLabel("300\u00B0"));
        tickLabels.put(360, new JLabel("360\u00B0"));
        slider.setLabelTable(tickLabels);
        slider.setPaintLabels(true);

        slider.addChangeListener(this);
        mainPanel.add(slider);
        getContentPane().add(mainPanel);

        URL structureUrl = classLoader.getResource("structures/ethane.pdb");
        MoleculeCollection molecules = new MoleculeCollection();
        try {molecules.loadPDBFormat(structureUrl);}
        catch (Exception exc) {System.err.println(exc);}

        // Create adjustable torsion angle
        ethane = molecules.molecules().iterator().next();
        Map<String, Atom> nameAtoms = new HashMap<String, Atom>();
        for (Atom atom : ethane.atoms()) nameAtoms.put(atom.getAtomName().trim(), atom);
        ethaneTorsion = new AtomTorsion(nameAtoms.get("H1"), nameAtoms.get("C1"), nameAtoms.get("C2"), nameAtoms.get("H4"));
        
        // TODO Clicking causes Exception if picking is enabled
        ((Tornado3DCanvas)canvas).doPick = false;
        
        canvas.clear();
        actorCartoon = new CappedBondActor();
        for (Molecule molecule : molecules.molecules())
            actorCartoon.addMolecule(molecule);
        actorCartoon.colorToon(DefaultColorScheme.DEFAULT_COLOR_SCHEME);
        canvas.add(actorCartoon);
        canvas.scaleByBoundingBox();
        canvas.centerByBoundingBox();
        
        pack();
        setSize(500,500);
        setVisible(true);
    }
    
    public void stateChanged(ChangeEvent event) {
        if (event.getSource() instanceof JSlider) {
            JSlider slider = (JSlider) event.getSource();
            int sliderValue = slider.getValue();
            double sliderFraction = (sliderValue + 0.0) / (slider.getMaximum() + 0.0);
            
            double torsionAngle = sliderFraction * Math.PI * 2.0;
            setTorsionAngle(torsionAngle);
        }
    }
    
    protected void setTorsionAngle(double angle) {
        DecimalFormat format = new DecimalFormat("0");
        // String message = "Angle value = " + format.format(angle * 180.0 / Math.PI);
        // System.out.println(message);
        ethaneTorsion.setAngle(angle);
        actorCartoon.updateAtomPositions(ethane.atoms());
        canvas.repaint();
    }
    
    protected void setUpMenuBar() {
        JMenu menu;
        JMenuItem menuItem;
        JCheckBoxMenuItem checkItem;

        JMenuBar menuBar = new JMenuBar();

        menu = new JMenu("File");
        menuBar.add(menu);

        menuItem = new JMenuItem("Exit " + userAppName);
        menuItem.addActionListener(new QuitAction());
        menu.add(menuItem);

        menu = new JMenu("Show/Hide");
        menuBar.add(menu);
        
        checkItem = new JCheckBoxMenuItem("Scale bar");
        menu.add(checkItem);
        if (canvas.scaleBar == null) canvas.scaleBar = new ScaleBar(canvas);
        checkItem.addActionListener(new ShowHideScaleBarAction(canvas.scaleBar, checkItem));
        checkItem.setSelected(true);
        
        if (canvas instanceof Tornado3DCanvas) {
            checkItem = new JCheckBoxMenuItem("SimTK logo");
            menu.add(checkItem);
            checkItem.addActionListener(new ShowHideLogoAction(((Tornado3DCanvas)canvas).logoActor, checkItem));
            checkItem.setSelected(true);
        }
        
        JMenu showGraphMenu = new JMenu("Graph");
        menu.add(showGraphMenu);        

        ButtonGroup graphButtonGroup = new ButtonGroup();
        
        checkItem = new JCheckBoxMenuItem("Hide graph");
        graphButtonGroup.add(checkItem);
        showGraphMenu.add(checkItem);
        checkItem.setSelected(true);
        
        checkItem = new JCheckBoxMenuItem("Energy vs. Frame");
        graphButtonGroup.add(checkItem);
        showGraphMenu.add(checkItem);
        
        checkItem = new JCheckBoxMenuItem("Torsion Angle vs. Frame");
        graphButtonGroup.add(checkItem);
        showGraphMenu.add(checkItem);
        
        menu = new JMenu("Help");
        menuBar.add(menu);

        menuItem = new JMenuItem("About " + userAppName);
        menuItem.addActionListener(new AboutDialogAction());
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
        menuBar.setEnabled(true);
    }

    class AboutDialogAction implements ActionListener {
        String aboutString = 
             " SimTK "+userAppName+"\n" +
             " By Christopher Bruns\n" +
             " This is just a demo program for internal prototyping\n";
            
        
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, aboutString, "About SimTK " + userAppName, 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    class ShowHideScaleBarAction implements ActionListener {
        private ScaleBar scaleBar;
        private AbstractButton scaleButton;
        ShowHideScaleBarAction(ScaleBar scaleBar, AbstractButton scaleButton) {
            this.scaleBar = scaleBar;
            this.scaleButton = scaleButton;
        }
        public void actionPerformed(ActionEvent e) {
            if (scaleButton.isSelected()) {
                for (vtkActor2D actor : scaleBar.getVtkActors()) {
                    actor.SetVisibility(1);
                }
                System.out.println("On");
            }
            else {
                for (vtkActor2D actor : scaleBar.getVtkActors()) {
                    actor.SetVisibility(0);
                }
                System.out.println("Off");                
            }
        }
    }

    class ShowHideLogoAction implements ActionListener {
        private AbstractButton logoButton;
        private vtkActor2D logoActor;
        
        ShowHideLogoAction(vtkActor2D logoActor, AbstractButton scaleButton) {
            this.logoActor = logoActor;
            this.logoButton = scaleButton;
        }
        public void actionPerformed(ActionEvent e) {
            if (logoButton.isSelected()) {
                logoActor.SetVisibility(1);
            }
            else {
                logoActor.SetVisibility(0);
            }
        }
    }
}

class AtomTorsion {
    private Atom atom1;
    private Atom atom2;
    private Atom atom3;
    private Atom atom4;
    
    // Which atoms move when torsion is changed
    private Set<Atom> mutableAtoms = new HashSet<Atom>();
    
    public AtomTorsion(Atom atom1, Atom atom2, Atom atom3, Atom atom4) {
        this.atom1 = atom1;
        this.atom2 = atom2;
        this.atom3 = atom3;
        this.atom4 = atom4;
        
        updateMutableAtoms();
    }
    
    /**
     * Figure out which atoms move when this torsion is moved
     *
     */
    protected void updateMutableAtoms() {
        Set<Atom> atomsToIgnore = new HashSet<Atom>();
        atomsToIgnore.add(atom1);
        atomsToIgnore.add(atom2);
        // atomsToIgnore.add(atom3);

        RecursiveBondFinder bondFinder = new RecursiveBondFinder();
        mutableAtoms.clear();
        mutableAtoms.addAll(bondFinder.getBondedAtoms(atom3, atomsToIgnore));
    }
    
    /** Change the torsion angle by a fixed amount
     * 
     * @param angleChange
     */
    public void increment(double angleChange) {
        Vector3D trans2 = atom3.getCoordinates(); // translate origin to atom C
        Vector3D trans1 = trans2.times(-1.0); // translate atom C to origin
        Vector3D axis = atom3.getCoordinates().minus(atom2.getCoordinates()).unit();
        Matrix3D rot1 = Matrix3DClass.axisAngle(axis, angleChange);

        MutableHomogeneousTransform h1 = new HomogeneousTransformClass();
        MutableHomogeneousTransform h2 = new HomogeneousTransformClass();
        MutableHomogeneousTransform h3 = new HomogeneousTransformClass();
        
        h1.setTranslation(trans1);
        h2.setRotation(rot1);
        h3.setTranslation(trans2);
        
        HomogeneousTransform transform = h3.times(h2.times(h1));
        for (Atom atom : mutableAtoms) {
            Vector3D newCoord = transform.times(atom.getCoordinates());
            atom.setCoordinates(newCoord);
        }
    }
    
    public void setAngle(double angle) {
        increment(angle - getAngle());
    }
    
    public double getAngle() {
        Vector3D axis = atom3.getCoordinates().minus(atom2.getCoordinates()).unit();
        Vector3D v1 = atom1.getCoordinates().minus(atom2.getCoordinates()).cross(axis).unit();
        Vector3D v2 = atom4.getCoordinates().minus(atom3.getCoordinates()).cross(axis).unit();
        double angle = Math.acos(v1.dot(v2));
        if (v1.cross(v2).dot(axis) < 0)
            angle = - angle;
        return angle;
    }
}

class RecursiveBondFinder {
    private Set<Atom> atomsToIgnore = new HashSet<Atom>();
    private Set<Atom> bondedAtoms = new HashSet<Atom>();

    public Set<Atom> getBondedAtoms(Atom atom, Set<Atom> atomsToIgnore) {
        this.atomsToIgnore.clear();
        this.bondedAtoms.clear();        
        this.atomsToIgnore.addAll(atomsToIgnore);

        recursiveGetBondedAtoms(atom);
        return this.bondedAtoms;
    }
    
    protected void recursiveGetBondedAtoms(Atom atom1) {
        this.bondedAtoms.add(atom1);
        this.atomsToIgnore.add(atom1);

        for (Atom atom : atom1.bonds()) {
            if (atomsToIgnore.contains(atom)) continue;
            recursiveGetBondedAtoms(atom);
        }
        
    }
}
