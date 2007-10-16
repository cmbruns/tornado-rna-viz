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
 * Created on Apr 20, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.color;

import java.awt.*;

import org.simtk.molecularstructure.*;
import org.simtk.molecularstructure.protein.*;
import org.simtk.molecularstructure.nucleicacid.*;

public class MoleculeColorScheme implements ColorScheme {
    private static Color paleAqua = new Color(200,255,255);
    private static Color paleBlue = new Color(200,230,255);
    private static Color paleOrangeYellow = new Color(255,240,200);
    private static Color palePink = new Color(255,225,200);
    
    static MoleculeColorScheme MOLECULE_COLORS = new MoleculeColorScheme();
    
    public Color colorOf(Object o) throws UnknownObjectColorException {
        if (o instanceof Protein)
            return paleAqua;
        else if (o instanceof DNA)
            return palePink;
        else if (o instanceof NucleicAcid)
            return palePink;
        else if (o instanceof Molecule)
            return Color.white;
        else
            throw new UnknownObjectColorException();
    }    
}
