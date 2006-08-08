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
 * Created on Aug 7, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.toon.secstruct;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import junit.framework.TestCase;

public class TestSecondaryStructureDiagram extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestSecondaryStructureDiagram.class);
    }

    /*
     * Test method for 'org.simtk.toon.secstruct.SecondaryStructureDiagramModel.createDiagram(RNA)'
     */
    public void testCreateDiagram() {
        SecondaryStructureDiagramModel diagram = new SecondaryStructureDiagramModel();
        
        MoleculeCollection molecules = new MoleculeCollection();
        try {
            molecules.loadPDBFormat("resources/structures/1GRZ.pdb");
        } 
        catch (java.io.FileNotFoundException exc) {return;}
        catch (java.io.IOException exc) {return;}
        catch (InterruptedException exc) {return;}
        
        for (Molecule molecule : molecules.molecules()) {
            if (! (molecule instanceof NucleicAcid)) continue;
            NucleicAcid rna = (NucleicAcid) molecule;
            for (BasePair basePair : rna.identifyBasePairs())
                rna.secondaryStructures().add(basePair);
            diagram.createDiagram(rna);
        }
    }

}
