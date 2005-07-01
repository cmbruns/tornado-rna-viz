/*
 * Created on Jun 29, 2005
 *
 */
package org.simtk.rnaml;

import java.util.Collection;
import java.util.Vector;

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

    Collection<RnamlNode> get(String nodeName) {
        Vector<RnamlNode> answer = new Vector<RnamlNode>();
        for (Node node : new IterableNodeList(privateNode.getChildNodes())) {
            if (node.getNodeName().equals(nodeName))
                answer.add(new RnamlNode(node));
        }
        return answer;
    }

    RnamlNode get1(String nodeName) {
        for (Node node : new IterableNodeList(privateNode.getChildNodes())) {
            if (node.getNodeName().equals(nodeName)) return new RnamlNode(node);
        }
        return null;
    }

    String getText() {
        return privateNode.getTextContent();
    }
    
    int getInt() {
        return new Integer(getText());
    }

    void printNodeHierarchy() {
        printNodeHierarchy(0);
    }
    void printNodeHierarchy(int indentLevel) {
        String indent = "";
        for (int i = 0; i < indentLevel; i++) indent = indent + " ";

        System.out.println(indent + privateNode.getNodeName());
        for ( Node node : new IterableNodeList(privateNode.getChildNodes()) ) {
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
        for (RnamlNode node : doc.get("rnaml"))
            rnaml = node;
        
        return rnaml;
    }
}
