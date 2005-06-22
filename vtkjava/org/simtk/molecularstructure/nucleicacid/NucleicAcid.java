/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.*;
import org.simtk.molecularstructure.*;
import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule of DNA or RNA
 */
public class NucleicAcid extends Biopolymer {    
    public NucleicAcid() {} // Empty molecule
    public NucleicAcid(PDBAtomSet atomSet) {super(atomSet);}
    
    protected void addGenericResidueBonds() {
        super.addGenericResidueBonds();
        addGenericResidueBond(" O3*", " P  ");
    }

    //    The purpose of this routine is to aid identification of structural 
    //      hairpins, not necessarily the identification of particular
    //      hydrogen bonding patterns.
    //    The following calculations apply only to the atoms of the base part
    //      of the nucleotides, not to the ribose or phosphate.
    //    1) Close in space:  Identify pairs of nitrogenouse base functional 
    //      groups whose centroids are within 8 Angstroms of
    //      one another.  Ignore self hits.
    //    2) Parallel planes:  Discard those whose plane normals differ by 
    //       more that 30 degrees.  Normal differences between 150
    //       and 180 degrees are OK too.
    //    3) In the same plane:  Discard those pairs for which the centroid 
    //      of one base is not within 1.5 Angstroms of the infinite
    //      plane of the other.
    //    4) Touching: Some pair of atoms between the two bases must be within 
    //      3.5 Angstroms of one another.
    public Vector<BasePair> identifyBasePairs() {
        // Adjustable parameters
        int minSequenceDistance = 3;
        double centroidDistanceCutoff = 8.70;
        double interplaneAngleCutoff = 46.0;
        double planeHeightCutoff = 3.20;
        double atomicDistance = 3.20;
        
        Vector<BasePair> basePairs = new Vector<BasePair>();
        Hashtable<Residue, Vector3D> residueCentroids = new Hashtable<Residue, Vector3D>();
        Hashtable<Residue, Plane3D> residuePlanes = new Hashtable<Residue, Plane3D>();

        // for debugging only  cmb
        int closePairCount = 0;
        int parallelCount = 0;
        int samePlaneCount = 0;

        // Close in space
        Hash3D<Residue> centroidHash = new Hash3D<Residue>(4.0);
        for (Residue residue : this.residues()) {
            if (residue instanceof Nucleotide) {
                Molecule base = residue.get(Nucleotide.baseGroup);
                Vector3D centroid = base.getCenterOfMass();
                
                residueCentroids.put(residue, centroid);
                residuePlanes.put(residue, base.bestPlane3D());
                
                centroidHash.put(centroid, residue);
            }
        }
        HashSet<Residue> residuesAlreadyTested = new HashSet<Residue>(); // only check each residue once
        for (BaseVector3D centroid : centroidHash.keySet()) {
            Residue residue = centroidHash.get(centroid);
            residuesAlreadyTested.add(residue);
            for (Residue otherResidue : centroidHash.neighborValues(centroid, centroidDistanceCutoff)) {
                if (residue == otherResidue) continue; // no self hits
                if (residuesAlreadyTested.contains(otherResidue)) continue;
                // If we got here there are two residues within 8 Angstroms of one another
                closePairCount ++;
                
                int number1 = residue.getResidueNumber();
                int number2 = otherResidue.getResidueNumber();
                int deltaNumber = Math.abs(number1 - number2);
                if (deltaNumber < minSequenceDistance) continue; // too close in sequence
                
                // Are the two planes within 20 degrees of parallel?
                double planeAngle = residuePlanes.get(residue).getNormal().dot(residuePlanes.get(otherResidue).getNormal());
                if (planeAngle < 0) planeAngle = -planeAngle;
                if ( planeAngle < Math.cos(interplaneAngleCutoff * Math.PI/180) ) continue; // angle is too large
                parallelCount ++;
                
                // Are the two bases in the same plane?
                if ( residuePlanes.get(residue).distance(residueCentroids.get(otherResidue)) > planeHeightCutoff)
                    continue; // out of plane
                if ( residuePlanes.get(otherResidue).distance(residueCentroids.get(residue)) > planeHeightCutoff)
                    continue; // out of plane
                samePlaneCount ++;
                
                // Atomic touching criterion - polar atoms
                double minDistance = 1000;
                for (Atom atom1 : residue.getAtoms()) {
                    if (! ((atom1 instanceof PDBOxygen) || (atom1 instanceof PDBNitrogen))) continue;
                    for (Atom atom2 : otherResidue.getAtoms()) {
                        if (! ((atom2 instanceof PDBOxygen) || (atom2 instanceof PDBNitrogen))) continue;
                        double testDistance = atom1.distance(atom2);
                        if (testDistance < minDistance) minDistance = testDistance;
                    }
                }
                if (minDistance > atomicDistance) continue; // No close atoms
                
                basePairs.add(new BasePair((Nucleotide) residue, (Nucleotide) otherResidue));
            }
        }
        // System.out.println("" + closePairCount + " close base pairs found");
        // System.out.println("" + parallelCount + " close and parallel base pairs found");
        // System.out.println("" + samePlaneCount + " close and parallel and coplanar base pairs found");
        
        return basePairs;
    }
    
