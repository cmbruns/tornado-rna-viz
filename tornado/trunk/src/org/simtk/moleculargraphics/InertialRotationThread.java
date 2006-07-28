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
 * Created on Apr 28, 2005
 *
 */
package org.simtk.moleculargraphics;

import javax.swing.SwingUtilities;
import vtk.vtkCamera;
import org.simtk.geometry3d.*;

enum RotationStyle {NONE, NUTATE, ROCK, ROTATE};

/** 
 * @author Christopher Bruns
 * 
 * Thread to periodically rotate the view so it spins without user intervention
 */
public class InertialRotationThread extends Thread {
    volatile RotationStyle rotationStyle = RotationStyle.NONE;
    volatile boolean pauseRotation = false;

    private double rockFactor = 1.0;
    
    private volatile double currentAngle = 0.0; // Degrees

    private double rotationPerFrame = 0.6; // Degrees
    
    private double nutationStep = -rotationPerFrame * 10; // Degrees
    private volatile double nutationAngle = 0.0;
    private double nutationConeAngle = 5.0; // Degrees
    
    private double rockRadius = 4.0; // Degrees
    private int direction = 1;
    private int millisecondsPerFrame = 100;

    // Don't let multiple rotations pile up
    private volatile boolean eventPending = false;
    
    private Tornado3DCanvas canvas;
    private Tornado tornado;
    private vtkCamera cam;
    
    public InertialRotationThread(Tornado t) {
        tornado = t;
        canvas = tornado.canvas;
        cam = tornado.canvas.GetRenderer().GetActiveCamera();
    }
    
    public void initialize() {
        currentAngle = 0.0;
        nutationAngle = 0.0;
        rockFactor = 1.0;
    }
    
    public void animate() {

        // this does the update on the awt event thread and
        // is necessary to keep the main thread from locking
        // up...
        Runnable updateAComponent = new Runnable() {
    	    public void run() { 
    	        switch(rotationStyle) {
                case NONE:
                    break;
                case NUTATE:                    
                    // Up direction of camera
                    Vector3D up = new Vector3DClass(cam.GetViewUp()).unit();

                    // Forward direction of camera
                    Vector3D focus = new Vector3DClass(cam.GetFocalPoint());
                    Vector3D camPos = new Vector3DClass(cam.GetPosition());
                    Vector3D forward = focus.minus(camPos);
                    double viewDist = forward.length();
                    forward = forward.unit();

                    // Left direction of camera
                    Vector3D left = up.cross(forward).unit();
                    
                    double radius = Math.sin(nutationConeAngle * Math.PI / 180.0) * viewDist;
                    double stepDistance = radius * 2.0 * Math.sin(0.5 * nutationStep * Math.PI / 180.0);
                    
                    double midAngle = nutationAngle + 0.5 * nutationStep;
                    Vector3D step = 
                        up.times(-Math.sin(midAngle * Math.PI / 180.0) * stepDistance).plus(
                        left.times(Math.cos(midAngle * Math.PI / 180.0) * stepDistance));

                    Vector3D newPos = camPos.plus(step);

                    //  System.out.println("Position = " + camPos);
                    //  System.out.println("   New position = " + newPos);
                    
                    cam.SetPosition(newPos.toArray());
                    
                    nutationAngle += nutationStep;
                    // TODO
                    
                    break;
                case ROCK:
                    if (currentAngle >= rockRadius) direction = -1;
                    if (currentAngle <= -rockRadius) direction = 1;
                    
                    // Make the rocking fast toward the center, slow toward the edges
                    // Rotation speed should follow a cosine function

                    rockFactor = 0.50 * (1.0 + Math.cos(0.75 * Math.PI * currentAngle / rockRadius));
                    if (rockFactor < 0.30) rockFactor = 0.30; // Don't let it go too slowly
                case ROTATE:
                    double actualRotation = rockFactor * rotationPerFrame * direction;                    
                    currentAngle += actualRotation;                    
                    cam.Azimuth(actualRotation);
                    break;
                }
                                
                canvas.resetCameraClippingRange();
                canvas.repaint();
                
                // We are done, so let another action come along
                eventPending = false;
            }
        };
        // Send action to the awt event thread
        SwingUtilities.invokeLater(updateAComponent);

    }

    public void run() {
        while (true) {
	        try {
                // Time step between rotations frames
	            sleep(millisecondsPerFrame);
                
                // So many ways to avoid doing rotations below...
                
                // The user has asked for there to be no rotation
                if (rotationStyle == RotationStyle.NONE) {
                    sleep(30000);
                    continue;
                }
                // Temporarily suspend rotation while something else is happening
	            if (pauseRotation) {
                    sleep(30000);
                    continue;
                }
                // Do not rotate if the user is trying to do something
                if (tornado.userIsInteracting()) {
                    tornado.flushUserIsInteracting();
                    continue;
                }
                // Do not add more rotations if there is still a rotation in the event queue
	            if (!eventPending) {
                    eventPending = true;
                    animate();
                }
	        } catch (InterruptedException exc) {}
        }
    }
}
