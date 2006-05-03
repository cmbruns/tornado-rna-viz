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
 * Created on May 3, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.pdb;

import junit.framework.TestCase;
import java.text.ParseException;
import org.simtk.chem.*;

public class TestBasePdbAtom extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestBasePdbAtom.class);
    }

    public void testSpaceFillingAtom() {
        String pdbLine = "ATOM     32  C1*   A B  97       9.995 -45.008 -47.871  1.00 26.64           C  ";
        PdbAtom atom = null;
        try {atom = BasePdbAtom.createAtom(pdbLine);}
        catch(ParseException exc) {assert(false);}

        assertEquals(9.995, atom.getX());
        assertEquals(-45.008, atom.getY());
        assertEquals(-47.871, atom.getZ());
        assertEquals(1.00, atom.getOccupancy());
        assertEquals(26.64, atom.getTemperatureFactor());
        assertEquals("C", atom.getElementSymbol());
        assertEquals(' ', atom.getAlternateLocationIndicator());
        assertEquals('B', atom.getChainIdentifier());
        assertEquals("ATOM  ", atom.getPdbRecordName());
    }

}
