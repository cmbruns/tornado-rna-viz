/*
 * Created on Apr 21, 2005
 *
 */
package org.simtk.molecularstructure;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.*; // Vector

import org.simtk.atomicstructure.*;
import org.simtk.geometry3d.*;
import org.simtk.molecularstructure.nucleicacid.*;
import org.simtk.molecularstructure.protein.*;

/**
 * @author Christopher Bruns
 *
 * \brief A single molecule structure.
 */
public class Molecule {
	protected Vector<Atom> atoms = new Vector<Atom>();
    // protected Vector<Bond> bonds = new Vector<Bond>();
	// Vector bonds = new Vector();

    Vector3D centerOfMass = new Vector3D();
    double mass = 0;

    public double getMass() {
        return mass;
    }
    public Vector3D getCenterOfMass() {
        if (mass <= 0) return null;
        return centerOfMass;
    }

    public Molecule() {} // Empty molecule
	public Molecule(PDBAtomSet atomSet) {
        for (Atom atom : atomSet) {
            addAtom(atom);
		}
        createBonds();
	}

    /**
     * Change the position of the molecule by the specified amount
     * @param t amount to translate
     */
    public void translate(BaseVector3D t) {
        for (Atom a : getAtoms()) {
            a.translate(t);
        }
    }
    
    public Vector<Atom> getAtoms() {return atoms;}
    
    /**
     * Place all atomic coordinates into a single large array of float values,
     * using the Vector3DWrapper class to redefine the coordinates
     * @return
     */
    public float[] packCoordinatesIntoFloatArray() {
        int atomCount = getAtomCount();
        float[] coordinateArray = new float[3 * atomCount];

        // TODO
        int arrayIndex = 0;
        for (Atom atom : getAtoms()) {
            for (Double coord : atom.getCoordinates()) {
                coordinateArray[arrayIndex] = coord.floatValue();
                arrayIndex ++;
            }
            atom.setCoordinates(new Vector3DFloatArrayWrapper(coordinateArray, arrayIndex - 3));
        }
        
        return coordinateArray;
    }
    
    public Plane3D bestPlane3D() {
        BaseVector3D[] coordinates = new Vector3D[getAtomCount()];
        double[] masses = new double[getAtomCount()];
        for (int a = 0; a < getAtomCount(); a++) {
            Atom atom = atoms.get(a);
            coordinates[a] = atom.getCoordinates();
            masses[a] = atom.getMass();
        }
        return Plane3D.bestPlane3D(coordinates, masses);
    }
    
    public void addAtom(Atom atom) {
        atoms.add(atom);
        mass += atom.getMass();
        double massRatio = atom.getMass() / mass;
        centerOfMass = centerOfMass.scale(1.0 - massRatio).plus(atom.getCoordinates().scale(massRatio));
    }
    
	public int getAtomCount() {return atoms.size();}
	public Atom getAtom(int i) {return (Atom) atoms.get(i);}
	
    public static Molecule createFactoryPDBMolecule(URL url) throws IOException {
        InputStream inStream = url.openStream();
        Molecule molecule = createFactoryPDBMolecule(inStream);        
        inStream.close();
        return molecule;
    }

