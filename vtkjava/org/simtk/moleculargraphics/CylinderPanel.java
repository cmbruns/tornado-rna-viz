/*
 * Created on Apr 20, 2005
 *
 */

package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Hashtable;

import vtk.*;

import org.simtk.atomicstructure.Atom;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.RNA;


/**
 * @author Christopher Bruns
 *
 * Practice panel for painting RNA cylinders
 */
public class CylinderPanel extends JPanel implements ActionListener, MouseMotionListener {
    public static final long serialVersionUID = 1L;
	vtkCanvas renWin;
	JButton exitButton;
	vtkRenderer renderer;
	
	Hashtable<vtkProp3D, String> vtkPropNames = new Hashtable<vtkProp3D, String>();
	Hashtable<vtkProp3D, Residue> vtkPropObjects = new Hashtable<vtkProp3D, Residue>();
	
	Cursor handCursor = new Cursor (Cursor.HAND_CURSOR);
	Cursor defaultCursor = new Cursor (Cursor.DEFAULT_CURSOR);
	
	static final double radiansToDegrees = 180.0 / Math.PI;

	CylinderPanel() {
		setLayout(new BorderLayout());
		// Create the buttons.
		renWin = new vtkCanvas();
		renderer = renWin.GetRenderer();
		
		renderer.SetBackground(1,1,1); // white
		
		vtkRenderWindow rw = renWin.GetRenderWindow();
		rw.SetStereoTypeToRedBlue();
		
		exitButton = new JButton("Exit");
		exitButton.addActionListener(this);
		
		add(renWin, BorderLayout.CENTER);
		add(exitButton, BorderLayout.EAST);
		
		renWin.addMouseMotionListener(this);
		
		 // Test reading of PDB file
		 RNA rna = new RNA();
		 try {
		     rna = (RNA) Molecule.createFactoryPDBMolecule("1x8w.pdb2");
			 System.out.println("" + rna.getAtomCount() + " atoms read");
		 }
		 catch (java.io.FileNotFoundException e) {System.err.println(e);}
		 catch (java.io.IOException e) {System.err.println(e);}
		 
		 // Put shapes around each phosphate
		 // (start with spheres)
		 BaseVector3D previousCenter = null;
		 for (int r = 0; r < rna.getResidueCount(); r++) {
		     Residue residue = rna.getResidue(r);
		     Atom phosphorus = residue.getAtom("P");
		     if (phosphorus != null) {
		         // Create sphere at phosphate
		         BaseVector3D center = phosphorus.getCoordinates();
		         addSphere(center, 2.0, Color.GREEN, residue);
		         
		         // Connect backbone with tubes
		         // TODO - figure out correct distance cutoff
		         if ( (previousCenter != null) && (center.distance(previousCenter) < 10) ) {
		             addCylinder(previousCenter, center, 1.0, 8, Color.WHITE);
		         }
		         previousCenter = center;
		     }
		 }

		 addCylinder(new Vector3D(-4.1, 2.5, -16.2), new Vector3D(5.6, 3.5, -24.2), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(12.1, -69.2, -33.3), new Vector3D(7.5, -51.0, -37.3), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(10.4, -27.7, 11.0), new Vector3D(15.7, -23.1, 6.0), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(-2.3, -21.6, -40.2), new Vector3D(-0.6, -8.3, -30.9), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(-48.5, -55.3, -46.0), new Vector3D(-13.2, -46.3, -30.4), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(-21.4, -46.0, -30.9), new Vector3D(-6.1, -31.6, -24.2), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(-4.8, -36.5, -39.8), new Vector3D(0.1, -46.1, -46.9), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(-33.2, -44.7, -15.9), new Vector3D(-12.9, -42.2, 1.2), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(-2.9, -25.2, 11.1), new Vector3D(6.1, -27.5, 14.0), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(4.7, -10.7, -3.6), new Vector3D(-5.3, -3.2, -16.4), 10.0, 20, Color.GRAY);
		 addCylinder(new Vector3D(23.0, -17.3, -1.1), new Vector3D(-14.5, -30.8, -31.1), 10.0, 20, Color.GRAY);

		 // rw.StereoRenderOn();
	}
	
	/** An ActionListener that listens to the radio buttons. */
	public void actionPerformed(ActionEvent e) 
	{
	    if (e.getSource().equals(exitButton)) {
	        System.exit(0);
	    }
	}

	public static void main(String[] args) {
	    CylinderPanel panel = new CylinderPanel();
		
	    System.out.println("Hello");
	    
		JFrame frame;
	    frame = new JFrame("CylinderPanel");
	    frame.addWindowListener(new WindowAdapter() 
	      {
	        public void windowClosing(WindowEvent e) {System.exit(0);}
	      });
	    frame.getContentPane().add("Center", panel);
	    frame.pack();
	    frame.setVisible(true);
	}
	
