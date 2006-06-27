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
 * Created on Dec 8, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.geometry3d;

public class MassBodyClass implements MassBody {
    private Vector3D centerOfMass = new Vector3DClass(0,0,0);
    private double mass = 0.0;
    
    public Vector3D getCenterOfMass() {
        return centerOfMass;
    }

    public double getMass() {
        return mass;
    }

    public void clear() {
        centerOfMass = new Vector3DClass(0,0,0);
        mass = 0.0;        
    }
    
    public void add (MassBody m) {
        double massFraction = m.getMass() / (m.getMass() + this.getMass());
        this.centerOfMass = m.getCenterOfMass().times(massFraction).plus(this.getCenterOfMass().times(1.0 - massFraction));
        this.mass += m.getMass();
    }
}
