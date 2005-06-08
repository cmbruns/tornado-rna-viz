/*
 * Created on Apr 24, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.util.Hashtable;
import vtk.*;
import java.awt.event.*;
import net.java.games.jogl.*;

import org.simtk.molecularstructure.Residue;

/** 
 * @author Christopher Bruns
 * 
 * Three dimensional rendering canvas for molecular structures in Tornado application
 */
public class Tornado3DCanvas extends vtkPanel 
 implements MouseMotionListener, MouseListener, ResidueActionListener, ComponentListener
{
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

    // TODO - create lookup table of residues by atomic positions, or centroids
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
            vtkImageData imageData = null;
            if (false)
            {
                Image logoImage = Toolkit.getDefaultToolkit().createImage(classLoader.getResource("simtk2.png"));
                Dimension logoDimension = new Dimension(logoImage.getWidth(this), logoImage.getHeight(this));
                PixelGrabber pixelGrabber = new PixelGrabber(logoImage, 0, 0, logoDimension.width, logoDimension.height, true);
                try {
                    pixelGrabber.grabPixels();
                    
                    Object pixels = pixelGrabber.getPixels();
                    if (pixels instanceof int[]) {
                        int intPixels[] = (int[]) pixels;
                        
                        imageData = new vtkImageData();
                        imageData.SetDimensions(logoDimension.width, logoDimension.height, 1);
                        imageData.SetOrigin(0.0, 0.0, 0.0);
                        imageData.SetSpacing(1.0, 1.0, 1.0);
                        imageData.SetScalarTypeToUnsignedChar();
                        // imageData.AllocateScalars();
                        vtkDataArray array = imageData.GetPointData().GetScalars();
                        
                        int iZ = 0;
                        for(int iY= 0; iY < logoDimension.height; iY++){
                            for(int iX = 0; iX < logoDimension.width; iX++){
                                // TODO this is probably terribly wrong
                                array.InsertNextTuple1(intPixels[iX * logoDimension.height + iY]);
                            }
                        }
                    }
                }
                catch (InterruptedException exc) {}
                
            }
            
            logoReader = new vtkPNGReader();
            logoReader.SetFileName("simtk2.png");
            logoReader.Update();
            int[] logoBounds = logoReader.GetDataExtent();

            logoActor = new vtkImageActor();
            if (imageData != null) logoActor.SetInput(imageData);
            else logoActor.SetInput(logoReader.GetOutput());
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
        
        createTestObject();
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
        residueActionBroadcaster.lubricateUserInteraction();
	    super.mouseDragged(event);

        if (event.isControlDown()) {
            // System.out.println("Control key is down");
        }
        else {
            // System.out.println("Control key is NOT down");
        }
        
        if (cam == null) return;
        myResetCameraClippingRange();
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

            double screenX = event.getX();
            double screenY = getSize().height - event.getY();
            
            int pickResult = picker.Pick(screenX, screenY, 0.0, ren);
            if (pickResult > 0) {
                setCursor(crosshairCursor);
                pickIsPending = true;
                double[] pickPosition;
                pickPosition = picker.GetPickPosition();
                // TODO - select the residue that is under the mouse
            }
            else {
                // Shows the user that her pointer is not over an object
                setCursor(defaultCursor);
            }
            pickIsPending = false;
        }
	}

    boolean doPick = true;
    public void mouseClicked(MouseEvent event) {
        if (doPick) {
            // TODO causes occasional crash
            double x = event.getX();
            double y = getSize().height - event.getY();
            
            // vtkPropPicker picker = new vtkPropPicker();
            vtkPicker picker = new vtkPicker();
            picker.SetTolerance(0.0f);
            int pickResult = picker.Pick(x, y, 0, ren);
            
            if (pickResult != 0)
                System.out.println("Picked something");
            else 
                System.out.println("Picked nothing");
        }
    }
    
    public void Azimuth (double a) {
        if (cam == null) return;
        cam.Azimuth(a);
    }
    
    public void mousePressed(MouseEvent event) {
        residueActionBroadcaster.lubricateUserInteraction();
        super.mousePressed(event);
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
    
    public void select(Residue r) {}
    public void unSelect(Residue r) {}
    public void add(Residue r) {}    
    public void clearResidues() {}
    public void centerOn(Residue r) {// TODO
    }
}
