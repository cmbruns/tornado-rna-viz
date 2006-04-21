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
 * Created on Apr 19, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.toon;

import junit.framework.TestCase;
import org.simtk.moleculargraphics.VTKLibraries;
import org.simtk.molecularstructure.atom.*;
import java.text.ParseException;

public class SpaceFillingAtomTest extends TestCase {
    static {VTKLibraries.load();}

    public static void main(String[] args) {
    }
    
    /*
     * Test method for 'org.simtk.mol.toon.SpaceFillingAtom.SpaceFillingAtom(LocatedAtom)'
     */
    public void testSpaceFillingAtom() {
        String pdbLine1 = "ATOM     32  C1*   A B  97       9.995 -45.008 -47.871  1.00 26.64           C  ";
        String pdbLine2 = "ATOM     33  C2*   A B  97      10.995 -45.008 -47.871  1.00 26.64           C  ";
        try {
            LocatedAtom atom1 = new PDBAtomClass(pdbLine1);
            SpaceFillingAtom toon1 = new SpaceFillingAtom(atom1);

            LocatedAtom atom2 = new PDBAtomClass(pdbLine2);
            SpaceFillingAtom toon2 = new SpaceFillingAtom(atom2);

        } catch (ParseException exc) { 
            assert(false);
        }        
    }
}
