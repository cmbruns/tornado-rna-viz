/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Jul 31, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.toon.secstruct;

import junit.framework.TestCase;
import java.net.URL;
import java.io.*;
import javax.swing.*;

import org.simtk.molecularstructure.Molecule;
import org.simtk.molecularstructure.MoleculeCollection;
import org.simtk.molecularstructure.nucleicacid.BasePair;
import org.simtk.molecularstructure.nucleicacid.NucleicAcid;

import java.awt.*;

public class TestSecondaryStructureCanvas extends TestCase {

    public static void main(String[] args) {
        // junit.textui.TestRunner.run(TestSecondaryStructureCanvas.class);

        // try {
        //     (new TestSecondaryStructureCanvas()).testLoadSStructViewFile();
        // } catch (IOException exc) {}
        
        testNussinovDiagram();
    }

    /*
     * Test method for 'org.simtk.toon.secstruct.SecondaryStructureCanvas.loadSStructViewFile(InputStream)'
     */
    public void testLoadSStructViewFile() throws IOException {
        SecondaryStructureCanvas canvas = new SecondaryStructureCanvas(null);
        
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("sstructs/e.coli16s.coord");
        InputStream testStream = url.openConnection().getInputStream();
        canvas.loadSStructViewFile(testStream);

        JFrame frame = new JFrame();
        frame.getRootPane().setLayout(new BorderLayout());
        frame.getRootPane().add(canvas, BorderLayout.CENTER);;
        frame.pack();
        frame.setSize(300, 300);
        frame.setVisible(true);
    }
    
    public static void testNussinovDiagram() {
        MoleculeCollection molecules = new MoleculeCollection();
        try {
            molecules.loadPDBFormat("structures/1GRZ.pdb");
        } 
        catch (java.io.FileNotFoundException exc) {return;}
        catch (java.io.IOException exc) {return;}
        catch (InterruptedException exc) {return;}
        
        NussinovDiagram diagram = null;
        for (Molecule molecule : molecules.molecules()) {
            if (! (molecule instanceof NucleicAcid)) continue;
            NucleicAcid rna = (NucleicAcid) molecule;
            for (BasePair basePair : rna.identifyBasePairs())
                rna.secondaryStructures().add(basePair);
            diagram = new NussinovDiagram(rna);
            break;
        }
        
        if (diagram != null) {
            SecondaryStructureCanvas canvas = new SecondaryStructureCanvas(null);            
            canvas.setDiagram(diagram);

            JFrame frame = new JFrame();
            frame.getRootPane().setLayout(new BorderLayout());
            frame.getRootPane().add(canvas, BorderLayout.CENTER);;
            frame.pack();
            frame.setSize(300, 300);
            frame.setVisible(true);
        }
    }
    
}
