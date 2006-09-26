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
 * Created on Apr 24, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure;

import org.simtk.molecularstructure.atom.*;
import java.util.*;

import org.simtk.geometry3d.InsufficientPointsException;
import org.simtk.geometry3d.MassBody;
import org.simtk.geometry3d.Plane3D;
import org.simtk.geometry3d.Vector3D;

/** 
 *  
  * @author Christopher Bruns
  * 
  * Common interface for molecules, functional groups, and residues.
  * As opposed to Molecule, which should be a complete molecule, while
  * Molecular can refer to a portion of a molecule.
 */
public interface Molecular extends Chemical, MassBody {
    public Set<Atom> atoms();
    public Plane3D bestPlane3D() throws InsufficientPointsException;
    // public Vector3D[] getCoordinates();
}
