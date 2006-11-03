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

import vtk.*;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.moleculargraphics.*;
import java.awt.Color;

public class ActorCartoonClass 
implements ActorCartoon, ResidueHighlightListener
{
    protected ColorScheme invisibleColorScheme = new ConstantColor(new Color(100,100,255,0));
    
    protected vtkActor actor = new vtkActor();
    protected vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    private ToonColors toonColors = new ToonColors(mapper);

    // protected vtkPolyDataMapper selectionMapper = new vtkPolyDataMapper();
    // protected ToonColors selectionToonColors = new ToonColors(selectionMapper);
    // protected vtkActor selectionActor = new vtkActor();

    protected boolean isPopulated = false;

    protected vtkActor highlightActor = new vtkActor();
    protected vtkPolyDataMapper highlightMapper = new vtkPolyDataMapper();
    protected ToonColors highlightToonColors = new ToonColors(highlightMapper);

    ActorCartoonClass() {
        actor.SetMapper(mapper);
        actor.GetProperty().BackfaceCullingOn();
        
        highlightActor.SetMapper(highlightMapper);
        highlightActor.GetProperty().SetRepresentationToWireframe();
        highlightActor.GetProperty().BackfaceCullingOn();
        highlightActor.GetProperty().SetLineWidth(2);

        // Failed attempts to remove shading of cages:
        // SetScalarMaterialModeToAmbient causes transparency to be ignored
        // SetScalarMaterialModeToAmbientAndDiffuse causes color to be way too pale
        // highlightActor.GetProperty().SetDiffuse(0.0);
        // highlightActor.GetProperty().SetAmbient(1000.0);
        // highlightActor.GetProperty().SetSpecular(0.0); 
        // highlightMapper.SetScalarMaterialModeToAmbientAndDiffuse();

        // Start with no visibility to minimize artefacts of
        // having invisible cages around everything.
        // and also to minimize performance problems
        highlightActor.SetVisibility(0);  
        
        // Until all classes implement a highlightActor, create
        // a default input to the mapper
        vtkPointSource pointSource = new vtkPointSource();
        pointSource.SetRadius(0.0);
        highlightMapper.SetInput(pointSource.GetOutput());
    }
    
    protected int getColorIndex(Chemical colorable) {
        int index = highlightToonColors.getColorIndex(colorable);
        return toonColors.getColorIndex(colorable);
    }
    
    public void colorToon(Object object, ColorScheme colorScheme) {
        toonColors.setColor(object, colorScheme);
    }
    
    public void colorToon(ColorScheme colorScheme) {
        toonColors.setColor(colorScheme);
    }
    
    public vtkActor getActor() {
        return actor;
    }
    
    public vtkPolyDataMapper getMapper() {
        return mapper;
    }    

    public boolean isPopulated() {return isPopulated;}
    
    public Chemical getChemicalFromScalar(int scalar) {
        return toonColors.getChemicalFromScalar(scalar);
    }

    public vtkActor getHighlightActor() {return highlightActor;}
    
    public void unhighlightResidues() {
        highlightToonColors.setColor(invisibleColorScheme);
    }
    public void unhighlightResidue(Residue residue) {
        unhighlightResidueByResidueScalars(residue);
    }
    public void highlightResidue(Residue residue, Color color) {
        highlightResidueByResidueScalars(residue, color);
    }
    
    protected void highlightResidueByResidueScalars(Residue residue, Color color) {
        ColorScheme colorScheme = new ConstantColor(color);
        highlightToonColors.setColor(residue, colorScheme);        

        if (highlightToonColors.hasVisibleColor() )
            highlightActor.SetVisibility(1);
    }
    
    protected void highlightResidueByAtomScalars(Residue residue, Color color) {
        ColorScheme colorScheme = new ConstantColor(color);
        highlightToonColors.setColor(residue, colorScheme);        
        for (Atom atom : residue.atoms()) {
            highlightToonColors.setColor(atom, colorScheme);
        }

        if (highlightToonColors.hasVisibleColor() )
            highlightActor.SetVisibility(1);
    }

    protected void unhighlightResidueByResidueScalars(Residue residue) {
        highlightToonColors.setColor(residue, invisibleColorScheme);        

        if (! highlightToonColors.hasVisibleColor() )
            highlightActor.SetVisibility(0);
    }
    
    protected void unhighlightResidueByAtomScalars(Residue residue) {
        highlightToonColors.setColor(residue, invisibleColorScheme);        
        for (Atom atom : residue.atoms()) {
            highlightToonColors.setColor(atom, invisibleColorScheme);
        }
        
        if (! highlightToonColors.hasVisibleColor() )
            highlightActor.SetVisibility(0);
    }
}
