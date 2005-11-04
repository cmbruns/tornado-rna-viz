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
package org.simtk.moleculargraphics;

import java.awt.Color;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.simtk.geometry3d.DoubleVector3D;
import org.simtk.moleculargraphics.cartoon.*;
import org.simtk.molecularstructure.*;

import vtk.*;

public class StructureCanvas extends vtkPanel 
implements MouseMotionListener, MouseListener, Observer
{
    static {
        // Keep vtk canvas from obscuring swing widgets
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    // was enum in Java 1.5, converted for Java 1.4 compatibility
    static class MouseDragAction {
        static MouseDragAction NONE = new MouseDragAction();
        static MouseDragAction CAMERA_ROTATE = new MouseDragAction();
        static MouseDragAction CAMERA_TRANSLATE = new MouseDragAction();
        static MouseDragAction CAMERA_ZOOM = new MouseDragAction();
        static MouseDragAction OBJECT_TRANSLATE = new MouseDragAction();
    }
    MouseDragAction mouseDragAction = MouseDragAction.CAMERA_ROTATE;

    MolecularCartoon.CartoonType currentCartoonType = MolecularCartoon.CartoonType.WIRE_FRAME; // default starting type
    public MolecularCartoon currentCartoon = new WireFrameCartoon();
    Color backgroundColor = new Color((float)0.92, (float)0.96, (float)1.0);

    public void setBackgroundColor(Color c) {
        backgroundColor = c;

        if (ren != null) {
            ren.SetBackground(
                    backgroundColor.getRed()/255.0,
                    backgroundColor.getGreen()/255.0,
                    backgroundColor.getBlue()/255.0);
        }
    }

    public void setMolecules(MoleculeCollection molecules) {
        // TODO this routine does not work for TornadoMorph
        currentCartoon = currentCartoonType.newInstance();
        currentCartoon.show(molecules);
        
        vtkAssembly assembly = currentCartoon.getAssembly();
        DoubleVector3D com = molecules.getCenterOfMass();

        if (assembly != null) {
            Lock();
            GetRenderer().RemoveAllProps();
            
            // AddProp deprecated in vtk 5.0
            // try{canvas.GetRenderer().AddViewProp(assembly);}
            // catch(NoSuchMethodError exc){canvas.GetRenderer().AddProp(assembly);}

            System.out.println("Number of assembly paths = " + assembly.GetNumberOfPaths());
            
            GetRenderer().AddProp(assembly);
            GetRenderer().GetActiveCamera().SetFocalPoint(com.getX(), com.getY(), com.getZ());
    
            UnLock();
            repaint();
        }
        else {
            System.out.println("No assembly in cartoon");
        }
    }
    
    public void update(Observable observable, Object object) {
        // TODO respond to changes in model
    }

    static final long serialVersionUID = 01L;
}
