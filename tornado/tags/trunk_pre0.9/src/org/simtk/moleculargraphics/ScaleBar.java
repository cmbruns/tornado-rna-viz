/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Aug 3, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import vtk.*;

import java.awt.event.*;
import java.awt.Color;
import java.util.*;
import java.text.DecimalFormat;

public class ScaleBar 
implements ComponentListener // Notice when window size changes
{
    StructureCanvas window;
    vtkCamera camera = null;
    
    private double[] whiteColor = {1,1,1};
    private double[] blackColor = {0,0,0};
    private double[] origin = {10, 10, 0};
    private double[] barColor = blackColor;
    private double opacity = 0.5;

    private vtkPoints points = new vtkPoints();
    // private int windowHeight = 100;

    private DecimalFormat numberFormat = new DecimalFormat("#.###");
    private Set<vtkActor2D> actors = new LinkedHashSet<vtkActor2D>();
    
    private vtkImageData backdropImageData = new vtkImageData();
    private vtkActor2D lineActor = new vtkActor2D();
    private vtkTextMapper textMapper = new vtkTextMapper();
    private vtkActor2D textActor = new vtkActor2D();
    private vtkActor2D backdropActor = new vtkActor2D();
    private int backdropBorder = 3;

    public ScaleBar(StructureCanvas window) {
        this.window = window;
        
        this.window.addComponentListener(this);
        // windowHeight = this.window.getHeight();
        
        // Place backdrop first, so it is behind the other stuff
        actors.add(createBackdrop());

        // Begin and end points of scale bar
        points.InsertNextPoint(origin);
        points.InsertNextPoint(origin);
        
        vtkCellArray cells = new vtkCellArray();
        cells.InsertNextCell(points.GetNumberOfPoints());
        for (int i = 0; i < points.GetNumberOfPoints(); i ++)
            cells.InsertCellPoint(i);

        vtkPolyData polyData = new vtkPolyData();
        polyData.SetPoints(points);
        polyData.SetLines(cells);
        
        vtkPolyDataMapper2D mapper = new vtkPolyDataMapper2D();
        mapper.SetInput(polyData);
        
        // Scale bar
        lineActor.SetMapper(mapper);
        lineActor.GetProperty().SetColor(barColor);
        lineActor.GetProperty().SetOpacity(opacity);
        lineActor.GetProperty().SetLineWidth(3.0);
        lineActor.SetVisibility(1);
        actors.add(lineActor);
        
        // Text label
        textMapper.SetInput(""); // This will be overwritten
        textMapper.GetTextProperty().SetColor(barColor);
        textMapper.GetTextProperty().SetOpacity(opacity);        
        textActor.SetMapper(textMapper);
        actors.add(textActor);

    }
    
    public void setBackgroundColor(Color color) {

        // White text for dark background, Black text for light
        double luminosity = 0.2 * color.getRed()/255.0 + 0.7 * color.getGreen()/255.0 + 0.1 * color.getBlue()/255.0;
        if (luminosity <= 0.5)
            barColor = whiteColor;
        else
            barColor = blackColor;
        lineActor.GetProperty().SetColor(barColor);
        textMapper.GetTextProperty().SetColor(barColor);
        
        int w = backdropImageData.GetDimensions()[0];
        int h = backdropImageData.GetDimensions()[1];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int iy = h - 1 - y; // inverse of y coordinate -- to make image right side up
                // SetScalarComponentFromDouble causes no such method error
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 3, 127); // alpha
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 0, color.getRed()); // red
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 1, color.getGreen()); // green
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 2, color.getBlue()); // blue
            }
        }        
    }
    
    private vtkActor2D createBackdrop() {
        // Put a transparent white rectangle
        // behind the scale bar.
        int w = 10;
        int h = 10;
        backdropImageData.SetDimensions(w, h, 1);
        backdropImageData.SetScalarTypeToUnsignedChar();
        backdropImageData.SetNumberOfScalarComponents(4);
        backdropImageData.AllocateScalars();
        // backdropImageData.SetOrigin(origin[0] - backdropBorder, origin[1] - backdropBorder, 0);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int iy = h - 1 - y; // inverse of y coordinate -- to make image right side up
                // SetScalarComponentFromDouble causes no such method error
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 3, 127); // alpha
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 0, 255); // red
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 1, 255); // green
                backdropImageData.SetScalarComponentFromDouble(x, iy, 0, 2, 255); // blue
            }
        }
        vtkImageMapper imageMapper = new vtkImageMapper();
        imageMapper.SetInput(backdropImageData);        
        imageMapper.SetColorWindow(255.0);
        imageMapper.SetColorLevel(127.5);
        imageMapper.SetRenderToRectangle(1);
        // vtkActor2D backdropActor = new vtkActor2D();
        backdropActor.SetMapper(imageMapper);
        
        backdropActor.SetPosition(5,5);
        backdropActor.SetPosition2(0.01, 0.01);
        
        return backdropActor;
    }
    
    public Set<vtkActor2D> getVtkActors() {
        return actors;
    }
    
    // Implement ComponentListener to know when the structure canvas has
    // changed size
    public void componentShown(ComponentEvent event) {}
    public void componentHidden(ComponentEvent event) {}
    public void componentMoved(ComponentEvent event) {}
    public void componentResized(ComponentEvent event) {
        // System.out.println("Window resized");
        // windowHeight = event.getComponent().getHeight();
        updateScaleBar(camera);
    }
    
    public void updateScaleBar(vtkCamera camera) {
        if (window == null) return;
        if (camera == null) return;
        
        this.camera = camera;
        
        // windowHeight = window.getHeight();
        double distance = camera.GetDistance();
        double angle = camera.GetViewAngle();
        double pixelsPerNanometer =
            window.getHeight() / 
            ( 2.0 * 0.10 * distance * Math.tan(angle * 0.5 * Math.PI / 180.0) )
            ;
        
        // System.out.println("pixelsPerAngstrom = "+pixelsPerAngstrom);
        
        double scale = 1.0;     
        double maxLength = 0.3 * window.getWidth();
        
        while (scale * pixelsPerNanometer > maxLength) {
            scale = scale * 0.1;
        }
        
        double minLength = 0.1 * maxLength;
        while (scale * pixelsPerNanometer < minLength) {
            scale = scale * 10.0;
        }
        
        double[] mantissas = {5,2,1};
        for (double mantissa : mantissas) {
            if ( (mantissa * scale * pixelsPerNanometer) < maxLength) {
                scale = scale * mantissa;
                break;
            }
        }

        double barWidth = scale * pixelsPerNanometer;
        
        points.SetPoint(points.GetNumberOfPoints() - 1, 
                origin[0] + barWidth,
                origin[1],
                origin[2]);
        points.Modified();
        
        // Choose units
        String units = " nm";
        double unitFactor = 1.0;
        
        if (scale <= 1.0) {
            unitFactor = 10.0;
            units = " A"; // Angstroms
        }
        else if (scale >= 100) {
            unitFactor = 0.001;
            units = " um"; // Angstroms
        }
        
        textMapper.SetInput(""+numberFormat.format(scale * unitFactor)+units);
        // textActor.SetPosition(0.5 * (barWidth - textActor.GetWidth()), origin[1] + 3);
        textActor.SetPosition(origin[0], origin[1] + 4);
        
        double textHeight = textMapper.GetHeight(window.GetRenderer());
        
        backdropActor.SetPosition(origin[0] - backdropBorder, origin[1] - backdropBorder);
        backdropActor.SetPosition2(
                (barWidth + 2 * backdropBorder) / window.getWidth(),
                (textHeight + 2 * backdropBorder) / window.getHeight() );
    }
}
