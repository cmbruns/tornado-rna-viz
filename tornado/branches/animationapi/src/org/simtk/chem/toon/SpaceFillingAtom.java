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
 * Created on Apr 19, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.toon;

import org.simtk.molecularstructure.atom.*;
import org.simtk.geometry3d.*;
import vtk.*;
import java.awt.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * One atom represented as a space-filling sphere.
  * There is one private singleton vtkGlyph3D object containing all instances of this class
 */
public class SpaceFillingAtom extends BaseMolToon {

    // singleton vtkGlyph3D container for all instances of this class
    static protected VtkSphereGlyphPipeline vtkSpherePipeline;
    static {
        vtkSpherePipeline = new VtkSphereGlyphPipeline(1.0, 10);
    }
    
    // This atom is the data model that we are presenting a View of
    protected LocatedAtom atom;
    
    // Keep track of internal data so we can know whether to initiate an update
    // Problem : we want to call "Modified()" only after all atoms are updated.
    protected Vector3D cachedAtomCenter;
    protected double cachedRadius;
    protected Color cachedColor;
    protected ColorScheme colorScheme = new AtomColorScheme();

    public SpaceFillingAtom(LocatedAtom atom) {
        this.atom = atom;
        createGlyph();
    }
    
    public boolean update() {
        // Don't update anything if the atom has not changed
        boolean isChanged = false;
        if (! cachedAtomCenter.equals(atom.getCoordinates())) isChanged = true;
        if (cachedRadius != atom.getVanDerWaalsRadius()) isChanged = true;
        if (isChanged) {
            createGlyph();
        }
        return isChanged;
    }

    public vtkAssembly getVtkAssembly() {
        return vtkSpherePipeline.getVtkAssembly();
    }
    
    // Implement atom sphere, for both construction and updates
    // This routine does not check whether the atom has changed.  See update() for that.
    private void createGlyph() {
        // Update cached values for change checking
        cachedAtomCenter = new Vector3DClass(atom.getCoordinates());
        cachedRadius = atom.getVanDerWaalsRadius();
        cachedColor = colorScheme.color(atom);
       
        vtkSpherePipeline.placeToon(this, cachedAtomCenter, cachedRadius, cachedColor);
    }
    
    public void show() {vtkSpherePipeline.show(this);}
    public void hide() {vtkSpherePipeline.hide(this);}
    public void highlight() {vtkSpherePipeline.highlight(this);}
    public void unHighlight() {vtkSpherePipeline.unHighlight(this);}
    
}
