//
// Copyright (c) 2005, Christopher M. Bruns and The Leland Stanford Junior
// University
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without 
// modification, are permitted provided that the following conditions are 
// met:
//
//     * Redistributions of source code must retain the above copyright 
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in 
//       the documentation and/or other materials provided with the 
//       distribution.
//     * Neither the name of the The Leland Stanford Junior University nor
//       the names of its contributors may be used to endorse or promote 
//       products derived from this software without specific prior 
//       written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/*
 * Created on Jun 21, 2005
 *
 */
package org.simtk.rnaml;

import java.io.*;
import java.util.*;

public class RNAMLTest {

    public static void main(String args[]) {

        // Open the input file as a stream
        RNAMLTest rnaMLTest = new RNAMLTest();
        ClassLoader classLoader = rnaMLTest.getClass().getClassLoader();
        InputStream inStream = null;
        try {inStream = classLoader.getResource("1x8w.pdb2.xml").openStream();}
        catch (IOException exc) {System.out.println(exc);}

        RnamlNode rnaml = RnamlNode.loadXML(inStream);

        RnamlNode molecule = rnaml.get1("molecule");
        
        // Read Sequence numbering
        // So we can map stupid residue numbers to PDB residue numbers
        RnamlNode sequence = molecule.get1("sequence");
        RnamlNode range = sequence.get1("numbering-system").get1("numbering-range");
        int rangeStart = range.get1("start").getInt();
        int rangeEnd = range.get1("end").getInt();
        Hashtable sequenceNumbers = new Hashtable();
        String numberTable = sequence.get1("numbering-table").getText();
        int indexNumber = rangeStart;
        String numbers[] = numberTable.split("\\s+");
        for (int iterNumber = 0; iterNumber < numbers.length; iterNumber++ ) {
            String number = numbers[iterNumber];
            // Ignore initial empty string
            if (number.length() < 1) continue;

            Integer pdbNumber = new Integer(number);
            sequenceNumbers.put(new Integer(indexNumber), pdbNumber);

            indexNumber ++;
        }
        // Sanity check
        if (indexNumber != (rangeEnd + 1)) {
            // throw new Exception();
            System.err.println("indexNumber = " + indexNumber);
            System.err.println("rangeEnd = " + rangeEnd);
        }
        
        // Read sequence letters
        
        // Read structural base pairs
        RnamlNode structure = molecule.get1("structure");
        RnamlNode model = structure.get1("model");
        RnamlNode annotation = model.get1("str-annotation");
        
        for (Iterator iterBasePair = annotation.get("base-pair").iterator(); iterBasePair.hasNext(); ) {
            RnamlNode basePair = (RnamlNode) iterBasePair.next();
            // Sequence residue numbers
            RnamlNode base1pos = basePair.get1("base-id-5p").get1("base-id").get1("position");
            RnamlNode base2pos = basePair.get1("base-id-3p").get1("base-id").get1("position");
            Integer b1 = (Integer) sequenceNumbers.get(new Integer(base1pos.getInt()));
            Integer b2 = (Integer) sequenceNumbers.get(new Integer(base2pos.getInt()));
            
            // Type of base pair
            String pairType1 = basePair.get1("edge-5p").getText();
            String pairType2 = basePair.get1("edge-3p").getText();
            
            // TODO - figure out what "+", "-", and "!" edge types are.
            // (presumably "W","H", and "S" are Watson-Crick, Hoogsteen, and sugar edges)
            
            System.out.println("base pair found " + b1 + ", " + b2 + " : " + pairType1 + pairType2);
        }

        System.exit(0);
    }
    
}
