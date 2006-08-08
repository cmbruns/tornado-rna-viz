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
 * Created on Aug 7, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.toon.secstruct;

import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.*;
import java.util.*;

public class SecondaryStructureDiagramModel {
    public void createDiagram(NucleicAcid rna) {
        // Extract base pairs
        Set<BasePair> basePairs = new HashSet<BasePair>();
        for(SecondaryStructure structure : rna.secondaryStructures()) {
            if (! (structure instanceof BasePair)) continue;
            BasePair basePair = (BasePair) structure;
            basePairs.add(basePair);
        }
        
        // Identify pseudoknots
        int pseudoknotCount = 0;
        int nonKnotCount = 0;
        for (BasePair bp1 : basePairs) {
            int i1 = bp1.getResidue1().getResidueNumber();
            int j1 = bp1.getResidue2().getResidueNumber();
            if (i1 > j1) {int swap = i1; i1 = j1; j1 = swap;}
            for (BasePair bp2 : basePairs) {
                int i2 = bp2.getResidue1().getResidueNumber();
                int j2 = bp2.getResidue2().getResidueNumber();
                if (i2 > j2) {int swap = i2; i2 = j2; j2 = swap;}
                
                if ((i1 > i2) && (j1 > j2)) pseudoknotCount ++;
                else if ((i1 < i2) && (j1 < j2)) pseudoknotCount ++;
                else nonKnotCount ++;
            }
        }
        System.out.println(""+pseudoknotCount+" pseudoknots found.");
        System.out.println(""+nonKnotCount+" non knots found.");
    }
}
