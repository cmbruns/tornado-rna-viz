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
 * Created on May 8, 2005
 *
 */
package org.simtk.moleculargraphics;

import java.awt.*;
import org.simtk.geometry3d.*;
import vtk.*;

public class GraphicsCylinder {
    static public vtkTransformPolyDataFilter getVtkCylinderFilter(Cylinder c, int resolution) {
        vtkCylinderSource cylinderSource = new vtkCylinderSource();

        cylinderSource.SetRadius(c.getRadius());
        Vector3DClass center = new Vector3DClass( c.getHead().plus(c.getTail()).times(0.5) );
        cylinderSource.SetHeight(c.getHead().distance(c.getTail()));
        cylinderSource.SetResolution(resolution);
        
        // Set orientation
        // vtkCylinderSource begins along the y axis
        Vector3DClass direction = new Vector3DClass( c.getHead().minus(c.getTail()).unit() );
        Vector3DClass xAxis = new Vector3DClass(1,0,0);
        Vector3DClass yAxis = new Vector3DClass(0,1,0);
        Vector3DClass zAxis = new Vector3DClass(0,0,1);
        // Project vector onto Y plane
        Vector3DClass yProjection = new Vector3DClass( new Vector3DClass(direction.getX(), 0, direction.getZ()).unit() );
        double radiansToDegrees = 180.0 / Math.PI;
        // How far is cylinder tilted from straight up?
        double yRotationAngle = Math.acos(yAxis.dot(direction)) * radiansToDegrees;
        double zRotationAngle = Math.acos(yProjection.dot(zAxis)) * radiansToDegrees;
        if (direction.getX() < 0) zRotationAngle *= -1.0;        
        vtkTransform orientation = new vtkTransform();
        // orientation.Identity();
        orientation.Translate(center.getX(), center.getY(), center.getZ());
        orientation.RotateY(zRotationAngle);
        orientation.RotateX(yRotationAngle); // Tilted this far from straight up
        vtkTransformPolyDataFilter cylinderFilter = new vtkTransformPolyDataFilter();
        cylinderFilter.SetInput(cylinderSource.GetOutput());
        cylinderFilter.SetTransform(orientation);
        
        return cylinderFilter;
    }
        

    static public vtkActor getVtkCylinder(Cylinder c, Color color, int resolution) {
        vtkTransformPolyDataFilter cylinderFilter = getVtkCylinderFilter(c, resolution);
        
        vtkPolyDataMapper cylinderMapper = new vtkPolyDataMapper();
        cylinderMapper. SetInput(cylinderFilter.GetOutput());
        vtkActor cylinderActor = new vtkActor();
        cylinderActor.SetMapper(cylinderMapper);
        // cylinderActor.GetProperty().SetOpacity(0.5);
        if (color != null)
            cylinderActor.GetProperty().SetColor(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
        return cylinderActor;
    }
}