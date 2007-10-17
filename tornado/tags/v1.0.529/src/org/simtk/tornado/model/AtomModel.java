/* Portions copyright (c) 2007 Stanford University and Christopher Bruns
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
 * Created on May 9, 2007
 * Original author: Christopher Bruns
 */
package org.simtk.tornado.model;

import java.util.Collection;
import org.simtk.geometry3d.Vector3D;
import org.simtk.molecularstructure.Residue;
import org.simtk.molecularstructure.atom.*;
import java.awt.Color;

/**
 * 
  * @author Christopher Bruns
  * 
  * Model-View-Controller model type for atom
 */
public class AtomModel
extends AbstractModel
implements Model, Atom 
{
    private Atom atom; // delegate by composition
    private boolean isSelected = false;
    private Color color;
    
    public AtomModel(Atom a) {
        this.atom = a;
    }
    
    public String getAtomName() {
        return atom.getAtomName();
    }
    
    public Vector3D getCoordinates() {
        return atom.getCoordinates();
    }
    
    public double getTemperatureFactor() {
        return atom.getTemperatureFactor();
    }
    
    public double getOccupancy() {
        return atom.getOccupancy();
    }
    
    public Collection<Atom> bonds() {
        return atom.bonds();
    }
    
    public double distance(Atom atom2) {
        return atom.distance(atom2);
    }
    
    public void setAtomName(String name) {
        atom.setAtomName(name);
    }
    
    public void addPosition(AtomPosition position) {
        atom.addPosition(position);
    }
    
    public void setCoordinates(Vector3D coordinates) {
        atom.setCoordinates(coordinates);
    }
    
    public Residue getResidue() {
        return atom.getResidue();
    }

    public String getElementSymbol() {
        return atom.getElementSymbol();
    }
    
    public String getElementName() {
        return atom.getElementName();
    }
    
    public double getMass() {
        return atom.getMass();
    }
    
    public double getCovalentRadius() {
        return atom.getCovalentRadius();
    }
    
    public double getVanDerWaalsRadius() {
        return atom.getVanDerWaalsRadius();
    }

    public Vector3D getCenterOfMass() {
        return atom.getCenterOfMass();
    }
}
