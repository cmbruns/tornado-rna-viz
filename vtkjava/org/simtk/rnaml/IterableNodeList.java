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
 * Created on Jun 21, 2005
 *
 */
package org.simtk.rnaml;

import java.util.Iterator;
import org.w3c.dom.*;

public class IterableNodeList implements NodeList, Iterable {

    private NodeList nodeList;
    IterableNodeList(NodeList n) {nodeList = n;}
    
    public Iterator iterator() {
        return new NodeIterator(nodeList);
    }

    public int getLength() {return nodeList.getLength();}
    public Node item(int i) {return nodeList.item(i);}
    
    class NodeIterator implements Iterator {
        NodeList iNodeList;
        int currentIndex = -1;
        NodeIterator(NodeList n) {
            iNodeList = n;
        }
        public boolean hasNext() {
            if (currentIndex < (iNodeList.getLength()) - 1) return true;
            else return false;
        }
        public Object next() {
            currentIndex ++;
            return iNodeList.item(currentIndex);
        }
        public void remove() {
            // optional operation - do nothing - no interface to NodeList for this
            throw new UnsupportedOperationException();
        }
    }
}
