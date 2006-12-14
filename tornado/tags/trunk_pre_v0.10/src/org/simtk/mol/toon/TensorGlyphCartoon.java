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
 * Created on Jul 8, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import vtk.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Use TensorGlyph instead of Glyph3D, so that full orientation can be specified.
 */
abstract public class TensorGlyphCartoon extends GlyphCartoon {

    private vtkTensorGlyph tensorGlyph = new vtkTensorGlyph();
    private vtkTensorGlyph highlightTensorGlyph = new vtkTensorGlyph();
    
    vtkFloatArray tensors = new vtkFloatArray();

    TensorGlyphCartoon() {
        tensorGlyph.ExtractEigenvaluesOff();  // Treat as a rotation matrix, not as something with eigenvalues
        tensorGlyph.ThreeGlyphsOff();
        tensorGlyph.SymmetricOff();

        highlightTensorGlyph.ExtractEigenvaluesOff();  // Treat as a rotation matrix, not as something with eigenvalues
        highlightTensorGlyph.ThreeGlyphsOff();
        highlightTensorGlyph.SymmetricOff();
        highlightTensorGlyph.SetScaleFactor(highlightFactor);

        tensors.SetNumberOfComponents(9);
        lineData.GetPointData().SetTensors(tensors);
        
        // disconnect the vtkGlyph3D, which is replaced by TensorGlyph in this derived class
        tensorGlyph.SetInput(lineData);
        mapper.SetInput(tensorGlyph.GetOutput());
        
        highlightTensorGlyph.SetInput(lineData);
        highlightMapper.SetInput(highlightTensorGlyph.GetOutput());
        
        // Cause errors if subclasses try to use overridden members
        // lineGlyph = null;
        // highlightGlyph = null;
    }

    // Override these functions to use TensorGlyph rather than vtkGlyph3D
    public void setGlyphSource(vtkPolyData data) {
        tensorGlyph.SetSource(data);
        highlightTensorGlyph.SetSource(data);
    }
    public void scaleNone() {
        tensorGlyph.SetScaling(0); // Do not adjust size
        highlightTensorGlyph.SetScaling(0); // Do not adjust size        
    }
    public void scaleByNormal() {
        throw new UnsupportedOperationException();
    }
    public void orientByNormal() {
        throw new UnsupportedOperationException();
    }
    public void colorByScalar() {
        tensorGlyph.ColorGlyphsOn();
        tensorGlyph.SetColorMode(0);
        highlightTensorGlyph.ColorGlyphsOn();
        highlightTensorGlyph.SetColorMode(0);
    }
}
