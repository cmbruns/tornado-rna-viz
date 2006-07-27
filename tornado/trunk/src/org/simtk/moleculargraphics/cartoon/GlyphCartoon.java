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
 * Created on Jul 6, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.Molecule;
import org.simtk.molecularstructure.Molecule;
import org.simtk.molecularstructure.MoleculeCollection;

import vtk.*;

/**
 * Cartoon consisting  of a single vtkGlyph3D.
 * Intended to be base class for wireframe and atomspheres, among others.
  * @author Christopher Bruns
  * 
 */
public abstract class GlyphCartoon extends ActorCartoonClass {
    // GlyphIndex glyphColors = new GlyphIndex();

    // Glyph positions
    vtkPolyData lineData = new vtkPolyData();
    vtkPoints linePoints = new vtkPoints(); // bond centers
    vtkFloatArray lineNormals = new vtkFloatArray(); // bond directions/lengths

    // vtkFloatArray lineScalars = new vtkFloatArray();

    vtkFloatArray colorScalars = new vtkFloatArray();
   
    private vtkGlyph3D lineGlyph = new vtkGlyph3D();
    // vtkPolyDataMapper glyphMapper = mapper;

    // vtkActor glyphActor = new vtkActor();

    // private MassBodyClass massBody = new MassBodyClass();
    
    GlyphCartoon() {
        super();
        
        colorScalars.SetNumberOfComponents(1);
        colorScalars.SetName("colors");

        // glyphColors.setData(lineData);        
        lineNormals.SetNumberOfComponents(3);
        lineData.SetPoints(linePoints);
        lineData.GetPointData().SetNormals(lineNormals);

        lineData.GetPointData().SetScalars(colorScalars);        
        
        lineGlyph.SetInput(lineData);

        mapper.SetInput(lineGlyph.GetOutput());
        
        actor.SetMapper(mapper);
    }

    // Override these functions to use TensorGlyph rather than vtkGlyph3D
    public void setGlyphSource(vtkPolyData data) {
        lineGlyph.SetSource(data);
    }
    public void scaleByNormal() {
        lineGlyph.SetScaleModeToScaleByVector(); // Take length from normal
        lineGlyph.SetVectorModeToUseNormal(); // Take direction from normal
    }
    public void scaleNone() {
        lineGlyph.SetScaleModeToDataScalingOff(); // Do not adjust size
    }
    public void orientByNormal() {
        lineGlyph.SetVectorModeToUseNormal(); // Take direction from normal        
    }
    public void colorByScalar() {
        lineGlyph.SetColorModeToColorByScalar(); // Take color from scalar        
    }

    public abstract void addMolecule(Molecule molecule);

    class GlyphPosition {
        private vtkPolyData glyphData;
        vtkDataArray colorIndexArray;
        int arrayIndex;
        int unselectedColorIndex;
        
        public GlyphPosition(vtkPolyData d, int i, int c) {
            glyphData = d;
            colorIndexArray = glyphData.GetPointData().GetScalars();
            // colorIndexArray = a;
            arrayIndex = i;
            unselectedColorIndex = c;
        }        
        public void setPosition(Vector3D v) {
            glyphData.GetPoints().SetPoint(arrayIndex, v.getX(), v.getY(), v.getZ());
        }
        public void setNormal(Vector3D v) {
            glyphData.GetPointData().GetNormals().SetTuple3(arrayIndex, v.getX(), v.getY(), v.getZ());
        }
    }
    
    
    class GlyphIndex {

        // Hashtable residueGlyphs = new Hashtable();
        // Hashtable atomGlyphs = new Hashtable();
        // Index atoms, residues, etc. to sets of glyphs
        Hashtable objectGlyphs = new Hashtable();
        HashSet allGlyphs = new HashSet();

        vtkPolyData vtkData;
        
        public GlyphIndex() {}
        void setData(vtkPolyData d) {
            vtkData = d;
        }
        
        boolean containsKey(Object o) {
            if (objectGlyphs.containsKey(o)) return true;
            // if (atomGlyphs.containsKey(o)) return true;
            return false;
        }

        public void add(Collection<Object> objectKeys, vtkPolyData pointData, int arrayIndex, int colorIndex) {
            GlyphPosition glyph = new GlyphPosition(pointData, arrayIndex, colorIndex);
            allGlyphs.add(glyph);
            // Index this one glyph by all of the entities it represents
            for (Iterator i = objectKeys.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (! objectGlyphs.containsKey(o)) objectGlyphs.put(o, new Vector());
                Vector glyphs = (Vector) objectGlyphs.get(o);
                glyphs.add(glyph);
            }
        }

    }
    
}
