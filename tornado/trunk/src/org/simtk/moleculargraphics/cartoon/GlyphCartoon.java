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

import java.awt.Color;
import java.util.*;

import org.simtk.molecularstructure.*;
import org.simtk.util.*;
import org.simtk.geometry3d.*;

import vtk.*;

/**
 * Cartoon consisting  of a single vtkGlyph3D.
 * Intended to be base class for wireframe and atomspheres, among others.
  * @author Christopher Bruns
  * 
 */
public abstract class GlyphCartoon extends MolecularCartoonNewWay {
    vtkLookupTable lut = new vtkLookupTable();
    static final int selectionColorIndex = 255;
    static final int highlightColorIndex = 254;
    static final int invisibleColorIndex = 253;
    // static final Color selectionColor = new Color(80, 80, 255);
    static final Color selectionColor = new Color(255,255,150);
    static final Color highlightColor = new Color(250, 250, 50);

    GlyphIndex glyphColors = new GlyphIndex();

    // Glyph positions
    vtkPolyData lineData = new vtkPolyData();
    vtkPoints linePoints = new vtkPoints(); // bond centers
    vtkFloatArray lineNormals = new vtkFloatArray(); // bond directions/lengths
    vtkFloatArray lineScalars = new vtkFloatArray();

    private vtkGlyph3D lineGlyph = new vtkGlyph3D();
    vtkPolyDataMapper glyphMapper = new vtkPolyDataMapper();

    vtkAssembly assembly = new vtkAssembly();
    vtkActor glyphActor = new vtkActor();

    GlyphCartoon() {
        super();
        
        lut.SetNumberOfTableValues(256);
        lut.SetRange(1.0, 60.0);
        lut.SetAlphaRange(1.0, 1.0);
        lut.SetValueRange(1.0, 1.0);
        lut.SetHueRange(0.0, 1.0);
        lut.SetSaturationRange(0.5, 0.5);
        lut.Build();
        
        lut.SetTableValue(selectionColorIndex, selectionColor.getRed()/255.0, selectionColor.getGreen()/255.0, selectionColor.getBlue()/255.0, 1.0);        
        lut.SetTableValue(highlightColorIndex, highlightColor.getRed()/255.0, highlightColor.getGreen()/255.0, highlightColor.getBlue()/255.0, 1.0);        
        lut.SetTableValue(invisibleColorIndex, 0.0, 0.0, 0.0, 0.0);        

        glyphColors.setData(lineData);        
        lineNormals.SetNumberOfComponents(3);
        lineData.SetPoints(linePoints);
        lineData.GetPointData().SetNormals(lineNormals);
        lineData.GetPointData().SetScalars(lineScalars);        
        
        lineGlyph.SetInput(lineData);

        glyphMapper.SetLookupTable(lut);
        glyphMapper.SetScalarRange(0, 255);
        glyphMapper.SetInput(lineGlyph.GetOutput());
        
        glyphActor.SetMapper(glyphMapper);
        
        assembly.AddPart(glyphActor);
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


    public vtkActor getActor() {return glyphActor;}
    
    public void clear() {
        lineNormals.Reset();
        lineNormals.Squeeze();
        lineScalars.Reset();
        lineScalars.Squeeze();
        linePoints.Reset();
        linePoints.Squeeze();
        glyphColors.clear();
    }
    
    public vtkAssembly getAssembly() {
        return assembly;
    }

    public void unSelect() {
        glyphColors.unSelect();
    }

    public void unSelect(Selectable s) {
        glyphColors.unSelect(s);
    }

    public void select(Selectable s) {
        glyphColors.select(s);
    }

    public void highlight(StructureMolecule molecule) {
        glyphColors.highlight(molecule);
        return;
    }
    
    public void hide(StructureMolecule molecule) {
        glyphColors.hide(molecule);
        return;
    }
    
    public void hide() {
        glyphColors.hide();
        return;
    }
    
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
        
        public void show() {
            if (colorIndexArray.GetTuple1(arrayIndex) == invisibleColorIndex)
                colorIndexArray.SetTuple1(arrayIndex, unselectedColorIndex);
        }
        public void hide() {
            colorIndexArray.SetTuple1(arrayIndex, invisibleColorIndex);
        }
        public void highlight() {
            if (colorIndexArray.GetTuple1(arrayIndex) != invisibleColorIndex)
                colorIndexArray.SetTuple1(arrayIndex, highlightColorIndex);
        }
        public void setColor(int colorIndex) {
            colorIndexArray.SetTuple1(arrayIndex, colorIndex);
        }
        public void unSelect() {
            if (colorIndexArray.GetTuple1(arrayIndex) == selectionColorIndex)
                setColor(unselectedColorIndex);
        }
        public void select() {
            if (colorIndexArray.GetTuple1(arrayIndex) != invisibleColorIndex)
                setColor(selectionColorIndex);
        }
        public void setPosition(Vector3D v) {
            glyphData.GetPoints().SetPoint(arrayIndex, v.getX(), v.getY(), v.getZ());
        }
        public void setNormal(Vector3D v) {
            glyphData.GetPointData().GetNormals().SetTuple3(arrayIndex, v.getX(), v.getY(), v.getZ());
        }
    }
    
    
    class GlyphIndex implements SelectionListener {
        static final long serialVersionUID = 0L;

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

