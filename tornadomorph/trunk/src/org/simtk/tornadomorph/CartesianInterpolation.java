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
 * Created on Dec 6, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.tornadomorph;

import java.util.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.geometry3d.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * Interpolate between two structures by moving each atom in a straight line between the start and end.
  * 
  * If alignment is null, the two structures are aligned atom by atom, and must each contain the same
  * number of atoms.
 */
public class CartesianInterpolation implements MoleculeInterpolator {

    public MoleculeTrajectory interpolate(LocatedMolecule origin,
            LocatedMolecule goal, SequenceAlignment alignment, int numberOfSteps) {
        
        MutableMoleculeTrajectory answer = new MoleculeTrajectoryClass(origin, numberOfSteps);
        
        // If there is no alignment, the total number of atoms must be the same
        if (alignment == null) {
            // No alignment? Align atom by atom
            
            if (origin.getAtomCount() != goal.getAtomCount())
                throw new RuntimeException("Unequal number of atoms in interpolation without alignment.");

            for (int step = 0; step < numberOfSteps; step ++) {
                double progress = (double)step / (double)(numberOfSteps - 1);

                Vector3D[] interpolatedStructure = new Vector3D[origin.getAtomCount()];
                
                // Move each atom
                Iterator originIterator = origin.getAtomIterator();
                Iterator goalIterator = goal.getAtomIterator();
                int atomCount = 0;
                while (originIterator.hasNext()) {
                    LocatedAtom originAtom = (LocatedAtom) originIterator.next();
                    LocatedAtom goalAtom = (LocatedAtom) goalIterator.next();
                    Vector3D originCoordinates = originAtom.getCoordinates();
                    Vector3D goalCoordinates = goalAtom.getCoordinates();

                    MutableVector3D interpolatedCoordinates = new Vector3DClass(originCoordinates);
                    interpolatedCoordinates.plusEquals(goalCoordinates.minus(originCoordinates).times(progress));
                    interpolatedStructure[atomCount] = interpolatedCoordinates;
                    
                    atomCount ++;
                }
                
                answer.add(interpolatedStructure);
            }
        }
        
        else {
            // TODO handle alignment case
        }

        return answer;
    }

}
