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
 * Created on Apr 20, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.toon;

import java.text.ParseException;

import javax.swing.*;

import org.simtk.chem.toon.SpaceFillingAtom;
import org.simtk.moleculargraphics.VTKLibraries;
import org.simtk.moleculargraphics.StructureCanvas;

import org.simtk.chem.LocatedAtom;
import org.simtk.chem.pdb.*;

// import org.simtk.molecularstructure.atom.*;

public class GUISpaceFillingAtomTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        VTKLibraries.load();
        
        // Create graphics window
        JFrame frame = new JFrame("Test space filling atoms");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StructureCanvas canvas = new StructureCanvas();
        frame.getContentPane().add(canvas);

        // Create atom graphics objects
        String pdbLine1 = "ATOM     32  C1*   A B  97       9.995 -45.008 -47.871  1.00 26.64           C  ";
        String pdbLine2 = "ATOM     33  C2*   A B  97      12.995 -48.008 -47.871  1.00 26.64           C  ";
        try {
            LocatedAtom atom1 = BasePdbAtom.createAtom(pdbLine1);
            SpaceFillingAtom toon1 = new SpaceFillingAtom(atom1);

            LocatedAtom atom2 = BasePdbAtom.createAtom(pdbLine2);
            SpaceFillingAtom toon2 = new SpaceFillingAtom(atom2);

            canvas.GetRenderer().AddActor(toon1.getVtkAssembly());
            
            canvas.setCenter(atom1);
        } catch (ParseException exc) { 
            assert(false);
        }        

        frame.pack();
        frame.setVisible(true);
    }

}
