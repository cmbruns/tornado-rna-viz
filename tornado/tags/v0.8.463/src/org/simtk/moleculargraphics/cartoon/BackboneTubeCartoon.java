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
 * Created on Jul 18, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.molecularstructure.*;
import java.util.List;

public class BackboneTubeCartoon extends MoleculeCartoonClass {
    protected double radius = 1.0;
    
    BackboneTubeCartoon(double radius) {
        this.radius = radius;
    }
    
    public void addMolecule(Molecule molecule) {
        if (! (molecule instanceof Biopolymer) ) return;
        Biopolymer biopolymer = (Biopolymer) molecule;
        addResidues(biopolymer.residues());
    }

    public void addResidues(List<Residue> residues) {
        try {            
            BackboneTubeActor actorToon = 
                new BackboneTubeActor(radius, residues);
            if (actorToon.isPopulated()) {
                subToons.add(actorToon);
                actorSet.add(actorToon);
            }
        } catch (NoCartoonCreatedException exc) {}
    }
}