    /**
     * @return
     */
    public Vector<Hairpin> identifyHairpins() {
        //        Identify base pairs using method outlined in another feature request.
        //        Cluster the base pairs into possible hairpins.  Two base pairs are clusterable if all of the following criteria are
        //        met:
        //         * Antiparallel in sequence: residues i1,j1 and i2,j2 in base pairs 1 and 2 have relationship i2 < i1 <= i2+3(?)
        //        and j2 > j1 >= j2 -3(?) for some ordering of base pairs and residues within the base pairs.
        //         * normal vectors to base pair planes differ by less than 20(?) degrees.
        //
        //        Cluster the base pairs using single linkage clustering.  
        //        Discard clusters with less than 3 base pairs.
        //        TODO Split clusters, if necessary, by using some yet to be identified criteria.
        
        // Adjustable parameters
        int sequenceDistanceCutoff = 4;
        double interplaneAngleCutoff = 20.0;
        int minBasePairCount = 3;
                
        Vector<BasePair> basePairs = identifyBasePairs();
        
        // First index the base pairs
        Hashtable<Integer, Vector<BasePair> > residueNumberPairs = new Hashtable<Integer, Vector<BasePair> >();
        Hashtable<Integer, Vector<BasePair> > pairSumPairs = new Hashtable<Integer, Vector<BasePair> >();        
        Hashtable<BasePair, Plane3D> pairPlanes = new Hashtable<BasePair, Plane3D>();
        for (BasePair pair : basePairs) {
            int number1 = pair.getResidue1().getResidueNumber();
            int number2 = pair.getResidue2().getResidueNumber();
            if (!residueNumberPairs.containsKey(number1)) residueNumberPairs.put(number1, new Vector<BasePair>());
            if (!residueNumberPairs.containsKey(number2)) residueNumberPairs.put(number2, new Vector<BasePair>());
            residueNumberPairs.get(number1).add(pair);
            residueNumberPairs.get(number2).add(pair);

            int hairpinPhase = number1 + number2;
            if (!pairSumPairs.containsKey(hairpinPhase)) pairSumPairs.put(hairpinPhase, new Vector<BasePair>());
            pairSumPairs.get(number1+number2).add(pair);

            pairPlanes.put(pair, pair.getBasePlane());
        }
        
        // Identify pairs of BasePairs that can be in the same hairpin
        HashSet<BasePair> testedPairs = new HashSet<BasePair>(); // examine each base pair only once
        Hashtable<BasePair, Vector<BasePair> > pairPairs = new Hashtable<BasePair, Vector<BasePair> >();
        for (BasePair pair : basePairs) {
            testedPairs.add(pair);
            // for antiparallel helices, sum the residue numbers to get the phase of the hairpin (subtract for parallel)
            // We are only looking for antiparallel double helices.
            int number1 = pair.getResidue1().getResidueNumber();
            int number2 = pair.getResidue2().getResidueNumber();
            int hairpinPhase = number1 + number2;
            // Only examine other BasePairs that are nearby in hairpin phase space
            for (int phase = hairpinPhase - sequenceDistanceCutoff;
                 phase <= hairpinPhase + sequenceDistanceCutoff;
                 phase ++) {
                if (!pairSumPairs.containsKey(phase)) continue;
                for (BasePair otherPair : pairSumPairs.get(phase)) {
                    // Make sure that we have not made this comparison before
                    if (otherPair == pair) continue;
                    if (testedPairs.contains(otherPair)) continue;

                    // Make sure that the two BasePairs are close in sequence
                    int otherNumber1 = otherPair.getResidue1().getResidueNumber();
                    int otherNumber2 = otherPair.getResidue2().getResidueNumber();
                    int diff1 = Math.abs(number1 - otherNumber1);
                    int diff2 = Math.abs(number2 - otherNumber2);
                    if (diff1 > sequenceDistanceCutoff) continue;
                    if (diff2 > sequenceDistanceCutoff) continue;
                    
                    // Make sure the two BasePair planes are parallel
                    double planeAngle = Math.abs(pairPlanes.get(pair).getNormal().dot(pairPlanes.get(otherPair).getNormal()));
                    if (planeAngle < Math.cos(interplaneAngleCutoff * Math.PI/180.0)) continue;
                    
                    // Passed! these two base pairs can be in the same hairpin
                    // Add relationship in both directions, since we will not be coming back this way
                    if (!pairPairs.containsKey(pair)) pairPairs.put(pair, new Vector<BasePair>());
                    if (!pairPairs.containsKey(otherPair)) pairPairs.put(otherPair, new Vector<BasePair>());
                    pairPairs.get(pair).add(otherPair);
                    pairPairs.get(otherPair).add(pair);                    
                }
            }
        }
        
        // Now use single linkage clustering to make hairpins
        Vector<Hairpin> hairpins = new Vector<Hairpin>();
        HashSet<BasePair> unassignedPairs = new HashSet<BasePair>();
        HashSet<BasePair> assignedPairs = new HashSet<BasePair>();
        for (BasePair pair : pairPairs.keySet())
            unassignedPairs.add(pair);
        while (! unassignedPairs.isEmpty()) {
            Hairpin hairpin = new Hairpin();
            BasePair startPair = unassignedPairs.iterator().next();

            // Pairs whose nieghbor list has not been examined
            HashSet<BasePair> freshPairs = new HashSet<BasePair>();
            HashSet<BasePair> stalePairs = new HashSet<BasePair>();

            freshPairs.add(startPair);
            while (!freshPairs.isEmpty()) {
                BasePair freshPair = freshPairs.iterator().next();

                hairpin.add(freshPair);
                unassignedPairs.remove(freshPair);
                assignedPairs.add(freshPair);

                // Once fresh, but now used
                stalePairs.add(freshPair);
                freshPairs.remove(freshPair);

                // System.out.println("" + freshPair);
                
                // Examine neighbors
                for (BasePair testPair : pairPairs.get(freshPair)) {
                    if (stalePairs.contains(testPair)) continue;
                    else freshPairs.add(testPair);
                }                
            }
            if (hairpin.size() >= minBasePairCount) hairpins.add(hairpin);
        }
        return hairpins;
    }
}
