/*
 * Created on Jun 21, 2005
 *
 */
package org.simtk.rnaml;

import java.io.*;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class RNAMLTest {
    static Document document;

    public static void main(String args[]) {
        System.out.println("Hey, this isn't testing the RNAML xml format!?!?!");

        // Open the input file as a stream
        RNAMLTest rnaMLTest = new RNAMLTest();
        ClassLoader classLoader = rnaMLTest.getClass().getClassLoader();
        InputStream inStream = null;
        try {inStream = classLoader.getResource("1x8w.pdb2.xml").openStream();}
        catch (IOException exc) {System.out.println(exc);}

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

        printNodeHierarchy(document);

        System.exit(0);
    }
    
    static void printNodeHierarchy(Node n) {
        printNodeHierarchy(n, 0);
    }
    static void printNodeHierarchy(Node n, int indentLevel) {
        String indent = "";
        for (int i = 0; i < indentLevel; i++) indent = indent + " ";

        System.out.println(indent + n.getNodeName());
        for ( Node node : new IterableNodeList(n.getChildNodes()) ) {
            if (node.getNodeType() == Node.TEXT_NODE) continue;
            printNodeHierarchy(node, indentLevel + 2);
        }        
    }
}
