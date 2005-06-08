/*
 * Created on Apr 28, 2005
 *
 */
package org.simtk.moleculargraphics;

import javax.swing.SwingUtilities;

/** 
 * @author Christopher Bruns
 * 
 * Thread to periodically rotate the view so it spins without user intervention
 */
public class InertialRotationThread extends Thread {
    volatile boolean pauseRotation = false;
    volatile boolean doRock = true; // Spin instead of rock if false
    volatile boolean sitStill = false;
    volatile double currentAngle = 0.0;

    double rotationPerFrame = 0.3;
    double rockRadius = 3.0;
    int direction = 1;
    int millisecondsPerFrame = 100;

    // Don't let multiple rotations pile up
    volatile boolean eventPending = false;
    
    Tornado3DCanvas canvas;
    Tornado tornado;
    
    public InertialRotationThread(Tornado t) {
        tornado = t;
        canvas = tornado.canvas;
    }
    
    public void animate() {

        // this does the update on the awt event thread and
        // is necessary to keep the main thread from locking
        // up...
        Runnable updateAComponent = new Runnable() {
    	    public void run() { 

                double rockFactor = 1.0;
                if (doRock) {
                    if (currentAngle >= rockRadius) direction = -1;
                    if (currentAngle <= -rockRadius) direction = 1;
                    
                    // Make the rocking fast toward the center, slow toward the edges
                    // Rotation speed should follow a cosine function

                    rockFactor = 0.50 * (1.0 + Math.cos(0.75 * Math.PI * currentAngle / rockRadius));
                    if (rockFactor < 0.30) rockFactor = 0.30; // Don't let it go too slowly
                }

                double actualRotation = rockFactor * rotationPerFrame * direction;
                
                currentAngle += actualRotation;
                
                canvas.Azimuth(actualRotation);
                
                canvas.myResetCameraClippingRange();
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
                if (sitStill) {
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
