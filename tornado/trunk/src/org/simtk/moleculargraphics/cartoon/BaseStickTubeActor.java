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
 * Created on Nov 8, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.molecularstructure.Residue;
import org.simtk.molecularstructure.atom.Atom;

/** 
 * 
  * @author Christopher Bruns
  * 
  * Molecule representation of one jointed stick to represent an
  * entire nucleotide.  The desired effect is for nucleotides that
  * are canonically base paired to point directly at one another
 */
public class BaseStickTubeActor extends BaseConnectorTubeActor {

    public BaseStickTubeActor(Residue residue) 
    throws NoCartoonCreatedException {

        // Backbone
        Atom p = residue.getAtom("P");
        Atom o5 = residue.getAtom("O5*");
        Atom c1 = residue.getAtom("C1*");
        
        // Where base attaches to ribose
        Atom n = residue.getAtom("N9");
        Atom wc = residue.getAtom("N1");
        if (n == null) {
            n = residue.getAtom("N1");
            wc = residue.getAtom("N3");
        }
        
        
        Atom[] atoms = {p,o5,c1,n,wc};

        setResidue(residue, atoms);
    }
}
