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
 * Created on Aug 11, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.toon.secstruct;

import org.simtk.molecularstructure.*;
import java.util.*;

public class NussinovDiagram {
    private double consecutiveBaseDistance = 1.0;
    private double openAngle = 0.1;  // radians not used for sequence
    private double startAngle = 0;
    private boolean proceedClockwise = false;
    private List<BasePosition> basePositions = new Vector<BasePosition>();
    
    public NussinovDiagram(Biopolymer molecule) {
        int numberOfResidues = molecule.residues().size();
        if (numberOfResidues < 2) throw new RuntimeException(); // TODO
        double residuesAngle = 2 * Math.PI - openAngle;
        double anglePerResidue = residuesAngle / (numberOfResidues - 1.0);
        double circumference = consecutiveBaseDistance * numberOfResidues * Math.PI * 2.0 / residuesAngle;
        double radius = 0.5 * circumference / Math.PI;
        
        double currentAngle = startAngle;
        for (Residue residue : molecule.residues()) {
            double x = radius * Math.cos(currentAngle);
            double y = radius * Math.sin(currentAngle);
            if (proceedClockwise) y *= -1.0;
            basePositions.add(new BasePosition(residue, x, y));

            currentAngle += anglePerResidue;
        }
    }
}
