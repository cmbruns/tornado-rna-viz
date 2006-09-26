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
 * Created on Dec 1, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure;

import java.util.*;
import org.simtk.geometry3d.*;

/**
 *  
  * @author Christopher Bruns
  * 
  * A set of "equivalent" residues in common between two proteins or 
  * nucleic acids.
  * The order of the pairs should match the order of the residues in the sequence.
  * Not every residue need be aligned.
 */
public class SequenceAlignment {
    
    private Biopolymer molecule1 = null;
    private Biopolymer molecule2 = null;
    private Vector alignedResidues = new Vector();    

    public SequenceAlignment(Biopolymer m1, Biopolymer m2) {
        this.molecule1 = m1;
        this.molecule2 = m2;
    }
    
    // TODO align sequences using dynamic programming

    /**
     * 
     * Creates an alignment where the first residue of the first molecule
     * is aligned with the first residue of the second molecule, the second
     * residue is aligned with the second residue, and so on.
     * 
     * This method is only appropriate when the two molecular sequences are
     * identical, or in other cases where there is a complete ordered one-to-one
     * mapping of the residues between the two molecules.
     */
    public void alignNaively() {
        Iterator i1 = molecule1.residues().iterator();
        Iterator i2 = molecule2.residues().iterator();

        while (i1.hasNext() && i2.hasNext())
            alignedResidues.add(new ResiduePair((Residue) i1.next(), (Residue) i2.next()));
    }
    
    public Iterator iterator() {
        return alignedResidues.iterator();
    }
    
    /**
     * Generates rigid-body transformation that when applied to the first
     * molecule's residues, minimizes the least squared deviation from the
     * second molecule's residues.
     * 
     * Only works for residues that have atomic structures.
     */
    public HomogeneousTransform getSuperposition() {
        Vector v1 = new Vector();
        Vector v2 = new Vector();
        
        // Extract backbone positions of structural residues from the alignment
        for (Iterator i = this.iterator(); i.hasNext();) {
            ResiduePair pair = (ResiduePair) i.next();
            Residue r1 = pair.getResidue1();
            Residue r2 = pair.getResidue2();
            if ( (r1 instanceof Residue) && (r2 instanceof Residue) ) {
                try {
                    Vector3D vec1 = ((Residue)r1).getBackbonePosition();
                    Vector3D vec2 = ((Residue)r2).getBackbonePosition(); 
                    if ( (vec1 != null) && (vec2 != null) ) {
                        v1.add(vec1);
                        v2.add(vec2);
                    }
                }
                catch (InsufficientAtomsException exc) {} // no backbone position found
            }
        }

        // Convert to arrays of Vector3D
        Vector3D[] va1 = new Vector3D[v1.size()];
        Vector3D[] va2 = new Vector3D[v2.size()];
        for (int i = 0; i < v1.size(); i++) {
            va1[i] = (Vector3D) v1.get(i);
            va2[i] = (Vector3D) v2.get(i);
        }

        return Superposition.kabsch78(va1, va2, null);
    }
}

class ResiduePair {
    private Residue residue1;
    private Residue residue2;
    static final AlignmentMethod MANUAL_METHOD = AlignmentMethod.MANUAL;
    static final AlignmentMethod AUTOMATIC_METHOD = AlignmentMethod.MANUAL;
    
    ResiduePair(Residue r1, Residue r2) {
        this.residue1 = r1;
        this.residue2 = r2;
    }    
    
    public Residue getResidue1() {return residue1;}
    public Residue getResidue2() {return residue2;}
}

class AlignmentMethod {
    static final AlignmentMethod MANUAL = new AlignmentMethod();
    static final AlignmentMethod AUTOMATIC = new AlignmentMethod();
    private AlignmentMethod(){}
}
