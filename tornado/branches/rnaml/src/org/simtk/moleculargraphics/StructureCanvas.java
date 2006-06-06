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
import java.util.*;
import javax.swing.*;
import org.simtk.geometry3d.*;
import org.simtk.moleculargraphics.cartoon.*;
// import org.simtk.molecularstructure.*;
import vtk.*;

public class StructureCanvas extends vtkPanel 
implements MouseMotionListener, MouseListener, MouseWheelListener, Observer, MassBody
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
        // static MouseDragAction OBJECT_TRANSLATE = new MouseDragAction();
    }
    MouseDragAction mouseDragAction = MouseDragAction.CAMERA_ROTATE;

    // MolecularCartoonClass.CartoonType currentCartoonType = MolecularCartoonClass.CartoonType.WIRE_FRAME; // default starting type
    // public MolecularCartoonClass currentCartoon = new WireFrameCartoon();
    Color backgroundColor = new Color((float)0.92, (float)0.96, (float)1.0);
    
    // private double totalMass = 0.0;
    // private Vector3D centerOfMass = new Vector3DClass(0, 0, 0);
    MassBodyClass massBody = new MassBodyClass();

    public StructureCanvas() {
        addMouseWheelListener(this);
        setUpLights();
    }
    
    public double getMass() {return massBody.getMass();}
    public Vector3D getCenterOfMass() {return massBody.getCenterOfMass();}
    
    protected void setUpLights() {
        // Remove or dim that darn initial headlight.
        lgt.SetIntensity(0.0);

        vtkLightKit lightKit = new vtkLightKit();
        lightKit.MaintainLuminanceOn();

        lightKit.SetKeyLightIntensity(0.9);
        lightKit.SetKeyLightWarmth(0.65); // Orange sun
        lightKit.SetKeyLightAngle(60, -40); // Upper left rear
        lightKit.SetKeyToHeadRatio(4); // Very dim head light
        lightKit.SetKeyToFillRatio(3); // Very dim fill light
        lightKit.SetKeyToBackRatio(3); // Very dim back light
        
        lightKit.SetBackLightWarmth(0.32);
        lightKit.SetFillLightWarmth(0.32);
        // lightKit.SetHeadLightWarmth(0.45); // vtk 5.0
        lightKit.SetHeadlightWarmth(0.45); // vtk 4.4
        
        lightKit.AddLightsToRenderer(ren);        
    }
    
    public void setBackgroundColor(Color c) {
        backgroundColor = c;

        if (ren != null) {
            ren.SetBackground(
                    backgroundColor.getRed()/255.0,
                    backgroundColor.getGreen()/255.0,
                    backgroundColor.getBlue()/255.0);
        }
    }

    // public void setMolecules(MoleculeCollection molecules, MolecularCartoonClass.CartoonType cartoonType) {
    // }
    public void add(MolecularCartoon cartoon) {
        
        vtkAssembly assembly = cartoon.getAssembly();
        
        if (assembly != null) {

            // Update mass distribution in display
            massBody.add(cartoon);

            Lock();
            
            // AddProp deprecated in vtk 5.0
            // try{canvas.GetRenderer().AddViewProp(assembly);}
            // catch(NoSuchMethodError exc){canvas.GetRenderer().AddProp(assembly);}

            // System.out.println("Number of assembly paths = " + assembly.GetNumberOfPaths());
            
            GetRenderer().AddProp(assembly); // vtk 4.4
            // GetRenderer().AddViewProp(assembly); // vtk 5.0
            
            System.out.println("Assembly added");
            
            // TODO This centering should be optional
            // TODO The view should also be scaled
            centerByMass();
    
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

    /**
     * Rotate the camera about the focal point
     * @param rotX angle in degrees
     * @param rotY angle in degrees
     */
    void rotateCameraXY(double rotX, double rotY) {
        if (cam == null) return;

        Lock();
        cam.Azimuth(-rotX);
        cam.Elevation(-rotY);
        cam.OrthogonalizeViewUp();
        resetCameraClippingRange();
        if (this.LightFollowCamera == 1)
          {
            lgt.SetPosition(cam.GetPosition());
            lgt.SetFocalPoint(cam.GetFocalPoint());
          }
        UnLock();
    }
    
    void zoomCamera(double zoomFactor) {
        if (cam == null) return;
        
        Lock();
        if (cam.GetParallelProjection() == 1)
          {
            cam.SetParallelScale(cam.GetParallelScale()/zoomFactor);
          }
        else
          {
            cam.Dolly(zoomFactor);
            resetCameraClippingRange();
          }
        UnLock();
    }
    
    void translateCameraXY(double tX, double tY) {
        if (cam == null) return;
        // Apply tX, tY in pixels
        
        Vector3D translation = screenToWorldTranslation(tX, tY);
        
        double  FPoint[]; // focal point
        double  PPoint[]; // camera position

        // get the current focal point and position
        FPoint = cam.GetFocalPoint();
        PPoint = cam.GetPosition();
        
        Lock();
        
        cam.SetFocalPoint(
                          FPoint[0] - translation.getX(),
                          FPoint[1] - translation.getY(),
                          FPoint[2] - translation.getZ());
        cam.SetPosition(
                        PPoint[0] - translation.getX(),
                        PPoint[1] - translation.getY(),
                        PPoint[2] - translation.getZ());
        UnLock();
        
        resetCameraClippingRange();
    }

    /**
     * Compute the translation in world coordinates that corresponds to the change in screen
     * coordinates.
     * @param event
     * @return
     */
    Vector3D screenToWorldTranslation(double tX, double tY) {        
        double arbitraryScale = 1.0; // was 0.5 in vtkPanel
        
        double  FPoint[];
        double  PPoint[];
        double  APoint[] = new double[3];
        double  RPoint[];
        double focalDepth;
        
        // get the current focal point and position
        FPoint = cam.GetFocalPoint();
        PPoint = cam.GetPosition();
        
        // calculate the focal depth since we'll be using it a lot
        ren.SetWorldPoint(FPoint[0],FPoint[1],FPoint[2],1.0);
        ren.WorldToDisplay();
        focalDepth = ren.GetDisplayPoint()[2];
        
        APoint[0] = rw.GetSize()[0]/2.0 + (tX);
        APoint[1] = rw.GetSize()[1]/2.0 + (tY);
        APoint[2] = focalDepth;
        ren.SetDisplayPoint(APoint);
        ren.DisplayToWorld();
        RPoint = ren.GetWorldPoint();
        if (RPoint[3] != 0.0)
          {
            RPoint[0] = RPoint[0]/RPoint[3];
            RPoint[1] = RPoint[1]/RPoint[3];
            RPoint[2] = RPoint[2]/RPoint[3];
          }
        
        Vector3D translation = new Vector3DClass(
                (RPoint[0]-FPoint[0]) * arbitraryScale,
                (RPoint[1]-FPoint[1]) * arbitraryScale,
                (RPoint[2]-FPoint[2]) * arbitraryScale
            );
        return translation;
    }    

    public void resetCameraClippingRange() {        
        if (cam == null) return;

        float distanceToFocus = (float) cam.GetDistance();
        float frontClip = 0.60f * distanceToFocus;
        float backClip = 2.00f * distanceToFocus;
        
        cam.SetClippingRange(frontClip, backClip);
    }

    public void mouseDragged(MouseEvent event) {
        // System.out.println("mouse dragged");

        // Reimpliment interactor vs. vtkPanel
        
        int x = event.getX();
        int y = event.getY();

        if (mouseDragAction == MouseDragAction.CAMERA_ROTATE) // rotate
            rotateCameraXY(x - lastX, lastY - y);
        else if (mouseDragAction == MouseDragAction.CAMERA_TRANSLATE) // translate
            translateCameraXY(x - lastX, lastY - y);
        else if (mouseDragAction == MouseDragAction.CAMERA_ZOOM) // zoom
            zoomCamera(Math.pow(1.02,(y - lastY)));

        repaint();
        
        lastX = x;
        lastY = y;
    }

    public void mousePressed(MouseEvent event) {
        rw.SetDesiredUpdateRate(5.0);
        lastX = event.getX();
        lastY = event.getY();
        
        // In case this press is followed by a drag, remember what the drag should do
        
        // Mouse Button number 2 - middle button - zoom
        // Button 2 seems to set ALT_MASK w/ Logitech mouse on WinXP
        // So make button 2 and alt key do the same thing
        if ( ((event.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK)
          || ((event.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK) )
        {
            // Button number 2 or alt key
            // System.out.println("mouse button 2 or alt key");
            mouseDragAction = MouseDragAction.CAMERA_TRANSLATE;
        }

        // Mouse Button number 3 - right button - translate
        // Button 2 seems to set ALT_MASK w/ Logitech mouse on WinXP
        // So make button 2 and alt key do the same thing
        else if ( ((event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
               || ((event.getModifiers() & InputEvent.META_MASK) == InputEvent.META_MASK) )
        {
            // Button number 3 or meta key
            // System.out.println("mouse button 3 or meta key");
            mouseDragAction = MouseDragAction.CAMERA_ZOOM;
        }
    
        else { // Mouse Button number 1 (left button), or other button, or default button
            // System.out.println("mouse button 1");
            mouseDragAction = MouseDragAction.CAMERA_ROTATE; // Default
        }
        
        // Also check for keyboard modifier: shift -> zoom
        if ( (event.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) {
            mouseDragAction = MouseDragAction.CAMERA_ZOOM;
        }
        
        // Any non-shift modifier key (ctrl, alt, command, option) should translate
        else if ( ((event.getModifiers() & InputEvent.ALT_GRAPH_MASK) == InputEvent.ALT_GRAPH_MASK )
               || ((event.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ) )
        {
            mouseDragAction = MouseDragAction.CAMERA_TRANSLATE;
        }

        // For debugging
        // if ( (event.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK )
        //     System.out.println("alt key");
        // if ( (event.getModifiers() & InputEvent.ALT_GRAPH_MASK) == InputEvent.ALT_GRAPH_MASK )
        //     System.out.println("alt graph key");
        // if ( (event.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK )
        //     System.out.println("ctrl key");
        // if ( (event.getModifiers() & InputEvent.META_MASK) == InputEvent.META_MASK )
        //     System.out.println("meta key");    
    }
    
    public void mouseWheelMoved(MouseWheelEvent event) {
        // System.out.println("wheel moved");
        int rotation = event.getWheelRotation();
        zoomCamera(Math.pow(1.30,(rotation)));
        repaint();
    }
    
    // Wobble for depth cueing
    private double nutationRangeAngle;
    private Vector3D currentNutationAxis = new Vector3DClass(0.0, 1.0, 0.0);
    public void prepareNutation(double nutationRangeAngle) {
        this.nutationRangeAngle = nutationRangeAngle;
        // TODO rotate view so nutation can begin
    }
    public void cleanUpNutation() {
        // TODO unrotate view after nutation is done
    }
    public void stepNutation(double nutationStepAngle) {
        // TODO adjust camera for a single nutation wobble step
    }

    public void clear() {
        GetRenderer().RemoveAllProps(); // vtk 4.4
        // GetRenderer().RemoveAllViewProps(); // vtk 5.0
        massBody.clear();
    }
    
    public void centerByMass() {
        Vector3D centerOfMass = massBody.getCenterOfMass();
        GetRenderer().GetActiveCamera().SetFocalPoint(centerOfMass.getX(), centerOfMass.getY(), centerOfMass.getZ());        
    }
    
    static final long serialVersionUID = 01L;
}
