/*
 * Created on Apr 24, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import vtk.*;
import java.awt.event.*;
import net.java.games.jogl.*;
import java.io.*;

import org.simtk.moleculargraphics.cartoon.MolecularCartoon;
import org.simtk.moleculargraphics.cartoon.RopeAndCylinderCartoon;
import org.simtk.molecularstructure.*;
import org.simtk.geometry3d.*;
import org.simtk.util.*;

/** 
 * @author Christopher Bruns
 * 
 * Three dimensional rendering canvas for molecular structures in Tornado application
 */
public class Tornado3DCanvas extends vtkPanel 
 implements MouseMotionListener, MouseListener, ResidueActionListener, ComponentListener
{
    enum MouseDragAction {
        NONE,
        CAMERA_ROTATE,
        CAMERA_TRANSLATE,
        CAMERA_ZOOM,
        OBJECT_TRANSLATE
    }
    MouseDragAction mouseDragAction = MouseDragAction.CAMERA_ROTATE;
    
    boolean doFog = true;
    boolean fogLinear = true;
    GL gl;

    // volatile boolean userIsInteracting = false;
    
    Color backgroundColor = new Color((float)0.92, (float)0.96, (float)1.0);
    
    // Discover version of vtk we are using
    vtkVersion versionVTK = new vtkVersion();
    int vtkMajorVersion = versionVTK.GetVTKMajorVersion();
    int vtkMinorVersion = versionVTK.GetVTKMinorVersion();
    int vtkBuildVersion = versionVTK.GetVTKBuildVersion();
    double vtkDoubleVersion = vtkMajorVersion + 0.1 * vtkMinorVersion + 0.001 * vtkBuildVersion;
    
    // protected vtkGenericRenderWindowInteractor iren = new vtkGenericRenderWindowInteractor();
    public static final long serialVersionUID = 1L;

    // Tornado tornado;
    ResidueActionBroadcaster residueActionBroadcaster;

    Hashtable<Residue, vtkProp> residueHighlights = new Hashtable<Residue, vtkProp>();
    vtkProp currentHighlight;
    Residue currentHighlightedResidue;
    
    boolean useLogoOverlay = true;
    
    vtkRenderer overlayRenderer;
    vtkPNGReader logoReader;
    vtkImageActor logoActor;
    int logoWidth;
    int logoHeight;

    Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    
    ClassLoader classLoader;

    MolecularCartoon.CartoonType currentCartoonType = MolecularCartoon.CartoonType.BALL_AND_STICK; // default starting type
    MolecularCartoon currentCartoon = new RopeAndCylinderCartoon();
    
	Tornado3DCanvas(ResidueActionBroadcaster b) {
        super();
        
        residueActionBroadcaster = b;
        classLoader = getClass().getClassLoader();
	    // tornado = t;
	    
        // System.out.println("vtk version = " + vtkDoubleVersion);
        
        // Try this way to display logo
        if (useLogoOverlay) {
            // Use a front layer to decorate the display
            overlayRenderer = new vtkRenderer();
            rw.AddRenderer(overlayRenderer);
            rw.SetNumberOfLayers(2);
            overlayRenderer.InteractiveOff();
            overlayRenderer.GetActiveCamera().ParallelProjectionOn();
            overlayRenderer.GetActiveCamera().SetParallelScale(100);
            
            // Layer order depends upon vtk version
            if (vtkDoubleVersion >= 4.5) {
                ren.SetLayer(0);
                overlayRenderer.SetLayer(1);                
            }
            else {
                ren.SetLayer(1);
                overlayRenderer.SetLayer(0);
            }

            // Create logo image for lower right hand corner

            // TODO make this png work with web start
            // i.e. read the image from a URL
            String tempImageFileName = null;
            // This causes rendering crash on Sherm's machine
            if (false)
            {
                Image logoImage = Toolkit.getDefaultToolkit().createImage(classLoader.getResource("resources/images/simtk3.simtk3"));

                // Create an actual file for the image, then have vtk read the file
                // I am sick of trying to convert a java image to a vtkImage
                try {
                    File tempImageFile = File.createTempFile("simtk3", "png");
                    FileOutputStream outStream = new FileOutputStream(tempImageFile);
                    InputStream inStream = classLoader.getResource("resources/images/simtk3.png").openStream();
                    int nibble;
                    while ( (nibble = inStream.read()) != -1)
                        outStream.write(nibble);
                    inStream.close();
                    outStream.close();
                    tempImageFileName = tempImageFile.getAbsolutePath();
                } catch (IOException exc) {
                    System.err.println(exc);
                }
            }
            
            logoReader = new vtkPNGReader();
            if (tempImageFileName != null)
                logoReader.SetFileName(tempImageFileName);
            else
                logoReader.SetFileName("resources/images/simtk3.png");
            logoReader.Update();
            int[] logoBounds = logoReader.GetDataExtent();

            logoActor = new vtkImageActor();
            logoActor.SetInput(logoReader.GetOutput());
            logoWidth = logoBounds[1] - logoBounds[0] + 1;
            logoHeight = logoBounds[3] - logoBounds[2] + 1;
            overlayRenderer.AddActor(logoActor);
        }
        
        // Remove or dim that darn initial headlight.
	    lgt.SetIntensity(0.0);
	    
        ren.SetBackground(
             backgroundColor.getRed()/255.0,
             backgroundColor.getGreen()/255.0,
             backgroundColor.getBlue()/255.0); // pale sky background

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
        lightKit.SetHeadlightWarmth(0.45);
        
        lightKit.AddLightsToRenderer(ren);
        
        addComponentListener(this);
        
        // Required for fog to work?
        createTestObject();
    }
    
    public void setBackgroundColor(Color c) {
        backgroundColor = c;

        if (ren != null) {
            ren.SetBackground(
                    backgroundColor.getRed()/255.0,
                    backgroundColor.getGreen()/255.0,
                    backgroundColor.getBlue()/255.0);
        }

        if (gl != null) {
            float[] fogColor = new float[] {
                    (float) (backgroundColor.getRed()/255.0),
                    (float) (backgroundColor.getGreen()/255.0),
                    (float) (backgroundColor.getBlue()/255.0)
            };
            
            gl.glFogfv(GL.GL_FOG_COLOR, fogColor);
        }
    }
    
    boolean firstPaint = true;
    public void paint(Graphics g) {
        super.paint(g);
        
        // The very first time we paint, turn on fog
        if ( (ren != null) && (firstPaint) && (doFog) ) {
            
            // This needs to follow a Render command?
            GLCapabilities capabilities = new GLCapabilities();
            capabilities.setHardwareAccelerated(true);
            GLCanvas glCanvas = GLDrawableFactory.getFactory().
                                  createGLCanvas(capabilities);
            gl = glCanvas.getGL();
            Render();

            if (fogLinear) { // Linear Fog
                gl.glFogi(GL.GL_FOG_MODE, GL.GL_LINEAR);
                gl.glFogf(GL.GL_FOG_START, (float)0.0);
                gl.glFogf(GL.GL_FOG_END, (float)100.0);
            }
            else { // Exponential Fog
                gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP2);
                gl.glFogf(GL.GL_FOG_DENSITY, 0.2f);
            }

            float[] fogColor = new float[] {
                    (float) (backgroundColor.getRed()/255.0),
                    (float) (backgroundColor.getGreen()/255.0),
                    (float) (backgroundColor.getBlue()/255.0)
            };
            
            gl.glFogfv(GL.GL_FOG_COLOR, fogColor);
            gl.glEnable(GL.GL_FOG);
            gl.glFogf(GL.GL_FOG_DENSITY, (float)0.8);

            firstPaint = false;
        }
    }
    
    public void componentResized(ComponentEvent e) 
    {
        if ((overlayRenderer != null) && (overlayRenderer.GetActiveCamera() != null)) {
            // Keep the logo small
            overlayRenderer.GetActiveCamera().SetParallelScale(getHeight()/2);
            if (logoReader != null)
                // Keep the logo in the lower right corner
                logoReader.SetDataOrigin(getWidth()/2 - logoWidth, -getHeight()/2, 0);
        }
    }
    public void componentMoved(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}

    public void setStereoRedBlue() {
        // vtk later than version 4.4 is required for full color anaglyph
        // try {rw.SetStereoTypeToAnaglyph();}
        // catch (NoSuchMethodError exc) {rw.SetStereoTypeToRedBlue();}
        rw.SetStereoTypeToRedBlue();

        rw.StereoRenderOn();
    }
    public void setStereoInterlaced() {
        rw.SetStereoTypeToInterlaced();
        rw.StereoRenderOn();
    }
    public void setStereoOff() {
        rw.StereoRenderOff();
    }
    public void setStereoCrossEye() {
        // TODO - create cross-eye view using multiple viewports
    }
        
    // Don't show all of the actors every time
    public void resetCameraClippingRange() {
        myResetCameraClippingRange();
    }

    public void myResetCameraClippingRange() {
        
        if (cam == null) return;

        float distanceToFocus = (float) cam.GetDistance();
        float frontClip = 0.60f * distanceToFocus;
        float backClip = 2.00f * distanceToFocus;
        cam.SetClippingRange(frontClip, backClip);
        if ( (doFog) && (gl != null) ) {
            // if (fogLinear) {
                gl.glFogf(GL.GL_FOG_START, 0.90f * distanceToFocus);
                gl.glFogf(GL.GL_FOG_END, backClip);
            // }
            // else 
                gl.glFogf(GL.GL_FOG_DENSITY, 0.7f / distanceToFocus);                
        }
    }

    void createTestObject() {
        // vtkPlatonicSolidSource dod = new vtkPlatonicSolidSource();
        vtkConeSource cone = new vtkConeSource();
        cone.SetResolution(8);
        vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
        coneMapper.SetInput(cone.GetOutput());
            
        vtkActor coneActor = new vtkActor();
        coneActor.SetMapper(coneMapper);
            
        GetRenderer().AddActor(coneActor);
    }
    
    // Make the lock/unlock methods public
    public int Lock() {return super.Lock();}
    public int UnLock() {return super.UnLock();}
    
	public void mouseDragged(MouseEvent event) {
        // TODO - implement mode for model modification
        
        residueActionBroadcaster.lubricateUserInteraction();
        
        // Reimpliment interactor
        int x = event.getX();
        int y = event.getY();

        if (mouseDragAction == MouseDragAction.CAMERA_ROTATE) // rotate
            rotateCameraXY(x - lastX, lastY - y);
        else if (mouseDragAction == MouseDragAction.CAMERA_TRANSLATE) // translate
            translateCameraXY(x - lastX, lastY - y);
        else if (mouseDragAction == MouseDragAction.CAMERA_ZOOM) // zoom
            zoomCamera(Math.pow(1.02,(y - lastY)));
        else if (mouseDragAction == MouseDragAction.OBJECT_TRANSLATE) // zoom
            translateMoleculeXY(currentHighlightedResidue, x - lastX, lastY -y);

	    // super.mouseDragged(event);

        if (event.isControlDown()) {
            // System.out.println("Control key is down");
        }
        else {
            // System.out.println("Control key is NOT down");
        }
        
        // myResetCameraClippingRange();
        repaint();
        lastX = x;
        lastY = y;
	}
	
    boolean pickIsPending = false;
    vtkCellPicker picker = new vtkCellPicker();        
	public void mouseMoved(MouseEvent event) {
        boolean pickOnHover = false;
        if (! pickOnHover) return;
        
	    // super.mouseMoved(event); // Uses up memory somehow?
        if (ren == null) return;

        if (ren.VisibleActorCount() <= 0) {return;}

        // Don't let pick events accumulate
        if (!pickIsPending) {
            residueActionBroadcaster.lubricateUserInteraction();

            pickIsPending = true;

            Residue residue = mouseResidue(event);
            if (residue != null) {
                setCursor(crosshairCursor);
                residueActionBroadcaster.fireHighlight(residue);
            }
            else {
                setCursor(defaultCursor);
            }
            
            pickIsPending = false;
        }
	}

    boolean doPick = true;
    public void mouseClicked(MouseEvent event) {
        System.out.println("Click");
        if (doPick) {
            Date startTime = new Date();
            
            Residue residue = mouseResidue(event);
            if (residue != null) {
                System.out.println("Residue found");
                residueActionBroadcaster.fireHighlight(residue);
            }
                
            Date endTime = new Date();
            long milliseconds = endTime.getTime() - startTime.getTime();
            System.out.println("pick took " + milliseconds + " milliseconds");
        }
    }
    
    Residue mouseResidue(MouseEvent e) {        
        double x = e.getX();
        double y = getSize().height - e.getY();
        
        Residue pickedResidue = null;
        Vector3D pickedPosition = null;
        
        Lock();
        
        if (false) { // vtkWorldPointPicker
            // This method is fast, but often picks positions
            // that are behind the atom I am trying to pick
            // Takes 0-20 ms for single duplex atom fill
            
            // pickResult is always 0 with vtkWorldPointPicker
            vtkWorldPointPicker picker = new vtkWorldPointPicker(); // Unrelated to models
            int pickResult = picker.Pick(x, y, -100, ren);
            double[] pickedPoint = picker.GetPickPosition();
            pickedPosition = new Vector3D(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
        }

        else if (false) { // vtkPropPicker
            // This method is fast, but often picks positions
            // that are behind the atom I am trying to pick
            // About 200 ms for single duplex, atom fill
            
            // pickResult is always 0 with vtkWorldPointPicker
            vtkPropPicker picker = new vtkPropPicker(); // Unrelated to models
            int pickResult = picker.Pick(x, y, -100, ren);
            System.out.println("Picked something");
            double[] pickedPoint = picker.GetPickPosition();
            System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
            pickedPosition = new Vector3D(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
        }
        
        else if (true) { // vtkCellPicker
            // With vtk 4.4 this seems to pick either the right thing
            // or nothing at all
            // About 200 ms for single duplex, atom fill
            
            vtkCellPicker picker = new vtkCellPicker(); // Unrelated to models
            // picker.SetTolerance(0.001);
            int pickResult = picker.Pick(x, y, -100, ren);
            if (pickResult != 0) {
                System.out.println("Picked something");
                double[] pickedPoint = picker.GetPickPosition();
                System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
                pickedPosition = new Vector3D(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
            }
        }
        
        else if (false) { // vtkPicker            
            vtkPicker picker = new vtkPicker(); // Unrelated to models
            picker.SetTolerance(0.0);
            int pickResult = picker.Pick(x, y, -100, ren);
            if (pickResult != 0) {
                System.out.println("Picked something");
                double[] pickedPoint = picker.GetPickPosition();
                
                pickedPoint = picker.GetProp3D().GetPosition();
                
                System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
                pickedPosition = new Vector3D(pickedPoint[0], pickedPoint[1], pickedPoint[2]);

                vtkProp3DCollection props3D = picker.GetProp3Ds();
                System.out.println("Number of Prop3Ds = " + props3D.GetNumberOfItems());
                
                vtkActorCollection actors = picker.GetActors();
                System.out.println("Number of Actors = " + actors.GetNumberOfItems());
                
            }
        }
        
        else if (false) { // default picker
            int pickResult = picker.Pick(x, y, -100, ren);
            if (pickResult != 0) {
                System.out.println("Picked something");
                double[] pickedPoint = picker.GetPickPosition();
                System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
                pickedPosition = new Vector3D(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
                
                vtkProp3DCollection props3D = picker.GetProp3Ds();
                System.out.println("Number of Prop3Ds = " + props3D.GetNumberOfItems());
                
                vtkActorCollection actors = picker.GetActors();
                System.out.println("Number of Actors = " + actors.GetNumberOfItems());                
            }
        }

        else {
            vtkPropPicker picker = new vtkPropPicker(); // takes about 2 seconds
            int pickResult = picker.PickProp(x, y, ren);
    
            // vtkPicker picker = new vtkPicker(); // I don't understand positions
    
    
            // vtkPointPicker picker = new vtkPointPicker(); // Exact points only?
            // vtkCellPicker picker = new vtkCellPicker();
    
            // picker.SetTolerance(0.1f);
            // int pickResult = picker.Pick(x, y, 0, ren);
            
            // if (pickResult != 0) {
            if (true) {
                System.out.println("Picked something");
    
                // vtkActor actor = picker.GetActor();
                // System.out.println("Actor = " + actor); // null for glyph3d?
                
    //            vtkAssembly assembly = picker.GetAssembly();
    //            if (assembly != null) {
    //                vtkAssemblyPath path = assembly.GetNextPath();
    //                if (path != null) {
    //                    vtkAssemblyNode node = path.GetFirstNode();
    //                    int nodeCount = 1;
    //                    while ((node = path.GetNextNode()) != null) {
    //                        nodeCount ++;
    //                    }
    //                    // I have never reached this statement...
    //                    System.out.println("Assembly node count = " + nodeCount);
    //                }
    //            }
    
                double[] pickedPoint = picker.GetPickPosition();
                System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
            }
            else 
                System.out.println("Picked nothing");
        }
        
        UnLock();

        // TODO
        
        return pickedResidue;
    }
    
    public void Azimuth (double a) {
        if (cam == null) return;
        cam.Azimuth(a);
    }
    
    public void mousePressed(MouseEvent event) {
        residueActionBroadcaster.lubricateUserInteraction();

        rw.SetDesiredUpdateRate(5.0);
        lastX = event.getX();
        lastY = event.getY();
        
        if ( event.isControlDown() ) 
            mouseDragAction = MouseDragAction.OBJECT_TRANSLATE;
        else if ((event.getModifiers()==InputEvent.BUTTON2_MASK) ||
            (event.getModifiers()==(InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)))
            mouseDragAction = MouseDragAction.CAMERA_TRANSLATE;
        else if (event.getModifiers()==InputEvent.BUTTON3_MASK)
            mouseDragAction = MouseDragAction.CAMERA_ZOOM;
        else 
            mouseDragAction = MouseDragAction.CAMERA_ROTATE;
        
        // super.mousePressed(event);
    }
    public void mouseReleased(MouseEvent event) {
        super.mouseReleased(event);
    }
    public void mouseEntered(MouseEvent event) {
        // tornado.resumeRotation();
        // super.mouseEntered(event);
    }
    public void mouseExited(MouseEvent event) {
   }
    
    public void clearResidueHighlights() {
        for (vtkProp highlight : residueHighlights.values()) {

            // try {ren.RemoveViewProp(highlight);}
            // catch (NoSuchMethodError exc) {
            //     ren.RemoveProp(highlight);
            // }
            ren.RemoveProp(highlight);
        }
        residueHighlights.clear();
    }
    
    public void addResidueHighlight(Residue r, vtkProp h) {
        if (h == null) return;
        h.SetVisibility(0);
        residueHighlights.put(r, h);

        // try{ren.AddViewProp(h);}
        // catch(NoSuchMethodError exc){ren.AddProp(h);}
        ren.AddProp(h);
    }
    
    public void highlight(Residue r) {
        if (r == currentHighlightedResidue) return;
        unHighlightResidue();
        if (residueHighlights.containsKey(r)) {
            currentHighlightedResidue = r;
            currentHighlight = residueHighlights.get(r);
            currentHighlight.SetVisibility(1);
            repaint();
        }
    }
    public void unHighlightResidue() {
        if (currentHighlight == null) return;
        currentHighlight.SetVisibility(0);
        currentHighlight = null;
        currentHighlightedResidue = null;
        repaint();
    }
    
    public void select(Selectable r) {}
    public void unSelect(Selectable r) {}
    public void unSelect() {}
    public void add(Residue r) {}    
    public void clearResidues() {}

    public void centerOn(Residue r) {
        double  FPoint[] = cam.GetFocalPoint() ;
        double  PPoint[] = cam.GetPosition();

        Vector3D oldFocalPoint = new Vector3D(FPoint[0], FPoint[1], FPoint[2]);
        Vector3D newFocalPoint = r.getCenterOfMass();
        Vector3D focalShift = newFocalPoint.minus(oldFocalPoint);

        Vector3D oldPosition = new Vector3D(PPoint[0], PPoint[1], PPoint[2]);
        Vector3D newPosition = oldPosition.plus(focalShift);
        
        cam.SetFocalPoint(newFocalPoint.getX(),newFocalPoint.getY(),newFocalPoint.getZ());
        cam.SetPosition(newPosition.getX(),newPosition.getY(),newPosition.getZ());
    }
    
    /**
     * Rotate the camera about the focal point
     * @param rotX angle in degrees
     * @param rotY angle in degrees
     */
    void rotateCameraXY(double rotX, double rotY) {
        cam.Azimuth(-rotX);
        cam.Elevation(-rotY);
        cam.OrthogonalizeViewUp();
        resetCameraClippingRange();
        if (this.LightFollowCamera == 1)
          {
            lgt.SetPosition(cam.GetPosition());
            lgt.SetFocalPoint(cam.GetFocalPoint());
          }
    }
    void zoomCamera(double zoomFactor) {
        if (cam.GetParallelProjection() == 1)
          {
            cam.SetParallelScale(cam.GetParallelScale()/zoomFactor);
          }
        else
          {
            cam.Dolly(zoomFactor);
            resetCameraClippingRange();
          }
    }
    void translateCameraXY(double tX, double tY) {
        // Apply tX, tY in pixels
        
        Vector3D translation = screenToWorldTranslation(tX, tY);
        
        double  FPoint[]; // focal point
        double  PPoint[]; // camera position

        // get the current focal point and position
        FPoint = cam.GetFocalPoint();
        PPoint = cam.GetPosition();
        
        cam.SetFocalPoint(
                          FPoint[0] - translation.getX(),
                          FPoint[1] - translation.getY(),
                          FPoint[2] - translation.getZ());
        cam.SetPosition(
                        PPoint[0] - translation.getX(),
                        PPoint[1] - translation.getY(),
                        PPoint[2] - translation.getZ());
        myResetCameraClippingRange();
    }
    
    /**
     * Tranlate molecule in screen coordinates
     * @param mol molecule to move
     * @param tX pixels to move horizontally
     * @param tY pixels to move vertically
     */
    void translateMoleculeXY(Molecule mol, double tX, double tY) {
        if (mol == null) return;
        Vector3D translation = screenToWorldTranslation(tX, tY);
        mol.translate(translation);
        currentCartoon.updateCoordinates(mol);
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
        
        Vector3D translation = new Vector3D(
                (RPoint[0]-FPoint[0]) * arbitraryScale,
                (RPoint[1]-FPoint[1]) * arbitraryScale,
                (RPoint[2]-FPoint[2]) * arbitraryScale
            );
        return translation;
    }
}
