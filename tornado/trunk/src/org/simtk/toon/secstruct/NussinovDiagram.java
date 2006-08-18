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
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.geometry3d.*;
import java.util.*;

public class NussinovDiagram 
extends SecondaryStructureDiagramClass
{
    private double openAngle = 0.1;  // radians not used for sequence
    private double startAngle = 0;
    private boolean proceedClockwise = false;
    
    public NussinovDiagram(Biopolymer molecule) {
        int numberOfResidues = molecule.residues().size();
        if (numberOfResidues < 2) throw new RuntimeException(); // TODO
        double residuesAngle = 2 * Math.PI - openAngle;
        double anglePerResidue = residuesAngle / (numberOfResidues - 1.0);
        double circumference = getConsecutiveBaseDistance() * numberOfResidues * Math.PI * 2.0 / residuesAngle;
        double radius = 0.5 * circumference / Math.PI;
        

        // 1) Store positions of residues
        double currentAngle = startAngle;
        Map<Residue, BasePosition> residuePositions = new HashMap<Residue, BasePosition>();
        for (Residue residue : molecule.residues()) {
            double x = radius * Math.cos(currentAngle);
            double y = radius * Math.sin(currentAngle);
            if (proceedClockwise) y *= -1.0;
            BasePosition position = new BasePosition(residue, new Vector2DClass(x,y));
            basePositions().add(position);
            residuePositions.put(residue, position);

            currentAngle += anglePerResidue;
        }
        
        // 2) Store base pair positions
        // int maxPairCount = 100;
        // int pairCount = 0;
        for (SecondaryStructure sstruct : molecule.secondaryStructures()) {
            if (!(sstruct instanceof BasePair)) continue;
            BasePair basePair = (BasePair) sstruct;
            Residue res1 = basePair.getResidue1();
            Residue res2 = basePair.getResidue2();
            if (res1 == null) continue;
            if (res2 == null) continue;
            BasePosition pos1 = residuePositions.get(res1);
            BasePosition pos2 = residuePositions.get(res2);
            if (pos1 == null) continue;
            if (pos2 == null) continue;
            
            // There are two circles containing the basePositions:
            //  1) the circle seen in the diagram
            //  2) the circle of which the base pair arc connecting the positions is a part
            // Angles on diagram circle
            double angle1 = Math.atan2(pos1.getY(), pos1.getX());
            double angle2 = Math.atan2(pos2.getY(), pos2.getX());
            double radius1 = Math.sqrt(pos1.getX() * pos1.getX() + pos1.getY() * pos1.getY());
            
            // alpha is half the angle separating the two positions
            double alpha = 0.5 * (angle2 - angle1);
            while (alpha > Math.PI) alpha -= Math.PI;
            while (alpha < 0) alpha += Math.PI;
            // Keep direction consistent
            // pos2 less than 180 degrees counterclockwise from pos1
            if (alpha > 0.5 * Math.PI) {
                alpha = Math.PI - alpha;
                BasePosition swapTemp = pos1;
                pos1 = pos2;
                pos2 = swapTemp;
            }
            
            // Check for straight line
            if (alpha > 0.99 * 0.50 * Math.PI) { // straight
                basePairPositions().add(new BasePairPosition(pos1, pos2));
            }
            else { // arc
                double radius2 = radius1 * Math.tan(alpha);
                Vector2D centerDirection = pos1.position.rotate(0.50 * Math.PI).unit();
                Vector2D center2 = pos1.position.plus(centerDirection.times(radius2));
                
                Vector2D posB1 = pos1.position.minus(center2);
                Vector2D posB2 = pos2.position.minus(center2);

                // Compute arc parameters, as used by Graphics.drawArc()
                double x = center2.x() - radius2;
                double y = center2.y() + radius2;
                double w = 2 * radius2;
                double h = 2 * radius2;
                double start = Math.atan2(posB1.y(), posB1.x()) * 180.0 / Math.PI;
                double end = Math.atan2(posB2.y(), posB2.x()) * 180.0 / Math.PI;
                double range = end - start;
                while (range > 360.0) range -= 360.0;
                while (range < -360.0) range += 360.0;
                
                // Always choose the shorter arc
                if (Math.abs(range) > 180.0) {
                    double sign = range < 0 ? 1.0 : -1.0; // change sign
                    range = sign * (360.0 - Math.abs(range));
                }
                    
                basePairPositions().add(
                        new BasePairPosition(
                                pos1, pos2,
                                x, y, w, h, start, range
                                ));
            }
            
            // pairCount ++;
            // if (pairCount >= maxPairCount) break;
        }
        
    }    
}
