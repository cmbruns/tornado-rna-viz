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
 * Created on Apr 29, 2005
 *
 */
package org.simtk.moleculargraphics;

import javax.swing.*;

import vtk.*;

/** 
 * @author Christopher Bruns
 * 
 * Me trying to figure out how to orient cylinders properly
 */
public class TestBondCylinder extends vtkCanvas {
    public static final long serialVersionUID = 1L;

    public TestBondCylinder() {
        
    }
    
    public static void main(String[] args) {
        TestBondCylinder canvas = new TestBondCylinder();
        JFrame frame = new JFrame();
        frame.add(canvas);
        
        // Start with the usual cone
        vtkConeSource coneSource = new vtkConeSource();
        vtkPolyDataMapper coneMapper = new vtkPolyDataMapper();
        coneMapper.SetInput(coneSource.GetOutput());
        vtkActor coneActor = new vtkActor();
        coneActor.SetMapper(coneMapper);
        // canvas.GetRenderer().AddActor(coneActor);
        
        // Make a cylinder
        vtkCylinderSource cylinderSource = new vtkCylinderSource();
        cylinderSource.SetResolution(5);
        cylinderSource.SetRadius(0.05);
        cylinderSource.SetHeight(1.0);

        // Rotate the cylinder so that the cylinder axis goes along the normals in glyphing
        vtkTransform cylinderTransform = new vtkTransform();
        cylinderTransform.Identity();
        cylinderTransform.RotateZ(90);
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        cylinderFilter.SetInput(cylinderSource.GetOutput());
        cylinderFilter.SetTransform(cylinderTransform);
        
        vtkPolyDataMapper cylinderMapper = new vtkPolyDataMapper();
        cylinderMapper.SetInput(cylinderSource.GetOutput());
        vtkActor cylinderActor = new vtkActor();
        cylinderActor.SetMapper(cylinderMapper);
        // canvas.GetRenderer().AddActor(cylinderActor);
        
        // Try two cylinder glyphs
        vtkGlyph3D cylinderGlyph = new vtkGlyph3D();

        vtkPoints points = new vtkPoints();
        points.InsertNextPoint(-1, 0, 0);
        points.InsertNextPoint(1, 0, 0);
        
        vtkFloatArray normals = new vtkFloatArray();
        normals.SetNumberOfComponents(3);
        normals.InsertNextTuple3(0, 1.5, 0);
        normals.InsertNextTuple3(0, 0, 0.68);

        vtkPolyData pointData = new vtkPolyData();
        pointData.SetPoints(points);
        pointData.GetPointData().SetNormals(normals);

        cylinderGlyph.SetSource(cylinderFilter.GetOutput());
        cylinderGlyph.SetInput(pointData);
        cylinderGlyph.SetVectorModeToUseNormal();
        cylinderGlyph.SetScaleModeToScaleByVector();
        
        vtkPolyDataMapper glyphMapper = new vtkPolyDataMapper();
        glyphMapper.SetInput(cylinderGlyph.GetOutput());
        vtkActor glyphActor = new vtkActor();
        glyphActor.SetMapper(glyphMapper);
        canvas.GetRenderer().AddActor(glyphActor);
        
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
