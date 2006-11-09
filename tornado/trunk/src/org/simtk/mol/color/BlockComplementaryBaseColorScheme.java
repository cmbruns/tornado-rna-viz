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
import org.simtk.molecularstructure.nucleicacid.*;

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
public class BlockComplementaryBaseColorScheme implements ColorScheme {
    private static final Color darkRed = new Color(190, 20, 20);
    private static final Color rose = new Color(255, 80, 110);
    private static final Color ceruleanBlue = new Color(30,120,230);
    private static final Color darkBlue = new Color(55, 0, 200);
    
    public static final BlockComplementaryBaseColorScheme SCHEME = new BlockComplementaryBaseColorScheme();
    
    public Color colorOf(Object colorable) throws UnknownObjectColorException {
        if (! (colorable instanceof Residue))
            throw new UnknownObjectColorException();
        
        ResidueType residueType = ((Residue) colorable).getResidueType();

        if (residueType instanceof Adenylate) return darkBlue;
        
        if (residueType instanceof Thymidylate) return ceruleanBlue;
        if (residueType instanceof Uridylate) return ceruleanBlue;
        
        if (residueType instanceof Guanylate) return darkRed;
        
        if (residueType instanceof Cytidylate) return rose;
        
        throw new UnknownObjectColorException();
    }
}