    public static Molecule createFactoryPDBMolecule(String fileName) throws IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
		Molecule molecule = createFactoryPDBMolecule(fileStream);
        fileStream.close();
        return molecule;
    }

    static Molecule createFactoryPDBMolecule(InputStream is) throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
		return createFactoryPDBMolecule(reader);
    }

    /**
     * Read one molecule from a PDB format stream.  Stop before the first atom of the next molecule.
     * @param reader
     * @return
     * @throws IOException
     */
    public static Molecule createFactoryPDBMolecule(LineNumberReader reader) throws IOException {
        PDBAtomSet currentMoleculeAtoms = new PDBAtomSet();
        
        char chainIdentifier = '\0';
        String residueName = null;
        char insertionCode = '\0';
        int residueIndex = -1;
        
		String PDBLine;
		reader.mark(200);
		FILE_LINE: while ((PDBLine = reader.readLine()) != null) {
			// Stop parsing after the END record
			if (PDBLine.substring(0,3).equals("END")) {
			    reader.reset(); // Leave the END tag for the next guy
                Molecule molecule = Molecule.createFactoryPDBMolecule(currentMoleculeAtoms); // empty molecule
				return molecule;
			}

			// Lines with atomic coordinates are used to create new atoms
			else if ((PDBLine.substring(0,6).equals("ATOM  ")) || (PDBLine.substring(0,6).equals("HETATM"))) {
				PDBAtom atom = PDBAtom.createFactoryPDBAtom(PDBLine);
				try {
					atom.readPDBLine(PDBLine);
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

					residueName = atom.getResidueName();
					insertionCode = atom.getInsertionCode();
					chainIdentifier = atom.getChainIdentifier();
					residueIndex = atom.getResidueIndex();
				}
				else { // not the first atom

				    boolean isSameResidue = ( (atom.getResidueIndex() == residueIndex)
				       &&(atom.getInsertionCode() == insertionCode) );
				    boolean isSameChain = ( atom.getChainIdentifier() == chainIdentifier );
				    boolean isSolvent = Residue.isSolvent(atom.getResidueName());

				    if ( isSameChain && (isSameResidue || !isSolvent) ) {
					    // Still the same molecule
						currentMoleculeAtoms.addElement(atom);
				    }
				    else { // Not the same molecule - return
				        reader.reset(); // Put latest atom back into the stream
				        Molecule molecule = Molecule.createFactoryPDBMolecule(currentMoleculeAtoms);
                        return molecule;
				    }
				}
			} // ATOM or HETATM record
			
			reader.mark(200); // Commit to reading this far into the file
		}
        return Molecule.createFactoryPDBMolecule(currentMoleculeAtoms);
    }

    static Molecule createFactoryPDBMolecule(PDBAtomSet bagOfAtoms) {
        if (bagOfAtoms == null) return null;
        if (bagOfAtoms.size() == 0) return null;
        
        // Determine if molecule is protein, nucleic acid, biopolymer, or other
        HashSet<String> residues = new HashSet<String>();
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
            if (atom.getAtomName().trim().equals("O2*")) atomO2Count ++;
            if (atom.getAtomName().trim().equals("C2*")) atomC2Count ++;            
            
			String residueKey = "" + atom.getChainIdentifier() + atom.getResidueIndex() + atom.getInsertionCode();
			if (residues.contains(residueKey)) continue; // Already saw this residue before
			
			if (Residue.isSolvent(atom.getResidueName())) solventCount ++;
			if (Residue.isProtein(atom.getResidueName())) proteinCount ++;
			if (Residue.isNucleicAcid(atom.getResidueName())) nucleicCount ++;
			if (Residue.isDNA(atom.getResidueName())) DNACount ++;
			if (Residue.isRNA(atom.getResidueName())) RNACount ++;
			
			residues.add(residueKey);
			residueCount ++;
        }

        if (residueCount == 1)
            return Residue.createFactoryResidue(bagOfAtoms);
        
        // If there are protein residues, this is a protein
        if ((proteinCount >= 1) && (proteinCount >= nucleicCount)) {
            // This is a protein molecule
            return new Protein(bagOfAtoms);
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
            return new Biopolymer(bagOfAtoms);
        }
        
        // OK, it's some other kind of molecule
        return new Molecule(bagOfAtoms);
    }

    // Create covalent bonds where it seems that they are needed
	void createBonds() {

        // Maybe iodine has the largest "ordinary" covalent radius of 1.33
        double maxCovalentRadius = 1.40;

        // Create a hash for rapid access
        Hash3D<Atom> atomHash = new Hash3D<Atom>(maxCovalentRadius);
        for (Atom atom : atoms)
            atomHash.put(atom.getCoordinates(), atom);
        for (Atom atom1 : atoms) {
            double cutoffDistance = (atom1.getCovalentRadius() + maxCovalentRadius) * 1.5;
            for (Atom atom2 : atomHash.neighborValues(atom1.getCoordinates(), cutoffDistance)) {
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
