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

// import java.awt.Color;
import java.util.*;

import org.simtk.geometry3d.Vector3D;
import org.simtk.molecularstructure.atom.*;

public interface Residue 
extends ResidueType, Molecular
{
    // public Color getDefaultColor();
    public Residue getPreviousResidue();
    public Residue getNextResidue();
    public int getResidueNumber();
    public ResidueType getResidueType();
    public Atom getAtom(String atomName);
    public char getPdbInsertionCode();

    // public Iterator getSecondaryStructureIterator();
    public Collection<SecondaryStructure> secondaryStructures();
    // public void addSecondaryStructure(SecondaryStructure structure);
    public void setPreviousResidue(Residue r);
    public void setNextResidue(Residue r);
    public void setResidueNumber(int residueNumber);

    public Set<Atom> getHydrogenBondDonors();
    public Set<Atom> getHydrogenBondAcceptors();
    public Vector3D getBackbonePosition() throws InsufficientAtomsException;
    public Vector3D getSideChainPosition() throws InsufficientAtomsException;
    public Molecular get(FunctionalGroup fg) throws InsufficientAtomsException; // TODO

    public boolean isStrand();
    public boolean isHelix();
    public boolean isAlphaHelix();

}
