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

import java.util.*;

import org.simtk.moleculargraphics.cartoon.BoundingBox;
import org.simtk.molecularstructure.nucleicacid.BasePair;

public class SecondaryStructureDiagramClass 
implements SecondaryStructureDiagram
{
    private double consecutiveBaseDistance = 1.0;
    private List<BasePosition> basePositions = new Vector<BasePosition>();
    private List<BasePairPosition> basePairPositions = new Vector<BasePairPosition>();
    private List<NumberTick> majorTicks = new Vector<NumberTick>();
    private List<NumberTick> minorTicks = new Vector<NumberTick>();
    
    public double getConsecutiveBaseDistance() {
        return consecutiveBaseDistance;
    }

    public List<BasePosition> basePositions() {return basePositions;}
    public List<BasePairPosition> basePairPositions() {return basePairPositions;}
    public List<NumberTick> majorTicks() {return majorTicks;}
    public List<NumberTick> minorTicks() {return minorTicks;}

    public BoundingBox getBoundingBox() {
        BoundingBox boundingBox = null;
        // Set transform
        for (BasePosition pos : basePositions()) {
            double x = pos.getX();
            double y = pos.getY();
            double[] bounds = {x-1,x+1,y-1,y+1,0,0};
            if (boundingBox == null) boundingBox = new BoundingBox(bounds);
            else boundingBox.add(new BoundingBox(bounds));
        }
        return boundingBox;
    }

    /**
     * Identify a set of base pairs, which, if removed, leaves a set that 
     * has no pseudoknots and no bases with multiple base pairs.
     * @param basePairs
     * @return a set of base pairs "most" responsible for pseudoknot conflicts
     */
    public static Set<BasePair> findWorstPseudoknotBasePairs(Set<BasePair> basePairs) {
        Set<BasePair> answer = new HashSet<BasePair>();
        
        Map<BasePair, Set<BasePair>> basePairConflicts = new HashMap<BasePair, Set<BasePair>>();

        // First compare every pair of base pairs to identify all pseudoknot pairs of base pairs
        for (BasePair bp1 : basePairs) {
            int i1 = bp1.getResidue1().getResidueNumber();
            int j1 = bp1.getResidue2().getResidueNumber();
            if (i1 > j1) {int swap = i1; i1 = j1; j1 = swap;}
            for (BasePair bp2 : basePairs) {
                int i2 = bp2.getResidue1().getResidueNumber();
                int j2 = bp2.getResidue2().getResidueNumber();
                if (i2 > j2) {int swap = i2; i2 = j2; j2 = swap;}

                boolean isPseudoknot = true;
                
                // Ignore self comparisons
                if ( (i1 == i2) && (j1 == j2) ) 
                    isPseudoknot = false;
                
                // Ignore non-overlapping ranges
                else if (j1 < i2) isPseudoknot = false;
                else if (j2 < i1) isPseudoknot = false;

                // Ignore total containment of one pair within another
                else if ( (i1 > i2) && (j1 < j2) ) isPseudoknot = false;
                else if ( (i1 < i2) && (j1 > j2) ) isPseudoknot = false;
                    
                // Verify that this is a pseudoknot (i.e. that above logic is correct)
                else {
                    if ( (i1 <= i2) && (i2 <= j1) && (j1 <= j2) ) {} // knot with bp1 on left
                    else if ( (i2 <= i1) && (i1 <= j2) && (j2 <= j1) ) {} // knot with bp1 on right
                    else {
                        // Something wrong with above logic
                        throw new RuntimeException("("+i1+","+j1+")("+i2+","+j2+") is not a pseudoknot!!!");
                    }
                
                    // Update disagreement counts
                    if (! basePairConflicts.containsKey(bp1))
                        basePairConflicts.put(bp1, new HashSet<BasePair>());
                    basePairConflicts.get(bp1).add(bp2);
    
                    if (! basePairConflicts.containsKey(bp2))
                        basePairConflicts.put(bp2, new HashSet<BasePair>());
                    basePairConflicts.get(bp2).add(bp1);
                }
            }
        }
        
        // If a base pair has more conflicts than does any of the pairs it conflicts with,
        // then mark it for removal
        int removedCount = 0;
        do {
            removedCount = 0;
            Set<BasePair> newlyUnconflictedPairs = new HashSet<BasePair>();
            for (BasePair bp1 : basePairConflicts.keySet()) {
                int count1 = basePairConflicts.get(bp1).size();
                if (count1 == 0) { // This pair no longer conflicts with anyone!
                    newlyUnconflictedPairs.add(bp1);
                    continue;
                }
                boolean isWorstConflict = true;
                for (BasePair bp2 : basePairConflicts.get(bp1)) {
                    int count2 = basePairConflicts.get(bp2).size();
                    if (count2 >= count1) {
                        isWorstConflict = false;
                        break;
                    }
                }
                if (isWorstConflict) {
                    answer.add(bp1);
                    removedCount ++;
                }
            }
            for (BasePair bp : newlyUnconflictedPairs) basePairConflicts.remove(bp);
            
            // Cull removed base pairs
            Set<BasePair> pairsToBeRemoved = new HashSet<BasePair>();
            for (BasePair bp1 : basePairConflicts.keySet()) {
                if (! answer.contains(bp1)) continue;
                for (BasePair bp2 : basePairConflicts.get(bp1))
                    basePairConflicts.get(bp2).remove(bp1);
                pairsToBeRemoved.add(bp1);
            }
            for (BasePair bp : pairsToBeRemoved) basePairConflicts.remove(bp);
        } while (removedCount > 0);
        
        // TODO - resolve remaining conflicts
        // 1) accumulate weighted duplex centers
        class DuplexWeights extends HashMap<Integer, Double> {
            public void addValue(int index, double deltaValue) {
                double previousValue = 0;
                if (containsKey(index)) previousValue = get(index);
                put(index, previousValue + deltaValue);
            }
            public Double get(Object index) {
                if (containsKey(index)) return super.get(index);
                else return 0.0;
            }
        }
        DuplexWeights centerWeights = new DuplexWeights();
        for (BasePair bp : basePairs) {
            int center = bp.getResidue1().getResidueNumber() + bp.getResidue2().getResidueNumber();

            centerWeights.addValue(center, 1.0);
            
            // And add a bit to the neighborhood, so "near perfect" duplexes get modded up
            centerWeights.addValue(center + 1, 0.25);
            centerWeights.addValue(center - 1, 0.25);
            centerWeights.addValue(center + 2, 0.06);
            centerWeights.addValue(center - 2, 0.06);            
        }
        
        // 2) Reconcile conflicts by duplex center weight
        Set<BasePair> pairsToBeRemoved = new HashSet<BasePair>();
        for (BasePair bp1 : basePairConflicts.keySet()) {
            int center1 = bp1.getResidue1().getResidueNumber() + bp1.getResidue2().getResidueNumber();
            double centerWeight1 = centerWeights.get(center1);

            for (BasePair bp2 : basePairConflicts.get(bp1)) {
                int center2 = bp2.getResidue1().getResidueNumber() + bp2.getResidue2().getResidueNumber();
                double centerWeight2 = centerWeights.get(center2);
                
                if (centerWeight2 < centerWeight1) {
                    pairsToBeRemoved.add(bp1);
                }
                else if (centerWeight2 > centerWeight1) {
                    pairsToBeRemoved.add(bp2);
                }
            }
        }
        for (BasePair bp : pairsToBeRemoved) basePairConflicts.remove(bp);
        
        // Include remaining conflicts -- TODO resolve them
        for (BasePair bp : basePairConflicts.keySet()) answer.add(bp);

        return answer;
    }
}
