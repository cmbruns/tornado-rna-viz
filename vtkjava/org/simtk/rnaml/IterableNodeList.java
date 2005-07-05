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
