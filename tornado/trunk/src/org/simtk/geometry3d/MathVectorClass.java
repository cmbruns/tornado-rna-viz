/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Jul 25, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.geometry3d;

public class MathVectorClass implements MathVector {
    private double m_array[] = null;

    private void initialize(int size) {
        // If memory is already allocated, do nothing
        if ( (m_array != null) && (m_array.length == size) ) return;
        
        // Otherwise allocate memory
        m_array = new double[size];        
    }
    
    public MathVectorClass(int size) {
        initialize(size);
    }

    public double get(int i) {
        return m_array[i];
    }

    /** copy contents of argument vector into this one
     * 
     * @param v2
     */
    public void copy(MathVectorClass v2) {
        initialize(v2.dimension());
        for (int i = 0; i < dimension(); i++) {
            set(i, v2.get(i));
        }        
    }
    
    public void set(int i, double d) {m_array[i] = d;}

    public int dimension() {return m_array.length;}
    
    public MathVectorClass plus(MathVectorClass v2) {
        checkDimension(v2);
        MathVectorClass answer = new MathVectorClass(dimension());
        for (int i = 0; i < dimension(); i++) {
            answer.set(i, get(i) + v2.get(i));
        }
        return answer;
    }
    
    public void plusEquals(MathVectorClass v2) {
        checkDimension(v2);
        for (int i = 0; i < dimension(); i++) {
            set(i, get(i) + v2.get(i));
        }        
    }
    
    public MathVectorClass minus(MathVectorClass v2) {
        checkDimension(v2);
        MathVectorClass answer = new MathVectorClass(dimension());
        for (int i = 0; i < dimension(); i++) {
            answer.set(i, get(i) - v2.get(i));
        }
        return answer;
    }
    
    public double dot(MathVectorClass v2) {
        checkDimension(v2);
        double answer = 0;
        for (int i = 0; i < dimension(); i++) {
            answer += ( get(i) * v2.get(i) );
        }
        return answer;
    }
    
    public double length() {
        return Math.sqrt(this.dot(this));
    }
    
    public double distance(MathVectorClass v2) {
        return this.minus(v2).length();
    }
    
    public double distanceSquared(MathVectorClass v2) {
        MathVectorClass difference = this.minus(v2);
        return difference.dot(difference);
    }
    
    public MathVectorClass unit() {
        return this.scale(1.0/length());
    }
    
    public void selfUnit() {
        this.selfScale(1.0/length());
    }
    
    public MathVectorClass scale(double s) {
        MathVectorClass answer = new MathVectorClass(dimension());
        for (int i = 0; i < dimension(); i++) {
            answer.set(i, get(i) * s);
        }
        return answer;
    }
    
    public void selfScale(double s) {
        for (int i = 0; i < dimension(); i++) {
            set(i, get(i) * s);
        }
    }
    
    private void checkDimension(MathVectorClass v2) {
        if ( dimension() != v2.dimension() ) throw new RuntimeException("Vector inner product dimension mismatch");        
    }
}
