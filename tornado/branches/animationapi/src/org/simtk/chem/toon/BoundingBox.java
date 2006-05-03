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
 * Created on May 3, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.toon;

import org.simtk.geometry3d.*;

public class BoundingBox {
    private double minX, maxX, minY, maxY, minZ, maxZ;

    public static final BoundingBox NO_BOX = 
        new BoundingBox(
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
                );
    
    public BoundingBox(
            double minX, double maxX,
            double minY, double maxY,
            double minZ, double maxZ
            ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public Vector3D getCenter() {
        return new Vector3DClass(
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0,
                (minZ + maxZ) / 2.0
                );
    }
    
    public BoundingBox combine(BoundingBox otherBox) {
        double newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ;

        newMinX = Math.min(minX, otherBox.minX);
        newMinY = Math.min(minY, otherBox.minY);
        newMinZ = Math.min(minZ, otherBox.minZ);

        newMaxX = Math.max(maxX, otherBox.maxX);
        newMaxY = Math.max(maxY, otherBox.maxY);
        newMaxZ = Math.max(maxZ, otherBox.maxZ);
        
        return new BoundingBox(
                newMinX, newMaxX, 
                newMinY, newMaxY, 
                newMinZ, newMaxZ);
    }
}
