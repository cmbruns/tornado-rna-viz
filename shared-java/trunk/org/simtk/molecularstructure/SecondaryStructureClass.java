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
 * Created on Dec 12, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure;

import java.util.*;

public class SecondaryStructureClass implements SecondaryStructure {
    private Collection residues = new Vector();
    private Biopolymer molecule = null;
    private String source = "";

    public Iterator getResidueIterator() {
        return this.residues.iterator();
    }
    
    public Collection<Residue> residues() {return residues;}

    public void addResidue(Residue residue) {
        this.residues.add(residue);
    }

    public void setMolecule(Biopolymer biopolymer) {
        this.molecule = biopolymer;
    }


    public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
