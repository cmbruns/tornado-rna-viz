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

import java.util.*;

public class MathVectorClass implements MutableMathVector {

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

    public double getElement(int i) {
        return get(i);
    }

    /** copy contents of argument vector into this one
     * 
     * @param v2
     */
    public void copy(MathVector v2) {
        initialize(v2.dimension());
        for (int i = 0; i < dimension(); i++) {
            set(i, v2.get(i));
        }        
    }
    
    public void set(int i, double d) {
        m_array[i] = d;
    }

    public int dimension() {return m_array.length;}
    
    public MathVector plus(MathVector v2) {
        checkDimension(v2);
        MutableMathVector answer = new MathVectorClass(dimension());
        for (int i = 0; i < dimension(); i++) {
            answer.set(i, get(i) + v2.get(i));
        }
        return answer;
    }
    
    public void plusEquals(MathVector v2) {
        checkDimension(v2);
        for (int i = 0; i < dimension(); i++) {
            set(i, get(i) + v2.get(i));
        }        
    }
    
    public void minusEquals(MathVector v2) {
        checkDimension(v2);
        for (int i = 0; i < dimension(); i++) {
            set(i, get(i) - v2.get(i));
        }        
    }
    
    public MathVector minus(MathVector v2) {
        checkDimension(v2);
        MutableMathVector answer = new MathVectorClass(dimension());
        for (int i = 0; i < dimension(); i++) {
            answer.set(i, get(i) - v2.get(i));
        }
        return answer;
    }
    
    public double dot(MathVector v2) {
        checkDimension(v2);
        double answer = 0;
        for (int i = 0; i < dimension(); i++) {
            answer += ( get(i) * v2.get(i) );
        }
        return answer;
    }
    
    public double lengthSquared() {
        return this.dot(this);
    }
    
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    
    public double distance(MathVector v2) {
        return this.minus(v2).length();
    }
    
    public double distanceSquared(MathVector v2) {
        MathVector difference = this.minus(v2);
        return difference.dot(difference);
    }
    
    public MathVector unit() {
        return this.scale(1.0/length());
    }
    
    public void selfUnit() {        
        double scale = length();
        if (scale > 0) scale = 1.0/scale;
        this.selfScale(scale);
    }
    
    public MathVector scale(double s) {
        MutableMathVector answer = new MathVectorClass(dimension());
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
    
    private void checkDimension(MathVector v2) {
        if ( dimension() != v2.dimension() ) throw new VectorSizeException("Vector inner product dimension mismatch");        
    }
    
    public Iterator iterator() {
        return new VectorIterator(this);
    }
    
    public Vector3D v3() {
        if (this instanceof Vector3D) return (Vector3D) this;
        if ( dimension() != 3 ) throw new VectorSizeException("Vector3D must have exactly three coordinates");
        return new Vector3DClass(this);
    }
    
    class VectorIterator implements Iterator {

        private int elementIndex = 0;
        private MathVector vector;
        
        VectorIterator(MathVector vector) {
            this.vector = vector;
        }
        
        public boolean hasNext() {
            if (elementIndex >= vector.dimension()) return false;
            else return true;
        }

        public Object next() {
            Object answer = null;
            if (hasNext()) answer = new Double(vector.getElement(elementIndex));
            elementIndex ++;
            return answer;
        }
        
        public void remove() {
            throw new UnsupportedOperationException("I cannot remove elements from a vector");
        }
    }
    
    public String toString() {
        String answer = "";
        answer += "(";
        for (int i = 0; i < dimension(); i++) {
            answer += get(i);
            if (i < (dimension() - 1))
                answer += ", ";
        }
        answer += ")";
        return answer;
    }
}
