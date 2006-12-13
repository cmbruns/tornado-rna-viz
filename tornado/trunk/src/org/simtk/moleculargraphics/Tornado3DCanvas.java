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
import javax.media.opengl.*;
import org.simtk.util.*;
import org.simtk.mol.toon.ActorCartoon;
import org.simtk.mol.toon.MoleculeCartoon;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.Atom;
import org.simtk.geometry3d.*;

/** 
 * @author Christopher Bruns
 * 
 * Three dimensional rendering canvas for molecular structures in Tornado application
 */
public class Tornado3DCanvas extends StructureCanvas 
 implements MouseMotionListener, MouseListener, 
 ComponentListener, KeyListener, ResidueHighlightListener,
 ResidueCenterListener
{
    protected ResidueHighlightBroadcaster residueHighlightBroadcaster = null;
    protected ResidueCenterBroadcaster residueCenterBroadcaster = null;
    protected Map<Residue, Color> highlightResidues = new LinkedHashMap<Residue, Color>();
    
    HashSet currentlyDepressedKeyboardKeys = new HashSet();
    
    boolean showLogo = true;
    boolean doFog = true;
    boolean fogLinear = true;
    GL gl;

    public static final long serialVersionUID = 1L;

    // ResidueActionBroadcaster residueActionBroadcaster;

//    vtkProp currentHighlight;
//    Residue currentHighlightedResidue;
//    MutableLocatedMolecule selectedAtoms = new MoleculeClass();
    
    vtkRenderer overlayRenderer;
    vtkImageData logoImageData = null;
    vtkPNGReader logoReader;
    vtkActor2D logoActor = null;
    int logoWidth;
    int logoHeight;

    Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    
    ClassLoader classLoader;
    
    public Tornado3DCanvas() {
        super();
        
        // residueActionBroadcaster = b;
        classLoader = getClass().getClassLoader();
        
        // Display logo
        if (showLogo)
            loadSimtkLogo();
        
        // setUpLights();
        
        setBackground(backgroundColor);
        
        addComponentListener(this); // Capture keyboard events
        
        // Required for fog to work?
        // createTestObject();
    }
    
    public void setResidueHighlightBroadcaster(ResidueHighlightBroadcaster r) {
        residueHighlightBroadcaster = r;
    }
    public void setResidueCenterBroadcaster(ResidueCenterBroadcaster r) {
        residueCenterBroadcaster = r;
    }
    
    private void loadSimtkLogo() {
        
        Image logoImage = Toolkit.getDefaultToolkit().createImage(classLoader.getResource("images/simtk3.png"));
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
            
        vtkImageMapper imageMapper = new vtkImageMapper();
        imageMapper.SetInput(logoImageData);
        
        imageMapper.SetColorWindow(255.0);
        imageMapper.SetColorLevel(127.5);

        logoActor = new vtkActor2D();
        logoActor.SetMapper(imageMapper);
    }
    
    public void clear() {
        super.clear();
        
        if (logoActor != null)
            GetRenderer().AddActor2D(logoActor);
    }
    
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        if (scaleBar != null)
            scaleBar.setBackground(c);
        if (gl != null) {
            float[] fogColor = new float[] {
                    (float) (backgroundColor.getRed()/255.0),
                    (float) (backgroundColor.getGreen()/255.0),
                    (float) (backgroundColor.getBlue()/255.0)
            };
            
            gl.glFogfv(GL.GL_FOG_COLOR, fogColor, 0);
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
        
        if (firstPaint) {
            // The very first time we paint, turn on fog
            if ( (ren != null) && (doFog) ) {
                
                // This needs to follow a Render command?
                GLCapabilities capabilities = new GLCapabilities();
                capabilities.setHardwareAccelerated(true);
                gl = GLDrawableFactory.getFactory().createExternalGLContext().getGL();
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
                
                gl.glFogfv(GL.GL_FOG_COLOR, fogColor, 0);
                gl.glEnable(GL.GL_FOG);
                gl.glFogf(GL.GL_FOG_DENSITY, (float)0.8);
            }
            
            // This solves problem with initially invisible molecule!
            resetCameraClippingRange();
            
            firstPaint = false;
        }
        
        UnLock();
    }
    
    public void componentResized(ComponentEvent e) 
    {
        // System.err.println("resize");
        
        if (logoActor != null) {
            Lock();
            
            // int logoX = getWidth()/2 - logoWidth;
            // int logoY = -getHeight()/2;
            int logoX = getWidth() - logoWidth - 5;
            int logoY = 5;
            
            if (logoImageData != null) {
                
                // logoImageData.SetOrigin(logoX, logoY, 0);
                // logoImageData.Modified();

                logoActor.SetPosition(logoX, logoY);

                // System.out.println("Logo data recentered at "+logoX+", "+logoY);
            }
            else if (logoReader != null) {
                // Keep the logo in the lower right corner
                logoReader.SetDataOrigin(logoX, logoY, 0);
                // System.out.println("Logo reader recentered at "+logoX+", "+logoY);
            }

            UnLock();
                
            // Cause update
            resetCameraClippingRange(); // somehow this is needed for screen update
        }
    }
    public void componentMoved(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}

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
            // residueActionBroadcaster.lubricateUserInteraction();

            pickIsPending = true;

//            Residue residue = mouseResidue(event);
//            if (residue != null) {
//                setCursor(crosshairCursor);
//                // residueActionBroadcaster.fireHighlight(residue);
//            }
//            else {
//                setCursor(defaultCursor);
//            }
            
            pickIsPending = false;
        }
	}

    boolean doPick = true;
    public void mouseClicked(MouseEvent event) {
        picker.SetTolerance(0.05);
        
        Vector3D pickPosition = null;
        Residue clickedResidue = null;
        
        if (doPick) do {
            Date startTime = new Date();
            
            picker.Pick(event.getX(), getHeight() - event.getY(), 0.0, ren);
            
            vtkActor actor = picker.GetActor();
            if (actor == null) {
                System.out.println("No actor");
                break;
            }

            pickPosition = new Vector3DClass(picker.GetPickPosition());
            
            // System.out.println("Point id = " + pointId);
            int cellId = picker.GetCellId();
            if (cellId < 0) {
                System.out.println("No cell");
                break;
            }

            vtkCell cell = actor.GetMapper().GetInputAsDataSet().GetCell(cellId);
            
            int pointId = cell.GetPointIds().GetId(0);
            vtkDataArray scalars = actor.GetMapper().GetInputAsDataSet().GetPointData().GetScalars();
            if (scalars == null) {
                System.out.println("No scalars");
                break;
            }
            double scalar = scalars.GetTuple1(pointId);

            ActorCartoon toon = actorCartoons.get(actor);
            if (toon == null) {
                System.out.println("No cartoon");
                break;
            }

            Chemical chemical = toon.getChemicalFromScalar((int)scalar);
            if (chemical == null) {
                System.out.println("No chemical");
                break;                
            }

            if (chemical instanceof Residue) {
                clickedResidue = (Residue) chemical;
            }
            else if (chemical instanceof Atom) {
                // figure out residue
                clickedResidue = ((Atom)chemical).getResidue();
            }
            
            break;
        } while (true);

        // Double click to center on residue, 
        // or center on whatever was clicked
        if (event.getClickCount() == 2) {
            if (clickedResidue != null) {
                residueCenterBroadcaster.fireCenter(clickedResidue);
                return;
            }
            else if (pickPosition != null) {
                cam.SetFocalPoint(pickPosition.toArray());
                resetCameraClippingRange();
                repaint();
                return;
            }
        }
        
        if (clickedResidue == null) return;
        
        // Shift/ctrl click to add or remove residue to selection
        else if ( event.isControlDown() || event.isShiftDown() ) { // Control click preserves other selections
            if (highlightResidues.containsKey(clickedResidue))
                residueHighlightBroadcaster.fireUnhighlightResidue(clickedResidue);
            else
                residueHighlightBroadcaster.fireHighlight(clickedResidue);
        }
        // Regular click to select just this residue
        else { // Normal click - unselect all
            residueHighlightBroadcaster.fireUnhighlightResidues();
            residueHighlightBroadcaster.fireHighlight(clickedResidue);            
        }
        
        repaint();

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
    
    public void keyPressed(KeyEvent e) {        
        currentlyDepressedKeyboardKeys.add(KeyEvent.getKeyText(e.getKeyCode()));
        super.keyPressed(e);
    }
    public void keyReleased(KeyEvent e) {
        currentlyDepressedKeyboardKeys.remove(KeyEvent.getKeyText(e.getKeyCode()));
        super.keyReleased(e);
    }

    public void unhighlightResidues() {
        highlightResidues.clear();
        for (ActorCartoon toon : actorCartoons.values()) {
            if (! (toon instanceof ResidueHighlightListener)) continue;
            ResidueHighlightListener listener = (ResidueHighlightListener) toon;
            listener.unhighlightResidues();
        }
        repaint();
    }
    public void unhighlightResidue(Residue residue) {
        highlightResidues.remove(residue);
        for (ActorCartoon toon : actorCartoons.values()) {
            if (! (toon instanceof ResidueHighlightListener)) continue;
            ResidueHighlightListener listener = (ResidueHighlightListener) toon;
            listener.unhighlightResidue(residue);
        }
        repaint();
    }
    public void highlightResidue(Residue residue, Color color) {
        highlightResidues.put(residue, color);
        for (ActorCartoon toon : actorCartoons.values()) {
            if (! (toon instanceof ResidueHighlightListener)) continue;
            ResidueHighlightListener listener = (ResidueHighlightListener) toon;
            listener.highlightResidue(residue, color);
        }
        repaint();
    }

    public void centerOnResidue(Residue residue) {
        Vector3D position = null;

        // Try back bone position
        if (position == null) {
            try {position = residue.getBackbonePosition();}
            catch (Exception exc) {}
        }
        
        // Try center of mass
        if (position == null) position = residue.getCenterOfMass();
        
        if (position == null) return;
        
        
        cam.SetFocalPoint(position.toArray());
        resetCameraClippingRange();
        repaint();
    }

    // Preserve highlights when a new cartoon is used
    @Override
    public void add(MoleculeCartoon cartoon) {
        super.add(cartoon);
        for (Residue residue : highlightResidues.keySet()) {
            highlightResidue(residue, highlightResidues.get(residue));
        }
    }

}