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
import java.net.URL;
// import java.nio.ByteOrder;
import java.text.ParseException;
import java.util.*; // Vector

import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.atom.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.mvc.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule structure.
 */
public class PDBMoleculeClass extends MoleculeMVCModel implements MutableLocatedMolecule, PDBMolecule {
    private Collection atoms = new LinkedHashSet();
    // protected Vector atoms = new Vector();
    // protected Vector<Bond> bonds = new Vector<Bond>();
	// Vector bonds = new Vector();

    Vector3DClass centerOfMass = new Vector3DClass();
    double mass = 0;
    private String chainID = " ";

    public Vector3D[] getCoordinates() {
        Vector3D[] answer = new Vector3DClass[atoms.size()];
        Iterator atomIterator = atoms.iterator();
        for (int i = 0; i < atoms.size(); i++) {
            LocatedAtom atom = (LocatedAtom) atomIterator.next();
            answer[i] = atom.getCoordinates();
        }
        return answer;
    }
    
    public void setChainID(String chainID) {this.chainID = chainID;}
    public String getChainID() {return this.chainID;}
    
    public void setCoordinates(Vector3D[] coordinates) {
        if (coordinates.length != atoms.size())
            throw new RuntimeException("Coordinate array mismatch");

        Iterator atomIterator = atoms.iterator();
        for (int atomIndex = 0; atomIndex < atoms.size(); atomIndex ++) {
            MovableAtom atom = (MovableAtom) atomIterator.next();
            atom.setCoordinates(coordinates[atomIndex]);
        }
    }
    
    public double getMass() {
        return mass;
    }
    public Vector3D getCenterOfMass() {
        if (mass <= 0) return null;
        return centerOfMass;
    }

    float[] referenceCoordinates = null;
    private void storeReferenceCoordinates() {
        int atomCount = getAtomCount();
        referenceCoordinates = new float[atomCount * 3];

        int coordinateIndex = 0;
        for (Iterator i = getAtomIterator(); i.hasNext(); ) {
            PDBAtom atom = (PDBAtom) i.next();
            for (Iterator i2 = atom.getCoordinates().iterator(); i2.hasNext(); ) {
                Double coordinate = (Double) i2.next();
                // Set reference coordinates once
                referenceCoordinates[coordinateIndex] = coordinate.floatValue();
                coordinateIndex ++;
            }
        }
    }
    
    public void relaxCoordinates() {
        
        int atomCount = getAtomCount();
//        int duplexCount = 0;
        float referenceCoordinates[] = new float[atomCount * 3];
//        int duplexRanges[] = new int[duplexCount * 4];
//        boolean swapBytes = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
        
        // First time relaxing the coordinates?  Store the initial configuration
        if ( (referenceCoordinates == null) || (referenceCoordinates.length != atomCount) )
            storeReferenceCoordinates();

        // TODO - relax the actual coordinates
        
        // Copy the coordinates to a float array
        float[] actualCoordinateArray = new float[atomCount * 3];
        int coordinateIndex = 0;
        for (Iterator i = getAtomIterator(); i.hasNext(); ) {
            PDBAtom atom = (PDBAtom) i.next();
            for (Iterator i2 = atom.getCoordinates().iterator(); i2.hasNext(); ) {
                Double coordinate = (Double) i2.next();
                // Set reference coordinates once
                actualCoordinateArray[coordinateIndex] = coordinate.floatValue();
                coordinateIndex ++;
            }
        }

        // Copy the coordinates back to the molecule
        coordinateIndex = 0;
        for (Iterator i = getAtomIterator(); i.hasNext(); ) {
            PDBAtom atom = (PDBAtom) i.next();
            
            // Make coordinates changeable
            MutableVector3D coord = (MutableVector3D) atom.getCoordinates();
            
            coord.setX(actualCoordinateArray[coordinateIndex]);
            coordinateIndex++;
            
            coord.setY(actualCoordinateArray[coordinateIndex]);
            coordinateIndex++;
            
            coord.setZ(actualCoordinateArray[coordinateIndex]);
            coordinateIndex++;
        }

    }

    public PDBMoleculeClass() {} // Empty molecule
	public PDBMoleculeClass(PDBAtomSet atomSet) {
        // for (Atom atom : atomSet) {
        for (Iterator i = atomSet.iterator(); i.hasNext();) {
            PDBAtom atom = (PDBAtom) i.next();
            addAtom(atom);
		}
        createBonds();
	}

