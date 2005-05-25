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
        Vector3D center = c.getHead().plus(c.getTail()).scale(0.5);
        cylinderSource.SetHeight(c.getHead().distance(c.getTail()));
        cylinderSource.SetResolution(resolution);
        
        // Set orientation
        // vtkCylinderSource begins along the y axis
        Vector3D direction = c.getHead().minus(c.getTail()).unit();
        Vector3D xAxis = new Vector3D(1,0,0);
        Vector3D yAxis = new Vector3D(0,1,0);
        Vector3D zAxis = new Vector3D(0,0,1);
        // Project vector onto Y plane
        Vector3D yProjection = new Vector3D(direction.getX(), 0, direction.getZ()).unit();
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
