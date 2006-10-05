/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Jul 26, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure;

import java.util.*;

import org.simtk.molecularstructure.nucleicacid.Adenylate;
import org.simtk.molecularstructure.nucleicacid.Cytidylate;
import org.simtk.molecularstructure.nucleicacid.Guanylate;
import org.simtk.molecularstructure.nucleicacid.Inositate;
import org.simtk.molecularstructure.nucleicacid.Thymidylate;
import org.simtk.molecularstructure.nucleicacid.Uridylate;
import org.simtk.molecularstructure.protein.Alanine;
import org.simtk.molecularstructure.protein.Arginine;
import org.simtk.molecularstructure.protein.Asparagine;
import org.simtk.molecularstructure.protein.Aspartate;
import org.simtk.molecularstructure.protein.Cysteine;
import org.simtk.molecularstructure.protein.Glutamate;
import org.simtk.molecularstructure.protein.Glutamine;
import org.simtk.molecularstructure.protein.Glycine;
import org.simtk.molecularstructure.protein.Histidine;
import org.simtk.molecularstructure.protein.Isoleucine;
import org.simtk.molecularstructure.protein.Leucine;
import org.simtk.molecularstructure.protein.Lysine;
import org.simtk.molecularstructure.protein.Methionine;
import org.simtk.molecularstructure.protein.Phenylalanine;
import org.simtk.molecularstructure.protein.Proline;
import org.simtk.molecularstructure.protein.Serine;
import org.simtk.molecularstructure.protein.Threonine;
import org.simtk.molecularstructure.protein.Tryptophan;
import org.simtk.molecularstructure.protein.Tyrosine;
import org.simtk.molecularstructure.protein.Valine;

public class ResidueTypeClass implements ResidueType {
    public static ResidueType UNKNOWN_RESIDUE_TYPE = new ResidueTypeClass('?', "UNK", "(unknown residue type)");

    /**
     * Stores key in upper case
     */
    private static Map<String, ResidueType> residueTypes = new HashMap<String, ResidueType>();
    
    public static ResidueType getType(String name) {
        String residueName = name.trim().toUpperCase();
        
        if (residueName.equals("UNK")) return UNKNOWN_RESIDUE_TYPE;

        if (residueName.equals("A")) return new Adenylate();
        if (residueName.equals("C")) return new Cytidylate();
        if (residueName.equals("G")) return new Guanylate();
        if (residueName.equals("I")) return new Inositate();
        if (residueName.equals("T")) return new Thymidylate();
        if (residueName.equals("U")) return new Uridylate();

        // Modified version of nucleotides
        if (ResidueClass.modifiedAdenylates.contains(residueName)) return new Adenylate();
        if (ResidueClass.modifiedCytidylates.contains(residueName)) return new Cytidylate();
        if (ResidueClass.modifiedGuanylates.contains(residueName)) return new Guanylate();
        if (ResidueClass.modifiedInositates.contains(residueName)) return new Inositate();
        if (ResidueClass.modifiedThymidylates.contains(residueName)) return new Thymidylate();
        if (ResidueClass.modifiedUridylates.contains(residueName)) return new Uridylate();

        
        // Amino Acids
        if (residueName.equals("ALA")) {return new Alanine();}
        if (residueName.equals("CYS")) {return new Cysteine();}
        if (residueName.equals("ASP")) {return new Aspartate();}
        if (residueName.equals("GLU")) {return new Glutamate();}
        if (residueName.equals("PHE")) {return new Phenylalanine();}
        if (residueName.equals("GLY")) {return new Glycine();}
        if (residueName.equals("HIS")) {return new Histidine();}
        if (residueName.equals("ILE")) {return new Isoleucine();}
        if (residueName.equals("LYS")) {return new Lysine();}
        if (residueName.equals("LEU")) {return new Leucine();}
        if (residueName.equals("MET")) {return new Methionine();}
        if (residueName.equals("ASN")) {return new Asparagine();}
        if (residueName.equals("PRO")) {return new Proline();}
        if (residueName.equals("GLN")) {return new Glutamine();}
        if (residueName.equals("ARG")) {return new Arginine();}
        if (residueName.equals("SER")) {return new Serine();}
        if (residueName.equals("THR")) {return new Threonine();}
        if (residueName.equals("VAL")) {return new Valine();}
        if (residueName.equals("TRP")) {return new Tryptophan();}
        if (residueName.equals("TYR")) {return new Tyrosine();}

        // Store other residue types as needed
        if (residueTypes.containsKey(residueName)) return residueTypes.get(residueName);
        else {
            char olc = '?';
            if (residueName.length() == 1) olc = residueName.charAt(0);
            residueTypes.put(residueName, new ResidueTypeClass(olc, residueName, residueName));
            return residueTypes.get(residueName);
        }
        
    }
    
    protected Map<String, Set<String>> genericBonds = new HashMap<String, Set<String>>();
    protected char oneLetterCode = '?';
    protected String threeLetterCode = "UNK";
    protected String residueName = "(unknown residue type)";
    
    public ResidueTypeClass(char olc, String tlc, String name) {
        this.oneLetterCode = olc;
        this.threeLetterCode = tlc;
        this.residueName = name;
        
        addGenericBonds();
    }

    public Set<String> getHydrogenBondDonorAtomNames() {
        return new HashSet<String>();        
    }
    
    public Set<String> getHydrogenBondAcceptorAtomNames() {
        return new HashSet<String>();
    };
    
    public final char getOneLetterCode() {
        return oneLetterCode;
    }

    public final String getResidueName() {
        return residueName;
    }
    
    public final String getThreeLetterCode() {
        return threeLetterCode;
    }

    public Map<String, Set<String>> genericBonds() {
        return genericBonds;
    }

    protected void addGenericBond(String atom1, String atom2) {
        if (! (genericBonds().containsKey(atom1)))
            genericBonds().put(atom1, new HashSet<String>());
        genericBonds().get(atom1).add(atom2);

        if (! (genericBonds().containsKey(atom2)))
            genericBonds().put(atom2, new HashSet<String>());
        genericBonds().get(atom2).add(atom1);
    }
    
    protected void addGenericBonds() {}
}
