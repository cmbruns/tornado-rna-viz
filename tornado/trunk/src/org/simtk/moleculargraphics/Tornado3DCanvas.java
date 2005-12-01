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

import java.awt.*;
import java.awt.image.*;
import java.awt.event.MouseEvent;
import java.util.*;
import vtk.*;
import java.awt.event.*;
import net.java.games.jogl.*;
import org.simtk.moleculargraphics.cartoon.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.geometry3d.*;
import org.simtk.util.*;

/** 
 * @author Christopher Bruns
 * 
 * Three dimensional rendering canvas for molecular structures in Tornado application
 */
public class Tornado3DCanvas extends StructureCanvas 
 implements MouseMotionListener, MouseListener, ResidueActionListener, ComponentListener, KeyListener
{
    
    HashSet currentlyDepressedKeyboardKeys = new HashSet();
    
    boolean doFog = true;
    boolean fogLinear = true;
    GL gl;

    public static final long serialVersionUID = 1L;

    ResidueActionBroadcaster residueActionBroadcaster;

    vtkProp currentHighlight;
    PDBResidue currentHighlightedResidue;
    MutableStructureMolecule selectedAtoms = new MoleculeClass();
    
    boolean useLogoOverlay = true;
    
    vtkRenderer overlayRenderer;
    vtkImageData logoImageData = null;
    vtkPNGReader logoReader;
    vtkImageActor logoActor;
    int logoWidth;
    int logoHeight;

    Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    
    ClassLoader classLoader;

    
    private Color selectionColor;
    public void setSelectionColor(Color c) {
        selectionColor = c;
    }

    public Tornado3DCanvas(ResidueActionBroadcaster b) {
        super();
        
        residueActionBroadcaster = b;
        classLoader = getClass().getClassLoader();
        
        // Display logo
        if (useLogoOverlay)
            loadSimtkLogo();
        
        // setUpLights();
        
        setBackgroundColor(backgroundColor);
        
        addComponentListener(this); // Capture keyboard events
        
        // Required for fog to work?
        // createTestObject();
    }
    
    private void loadSimtkLogo() {
        // Discover version of vtk we are using
        vtkVersion versionVTK = new vtkVersion();
        int vtkMajorVersion = versionVTK.GetVTKMajorVersion();
        int vtkMinorVersion = versionVTK.GetVTKMinorVersion();
        int vtkBuildVersion = versionVTK.GetVTKBuildVersion();
        double vtkDoubleVersion = vtkMajorVersion + 0.1 * vtkMinorVersion + 0.001 * vtkBuildVersion;
        
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

        // Create vtk image pixel by pixel
        
        Image logoImage = Toolkit.getDefaultToolkit().createImage(classLoader.getResource("resources/images/simtk3.png"));
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(logoImage, 0);
        try {tracker.waitForAll();}
        catch (InterruptedException e) {}
        
        int w = logoImage.getWidth(this);
        int h = logoImage.getHeight(this);
        PixelGrabber pixelGrabber = new PixelGrabber(logoImage, 0, 0, w, h, true);

        // System.out.println("Reading pixel data, width = "+w+", height = "+h);
        try{pixelGrabber.grabPixels();}
        catch (Exception e){System.out.println("PixelGrabber exception");}
        int pixels[] = (int[]) pixelGrabber.getPixels();

        logoImageData = new vtkImageData();
        logoImageData.SetDimensions(w, h, 1);
        logoImageData.SetScalarTypeToUnsignedChar();
        logoImageData.SetNumberOfScalarComponents(4);
        logoImageData.AllocateScalars();
        
        int pixelCount = 0;
        int opaquePixelCount = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = y * w + x;
                int color = pixels[pixel];

                int alpha = (color & 0xFF000000) >> 24;
                int red   = (color & 0x00FF0000) >> 16;
                int green = (color & 0x0000FF00) >> 8;
                int blue  = (color & 0x000000FF);

                pixelCount ++;
                if (alpha > 0) opaquePixelCount ++;
                
                int iy = h - 1 - y;
                
                // SetScalarComponentFromDouble causes no such method error
                logoImageData.SetScalarComponentFromDouble(x, iy, 0, 3, alpha);
                logoImageData.SetScalarComponentFromDouble(x, iy, 0, 0, red);
                logoImageData.SetScalarComponentFromDouble(x, iy, 0, 1, green);
                logoImageData.SetScalarComponentFromDouble(x, iy, 0, 2, blue);
            }
        }
        logoWidth = w;
        logoHeight = h;
            
        logoActor = new vtkImageActor();

        if (logoImageData != null)
            logoActor.SetInput(logoImageData);
        else
            throw new RuntimeException("Logo load failed");

        overlayRenderer.AddActor(logoActor);        
    }
    
    public void setBackgroundColor(Color c) {
        super.setBackgroundColor(c);
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
    
    Stopwatch fpsStopWatch = new Stopwatch();
    int frameCount = 0;
    DoubleRing fpsRing = new DoubleRing(10);
    
    public void paint(Graphics g) {

        if (ren == null) return;

        if (ren.VisibleActorCount() <= 0) return;

        Lock();

        fpsStopWatch.restart();

        super.paint(g);
        
        long renderTime = fpsStopWatch.getMilliseconds();
        fpsRing.push(renderTime);
        frameCount ++;
        if (frameCount > 10) {
            frameCount = 0;
            // System.err.println("Frames per second (rendering only) = " + 1000.0/fpsRing.mean());
        }
        
        // The very first time we paint, turn on fog
        if ( (ren != null) && (firstPaint) && (doFog) ) {
            
            // This needs to follow a Render command?
            GLCapabilities capabilities = new GLCapabilities();
            capabilities.setHardwareAccelerated(true);
            GLCanvas glCanvas = GLDrawableFactory.getFactory().
                                  createGLCanvas(capabilities);
            gl = glCanvas.getGL();
            // Render();

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
        UnLock();
    }
    
    public void componentResized(ComponentEvent e) 
    {
        // System.err.println("resize");
        
        if ((overlayRenderer != null) && (overlayRenderer.GetActiveCamera() != null)) {
            Lock();

            // Keep the logo small
            overlayRenderer.GetActiveCamera().SetParallelScale(getHeight()/2);
            
            if (logoImageData != null)
                logoImageData.SetOrigin(getWidth()/2 - logoWidth, -getHeight()/2, 0);
            else if (logoReader != null)
                // Keep the logo in the lower right corner
                logoReader.SetDataOrigin(getWidth()/2 - logoWidth, -getHeight()/2, 0);

            UnLock();
            
            // Cause update
            resetCameraClippingRange(); // somehow this is needed for screen update
            
            // repaint();
        }
    }
    public void componentMoved(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}

    public void setStereoRedBlue() {
        // vtk later than version 4.4 is required for full color anaglyph
        // try {rw.SetStereoTypeToAnaglyph();}
        // catch (NoSuchMethodError exc) {rw.SetStereoTypeToRedBlue();}
        
        Lock();
        
        rw.SetStereoTypeToRedBlue();

        rw.StereoRenderOn();
        
        UnLock();
    }
    public void setStereoInterlaced() {
        Lock();
        
        rw.SetStereoTypeToInterlaced();
        rw.StereoRenderOn();
        
        UnLock();
    }
    public void setStereoOff() {
        Lock();
        
        rw.StereoRenderOff();
        
        UnLock();
    }
    public void setStereoCrossEye() {
        // TODO - create cross-eye view using multiple viewports
    }
        
    public void resetCameraClippingRange() {
        super.resetCameraClippingRange();

        if (cam == null) return;

        float distanceToFocus = (float) cam.GetDistance();
        float frontClip = 0.60f * distanceToFocus;
        float backClip = 2.00f * distanceToFocus;

        if ( (doFog) && (gl != null) ) {
            // if (fogLinear) {
                gl.glFogf(GL.GL_FOG_START, 0.90f * distanceToFocus);
                gl.glFogf(GL.GL_FOG_END, backClip);
            // }
            // else 
                gl.glFogf(GL.GL_FOG_DENSITY, 0.7f / distanceToFocus);                
        }
    }

    public void testFullScreen() {
        rw.FullScreenOff();
        rw.FullScreenOn();
       
        repaint();
        // rw.FullScreenOff();
    }
    
    void createTestObject() {
        // vtkPlatonicSolidSource dod = new vtkPlatonicSolidSource();
        
        // Lock();
        
        vtkConeSource cone = new vtkConeSource();
        cone.SetResolution(8);
        vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
        coneMapper.SetInput(cone.GetOutput());
            
        vtkActor coneActor = new vtkActor();
        coneActor.SetMapper(coneMapper);
            
        GetRenderer().AddActor(coneActor);
        
        // UnLock();
    }
    
    // Make the lock/unlock methods public
    public int Lock() {return super.Lock();}
    public int UnLock() {return super.UnLock();}
    
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
        if (doPick) {
            Date startTime = new Date();
            
            Residue residue = mouseResidue(event);
            if (residue != null) {
                // System.out.println("Residue found");
                residueActionBroadcaster.fireHighlight(residue);
            }
                
            Date endTime = new Date();
            long milliseconds = endTime.getTime() - startTime.getTime();
            // System.out.println("pick took " + milliseconds + " milliseconds");
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
            pickedPosition = new Vector3DClass(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
        }

        else if (false) { // vtkPropPicker
            // This method is fast, but often picks positions
            // that are behind the atom I am trying to pick
            // About 200 ms for single duplex, atom fill
            
            // pickResult is always 0 with vtkWorldPointPicker
            vtkPropPicker picker = new vtkPropPicker(); // Unrelated to models
            int pickResult = picker.Pick(x, y, -100, ren);
            // System.out.println("Picked something");
            double[] pickedPoint = picker.GetPickPosition();
            // System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
            pickedPosition = new Vector3DClass(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
        }
        
        else if (true) { // vtkCellPicker
            // With vtk 4.4 this seems to pick either the right thing
            // or nothing at all
            // About 200 ms for single duplex, atom fill
            
            vtkCellPicker picker = new vtkCellPicker(); // Unrelated to models
            // picker.SetTolerance(0.001);
            int pickResult = picker.Pick(x, y, -100, ren);
            if (pickResult != 0) {
                // System.out.println("Picked something");
                double[] pickedPoint = picker.GetPickPosition();
                // System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
                pickedPosition = new Vector3DClass(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
            }
        }
        
        else if (false) { // vtkPicker            
            vtkPicker picker = new vtkPicker(); // Unrelated to models
            picker.SetTolerance(0.0);
            int pickResult = picker.Pick(x, y, -100, ren);
            if (pickResult != 0) {
                // System.out.println("Picked something");
                double[] pickedPoint = picker.GetPickPosition();
                
                pickedPoint = picker.GetProp3D().GetPosition();
                
                // System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
                pickedPosition = new Vector3DClass(pickedPoint[0], pickedPoint[1], pickedPoint[2]);

                vtkProp3DCollection props3D = picker.GetProp3Ds();
                // System.out.println("Number of Prop3Ds = " + props3D.GetNumberOfItems());
                
                vtkActorCollection actors = picker.GetActors();
                // System.out.println("Number of Actors = " + actors.GetNumberOfItems());
                
            }
        }
        
        else if (false) { // default picker
            int pickResult = picker.Pick(x, y, -100, ren);
            if (pickResult != 0) {
                // System.out.println("Picked something");
                double[] pickedPoint = picker.GetPickPosition();
                System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
                pickedPosition = new Vector3DClass(pickedPoint[0], pickedPoint[1], pickedPoint[2]);
                
                vtkProp3DCollection props3D = picker.GetProp3Ds();
                // System.out.println("Number of Prop3Ds = " + props3D.GetNumberOfItems());
                
                vtkActorCollection actors = picker.GetActors();
                // System.out.println("Number of Actors = " + actors.GetNumberOfItems());                
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
                // System.out.println("Picked something");
    
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
                // System.out.println(""+pickedPoint[0]+", "+pickedPoint[1]+", "+pickedPoint[2]);
            }
            else {
                // System.out.println("Picked nothing");
            }
        }
        
        UnLock();

        // TODO
        
        return pickedResidue;
    }
    
    public void Azimuth (double a) {
        if (cam == null) return;
        
        Lock();
        
        cam.Azimuth(a);
        
        UnLock();
    }
    
    public void mouseEntered(MouseEvent event) {
        // tornado.resumeRotation();
        // super.mouseEntered(event);
    }
    public void mouseExited(MouseEvent event) {
   }
    
//    public void clearResidueHighlights() {
//        vtkProp highlight;
//        for (Iterator i = residueHighlights.values().iterator();
//             i.hasNext();
//        ) {
//            highlight = (vtkProp) (i.next());
//        // for (vtkProp highlight : residueHighlights.values()) {
//
//            // try {ren.RemoveViewProp(highlight);}
//            // catch (NoSuchMethodError exc) {
//            //     ren.RemoveProp(highlight);
//            // }
//            ren.RemoveProp(highlight);
//        }
//        residueHighlights.clear();
//    }
//    
//    public void addResidueHighlight(Residue r, vtkProp h) {
//        if (h == null) return;
//        h.SetVisibility(0);
//        residueHighlights.put(r, h);
//
//        // try{ren.AddViewProp(h);}
//        // catch(NoSuchMethodError exc){ren.AddProp(h);}
//        ren.AddProp(h);
//    }
    
    public void highlight(Residue r) {
        if (r == currentHighlightedResidue) return;
        unHighlightResidue();
        
        if (r instanceof StructureResidue) {
            StructureResidue sr = (StructureResidue) r;
            Lock();
            currentCartoon.highlight(sr);
            UnLock();
        }
        repaint();
    }
    public void unHighlightResidue() {
        if (currentHighlight == null) return;
        Lock();
        currentHighlight.SetVisibility(0);
        UnLock();
        currentHighlight = null;
        currentHighlightedResidue = null;
        repaint();
    }
    
    public void select(Selectable r) {
        Lock();
        currentCartoon.select(r);
        
        // Put atoms into our special "selected" molecule
        if (r instanceof PDBAtom) {
            selectedAtoms.addAtom((PDBAtom)r);
        }
        else if (r instanceof MoleculeClass) {
            for (Iterator i = ((StructureMolecule)r).getAtomIterator(); i.hasNext();) {
                PDBAtom a = (PDBAtom) i.next();
                selectedAtoms.addAtom(a);
            }
        }

        UnLock();
    }
    public void unSelect(Selectable r) {
        Lock();
        currentCartoon.unSelect(r);

        // Remove atoms from our special "selected" molecule
        if (r instanceof PDBAtom) {
            selectedAtoms.removeAtom((PDBAtom)r);
        }
        else if (r instanceof MoleculeClass) {
            for (Iterator i = ((StructureMolecule)r).getAtomIterator(); i.hasNext();) {
                PDBAtom a = (PDBAtom) i.next();
                selectedAtoms.removeAtom(a);
            }
        }

        UnLock();
    }
    public void unSelect() {
        Lock();

        currentCartoon.unSelect();
        
        // Empty out our personal selected atoms container
        selectedAtoms = new MoleculeClass();

        UnLock();
    }
    public void add(Residue r) {}    
    public void clearResidues() {}

    public void centerOn(Residue residue) {
        if (! (residue instanceof StructureResidue)) return;
        
        StructureResidue r = (StructureResidue) residue;
        
        double  FPoint[] = cam.GetFocalPoint() ;
        double  PPoint[] = cam.GetPosition();

        Vector3D oldFocalPoint = new Vector3DClass(FPoint[0], FPoint[1], FPoint[2]);
        Vector3D newFocalPoint = r.getCenterOfMass();
        Vector3D focalShift = new Vector3DClass( newFocalPoint.minus(oldFocalPoint) );

        Vector3D oldPosition = new Vector3DClass(PPoint[0], PPoint[1], PPoint[2]);
        Vector3D newPosition = new Vector3DClass( oldPosition.plus(focalShift) );
        
        Lock();
        cam.SetFocalPoint(newFocalPoint.getX(),newFocalPoint.getY(),newFocalPoint.getZ());
        cam.SetPosition(newPosition.getX(),newPosition.getY(),newPosition.getZ());
        UnLock();
    }
    
    
    /**
     * Translate molecule in screen coordinates
     * @param mol molecule to move
     * @param tX pixels to move horizontally
     * @param tY pixels to move vertically
     */
    void translateMoleculeXY(MutableStructureMolecule mol, double tX, double tY) {
        if (mol == null) return;
        Vector3D translation = screenToWorldTranslation(tX, tY);

        Lock();

        mol.translate(translation);
        
        // TODO - turn this back on for all representations
        if (currentCartoon instanceof WireFrameCartoon) {
            ((WireFrameCartoon)currentCartoon).updateCoordinates(mol);
            repaint();
        }
        
        UnLock();
    }
    
    public void mousePressed(MouseEvent event) {
        residueActionBroadcaster.lubricateUserInteraction();
        super.mousePressed(event);
    }

    public void mouseDragged(MouseEvent event) {
        residueActionBroadcaster.lubricateUserInteraction();
        super.mouseDragged(event);
    }
    
    public void keyTyped(KeyEvent e) {
        super.keyTyped(e);
    }
    public void keyPressed(KeyEvent e) {        
        currentlyDepressedKeyboardKeys.add(KeyEvent.getKeyText(e.getKeyCode()));
        super.keyPressed(e);
    }
    public void keyReleased(KeyEvent e) {
        currentlyDepressedKeyboardKeys.remove(KeyEvent.getKeyText(e.getKeyCode()));
        super.keyReleased(e);
    }
}