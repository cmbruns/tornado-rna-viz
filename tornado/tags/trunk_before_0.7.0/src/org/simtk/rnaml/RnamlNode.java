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
 * Created on Jun 29, 2005
 *
 */
package org.simtk.rnaml;

import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RnamlNode {
    Node privateNode;
    RnamlNode(Node n) {
        privateNode = n;
    }

    Collection get(String nodeName) {
        Vector answer = new Vector();
        for (Iterator iterNode = (new IterableNodeList(privateNode.getChildNodes())).iterator(); iterNode.hasNext(); ) {
            Node node = (Node) iterNode.next();
            if (node.getNodeName().equals(nodeName))
                answer.add(new RnamlNode(node));
        }
        return answer;
    }

    RnamlNode get1(String nodeName) {
        for (Iterator iterNode = (new IterableNodeList(privateNode.getChildNodes())).iterator(); iterNode.hasNext(); ) {
            Node node = (Node) iterNode.next();
            if (node.getNodeName().equals(nodeName)) return new RnamlNode(node);
        }
        return null;
    }

    String getText() {
        // return privateNode.getTextContent(); // Java 1.5 specific
        return privateNode.getNodeValue();
    }
    
    int getInt() {
        return (new Integer(getText())).intValue();
    }

    void printNodeHierarchy() {
        printNodeHierarchy(0);
    }
    void printNodeHierarchy(int indentLevel) {
        String indent = "";
        for (int i = 0; i < indentLevel; i++) indent = indent + " ";

        System.out.println(indent + privateNode.getNodeName());
        for (Iterator iterNode = (new IterableNodeList(privateNode.getChildNodes())).iterator(); iterNode.hasNext(); ) {
            Node node = (Node) iterNode.next();
            if (node.getNodeType() == Node.TEXT_NODE) continue;
            printNodeHierarchy(indentLevel + 2);
        }        
    }
    
    static RnamlNode loadXML(InputStream inStream) {
        Document document = null;
        
        // Parse the xml file into the variable "document"
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inStream);
        } catch (SAXException sxe) {
           // Error generated during parsing
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
           x.printStackTrace();

        } catch (ParserConfigurationException pce) {
           // Parser with specified options can't be built
           pce.printStackTrace();

        } catch (IOException ioe) {
           // I/O error
           ioe.printStackTrace();
        }

        // Print list of base pairs
        RnamlNode doc = new RnamlNode(document);        

        // Final rnaml node is the correct one.
        // I don't know what the first one is
        RnamlNode rnaml = null;
        for (Iterator iterNode = (doc.get("rnaml")).iterator(); iterNode.hasNext(); ) {
            RnamlNode node = (RnamlNode) iterNode.next();
            rnaml = node;
        }
        
        return rnaml;
    }
}
