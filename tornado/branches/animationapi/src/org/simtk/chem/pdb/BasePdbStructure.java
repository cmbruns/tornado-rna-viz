/* Copyright (c) 2005 Stanford University and Christopher Bruns
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Apr 28, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.chem.pdb;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.text.ParseException;

import org.simtk.chem.BetaStrand;
import org.simtk.chem.Molecule;
import org.simtk.chem.ProteinHelix;
import org.simtk.chem.ProteinHelixType;
import org.simtk.chem.pdb.*;

public class BasePdbStructure implements PdbStructure {

    private String pdbId = null;
    private String pdbTitle = "";
    private URL inputUrl = null;
    private Collection<Molecule> molecules = new LinkedHashSet<Molecule>();
    private Map<Character, PdbMolecule> chains = new LinkedHashMap<Character, PdbMolecule>();

    public String getPdbTitle() {return pdbTitle;}
    protected void setPdbTitle(String pdbTitle) {this.pdbTitle = pdbTitle;}
    
    public String getPdbId() {return pdbId;}
    protected void setPdbId(String pdbId) {this.pdbId = pdbId;}

    public URL getStructureSource() {return inputUrl;}
    
    protected BasePdbStructure() {} // Hide the constructor
    
    public static PdbStructure createPdbStructureFromFile(String fileName)
            throws FileNotFoundException, IOException, InterruptedException {
        PdbStructure answer;        
        answer = createPdbStructure((new File(fileName)).toURL());        
        return answer;
    }

    public static PdbStructure createPdbStructure(URL url) throws IOException, InterruptedException {
        BasePdbStructure answer;
        
        InputStream inStream = url.openStream();
        answer = createPdbStructure(inStream);
        inStream.close();
        
        answer.inputUrl = url;
        return answer;
    }

    public static BasePdbStructure createPdbStructure(InputStream is) throws IOException, InterruptedException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
        return createPdbStructure(reader);
    }

    public static BasePdbStructure createPdbStructure(LineNumberReader reader) throws IOException, InterruptedException {
        BasePdbStructure pdbStructure = new BasePdbStructure();
        
        // TODO read header information, specfically the name(s) of the molecules
        String pdbLine;
        
        reader.mark(200);
        FILE_LINE: while ((pdbLine = reader.readLine()) != null) {

            // Stop parsing after the END record
            if (pdbLine.substring(0,3).equals("END")) {
                reader.reset(); // Leave the END tag for the next guy
                return pdbStructure;
            }

            // If we get to ATOM records, we need to stop and send the stream to the Molecule parser
            else if ((pdbLine.substring(0,6).equals("ATOM  ")) || (pdbLine.substring(0,6).equals("HETATM"))) {

                try {pdbStructure.creativeGetAtom(pdbLine);}
                catch (ParseException exc) {
                    System.err.println("Problem parsing atom line:\npdbLine\nat line number "+reader.getLineNumber()+"\n");
                };
            }

            else if (pdbLine.substring(0,6).equals("TITLE ")) {
                String titlePart = pdbLine.substring(10,60).trim();
                String title = pdbStructure.getPdbTitle();
                if (titlePart.length() > 0) {
                    if (title.length() > 0) title = title + " " + titlePart;
                    else title = titlePart;
                    pdbStructure.setPdbTitle(title);
                }
            }

            else if (pdbLine.substring(0,6).equals("HEADER")) {
                if (pdbLine.length() >= 56) {
                    String id = pdbLine.substring(52,56).trim();
                    if (id.length() == 4) pdbStructure.setPdbId(id);
                }
            }

            // Parse secondary structure
            else if (pdbLine.substring(0,6).equals("HELIX ")) {

                /*
                 * COLUMNS        DATA TYPE       FIELD           DEFINITION
                 * ---------------------------------------------------------------------------------
                 *  1 -  6        Record name     "HELIX "
                 * 
                 *  8 - 10        Integer         serNum          Serial number of the helix.
                 *                                                This starts at 1 and increases
                 *                                                incrementally.
                 * 
                 * 12 - 14        LString(3)      helixID         Helix identifier.  In addition
                 *                                                to a serial number, each helix is
                 *                                                given an alphanumeric character
                 *                                                helix identifier.
                 * 
                 * 16 - 18        Residue name    initResName     Name of the initial residue.
                 * 
                 * 20             Character       initChainID     Chain identifier for the chain
                 *                                                containing this helix.
                 * 
                 * 22 - 25        Integer         initSeqNum      Sequence number of the initial
                 *                                                residue.
                 * 
                 * 26             AChar           initICode       Insertion code of the initial
                 *                                                residue.
                 * 
                 * 28 - 30        Residue name    endResName      Name of the terminal residue of
                 *                                                the helix.
                 * 
                 * 32             Character       endChainID      Chain identifier for the chain
                 *                                                containing this helix.
                 * 
                 * 34 - 37        Integer         endSeqNum       Sequence number of the terminal
                 *                                                residue.
                 * 
                 * 38             AChar           endICode        Insertion code of the terminal
                 *                                                residue.
                 * 
                 * 39 - 40        Integer         helixClass           Helix class (see below).
                 * 
                 * 41 - 70        String          comment         Comment about this helix.
                 * 
                 * 72 - 76        Integer         length          Length of this helix.
                 */
                
                /*
                 *            TYPE OF HELIX             CLASS NUMBER (COLUMNS 39 - 40)
                 *     --------------------------------------------------------------
                 *     Right-handed alpha (default)                1
                 *     Right-handed omega                          2
                 *     Right-handed pi                             3
                 *     Right-handed gamma                          4
                 *     Right-handed 310                            5
                 *     Left-handed alpha                           6
                 *     Left-handed omega                           7
                 *     Left-handed gamma                           8
                 *     27 ribbon/helix                             9
                 *     Polyproline                                10
                 */

                // String helixId = pdbLine.substring(11, 14);

                String initResidueName = pdbLine.substring(15, 18);
                char initResidueChain = pdbLine.charAt(19);
                int initResidueNumber = new Integer(pdbLine.substring(21, 25).trim());
                char initICode = pdbLine.charAt(25);
                PdbResidue initRes = pdbStructure.creativeGetResidue(initResidueName, initResidueChain, initResidueNumber, initICode);
                
                String endResidueName = pdbLine.substring(27, 30);
                char endResidueChain = pdbLine.charAt(31);
                int endResidueNumber = new Integer(pdbLine.substring(33, 37).trim());
                char endICode = pdbLine.charAt(37);
                PdbResidue endRes = pdbStructure.creativeGetResidue(endResidueName, endResidueChain, endResidueNumber, endICode);
                
                ProteinHelixType helixType;

                int helixTypeIndex = new Integer(pdbLine.substring(38, 40).trim());
                switch(helixTypeIndex) {
                case 1:
                    helixType = ProteinHelix.ALPHA; break;
                case 2:
                    helixType = ProteinHelix.RIGHT_HANDED_OMEGA; break;
                case 3:
                    helixType = ProteinHelix.RIGHT_HANDED_PI; break;
                case 4:
                    helixType = ProteinHelix.RIGHT_HANDED_GAMMA; break;
                case 5:
                    helixType = ProteinHelix.RIGHT_HANDED_310; break;
                case 6:
                    helixType = ProteinHelix.LEFT_HANDED_ALPHA; break;
                case 7:
                    helixType = ProteinHelix.LEFT_HANDED_OMEGA; break;
                case 8:
                    helixType = ProteinHelix.LEFT_HANDED_GAMMA; break;
                case 9:
                    helixType = ProteinHelix.RIBBON_HELIX_27; break;
                case 10:
                    helixType = ProteinHelix.POLYPROLINE; break;
                default:
                    helixType = ProteinHelix.ALPHA; break;                    
                }
                
                ProteinHelix helix = new ProteinHelix(initRes, endRes, helixType);
                
                // System.out.println("HELIX record found");
                
                PdbMolecule molecule = pdbStructure.creativeGetChain(initResidueChain);
                molecule.addSecondaryStructure(helix);
            }
            else if (pdbLine.substring(0,6).equals("SHEET ")) {

                /*
                 * COLUMNS        DATA TYPE       FIELD           DEFINITION
                 *----------------------------------------------------------------------------------
                 * 1 -  6        Record name     "SHEET "
                 *
                 * 8 - 10        Integer         strand          Strand number which starts at 1 for
                 *                                                each strand within a sheet and
                 *                                                increases by one.
                 * 
                 * 12 - 14        LString(3)      sheetID         Sheet identifier.
                 *
                 * 15 - 16        Integer         numStrands      Number of strands in sheet.
                 *
                 * 18 - 20        Residue name    initResName     Residue name of initial residue.
                 *
                 * 22             Character       initChainID     Chain identifier of initial residue
                 *                                                in strand.
                 * 
                 * 23 - 26        Integer         initSeqNum      Sequence number of initial residue
                 *                                                in strand.
                 * 
                 * 27             AChar           initICode       Insertion code of initial residue
                 *                                                in strand.
                 * 
                 * 29 - 31        Residue name    endResName      Residue name of terminal residue.
                 * 
                 * 33             Character       endChainID      Chain identifier of terminal
                 *                                                residue.
                 * 
                 * 34 - 37        Integer         endSeqNum       Sequence number of terminal residue.
                 * 
                 * 38             AChar           endICode        Insertion code of terminal residue.
                 * 
                 * 39 - 40        Integer         sense           Sense of strand with respect to
                 *                                                previous strand in the sheet. 0
                 *                                                if first strand, 1 if parallel,
                 *                                                -1 if anti-parallel.
                 * 
                 * 42 - 45        Atom            curAtom         Registration. Atom name in current
                 *                                                strand.
                 * 
                 * 46 - 48        Residue name    curResName      Registration. Residue name in
                 *                                                current strand.
                 * 
                 * 50             Character       curChainId      Registration. Chain identifier in
                 *                                                current strand.
                 * 
                 * 51 - 54        Integer         curResSeq       Registration. Residue sequence
                 *                                                number in current strand.
                 * 
                 * 55             AChar           curICode        Registration. Insertion code in
                 *                                                current strand.
                 * 
                 * 57 - 60        Atom            prevAtom        Registration. Atom name in
                 *                                                previous strand.
                 * 
                 * 61 - 63        Residue name    prevResName     Registration. Residue name in
                 *                                                previous strand.
                 * 
                 * 65             Character       prevChainId     Registration. Chain identifier in
                 *                                                previous strand.
                 * 
                 * 66 - 69        Integer         prevResSeq      Registration. Residue sequence
                 *                                                number in previous strand.
                 * 
                 * 70             AChar           prevICode       Registration. Insertion code in
                 *                                                previous strand.
                 */
                
                String initResidueName = pdbLine.substring(17, 20);
                char initResidueChain = pdbLine.charAt(21);
                int initResidueNumber = new Integer(pdbLine.substring(22, 26).trim());
                char initICode = pdbLine.charAt(26);
                PdbResidue initRes = pdbStructure.creativeGetResidue(initResidueName, initResidueChain, initResidueNumber, initICode);
                
                String endResidueName = pdbLine.substring(28, 31);
                char endResidueChain = pdbLine.charAt(32);
                int endResidueNumber = new Integer(pdbLine.substring(33, 37).trim());
                char endICode = pdbLine.charAt(37);
                PdbResidue endRes = pdbStructure.creativeGetResidue(endResidueName, endResidueChain, endResidueNumber, endICode);

                BetaStrand strand = new BetaStrand(initRes, endRes);
                PdbMolecule molecule = pdbStructure.creativeGetChain(initResidueChain);
                molecule.addSecondaryStructure(strand);
            }
            
            reader.mark(200); // Commit to reading this far into the file
        }
        
        return pdbStructure;        
    }

    // If necessary, instantiate chain, molecule, residue, atom
    // All of these "creativeGet" methods return the preexisting structure if it already exists
    // or the new one if it does not.
    public PdbAtom getAtom(String pdbLine) throws ParseException {
        // Look in the correct chain
        char chainId = BasePdbAtom.getChainId(pdbLine);
        PdbMolecule pdbChain = getChain(chainId);
        if (pdbChain == null) return null;
        else return pdbChain.getAtom(pdbLine);
    }
    public PdbAtom creativeGetAtom(String pdbLine) throws ParseException {
        // Look in the correct chain
        char chainId = BasePdbAtom.getChainId(pdbLine);
        PdbMolecule pdbChain = creativeGetChain(chainId);
        return pdbChain.creativeGetAtom(pdbLine);
    }
    
    public PdbResidue getResidue(String resName, char chainId, int resNum, char iCode) {
        PdbMolecule pdbChain = getChain(chainId);
        if (pdbChain == null) return null;
        else return pdbChain.getResidue(resName, resNum, iCode);        
    }
    public PdbResidue creativeGetResidue(String resName, char chainId, int resNum, char iCode) {
        PdbMolecule pdbChain = creativeGetChain(chainId);
        return pdbChain.creativeGetResidue(resName, resNum, iCode);
    }
    
    public PdbMolecule getChain(char chainId) {
        return chains.get(chainId);
    }
    public PdbMolecule creativeGetChain(char chainId) {
        PdbMolecule pdbChain = getChain(chainId);
        if (pdbChain == null) {
            pdbChain = BasePdbMolecule.createPdbMolecule(chainId);
            chains.put(chainId, pdbChain);
            molecules.add(pdbChain); 
        }
        return pdbChain;
    }
    
    // Collection interface
    public boolean     add(Molecule o) {return molecules.add(o);}
    public boolean     addAll(Collection<? extends Molecule> c) {return molecules.addAll(c);}
    public void        clear() {molecules.clear();}
    public boolean     remove(Object o) {return molecules.remove(o);}
    public boolean     removeAll(Collection<?> c) {return molecules.removeAll(c);}
    public boolean     retainAll(Collection<?> c) {return molecules.retainAll(c);}

    public boolean     contains(Object o) {return molecules.contains(o);}
    public boolean     containsAll(Collection<?> c) {return molecules.containsAll(c);}
    public boolean     isEmpty() {return molecules.isEmpty();}
    public Iterator<Molecule> iterator() {return molecules.iterator();}
    public int         size() {return molecules.size();}
    public Object[]    toArray() {return molecules.toArray();}
    public <T> T[]     toArray(T[] a) {return molecules.toArray(a);}

}
