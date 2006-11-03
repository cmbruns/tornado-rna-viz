/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Sep 6, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import javax.jnlp.*;
import javax.swing.*;
import edu.stanford.ejalbert.BrowserLauncher;

/**
 *  
  * @author Christopher Bruns
  * 
  * Base class for molecule viewing applications
 */
public class MolApp extends JFrame {

    static {
        // Keep vtk canvas from obscuring swing widgets
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        // Put the menu bar at the top of the screen on the mac
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // Native libraries for visualization tool kit
        VTKLibraries.load();

        // Try to get Windows look and feel on Windows platform
        // Unless a non-Java look and feel is already selected
        if (UIManager.getLookAndFeel().getClass().getName().equals(UIManager.getCrossPlatformLookAndFeelClassName()))
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName() );
            } catch (Exception e) {}

    }

    protected ClassLoader classLoader;
    protected StructureCanvas canvas = new Tornado3DCanvas();

    public MolApp() {
        // Avoid upper left sucky Java window location
        setLocationRelativeTo(null);
        
        classLoader = getClass().getClassLoader();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setBackgroundColor(Color.white);
    }

    public void setBackgroundColor(Color c) {
        if (canvas == null) return;
        canvas.setBackgroundColor(c);
    }        

    public Color getBackgroundColor() {
        if (canvas == null) return null;
        return canvas.getBackgroundColor();
    }
    
    class QuitAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);  // terminate this program
        }
    }

    class BrowserLaunchAction implements ActionListener {
        String urlString;
        BrowserLaunchAction(String u) {urlString = u;}
        public void actionPerformed(ActionEvent e) {
            
            // Show information dialog, so the savvy user will be able to
            // go to the url manually, in case the browser open fails.
            JOptionPane.showConfirmDialog(
                    null, 
                    "Your browser will open to page " + urlString + " in a moment\n", 
                    "Browse to SimTK.org",
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE
                    );

            // New way
            URL url;
            try {url = new URL(urlString);}            
            catch (MalformedURLException exc) {
                launchErrorConfirmDialog("Problem opening browser to page " + urlString + "\n" + exc,
                "Web URL error!");
                return;
            }
            try {
                // This only works when started in a web start application
                BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
                bs.showDocument(url);
            } 
            catch (UnavailableServiceException exc) {
                // launchErrorConfirmDialog("Problem opening browser to page " + urlString + "\n" + exc,
                // "JNLP Error!");
                try {BrowserLauncher.openURL(urlString);}
                catch (IOException exc2) {
                    launchErrorConfirmDialog("Problem opening browser to page " + urlString + "\n" + exc2,
                                             "Web URL error!");
                }        
            }
        }
    }
    
    void launchErrorConfirmDialog(String msg, String title) {
        String[] options = {"Bummer!"};
        JOptionPane.showOptionDialog(
                null, 
                msg, 
                "Web URL error!",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]);        
    }

}