    /**
     * Change the position of the molecule by the specified amount
     * @param t amount to translate
     */
    public void translate(Vector3D t) {
        for (Iterator i = getAtomIterator(); i.hasNext(); ) {
            Object o = i.next();
            MovableAtom atom = (MovableAtom) o;
            atom.translate(t);
        }
    }
    
    public void rotate(Matrix3D m) {
        for (Iterator i = getAtomIterator(); i.hasNext(); ) {
            Object o = i.next();
            MovableAtom atom = (MovableAtom) o;
            atom.setCoordinates(m.times(atom.getCoordinates()));
        }
    }
    
    public void transform(HomogeneousTransform h) {
        for (Iterator i = getAtomIterator(); i.hasNext(); ) {
            Object o = i.next();
            MovableAtom atom = (MovableAtom) o;
            atom.setCoordinates(h.times(atom.getCoordinates()));
        }
    }
    
    public Iterator getAtomIterator() {return atoms.iterator();}
    // public Vector getAtoms() {return atoms;}
    
    /**
     * Place all atomic coordinates into a single large array of float values,
     * using the Vector3DWrapper class to redefine the coordinates
     * @return
     */
    public float[] packCoordinatesIntoFloatArray() {
        int atomCount = getAtomCount();
        float[] coordinateArray = new float[3 * atomCount];

        int arrayIndex = 0;
        for (Iterator i = getAtomIterator(); i.hasNext(); ) {
            Object o = i.next();
            MovableAtom atom = (MovableAtom) o;
            for (Iterator i2 = atom.getCoordinates().iterator(); i2.hasNext(); ) {
                Double coord = (Double) i2.next();
                coordinateArray[arrayIndex] = coord.floatValue();
                arrayIndex ++;
            }
            atom.setCoordinates(new Vector3DFloatArrayWrapper(coordinateArray, arrayIndex - 3));
        }
        
        return coordinateArray;
    }
    
    public Plane3D bestPlane3D() {
        Vector3D[] coordinates = new Vector3DClass[getAtomCount()];
        double[] masses = new double[getAtomCount()];

        int a = 0;
        for (Iterator i = getAtomIterator(); i.hasNext();) {
            PDBAtom atom = (PDBAtom) i.next();
            coordinates[a] = atom.getCoordinates();
            masses[a] = atom.getMass();

            a++;
        }
        return Plane3D.bestPlane3D(coordinates, masses);
    }
    
    public void addAtom(PDBAtom atom) {
        if (atoms.contains(atom)) return; // no change
        
        atoms.add(atom);
        mass += atom.getMass();
        double massRatio = atom.getMass() / mass;
        centerOfMass = new Vector3DClass( centerOfMass.scale(1.0 - massRatio).plus(atom.getCoordinates().times(massRatio)) );
    }

    public void removeAtom(PDBAtom atom) {
        if (! atoms.contains(atom)) return; // no change
        
        atoms.remove(atom);
        double massRatio = atom.getMass() / mass;
        mass -= atom.getMass();
        centerOfMass = new Vector3DClass( centerOfMass.scale(1.0 - massRatio).minus(atom.getCoordinates().times(massRatio)) );
    }
    
    public boolean containsAtom(PDBAtom atom) {
        return atoms.contains(atom);
    }
    
	public int getAtomCount() {return atoms.size();}
	// public Atom getAtom(int i) {return (Atom) atoms.get(i);}
	
    public static PDBMolecule createFactoryPDBMolecule(URL url) throws IOException {
        InputStream inStream = url.openStream();
        PDBMolecule molecule = createFactoryPDBMolecule(inStream);        
        inStream.close();
        return molecule;
    }

