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
 * Created on Nov 15, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.gui;

import javax.swing.*;
import java.awt.*;
import org.simtk.mvc.*;
import java.util.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Watches a background process and updates a progress dialog
 */
public class ProgressManager extends Thread implements ObservableInterface {
    
    private MonitoredProcess managedProcess;
    private ProgressDialog progressDialog;
    private boolean processIsRunning = true;
    private ObservableInterface observable = new SimpleObservable();
    // private ProgressPanel progressPanel = null;
    
    public ProgressManager(MonitoredProcess process, ProgressDialog progressPanel) {
        managedProcess = process;
        this.progressDialog = progressPanel;
    }
    
    public ProgressManager(MonitoredProcess process, 
            Component parent, 
            String description) {

        managedProcess = process;
        
        ProgressDialogClass progressDialogClass = new ProgressDialogClass(description);
        progressDialogClass.setLocationRelativeTo(parent);
        progressDialogClass.show();
        this.progressDialog = progressDialogClass;
    }

    public void run() {
        try {
            progressDialog.setStartTime(new Date());
            
            while(processIsRunning) {
                // Update dialog every 200 milliseconds
                sleep(200);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateDialog();
                    }
                });
            }
        }
        catch (InterruptedException exc) {
            hideDialog(); // do this first, abort could take a while
            managedProcess.abort();
            // TODO figure out how to reenable the user to launch another process
        }
        finally {
            hideDialog();
        }
    }

    // Delegate observable interface
    public void addObserver(Observer observer) {observable.addObserver(observer);}
    public void deleteObserver(Observer observer) {observable.deleteObserver(observer);}
    public void deleteObservers() {observable.deleteObservers();}
    public void notifyObservers(Object object) {observable.notifyObservers(object);}
    public void notifyObservers() {observable.notifyObservers();}
    public boolean hasChanged() {return observable.hasChanged();}
    
    public void hideDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressDialog.hide();
            }
        });        
    }
    
    public void abort() {
        processIsRunning = false;
        interrupt();
    }
    
    private void updateDialog() {
        // Is the process complete?
        if (managedProcess.isSuccessful()) {
            processIsRunning = false;
            progressDialog.hide();
        }
        // Is the process dead, presumably by error?
        else if ( (managedProcess.isFailed()) || (! managedProcess.isAlive()) ) {
            processIsRunning = false;
            // TODO show error dialog
        }
        else if (progressDialog.isCancelled()) {
            abort();
        }
        // Update process status
        else {
            progressDialog.updateState();
            // TODO
        }
    }
}