	int sphereCount = 0;
	public void addSphere(BaseVector3D center, double radius, Color color, Residue residue) {
	    sphereCount ++;
	    
	    vtkSphereSource sphere = new vtkSphereSource();
	    sphere.SetThetaResolution(7);
	    sphere.SetPhiResolution(7);
	    sphere.SetRadius(radius);
	    
		vtkPolyDataMapper sphereMapper = new vtkPolyDataMapper();
		sphereMapper.SetInput(sphere.GetOutput());

		vtkActor sphereActor = new vtkActor();
		sphereActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
	    
		vtkTransform translation = new vtkTransform();
		translation.Translate(center.getX(), center.getY(), center.getZ());

		sphereActor.SetUserMatrix(translation.GetMatrix());
		
		sphereActor.SetMapper(sphereMapper);
		    
		renWin.GetRenderer().AddActor(sphereActor);

		String sphereName = "Sphere number " + sphereCount;
		vtkPropNames.put(sphereActor, sphereName);
		vtkPropObjects.put(sphereActor, residue);
	}
	
	// put another cylinder in the display field
	int cylinderCount = 0;
	public void addCylinder(BaseVector3D head, BaseVector3D tail, double radius, int resolution, Color color) {
	    cylinderCount ++;
		vtkCylinderSource cylinder = new vtkCylinderSource();		
		cylinder.SetResolution(resolution);
		cylinder.SetHeight(head.distance(tail));
		cylinder.SetRadius(radius);
		
		vtkPolyDataMapper cylinderMapper = new vtkPolyDataMapper();
		cylinderMapper.SetInput(cylinder.GetOutput());
		    
		vtkActor cylinderActor = new vtkActor();
		cylinderActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);

		// Change orientation from straight up
		vtkTransform orientation = new vtkTransform();
		// Project vector onto Z plane
		Vector3D direction = head.minus(tail).unit();
		Vector3D xAxis = new Vector3D(1,0,0);
		Vector3D yAxis = new Vector3D(0,1,0);
		Vector3D zAxis = new Vector3D(0,0,1);
		Vector3D yProjection = new Vector3D(direction.getX(), 0, direction.getZ()).unit();

		// How far is cylinder tilted from straight up?
		double yRotationAngle = Math.acos(yAxis.dot(direction)) * radiansToDegrees;
		// Is there a problem if yRotationAngle is > 90 degrees?
		// if (yRotationAngle > 90) yRotationAngle = 180 - yRotationAngle;

		double zRotationAngle = Math.acos(yProjection.dot(zAxis)) * radiansToDegrees;
		if (direction.getX() < 0) zRotationAngle *= -1.0;

		// Translate cylinder
		Vector3D center = head.plus(tail).scale(0.5);

		
		orientation.Translate(center.getX(), center.getY(), center.getZ());
		orientation.RotateY(zRotationAngle);
		orientation.RotateX(yRotationAngle); // Tilted this far from straight up

		cylinderActor.SetUserMatrix(orientation.GetMatrix());
		
		cylinderActor.SetMapper(cylinderMapper);
		    
		renWin.GetRenderer().AddActor(cylinderActor);
		
		String cylinderName = "Cylinder number " + cylinderCount;
		vtkPropNames.put(cylinderActor, cylinderName);
	}
	
	public void mouseDragged(MouseEvent event) {
	    
	}
	
	vtkPropPicker picker;
	public void mouseMoved(MouseEvent event) {
	    if (picker == null)
	        picker = new vtkPropPicker();
	    
	    float x = event.getX();
	    float y = renWin.getSize().height - event.getY();

	    boolean mouseIsOverAResidue = false;
	    picker.PickProp(x, y, renderer);
	    vtkAssemblyPath path = picker.GetPath();
	    if (path != null) {
	        vtkAssemblyNode node = path.GetLastNode();
	        if (node != null) {
		        vtkProp prop = path.GetLastNode().GetProp();
		        if (prop != null) {
		            // System.out.println("Got a prop!");
		            
		            String propName = (String) vtkPropNames.get(prop);
		            if (propName != null) {
			            // System.out.println(propName);
		            }

		            Residue pickedResidue = (Residue) vtkPropObjects.get(prop);
		            if (pickedResidue != null) {
		                String residueLabel = "Residue " + pickedResidue.getOneLetterCode() + " " + pickedResidue.residueNumber;
			            System.out.println(residueLabel);
			            mouseIsOverAResidue = true;
		            }
		        }
	        }
	    }
	    
	    if (mouseIsOverAResidue)   setCursor(handCursor);
	    else                       setCursor(defaultCursor);
	    
	}
}
