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
    volatile boolean rock = true; // Spin instead of rock if false
    volatile boolean sitStill = false;
    double rotationPerFrame = 0.5;
    double rockRadius = 3.0;
    volatile double currentAngle = 0.0;

    // Don't let multiple rotations pile up
    volatile boolean eventPending = false;
    
    Tornado3DCanvas canvas;
    Tornado tornado;
    
    public InertialRotationThread(Tornado t) {
        tornado = t;
        canvas = tornado.canvas;
    }
    
    public void animate() {

        // this does the update on the main gui thread and
        // is necessary to keep the main thread from locking
        // up...
        Runnable updateAComponent = new Runnable() {
    	    public void run() { 

                if (rock && (currentAngle > rockRadius) && (rotationPerFrame > 0)) rotationPerFrame *= -1;
                if (rock && (currentAngle < -rockRadius) && (rotationPerFrame < 0)) rotationPerFrame *= -1;
                currentAngle += rotationPerFrame;

                canvas.Azimuth(rotationPerFrame);
                canvas.myResetCameraClippingRange();
                canvas.repaint();
                
                // We are done, so let another action come along
                eventPending = false;
            }
        };
        SwingUtilities.invokeLater(updateAComponent);

    }

    public void run() {
        while (true) {
	        try {
                // Time step between rotations frames
	            sleep(100); // milliseconds
                
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
                if (tornado.userIsInteracting) {
                    tornado.userIsInteracting = false;
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