        public void add(Vector objectKeys, vtkPolyData pointData, int arrayIndex, int colorIndex) {
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

        public void unSelect() {
            for (Iterator i = allGlyphs.iterator(); i.hasNext(); ) {
                GlyphPosition g = (GlyphPosition) i.next();
                g.unSelect();
            }
            vtkData.GetPointData().GetScalars().Modified();
        }
        
        public void unSelect(Selectable s) {
            if (! (objectGlyphs.containsKey(s))) return;
            Vector glyphVector = (Vector) objectGlyphs.get(s);
            for (int i = 0; i < glyphVector.size(); i++) {
                GlyphPosition g = (GlyphPosition) glyphVector.get(i);
                g.unSelect();
            }
            vtkData.GetPointData().GetScalars().Modified();
        }        
        public void select(Selectable s) {
            // System.out.println("select1");
            if (! (objectGlyphs.containsKey(s))) return;
            Vector glyphVector = (Vector) objectGlyphs.get(s);
            for (int i = 0; i < glyphVector.size(); i++) {
                GlyphPosition g = (GlyphPosition) glyphVector.get(i);
                g.select();
            }
            vtkData.GetPointData().GetScalars().Modified();
            // System.out.println("select2");
        }
        public void hide(Object o) {
            if (! (objectGlyphs.containsKey(o))) return;
            Vector glyphVector = (Vector) objectGlyphs.get(o);
            for (Iterator i = glyphVector.iterator(); i.hasNext(); ) {
                GlyphPosition g = (GlyphPosition) i.next();
                g.hide();
            }
            vtkData.GetPointData().GetScalars().Modified();
        }
        public void hide() {
            Collection glyphVector = allGlyphs;
            for (Iterator i = glyphVector.iterator(); i.hasNext(); ) {
                GlyphPosition g = (GlyphPosition) i.next();
                g.hide();
            }
            vtkData.GetPointData().GetScalars().Modified();
        }

        public void show(Object o) {
            if (! (objectGlyphs.containsKey(o))) return;
            Vector glyphVector = (Vector) objectGlyphs.get(o);
            for (Iterator i = glyphVector.iterator(); i.hasNext(); ) {
                GlyphPosition g = (GlyphPosition) i.next();
                g.show();
            }
            vtkData.GetPointData().GetScalars().Modified();
        }
        public void show() {
            Collection glyphVector = allGlyphs;
            for (Iterator i = glyphVector.iterator(); i.hasNext(); ) {
                GlyphPosition g = (GlyphPosition) i.next();
                g.show();
            }
            vtkData.GetPointData().GetScalars().Modified();
        }

        
        public void highlight(Object o) {
            if (! (objectGlyphs.containsKey(o))) return;
            Vector glyphVector = (Vector) objectGlyphs.get(o);
            for (int i = 0; i < glyphVector.size(); i++) {
                GlyphPosition g = (GlyphPosition) glyphVector.get(i);
                g.highlight();
            }
            vtkData.GetPointData().GetScalars().Modified();
        }
        
        void setDefaultColor(Object residue) {
            if (! objectGlyphs.containsKey(residue)) return;
            Vector glyphs = (Vector) objectGlyphs.get(residue);
            for (int g = 0; g < glyphs.size(); g++) {
                GlyphPosition glyph = (GlyphPosition) glyphs.get(g);
                glyph.show();
            }
        }
        void setColor(Object residue, int colorIndex) {
            if (! objectGlyphs.containsKey(residue)) return;
            Vector glyphs = (Vector) objectGlyphs.get(residue);
            for (int g = 0; g < glyphs.size(); g++) {
                GlyphPosition glyph = (GlyphPosition) glyphs.get(g);
                glyph.setColor(colorIndex);
            }
        }
        void clear() {
            objectGlyphs.clear();
        }
    }
    
}