    public static PDBMolecule createFactoryPDBMolecule(String fileName) throws IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
        PDBMolecule molecule = createFactoryPDBMolecule(fileStream);
        fileStream.close();
        return molecule;
    }

    static PDBMolecule createFactoryPDBMolecule(InputStream is) throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
		return createFactoryPDBMolecule(reader);
    }

    /**
     * Read one molecule from a PDB format stream.  Stop before the first atom of the next molecule.
     * @param reader
     * @return
     * @throws IOException
     */
    public static PDBMolecule createFactoryPDBMolecule(LineNumberReader reader) throws IOException {
        PDBAtomSet currentMoleculeAtoms = new PDBAtomSet();
        
        char chainIdentifier = '\0';
        // String residueName;
        char insertionCode = '\0';
        int residueIndex = -1;
        
		String PDBLine;
		reader.mark(200);
        
        // NOTE - parsing of the header section of PDB files occurs in the class MoleculeCollection
        // This routine parses coordinates, presumably after the header has already been parsed.
        
		FILE_LINE: while ((PDBLine = reader.readLine()) != null) {

            // Stop parsing after the END record
			if (PDBLine.substring(0,3).equals("END")) {
			    reader.reset(); // Leave the END tag for the next guy
                PDBMolecule molecule = PDBMoleculeClass.createFactoryPDBMolecule(currentMoleculeAtoms); // empty molecule
				return molecule;
			}

			// Lines with atomic coordinates are used to create new atoms
			else if ((PDBLine.substring(0,6).equals("ATOM  ")) || (PDBLine.substring(0,6).equals("HETATM"))) {
                PDBAtom atom;
                try {
                    atom = new PDBAtomClass(PDBLine);
				} catch (ParseException exc) {
					System.err.println("ERROR: Line " + reader.getLineNumber() + " of PDB file: " + exc);
					continue FILE_LINE;
				}
				
				// Separate molecules when:
				//  a) The chain ID changes
				//  b) Known solvent molecule types have a new residue number
				//  c) This is the very first atom
				if (currentMoleculeAtoms.size() == 0) { // The very first atom of the molecule
					currentMoleculeAtoms.addElement(atom);

					// residueName = atom.getPDBResidueName();
					insertionCode = atom.getInsertionCode();
					chainIdentifier = atom.getChainIdentifier();
					residueIndex = atom.getResidueNumber();
				}
				else { // not the first atom

				    boolean isSameResidue = ( (atom.getResidueNumber() == residueIndex)
				       &&(atom.getInsertionCode() == insertionCode) );
				    boolean isSameChain = ( atom.getChainIdentifier() == chainIdentifier );
				    boolean isSolvent = PDBResidueClass.isSolvent(atom.getPDBResidueName());

				    if ( isSameChain && (isSameResidue || !isSolvent) ) {
					    // Still the same molecule
						currentMoleculeAtoms.addElement(atom);
				    }
				    else { // Not the same molecule - return
				        reader.reset(); // Put latest atom back into the stream
                        PDBMolecule molecule = PDBMoleculeClass.createFactoryPDBMolecule(currentMoleculeAtoms);
                        return molecule;
				    }
				}
			} // ATOM or HETATM record
			
            
			reader.mark(200); // Commit to reading this far into the file
		}

        PDBMolecule answer =  PDBMoleculeClass.createFactoryPDBMolecule(currentMoleculeAtoms);
        
        answer.setChainID(new String("" + chainIdentifier));
        
        return answer;
    }

    static PDBMolecule createFactoryPDBMolecule(PDBAtomSet bagOfAtoms) {
        if (bagOfAtoms == null) return null;
        if (bagOfAtoms.size() == 0) return null;
        
        // Determine if molecule is protein, nucleic acid, biopolymer, or other
        HashSet residues = new HashSet();
        int solventCount = 0;
        int proteinCount = 0;
        int nucleicCount = 0;
        int RNACount = 0;
        int DNACount = 0;
        int atomO2Count = 0;
        int atomC2Count = 0;
        
        int residueCount = 0;
        // Count the residues of each type
        for (int a = 0; a < bagOfAtoms.size(); a++) {
            PDBAtom atom = (PDBAtom) bagOfAtoms.elementAt(a);
            
            // Look for 2' hydroxyl atom to distinguish DNA from RNA
            if (atom.getPDBAtomName().trim().equals("O2*")) atomO2Count ++;
            if (atom.getPDBAtomName().trim().equals("C2*")) atomC2Count ++;            
            
			String residueKey = "" + atom.getChainIdentifier() + atom.getResidueNumber() + atom.getInsertionCode();
			if (residues.contains(residueKey)) continue; // Already saw this residue before
			
			if (PDBResidueClass.isSolvent(atom.getPDBResidueName())) solventCount ++;
			if (PDBResidueClass.isProtein(atom.getPDBResidueName())) proteinCount ++;
			if (PDBResidueClass.isNucleicAcid(atom.getPDBResidueName())) nucleicCount ++;
			if (PDBResidueClass.isDNA(atom.getPDBResidueName())) DNACount ++;
			if (PDBResidueClass.isRNA(atom.getPDBResidueName())) RNACount ++;
			
			residues.add(residueKey);
			residueCount ++;
        }

        if (residueCount == 1)
            return PDBResidueClass.createFactoryResidue(bagOfAtoms);
        
        // If there are protein residues, this is a protein
        if ((proteinCount >= 1) && (proteinCount >= nucleicCount)) {
            // This is a protein molecule
            return new PDBProteinClass(bagOfAtoms);
        }
 
        // If there are nucleic acid residues, this is a nucleic acid
        if ((nucleicCount >= 1)) {
            // This is a nucleic acid molecule
            // Distinguish between RNA and DNA
            if ( (atomC2Count/nucleicCount > 0.2) // Some residues have ribose atoms
               &&(atomC2Count > 1) ) {
                if (atomO2Count/atomC2Count > 0.5) {
                    // RNA has O2 atoms, DNA does not
                    return  new RNA(bagOfAtoms);
                }
                else {
                    return new DNA(bagOfAtoms);
                }
            }
            if (DNACount > RNACount) {
                return new DNA(bagOfAtoms);
            }
            if (RNACount > DNACount) {
                return new RNA(bagOfAtoms);
            }
            return new NucleicAcid(bagOfAtoms);
        }

        // If it has enough residues, it may still be a polymer
        if ((residueCount - solventCount) > 2) {
            return new BiopolymerClass(bagOfAtoms);
        }
        
        // OK, it's some other kind of molecule
        return new PDBMoleculeClass(bagOfAtoms);
    }

    // Create covalent bonds where it seems that they are needed
	void createBonds() {

        // Maybe iodine has the largest "ordinary" covalent radius of 1.33
        double maxCovalentRadius = 1.40;

        // Create a hash for rapid access
        Hash3D atomHash = new Hash3D(maxCovalentRadius);
        for (Iterator a = atoms.iterator(); a.hasNext(); ) {
            LocatedMoleculeAtom atom = (LocatedMoleculeAtom) a.next();
            atomHash.put(atom.getCoordinates(), atom);
        }
        for (Iterator a1 = atoms.iterator(); a1.hasNext(); ) {
            MutablePDBAtom atom1 = (MutablePDBAtom) a1.next();
            double cutoffDistance = (atom1.getCovalentRadius() + maxCovalentRadius) * 1.5;
            for (Iterator a2 = atomHash.neighborValues(atom1.getCoordinates(), cutoffDistance).iterator(); a2.hasNext(); ) {
                MutablePDBAtom atom2 = (MutablePDBAtom) a2.next();
                if (atom1.equals(atom2)) continue;
                
                // Make sure the bond length is about right
                double distance = atom1.distance(atom2);
                double covalentDistance = atom1.getCovalentRadius() + atom2.getCovalentRadius();
                double vanDerWaalsDistance = atom1.getVanDerWaalsRadius() + atom2.getVanDerWaalsRadius();
                // Bond length must be at least 3/4 of that expected
                double minDistance = 0.75 * (covalentDistance);
                // Bond length must be closer to covalent than to van der Waals distance
                if (covalentDistance >= vanDerWaalsDistance) continue;
                double discriminantDistance = vanDerWaalsDistance - covalentDistance;
                double maxDistance = covalentDistance + 0.25 * discriminantDistance;
                if (maxDistance > 1.25 * covalentDistance) maxDistance = 1.25 * covalentDistance;
                if (distance < minDistance) continue;
                if (distance > maxDistance) continue;
                
                // Make sure it is in the same molecule or part
                if ( (atom1 instanceof PDBAtom) && (atom2 instanceof PDBAtom) ) {
                    PDBAtom pdbAtom1 = (PDBAtom) atom1;
                    PDBAtom pdbAtom2 = (PDBAtom) atom2;
                    // Must be in the same chain
                    if (pdbAtom1.getChainIdentifier() != pdbAtom2.getChainIdentifier()) continue;
                    // Must be in the same alternate location group
                    if (pdbAtom1.getAlternateLocationIndicator() != pdbAtom2.getAlternateLocationIndicator()) continue;
                }
                
                // bonds.add(new Bond(atom1, atom2));
                atom1.addBond(atom2);
                atom2.addBond(atom1);
            }
        }
    }
}
