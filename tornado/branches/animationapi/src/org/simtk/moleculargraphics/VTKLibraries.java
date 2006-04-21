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
 * Created on Nov 1, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

/** Class to manage loading of native shared libraries for vtk visualization
 * tool kit.  The "load" method should be invoked before vtk gets a chance to
 * instantiate vtkPanel, which can cause problems with library loading order
 * in Java web start.
 * 
  * @author Christopher Bruns
  * 

 */
public class VTKLibraries {

    // This must be called once before instantiating a vtkPanel, so
    // that the libraries can be loaded in the correct order
    public static void load() {
        loadNativeLibraries();
    }
    
    private static void loadNativeLibraries() {
        loadOneNativeLibrary("jogl"); 

        // To supplement those libraries loaded by vtkPanel
        // when in Java Web Start mode
        loadOneNativeLibrary("vtkNetCDF");
        loadOneNativeLibrary("vtkexoIIc");
        loadOneNativeLibrary("vtkfreetype"); 
        loadOneNativeLibrary("vtkexpat"); 
        loadOneNativeLibrary("vtkjpeg"); 
        loadOneNativeLibrary("vtkzlib"); 
        loadOneNativeLibrary("vtktiff"); 
        loadOneNativeLibrary("vtkpng"); 
        loadOneNativeLibrary("vtkftgl"); 
        loadOneNativeLibrary("vtkCommon"); 
        loadOneNativeLibrary("vtkFiltering"); 
        loadOneNativeLibrary("vtkDICOMParser"); 
        loadOneNativeLibrary("vtkMPEG2Encode");
        
        loadOneNativeLibrary("vtkImaging"); 
        loadOneNativeLibrary("vtkGraphics"); 
        loadOneNativeLibrary("vtkIO"); 
        loadOneNativeLibrary("vtkRendering");
        loadOneNativeLibrary("vtkVolumeRendering");
        loadOneNativeLibrary("vtkHybrid"); 
        // loadOneNativeLibrary("jogl_cg"); 

        
        loadOneNativeLibrary("vtkCommonJava"); 
        loadOneNativeLibrary("vtkFilteringJava"); 
        loadOneNativeLibrary("vtkIOJava"); 
        loadOneNativeLibrary("vtkImagingJava"); 
        loadOneNativeLibrary("vtkGraphicsJava"); 
        loadOneNativeLibrary("vtkRenderingJava"); 
        loadOneNativeLibrary("vtkVolumeRenderingJava");
        loadOneNativeLibrary("vtkHybridJava");
    }
    
    private static void loadOneNativeLibrary(String libName) {
        try {System.loadLibrary(libName);}
        catch (UnsatisfiedLinkError exc) {
            System.err.println("Failed to load native library " + libName + " : " + exc);
        }
    }
    
}
