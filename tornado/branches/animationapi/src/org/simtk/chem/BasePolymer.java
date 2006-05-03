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
 * Created on Apr 28, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem;

import java.util.*;

public class BasePolymer extends BaseMolecular implements Polymer {
    private PolymerType moleculeType = MoleculeType.POLYMER;
    private Collection<Residue> residues = new LinkedHashSet<Residue>();
    private Collection<SecondaryStructure> secondaryStructures = new LinkedHashSet<SecondaryStructure>();
    private Map<Integer, Residue> numberResidues = new HashMap<Integer, Residue>();
    
    public PolymerType getMoleculeType() {return moleculeType;}
    public Iterable<Residue> residues() {return residues;}
    public Residue getResidueByNumber(int resNum) {
        return numberResidues.get(resNum);
    }
    
    public Iterable<SecondaryStructure> secondaryStructures() {return secondaryStructures;}
    public void addResidue(Residue residue) {
        residues.add(residue);
        numberResidues.put(residue.getResidueNumber(), residue);
    }
    public void addSecondaryStructure(SecondaryStructure secondaryStructure) {
        secondaryStructures.add(secondaryStructure);
    }
    
    public static Polymer createPolymer(Collection<Residue> r) {
        BasePolymer answer = new BasePolymer();
        
        if (r != null) for (Residue residue : r) {
            answer.addResidue(residue);
        }
        
        return answer;
    }
    
    protected BasePolymer() {} // Hide constructor    
}
