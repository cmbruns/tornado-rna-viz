/*
 * Created on Apr 20, 2005
 *
 */

/**
 * @author Christopher Bruns
 * 
 * 
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import vtk.*;

/**
 * An application that displays a JButton and several JRadioButtons.
 * The JRadioButtons determine the look and feel used by the application.
 */
public class HelloVtk extends JPanel implements ActionListener {
    // To supplement those libraries loaded by vtkPanel
    // when in Java Web Start mode
    static { 
        System.loadLibrary("vtkfreetype"); 
        System.loadLibrary("vtkexpat"); 
        System.loadLibrary("vtkjpeg"); 
        System.loadLibrary("vtkzlib"); 
        System.loadLibrary("vtktiff"); 
        System.loadLibrary("vtkpng"); 
        System.loadLibrary("vtkftgl"); 
        System.loadLibrary("vtkCommon"); 
        System.loadLibrary("vtkFiltering"); 
        System.loadLibrary("vtkIO"); 
        System.loadLibrary("vtkImaging"); 
        System.loadLibrary("vtkGraphics"); 
        System.loadLibrary("vtkRendering"); 
        System.loadLibrary("vtkHybrid"); 
    }

    
  static JFrame frame;
  vtkCanvas renWin;
  JButton exitButton;
  public static final long serialVersionUID = 1L;

  public HelloVtk() {
    setLayout(new BorderLayout());
    // Create the buttons.
    renWin = new vtkCanvas();
    renWin.GetRenderer().SetBackground(1,1,1); // white
    
    vtkCylinderSource cylinder = new vtkCylinderSource();
    cylinder.SetResolution(20);
    cylinder.SetHeight(50);
    cylinder.SetRadius(10);
    vtkPolyDataMapper cylinderMapper = new vtkPolyDataMapper();
    cylinderMapper.SetInput(cylinder.GetOutput());
        
    vtkActor cylinderActor = new vtkActor();
    cylinderActor.GetProperty().SetColor(1.0, 0.8, 0.8);
    cylinderActor.SetMapper(cylinderMapper);
        
    renWin.GetRenderer().AddActor(cylinderActor);

    exitButton = new JButton("Exit");
    exitButton.addActionListener(this);

    add(renWin, BorderLayout.CENTER);
    add(exitButton, BorderLayout.EAST); 
  }


  /** An ActionListener that listens to the radio buttons. */
  public void actionPerformed(ActionEvent e) 
  {
    if (e.getSource().equals(exitButton)) 
      {
        System.exit(0);
      }
  }

  public static void main(String s[]) 
  {
    HelloVtk panel = new HelloVtk();
	
    frame = new JFrame("HelloVTK");
    frame.addWindowListener(new WindowAdapter() 
      {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });
    frame.getContentPane().add("Center", panel);
    frame.pack();
    frame.setVisible(true);
  }
}

