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
 * Created on Apr 6, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics;

import junit.framework.TestCase;

public class TestVtkCube extends TestCase {
    private VTKCube vtkCube;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestVtkCube.class);
    }

    public void testJUnit() {
        assertEquals(73, 73);
        assertFalse(72 == 73);
    }
    
    protected void setUp() {
        vtkCube = new VTKCube();
    }
    
    public void testVTKCubeNotNull() {
        assertNotNull(vtkCube);
        assertNotNull(vtkCube.frame);
        assertNotNull(vtkCube.vtkPanel);
    }
}
