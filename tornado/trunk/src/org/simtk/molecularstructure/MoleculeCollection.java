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
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure;

import java.io.*;
import java.util.*;
import java.net.*;
import org.simtk.molecularstructure.atom.*;

import org.simtk.geometry3d.*;

/**
 * @author Christopher Bruns
 *
 * A collection of one or more molecules, as might be found in a PDB file.
 */
public class MoleculeCollection {
    Vector atoms = new Vector();
    Vector molecules = new Vector();

    Vector3DClass centerOfMass = new Vector3DClass();
    double mass = 0;
    
    private String mTitle = "";
    public String getTitle() {return mTitle;}
    public void setTitle(String t) {mTitle = t;}
    
    private String mPdbId = null;
    public String getPdbId() {return mPdbId;}
    public void setPdbId(String i) {mPdbId = i;}

    private String m_inputStructureFileName = null;
    public String getInputStructureFileName() {return m_inputStructureFileName;}
    public void setInputStructureFileName(String s) {m_inputStructureFileName = s;}

    public double getMass() {
        return mass;
    }
    public Vector3DClass getCenterOfMass() {
        if (mass <= 0) return null;
        return centerOfMass;
    }
    
    public void relaxCoordinates() {
        for (Iterator i = molecules.iterator(); i.hasNext(); ) {
            Molecule m = (Molecule) i.next();
            m.relaxCoordinates();
        }
    }
    
    public Vector molecules() {return molecules;}
    
    public int getMoleculeCount() {return molecules.size();}
    public Molecule getMolecule(int i) {return (Molecule) molecules.elementAt(i);}
    
    public int getAtomCount() {return atoms.size();}
    
    public void loadPDBFormat(String fileName) throws FileNotFoundException, IOException, InterruptedException {
		FileInputStream fileStream = new FileInputStream(fileName);
		loadPDBFormat(fileStream);
        fileStream.close();
	}

    public void loadPDBFormat(URL url) throws IOException, InterruptedException {
        InputStream inStream = url.openStream();
        loadPDBFormat(inStream);
        inStream.close();
    }

    public void loadPDBFormat(InputStream is) throws IOException, InterruptedException {
        
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
        
        // TODO read header information, specfically the name(s) of the molecules
        String PDBLine;
        String title = "";
        
        reader.mark(200);
        FILE_LINE: while ((PDBLine = reader.readLine()) != null) {

            // Stop parsing after the END record
            if (PDBLine.substring(0,3).equals("END")) {
                reader.reset(); // Leave the END tag for the next guy
                return;
            }

            // If we get to ATOM records, we need to stop and send the stream to the Molecule parser
            else if ((PDBLine.substring(0,6).equals("ATOM  ")) || (PDBLine.substring(0,6).equals("HETATM"))) {
                // Oops, we passed the header, and are now in the coordinates
                // Pass this off to the Molecule parser
                reader.reset();
                break;
            }

            else if (PDBLine.substring(0,6).equals("TITLE ")) {
                String titlePart = PDBLine.substring(10,60).trim();
                if (titlePart.length() > 0) {
                    if (title.length() > 0) title = title + " " + titlePart;
                    else title = titlePart;
                }
            }

            else if (PDBLine.substring(0,6).equals("HEADER")) {
                if (PDBLine.length() >= 56) {
                    String id = PDBLine.substring(52,56).trim();
                    if (id.length() == 4) setPdbId(id);
                }
            }

            reader.mark(200); // Commit to reading this far into the file
        }
        
        // Populate title
        if (title.length() > 0) setTitle(title);
        
		Molecule mol = Molecule.createFactoryPDBMolecule(reader);

		// TODO do something more proactive if there are no molecules (such as throw an exception)
		if (mol == null) {return;}
		
		while (mol.getAtomCount() > 0) {
		    molecules.addElement(mol);

		    double myMassProportion = getMass() / (getMass() + mol.getMass());
		    centerOfMass = new Vector3DClass( centerOfMass.scale(myMassProportion).plus(
		            mol.getCenterOfMass().scale(1.0 - myMassProportion) ) );
		    
		    mass += mol.getMass();
		    
            for (Iterator i = mol.getAtomIterator(); i.hasNext(); ) {
                LocatedAtom a = (LocatedAtom) i.next();
                atoms.addElement(a);
            }
		    
		    mol = Molecule.createFactoryPDBMolecule(reader);
		    if (mol == null) break;
		}
	}
}
