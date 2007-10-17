/* Portions copyright (c) 2007 Stanford University and Christopher Bruns
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
 * Created on Mar 20, 2007
 * Original author: Christopher Bruns
 */
package org.simtk.tornado.command;

import java.awt.event.ActionEvent;
import javax.swing.*;
import vtk.*;
import java.awt.Component;
import org.simtk.util.MyJFileFilter;

public class SaveWavefrontObjFileAction extends AbstractAction {

    protected JFileChooser fileChooser = new JFileChooser();
    protected vtkPanel canvas;
    protected Component parentComponent;
    
    public SaveWavefrontObjFileAction(vtkPanel canvas, Component parentComponent) {
        super("Save Wavefront .obj/.mtl file...");
        
        this.canvas = canvas;
        this.parentComponent = parentComponent;
        
        MyJFileFilter fileFilter = new MyJFileFilter();
        fileFilter.addExtension("obj");
        fileFilter.addExtension("mtl");
        fileFilter.setDescription("Wavefront files (*.obj, *.mtl)");
        fileChooser.setFileFilter(fileFilter);
    }
    
    public void actionPerformed(ActionEvent arg0) {
        int returnVal = fileChooser.showSaveDialog(parentComponent);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            
            vtkOBJExporter objExporter = new vtkOBJExporter();
            objExporter.SetInput( canvas.GetRenderWindow() );
            objExporter.SetFilePrefix(fileName);
            objExporter.Write();
            
            System.out.println("Wrote " + fileName);
        }

    }

}
