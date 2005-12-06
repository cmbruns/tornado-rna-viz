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
 * Tubes connecting backbone, plus rods for nucleotides
 */
public class TubeAndStickTrace extends MolecularCartoonNewWay {
    double backboneRadius = 1.50;
    double rodRadius = 0.50;

    int stickResolution = 5;

    BackboneStick tubes;
    NucleotideStickCartoon rods;
    
    vtkAssembly assembly = new vtkAssembly();

    public TubeAndStickTrace() {
        this(1.50, 0.5);
    }

    public TubeAndStickTrace(double r1, double r2) {
        backboneRadius = r1;
        rodRadius = r2;

        tubes = new BackboneStick(backboneRadius);
        rods = new NucleotideStickCartoon(rodRadius);
        
        assembly.AddPart(tubes.getAssembly());
        assembly.AddPart(rods.getActor());
    }
    
    public void select(Selectable s) {
        tubes.select(s);
        rods.select(s);
    }
    public void unSelect(Selectable s) {
        tubes.unSelect(s);
        rods.unSelect(s);
    }
    public void unSelect() {
        tubes.unSelect();
        rods.unSelect();
    }
    public void highlight(LocatedMolecule m) {
        tubes.highlight(m);
        rods.highlight(m);
    }
    public void hide(LocatedMolecule m) {
        tubes.hide(m);
        rods.hide(m);
    }
    public void show(LocatedMolecule m) {
        tubes.show(m);
        rods.show(m);
    }
    public void clear() {
        tubes.clear();
        rods.clear();
    }
    public vtkAssembly getAssembly() {return assembly;}
}
