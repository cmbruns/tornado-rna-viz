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
 * Created on Jun 13, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.molecularstructure.LocatedMolecule;
import java.util.*;
import vtk.vtkAssembly;
import vtk.vtkProp3D;

public abstract class CompositeCartoon extends MolecularCartoonClass {
    protected List<MutableMolecularCartoon> subToons = new Vector<MutableMolecularCartoon>();
    protected vtkAssembly assembly = new vtkAssembly();

    public CompositeCartoon() {}
    
    public CompositeCartoon(Collection<MutableMolecularCartoon> toons) {
        for (MutableMolecularCartoon toon : toons) {
            addSubToon(toon);
        }
    }
    
    public void addSubToon(MutableMolecularCartoon toon) {
        subToons.add(toon);
        assembly.AddPart(toon.getVtkProp3D());
    }
    
    public void add(LocatedMolecule m) {
        for (MutableMolecularCartoon toon : subToons) 
            toon.add(m);
    }
    
    public vtkProp3D getVtkProp3D() {
        return assembly;
    }

}
