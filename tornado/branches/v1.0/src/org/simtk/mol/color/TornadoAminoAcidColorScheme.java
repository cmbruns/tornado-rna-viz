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
 * Created on Nov 8, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.color;

import java.awt.Color;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.protein.*;

/**
 * 
  * @author Christopher Bruns
  * 
  * Color scheme for base pairs in secondary structure diagrams
  * as shown in publications from the Block laboratory, such
  * as Woodside et al (2006) PNAS 103(16) 6190-6195.
  * 
  * AT base pairs are red, GC pairs are blue
  * A and G are darker and more saturated
 */
public class TornadoAminoAcidColorScheme implements ColorScheme {
    private static final Color paleGrey = new Color(200, 200, 200);
    private static final Color medGrey = new Color(160, 160, 160);
    private static final Color greenGrey = new Color(160, 180, 160);
    private static final Color darkGrey = new Color(120, 120, 120);
    private static final Color purpleGrey = new Color(150,120,150);
    
    private static final Color blue = new Color(110, 120, 255);
    private static final Color softBlue = new Color(170, 180, 255);
    private static final Color red = new Color(255, 70, 100);
    private static final Color softMagenta = new Color(240, 130, 255);
    private static final Color yellow = Color.yellow;
    private static final Color softYellow = new Color(180, 180, 100);
    
    public static final TornadoAminoAcidColorScheme SCHEME = new TornadoAminoAcidColorScheme();
    
    /**
     * Takes either Residue or ResidueType as an argument
     */
    public Color colorOf(Object colorable) throws UnknownObjectColorException {
        ResidueType residueType = null;
        if (colorable instanceof Residue)
            residueType = ((Residue)colorable).getResidueType();
        else if (colorable instanceof ResidueType)
            residueType = (ResidueType) colorable;
        
        if (residueType == null)
            throw new UnknownObjectColorException();
        
        if (! (residueType instanceof AminoAcid)) 
            throw new UnknownObjectColorException();
        
        AminoAcid aminoAcid = (AminoAcid) residueType;

        // Small non-polar
        if (aminoAcid instanceof Glycine) return paleGrey;
        if (aminoAcid instanceof Alanine) return paleGrey;
        
        // Aliphatic
        if (aminoAcid instanceof Valine) return medGrey;
        if (aminoAcid instanceof Leucine) return medGrey;
        if (aminoAcid instanceof Isoleucine) return medGrey;
        if (aminoAcid instanceof Proline) return greenGrey;
        
        // Aromatic
        if (aminoAcid instanceof Phenylalanine) return darkGrey;
        if (aminoAcid instanceof Tyrosine) return purpleGrey;
        if (aminoAcid instanceof Tryptophan) return purpleGrey;
           
        // so-called basic
        if (aminoAcid instanceof Lysine) return blue;
        if (aminoAcid instanceof Arginine) return blue;
        if (aminoAcid instanceof Histidine) return softBlue;
        
        // so-called acidic
        if (aminoAcid instanceof Aspartate) return red;        
        if (aminoAcid instanceof Glutamate) return red;
        
        // polar
        if (aminoAcid instanceof Asparagine) return softMagenta;
        if (aminoAcid instanceof Glutamine) return softMagenta;
        if (aminoAcid instanceof Threonine) return softMagenta;
        if (aminoAcid instanceof Serine) return softMagenta;
        
        // sulfur
        if (aminoAcid instanceof Cysteine) return yellow;
        if (aminoAcid instanceof Methionine) return softYellow;
        
        throw new UnknownObjectColorException();
    }
}
