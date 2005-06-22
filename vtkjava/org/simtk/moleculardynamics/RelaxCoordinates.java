/*
 * Created on Jun 17, 2005
 *
 */
package org.simtk.moleculardynamics;

import java.net.*;
import java.io.*;
import java.nio.ByteOrder;
import org.simtk.molecularstructure.*;
import org.simtk.atomicstructure.*;

public class RelaxCoordinates {

    static {
        System.loadLibrary("relaxCoordinates");
    }
    
    static native int relaxCoordinates1(
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

        Molecule molecule = null;
        try { molecule = Molecule.createFactoryPDBMolecule(pdbFileURL); }
        catch (IOException exc) {System.out.println(""+exc);System.exit(0);}

        if (molecule != null) {
            int atomCount = molecule.getAtomCount();
            int duplexCount = 0;
            float referenceCoordinates[] = new float[atomCount * 3];
            int duplexRanges[] = new int[duplexCount * 4];
            boolean swapBytes = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
            
            int coordinateIndex = 0;
            for (Atom atom : molecule.getAtoms()) {
                for (Double coordinate : atom.getCoordinates()) {
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
