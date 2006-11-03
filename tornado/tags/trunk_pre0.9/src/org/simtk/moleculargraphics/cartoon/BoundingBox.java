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
 * Created on Jun 28, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.geometry3d.*;

public class BoundingBox {
    public double xMin, xMax, yMin, yMax, zMin, zMax;
    
    public BoundingBox(double[] bounds) {
        this.xMin = bounds[0];
        this.xMax = bounds[1];
        this.yMin = bounds[2];
        this.yMax = bounds[3];
        this.zMin = bounds[4];
        this.zMax = bounds[5];
    }
    
    public void add(BoundingBox otherBox) {
        this.xMin = Math.min(this.xMin, otherBox.xMin);
        this.yMin = Math.min(this.yMin, otherBox.yMin);
        this.zMin = Math.min(this.zMin, otherBox.zMin);
        
        this.xMax = Math.max(this.xMax, otherBox.xMax);
        this.yMax = Math.max(this.yMax, otherBox.yMax);
        this.zMax = Math.max(this.zMax, otherBox.zMax);
    }
    
    public Vector3D center() {
        return new Vector3DClass(
                0.5 * (xMin + xMax),
                0.5 * (yMin + yMax),
                0.5 * (zMin + zMax));
    }
}
