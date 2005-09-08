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

import java.util.Iterator;

import org.simtk.molecularstructure.Molecule;
import org.simtk.molecularstructure.MoleculeCollection;
import org.simtk.util.SelectionListener;

import vtk.vtkAssembly;

abstract public class MolecularCartoon implements SelectionListener {
    // replaced Java 1.5 enum with Java 1.4 compliant
    public static class CartoonType {
        private Class cartoonClass;
        private CartoonType(Class c) {cartoonClass = c;}
        
        static public CartoonType SPACE_FILLING = new CartoonType(AtomSphereCartoon.class);
        static public CartoonType BALL_AND_STICK = new CartoonType(BallAndStickCartoon.class);

        static public CartoonType ROPE_AND_CYLINDER2 = new CartoonType(RopeAndCylinder.class);
        static public CartoonType DUPLEX_CYLINDER = new CartoonType(DuplexCylinderCartoon.class);
        static public CartoonType NUCLEOTIDE_WEDGE = new CartoonType(DuplexResidueWedge.class);

        static public CartoonType RESIDUE_SPHERE = new CartoonType(ResidueSphereCartoon.class);
        static public CartoonType BOND_STICK = new CartoonType(BondStickCartoon.class);
        static public CartoonType BACKBONE_TRACE = new CartoonType(BackboneCurveCartoon.class);
        static public CartoonType BACKBONE_STICK = new CartoonType(BackboneStick.class);
        static public CartoonType TUBE_AND_STICK = new CartoonType(TubeAndStickTrace.class);
        static public CartoonType NUCLEOTIDE_STICK = new CartoonType(NucleotideStickCartoon.class);
        static public CartoonType WIRE_FRAME = new CartoonType(WireFrameCartoon.class);

        public MolecularCartoon newInstance() {
            try {return (MolecularCartoon) cartoonClass.newInstance();}
            catch (Exception e) {} // TODO
            return null;
        }
    };
    /**
     * Show a molecule in this representation.
     * Create the graphics primitives if they do not already exist.
     * @param molecule
     */
    abstract public void show(Molecule molecule);
    public void show(MoleculeCollection moleculeCollection) {
        for (Iterator iterMolecule = moleculeCollection.molecules().iterator(); iterMolecule.hasNext(); ) {
            show((Molecule) iterMolecule.next());
        }
    }

    /**
     * Make the specified structure invisible
     * @param molecule
     */
    abstract public void hide(Molecule molecule);

    /**
     * Remove all graphics primitives and data structures.
     *
     */
    abstract public void clear();

    /**
     * Return a vtk object containing all of this cartoon style.
     * @return
     */
    abstract public vtkAssembly getAssembly();

    // TODO - put this in later, when testing interactive modelling
    // public abstract void updateCoordinates(Molecule molecule);

    /**
     * Note: only put highlights on things that are already visible.
     * @param molecule
     */
    abstract public void highlight(Molecule molecule);
}
