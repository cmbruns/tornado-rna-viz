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
 * Created on Jun 13, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.geometry3d.Vector3D;
import java.awt.Color;

/**
 *  
  * @author Christopher Bruns
  * 
  * One fat cylinder per base pair, connecting the backbone positions
  * of two nucleotides, colored by the nucleotide colors.
 */
public class BasePairRod extends CylinderCartoon {
    protected ColorScheme colorScheme = 
        SequencingNucleotideColorScheme.SEQUENCING_NUCLEOTIDE_COLOR_SCHEME;

    BasePairRod() {
        super(1.00, 5.0); // radius, min length of half rod
        cylinderSource.SetCapping(1);
    }

    public void addMolecule(Molecule molecule) {
        if (molecule instanceof NucleicAcid) {
            NucleicAcid rna = (NucleicAcid) molecule;
            
            // old to do-- move this base pair identification elsewhere; should now be handled in Tornado
            //rna.secondaryStructures().addAll(rna.identifyBasePairs());            
            
            for (SecondaryStructure structure : rna.displayableStructures()) {
                if (structure instanceof BasePair) {
                    BasePair basePair = (BasePair) structure;
                    try {addBasePair(basePair);}
                    catch (InsufficientAtomsException exc) {} // Skip base pairs lacking backbone atoms
                }
            }
        }
    }
    
    public void addBasePair(BasePair basePair) throws InsufficientAtomsException {
        Residue base1 = basePair.getResidue1();
        Residue base2 = basePair.getResidue2();

        // Get backbone positions, if they exist
        Vector3D start, end;
        start = base1.getBackbonePosition();
        end = base2.getBackbonePosition();
        if (start == null) throw new InsufficientAtomsException();
        if (end == null) throw new InsufficientAtomsException();
        
        Vector3D midpoint = start.plus(end.minus(start).times(0.5));

        Color color1, color2;
        try {color1 = colorScheme.colorOf(base1);}
        catch (UnknownObjectColorException exc) {color1 = Color.white;}
        try {color2 = colorScheme.colorOf(base2);}
        catch (UnknownObjectColorException exc) {color2 = Color.white;}
        
        // Color color1 = base1.getDefaultColor();
        // Color color2 = base2.getDefaultColor();

        // Construct rod in two segments, one for each nucleotide
        addCylinder(start, midpoint, color1, base1);
        addCylinder(midpoint, end, color2, base2);
    }
}
