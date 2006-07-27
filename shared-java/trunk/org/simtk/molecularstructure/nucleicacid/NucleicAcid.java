/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure.nucleicacid;

import java.util.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule of DNA or RNA
 */
public class NucleicAcid extends BiopolymerClass {    
    public NucleicAcid(char chainId) {super(chainId);} // Empty molecule
    
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
    public Collection<BasePair> identifyBasePairs() {
        // Adjustable parameters
        int minSequenceDistance = 3;
        double centroidDistanceCutoff = 8.70;
        double interplaneAngleCutoff = 30.0;
        double planeHeightCutoff = 2.00;
        double atomicDistance = 3.10;
        
        Collection<BasePair> basePairs = new Vector<BasePair>();
        Map<Residue, Vector3D> residueCentroids = new HashMap<Residue, Vector3D>();
        Map<Residue, Plane3D> residuePlanes = new HashMap<Residue, Plane3D>();

        // for debugging only  cmb
        int closePairCount = 0;
        int parallelCount = 0;
        int samePlaneCount = 0;

        // Close in space
        Hash3D<Residue> centroidHash = new Hash3D<Residue>(4.0);

        RESIDUE: for (Residue residue : this.residues()) {

            // Only use nucleotides
            if (! (residue.getResidueType() instanceof Nucleotide)) continue RESIDUE;

            // Only use residues with coordinates
            if (! (residue instanceof Residue)) continue RESIDUE;            
            Residue locatedResidue = (Residue) residue;

            // Only use residues with base coordinates
            Molecular base;
            try {base = locatedResidue.get(Nucleotide.baseGroup);}//see PBDResidueClass.java
            catch (InsufficientAtomsException exc) {continue RESIDUE;}
            
            // Only use residues with well defined base planes
            try {
                Plane3D plane = base.bestPlane3D();
                residuePlanes.put(residue, plane);
            }
            catch (InsufficientPointsException exc) {continue RESIDUE;}
            
            Vector3D centroid = null;
			centroid = base.getCenterOfMass();            
            residueCentroids.put(residue, centroid);

            centroidHash.put(centroid, residue);
        }
        
        Set<Residue> residuesAlreadyTested = new HashSet<Residue>(); // only check each residue once
        for (Iterator iterCentroid = centroidHash.keySet().iterator(); iterCentroid.hasNext(); ) {
            Vector3D centroid = (Vector3D) iterCentroid.next();
            Residue residue = centroidHash.get(centroid);
            residuesAlreadyTested.add(residue);
            for (Residue otherResidue : centroidHash.neighborValues(centroid, centroidDistanceCutoff)) {
            // for (Iterator iterOtherResidue = centroidHash.neighborValues(centroid, centroidDistanceCutoff).iterator(); iterOtherResidue.hasNext(); ) {
            //     Residue otherResidue = (Residue) iterOtherResidue.next();
                if (residue == otherResidue) continue; // no self hits
                if (residuesAlreadyTested.contains(otherResidue)) continue;
                // If we got here there are two residues within 8 Angstroms of one another
                closePairCount ++;
                
                int number1 = residue.getResidueNumber();
                int number2 = otherResidue.getResidueNumber();
                int deltaNumber = Math.abs(number1 - number2);
                if (deltaNumber < minSequenceDistance) continue; // too close in sequence
                
                // Are the two planes within 20 degrees of parallel?
                double planeAngle = ((Plane3D)residuePlanes.get(residue)).getNormal().dot(((Plane3D)residuePlanes.get(otherResidue)).getNormal());
                if (planeAngle < 0) planeAngle = -planeAngle;
                if ( planeAngle < Math.cos(interplaneAngleCutoff * Math.PI/180) ) continue; // angle is too large
                parallelCount ++;
                
                // Are the two bases in the same plane?
                if ( ((Plane3D)residuePlanes.get(residue)).distance(((Vector3D)residueCentroids.get(otherResidue))) > planeHeightCutoff)
                    continue; // out of plane
                if ( ((Plane3D)(residuePlanes.get(otherResidue))).distance((Vector3D)(residueCentroids.get(residue))) > planeHeightCutoff)
                    continue; // out of plane
                samePlaneCount ++;
                
                // Atomic touching criterion - polar atoms
                double minDistance = 1000;
                for (Iterator<Atom> iterAtom1 = residue.atoms().iterator(); iterAtom1.hasNext(); ) {
                    Atom atom1 =  iterAtom1.next();
                    if (! ((atom1.getElementName().equals("oxygen")) || (atom1.getElementName().equals("nitrogen")))) continue;
                    for (Iterator<Atom> iterAtom2 = otherResidue.atoms().iterator(); iterAtom2.hasNext(); ) {
                        Atom atom2 =  iterAtom2.next();
                        if (! ((atom2.getElementName().equals("oxygen")) || (atom2.getElementName().equals("nitrogen")))) continue;
                        double testDistance = atom1.distance(atom2);
                        if (testDistance < minDistance) minDistance = testDistance;
                    }
                }
                if (minDistance > atomicDistance) continue; // No close atoms
                
                basePairs.add(new BasePair(residue, otherResidue));
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
    public Collection<Duplex> identifyHairpins() {
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
                
        Collection<BasePair> basePairs = identifyBasePairs();
        
        // First index the base pairs

        // Residue number -> base pair mapping
        Map<Integer, Collection<BasePair> > residueNumberPairs = new HashMap<Integer, Collection<BasePair> >();

        // Sum of two residue numbers -> base pair mapping 
        // (sum is "phase" of duplex, and is constant throughout a single perfect duplex)
        Map<Integer, Collection<BasePair> > pairSumPairs = new HashMap<Integer, Collection<BasePair> >();

        // Basepair -> plane mapping
        Map<BasePair, Plane3D> pairPlanes = new HashMap<BasePair, Plane3D>();
        
        for (BasePair pair : basePairs) {
            Integer number1 = new Integer (pair.getResidue1().getResidueNumber());
            Integer number2 = new Integer (pair.getResidue2().getResidueNumber());
            if (!residueNumberPairs.containsKey(number1)) residueNumberPairs.put(number1, new HashSet<BasePair>());
            if (!residueNumberPairs.containsKey(number2)) residueNumberPairs.put(number2, new HashSet<BasePair>());
            residueNumberPairs.get(number1).add(pair);
            residueNumberPairs.get(number2).add(pair);

            Integer hairpinPhase = new Integer(number1.intValue() + number2.intValue());
            if (!pairSumPairs.containsKey(hairpinPhase)) pairSumPairs.put(hairpinPhase, new HashSet<BasePair>());
            pairSumPairs.get(hairpinPhase).add(pair);

            try {pairPlanes.put(pair, pair.getBasePlane());}
            catch (InsufficientAtomsException exc) {}; // Make sure to notice missing planes later in this method
        }
        
        // Identify pairs of BasePairs that can be in the same hairpin
        Set<BasePair> testedPairs = new HashSet<BasePair>(); // examine each base pair only once
        Map<BasePair, Collection<BasePair> > pairPairs = new HashMap<BasePair, Collection<BasePair> >();
        for (Iterator iterPair = basePairs.iterator(); iterPair.hasNext(); ) {
            BasePair pair = (BasePair) iterPair.next();
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
                
                BASEPAIR: for (BasePair otherPair : pairSumPairs.get(phase)) {
                    // Make sure that we have not made this comparison before
                    if (otherPair == pair) continue BASEPAIR;
                    if (testedPairs.contains(otherPair)) continue BASEPAIR;

                    // Make sure that the two BasePairs are close in sequence
                    int otherNumber1 = otherPair.getResidue1().getResidueNumber();
                    int otherNumber2 = otherPair.getResidue2().getResidueNumber();
                    int diff1 = Math.abs(number1 - otherNumber1);
                    int diff2 = Math.abs(number2 - otherNumber2);
                    if (diff1 > sequenceDistanceCutoff) continue BASEPAIR;
                    if (diff2 > sequenceDistanceCutoff) continue BASEPAIR;
                    
                    // Make sure the two BasePair planes are parallel
                    Plane3D plane1 = pairPlanes.get(pair);
                    Plane3D plane2 = pairPlanes.get(otherPair);
                    if (plane1 == null) continue BASEPAIR;
                    if (plane2 == null) continue BASEPAIR;
                    double planeAngle = Math.abs(plane1.getNormal().dot(plane2.getNormal()));
                    if (planeAngle < Math.cos(interplaneAngleCutoff * Math.PI/180.0)) continue BASEPAIR;
                    
                    // Passed! these two base pairs can be in the same hairpin
                    // Add relationship in both directions, since we will not be coming back this way
                    if (!pairPairs.containsKey(pair)) pairPairs.put(pair, new LinkedHashSet<BasePair>());
                    if (!pairPairs.containsKey(otherPair)) pairPairs.put(otherPair, new LinkedHashSet<BasePair>());
                    pairPairs.get(pair).add(otherPair);
                    pairPairs.get(otherPair).add(pair);                    
                }
            }
        }
        
        // Now use single linkage clustering to make hairpins
        Collection<Duplex> hairpins = new LinkedHashSet<Duplex>();
        Set<BasePair> unassignedPairs = new HashSet<BasePair>();
        Set<BasePair> assignedPairs = new HashSet<BasePair>();
        for (Iterator iterPair = pairPairs.keySet().iterator(); iterPair.hasNext(); ) {
            BasePair pair = (BasePair) iterPair.next();
            unassignedPairs.add(pair);
        }
        while (! unassignedPairs.isEmpty()) {
            Duplex hairpin = new Duplex();
            BasePair startPair = (BasePair) unassignedPairs.iterator().next();

            // Pairs whose nieghbor list has not been examined
            Set<BasePair> freshPairs = new HashSet<BasePair>();
            Set<BasePair> stalePairs = new HashSet<BasePair>();

            freshPairs.add(startPair);
            while (!freshPairs.isEmpty()) {
                BasePair freshPair = (BasePair) freshPairs.iterator().next();

                hairpin.addBasePair(freshPair);
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
            if (hairpin.basePairs().size() >= minBasePairCount) hairpins.add(hairpin);
        }
        return hairpins;
    }
    
    public Collection computeBaseHydrogenBonds() {
        double maxHydrogenBondDistance = 3.50;
        // Angle minHydrogenBondAngle = new Angle(270, Angle.Units.DEGREES);        
        Vector answer = new Vector();

        // Create a hash of hydrogen bond donor atoms
        Hash3D<Atom> donorAtoms = new Hash3D<Atom>(3.50);
        Map<Atom, Residue> donorNucleotides = new HashMap<Atom, Residue>();
        for (Residue nucleotide : residues()) {
            if (! (nucleotide.getResidueType() instanceof Nucleotide)) continue;
            for (Atom atom : nucleotide.getHydrogenBondDonors()) {
                donorAtoms.put(atom.getCoordinates(), atom);
                donorNucleotides.put(atom, nucleotide);
            }
        }
        
        // Loop over acceptor atoms, try to find donors
        for (Iterator iterResidue = residues().iterator(); iterResidue.hasNext(); ) {
            Residue residue = (Residue) iterResidue.next();
            if (! (residue.getResidueType() instanceof Nucleotide)) continue;
            Residue acceptorNucleotide = (Residue) residue;
            for (Iterator<Atom> iterAcceptorAtom = acceptorNucleotide.getHydrogenBondAcceptors().iterator(); iterAcceptorAtom.hasNext(); ) {
                Atom acceptorAtom =  iterAcceptorAtom.next();
                for (Iterator<Atom> iterDonorAtom = donorAtoms.neighborValues(acceptorAtom.getCoordinates(), maxHydrogenBondDistance).iterator(); iterDonorAtom.hasNext(); ) {
                    Atom donorAtom =  iterDonorAtom.next();
                    // This atom pair are now closer than the maximum distance cutoff of 3.5 Angstroms
                    
                    // Exclude atoms in the same residue
                    if ( donorNucleotides.get(donorAtom).equals(acceptorNucleotide) ) continue;
                    
                    // Exclude pairs closer than 2.0 Angstroms
                    if (donorAtom.distance(acceptorAtom) < 2.0) continue;
                    
                    // TODO - exclude pairs whose hydrogen bond angles differ by less than 270 degrees
                    // This is the hard part
                }
            }
        }
        
        return answer;
    }
}
