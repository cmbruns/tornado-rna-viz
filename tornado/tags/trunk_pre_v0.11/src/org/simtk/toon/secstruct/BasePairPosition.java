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
 * Created on Aug 17, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.toon.secstruct;

public class BasePairPosition {
    public BasePosition position1;
    public BasePosition position2;
    public boolean straightLine = true;

    public double arcX, arcY, arcWidth, arcHeight, arcStart, arcRange;
    
    BasePairPosition(BasePosition pos1, BasePosition pos2) {
        this.position1 = pos1;
        this.position2 = pos2;
        straightLine = true;
    }
    
    // TODO Store arc position as in Graphics.drawArc
    BasePairPosition(BasePosition pos1, BasePosition pos2, double x, double y, double width, double height, double startAngle, double angle) {
        this.position1 = pos1;
        this.position2 = pos2;

        this.arcX = x;
        this.arcY = y;
        this.arcWidth = width;
        this.arcHeight = height;
        this.arcStart = startAngle;
        this.arcRange = angle;
        
        straightLine = false;
    }
}
