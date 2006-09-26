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
 * Created on Apr 26, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure;

import java.util.*;

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.atom.*;

public class MolecularClass implements Molecular {
    private Set<Atom> atoms = new LinkedHashSet<Atom>();

    public MolecularClass() {}

    public MolecularClass(AtomSet atomSet) {
        atoms().addAll(atomSet);
    }

    public Set<Atom> atoms() {return atoms;}

    public double getMass() {
        double mass = 0.0;
        for (Atom atom : atoms()) {
            mass += atom.getMass();
        }
        return mass;
    }
    
    public Vector3D getCenterOfMass() {
        Vector3D com = new Vector3DClass(0,0,0);
        double totalMass = 0.0;
        for (Atom atom : atoms()) {
            double mass = atom.getMass();
            com = com.plus(atom.getCenterOfMass().times(mass));
            totalMass += mass;
        }
        if (totalMass == 0.0) return null;
        else return com.times(1.0 / totalMass);
    }

    /**
     * Change the position of the molecule by the specified amount
     * @param t amount to translate
     */
    public Plane3D bestPlane3D() 
    throws InsufficientPointsException
    {
        Vector3D[] coordinates = new Vector3DClass[atoms().size()];
        double[] masses = new double[atoms().size()];

        int a = 0;
        for (Atom atom : atoms()) {
            coordinates[a] = atom.getCoordinates();
            masses[a] = atom.getMass();

            a++;
        }
        return Plane3D.bestPlane3D(coordinates, masses);
    }
}
