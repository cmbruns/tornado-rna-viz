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
 * Created on Jun 17, 2005
 *
 */
package org.simtk.moleculardynamics;

import java.net.*;
import java.io.*;
import java.nio.ByteOrder;
import java.util.*;
import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.atom.*;

public class RelaxCoordinates {

    static {
        System.loadLibrary("relaxCoordinates");
    }
    
    public static native int relaxCoordinates1(
            int nAtoms,
            float[] coords,
            float[] coordsRef,
            float resourceLimit,
            int nRigidBodies,
            int[] rigidBodies,
            float kChain,
            float kRigid,
            int verbose);

    /**
     * @param args
     */
    public static void main(String[] args) {
        RelaxCoordinates thisApp = new RelaxCoordinates();
        URL pdbFileURL = thisApp.getClass().getClassLoader().getResource("resources/structures/OneRNAHairpin.pdb");
        System.out.println(""+pdbFileURL);

        MoleculeClass molecule = null;
        try { molecule = MoleculeClass.createFactoryPDBMolecule(pdbFileURL); }
        catch (IOException exc) {System.out.println(""+exc);System.exit(0);}

        if (molecule != null) {
            int atomCount = molecule.getAtomCount();
            int duplexCount = 0;
            float referenceCoordinates[] = new float[atomCount * 3];
            int duplexRanges[] = new int[duplexCount * 4];
            boolean swapBytes = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
            
            int coordinateIndex = 0;
            // for (Atom atom : molecule.getAtoms()) {
            for (Iterator i = molecule.getAtomIterator(); i.hasNext(); ) {
                LocatedAtom atom = (LocatedAtom) i.next();
                // for (Double coordinate : atom.getCoordinates()) {
                for (Iterator i2 = atom.getCoordinates().iterator(); i2.hasNext(); ) {
                    Double coordinate = (Double) i.next();
                    // Set reference coordinates once
                    referenceCoordinates[coordinateIndex] = coordinate.floatValue();
                    coordinateIndex ++;
                }
            }

            // Keep official coordinates in a float array
            float[] actualCoordinateArray = molecule.packCoordinatesIntoFloatArray();
            
            relaxCoordinates1(
                    atomCount,
                    actualCoordinateArray,
                    referenceCoordinates,
                    5.0f,
                    duplexCount,
                    duplexRanges,
                    1.0f,
                    1.0f,
                    1);
        }
    }
}
