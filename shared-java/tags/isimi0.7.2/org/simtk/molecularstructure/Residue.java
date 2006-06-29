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
 * Created on Dec 1, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure;

import java.awt.Color;
import java.util.*;

public interface Residue {
    public Color getDefaultColor();
    public Residue getPreviousResidue();
    public Residue getNextResidue();
    public char getOneLetterCode();
    public String getResidueName();
    public int getResidueNumber();
    public Iterator getSecondaryStructureIterator();
    public Collection<SecondaryStructure> secondaryStructures();
    public void addSecondaryStructure(SecondaryStructure structure);
    public void setPreviousResidue(PDBResidueClass r);
    public void setNextResidue(PDBResidueClass r);
}