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
package org.simtk.moleculargraphics.cartoon;

import org.simtk.util.*;
import org.simtk.molecularstructure.*;
import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * Rope and cylinder RNA structure, using modular components
 */
public class RopeAndCylinder2 extends MolecularCartoonNewWay {

    DuplexCylinderCartoon duplexes = new DuplexCylinderCartoon();
    TubeAndStickTrace ropes = new TubeAndStickTrace(1.50, 0.50);
    
    vtkAssembly assembly = new vtkAssembly();
    
    public RopeAndCylinder2() {
        assembly.AddPart(duplexes.getAssembly());
        assembly.AddPart(ropes.getAssembly());
    }
    
    public void select(Selectable s) {
        duplexes.select(s);
        ropes.select(s);
    }
    public void unSelect(Selectable s) {
        duplexes.unSelect(s);
        ropes.unSelect(s);
    }
    public void unSelect() {
        duplexes.unSelect();
        ropes.unSelect();
    }
    public void highlight(Molecule m) {
        duplexes.highlight(m);
        ropes.highlight(m);
    }
    public void hide(Molecule m) {
        duplexes.hide(m);
        ropes.hide(m);
    }
    public void show(Molecule m) {
        duplexes.show(m);
        ropes.show(m);
    }
    public void clear() {
        duplexes.clear();
        ropes.clear();
    }
    public vtkAssembly getAssembly() {return assembly;}
}
