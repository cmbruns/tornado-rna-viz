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
 * Created on Jul 19, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.util;

/**
 *  
  * @author Christopher Bruns
  * 
  * ring buffer queue, which maintains a finite number of previously added numbers
 */
public class DoubleRing {
    private int m_maxSize;
    private int m_currentIndex;
    private int m_firstIndex; // -1 means array is empty
    private double[] m_array;

    public DoubleRing(int maxSize) {
        if (maxSize < 1) throw new IndexOutOfBoundsException("ERROR: ring size must be positive");
        m_maxSize = maxSize;
        m_array = new double[m_maxSize];
        m_currentIndex = -1;
        m_firstIndex = -1;
    }
    
    public void push(double d) {
        m_currentIndex = incrementIndex(m_currentIndex);
        m_array[m_currentIndex] = d;
        
        // has ring caught up to its own tail?
        if (m_firstIndex == m_currentIndex)
            m_firstIndex = incrementIndex(m_currentIndex);
        
        // is this the first element inserted?
        if (m_firstIndex < 0) m_firstIndex = m_currentIndex;
    }

    public void pop() {
        // Is the ring now empty?
        if (m_currentIndex == m_firstIndex) {
            m_currentIndex = -1;
            m_firstIndex = -1;
        }
        
        else
            m_currentIndex = decrementIndex(m_currentIndex);
    }

    public boolean isEmpty() {if (m_firstIndex < 0) return true; return false;}
    
    /** Compute mean value of the elements in the ring.
     * 
     * @return
     */
    public double mean() {

        if (m_firstIndex < 0) return 0;
        if (m_currentIndex < 0) return 0;
        
        double answer = m_array[m_currentIndex];
        int elementCount = 1;
        for (int i = m_firstIndex; i != m_currentIndex; i = incrementIndex(i)) {
            answer += m_array[i];
            elementCount ++;
        }
        if (elementCount > 0) answer = answer / elementCount;
        
        return answer;
    }
    
    /**
     * Compute the array index of the element following the element at index i
     * @param i
     * @return
     */
    private int incrementIndex(int i) {
        int answer = i + 1;
        if (answer >= m_maxSize) answer = 0;
        return answer;
    }

    /**
     * Compute the array index of the element preceding the element at index i
     * @param i
     * @return
     */
    private int decrementIndex(int i) {
        int answer = i - 1;
        if (answer < 0) answer = m_maxSize - 1;
        return answer;
    }
}
