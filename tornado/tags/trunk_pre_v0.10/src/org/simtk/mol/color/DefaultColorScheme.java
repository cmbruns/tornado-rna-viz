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
 * Created on Jul 11, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.mol.color;

import java.awt.Color;

import org.simtk.molecularstructure.atom.Atom;
import org.simtk.molecularstructure.*;

public class DefaultColorScheme implements ColorScheme {
    public static ColorScheme DEFAULT_COLOR_SCHEME = new DefaultColorScheme();
    
    private ColorScheme defaultColorScheme = new ConstantColor(Color.white);
    
    public Color colorOf(Object colorable) throws UnknownObjectColorException {
        
        if (colorable instanceof Atom) try {
            return AtomColorScheme.PALE_CPK_COLORS.colorOf(colorable);
        } catch (UnknownObjectColorException exc) {}
        
        if (colorable instanceof Residue) try {
            return ResidueColorScheme.SCHEME.colorOf(colorable);
            // return SequencingNucleotideColorScheme.SEQUENCING_NUCLEOTIDE_COLOR_SCHEME.colorOf(colorable);
        } catch (UnknownObjectColorException exc) {}
        
        if (colorable instanceof Molecule) try {
            return MoleculeColorScheme.MOLECULE_COLORS.colorOf(colorable);            
        } catch (UnknownObjectColorException exc) {}
        
        return defaultColorScheme.colorOf(colorable);
    }

}
