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
 * Created on Jul 6, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.LocatedMolecule;

abstract public class MolecularCartoonClass implements MutableMolecularCartoon {
    protected MassBodyClass massBody = new MassBodyClass();
    
    public double getMass() {return massBody.getMass();}
    public Vector3D getCenterOfMass() {return massBody.getCenterOfMass();}

    // Derived classes should override this, so that something happens
    public void add(LocatedMolecule m) {
        massBody.add(m);
        // System.out.println("Center of mass = " + massBody.getCenterOfMass());
    }
    
    public void clear() {
        massBody.clear();
    }
    
    // replaced Java 1.5 enum with Java 1.4 compliant
    public static class CartoonType {
        private Class cartoonClass;
        private CartoonType(Class c) {cartoonClass = c;}
        
        static public CartoonType BOND_STICK = new CartoonType(BondStickCartoon.class);
        static public CartoonType BACKBONE_TRACE = new CartoonType(OldBackboneCurveCartoon.class);
        static public CartoonType BACKBONE_STICK = new CartoonType(BackboneStick.class);
        static public CartoonType BALL_AND_STICK = new CartoonType(BallAndStickCartoon.class);
        static public CartoonType BASE_PAIR_CYLINDERS = new CartoonType(BasePairRod.class);

        static public CartoonType DUPLEX_CYLINDER = new CartoonType(DuplexCylinderCartoon.class);
        static public CartoonType NUCLEOTIDE_WEDGE = new CartoonType(DuplexResidueWedge.class);
        static public CartoonType NUCLEOTIDE_STICK = new CartoonType(NucleotideStickCartoon.class);
        static public CartoonType PROTEIN_RIBBON = new CartoonType(ProteinRibbon.class);
        static public CartoonType PROTEIN_RIBBON_TEST = new CartoonType(ProteinRibbonSphereTest.class);
        static public CartoonType RESIDUE_SPHERE = new CartoonType(ResidueSphereCartoon.class);
        static public CartoonType ROPE_AND_CYLINDER2 = new CartoonType(RopeAndCylinder.class);
        static public CartoonType SPACE_FILLING = new CartoonType(AtomSphereCartoon.class);
        static public CartoonType TUBE_AND_STICK = new CartoonType(TubeAndStickTrace.class);
        static public CartoonType WIRE_FRAME = new CartoonType(WireFrameCartoon.class);

        public MolecularCartoonClass newInstance() {
            try {return (MolecularCartoonClass) cartoonClass.newInstance();}
            catch (Exception e) {} // TODO
            return null;
        }
    };
    
}
