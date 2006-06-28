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
 * Created on Jun 28, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.cartoon;

import java.awt.Color;
import org.simtk.molecularstructure.nucleicacid.*;

public class SequencingNucleotideColorScheme implements ColorScheme {
    private static Color adenylateColor = new Color(200, 255, 200); // green
    private static Color guanylateColor = new Color(255, 255, 200); // yellow or black
    private static Color cytidylateColor = new Color(150, 220, 255); // blue or cyan
    private static Color thymidylateColor = new Color(255, 200, 220); // red
    private static Color uridylateColor = new Color(255, 200, 220); // red
    
    public static SequencingNucleotideColorScheme
        SEQUENCING_NUCLEOTIDE_COLOR_SCHEME = new SequencingNucleotideColorScheme();
    
    public Color colorOf(Object colorable) throws UnknownObjectColorException {
        if (! (colorable instanceof Nucleotide))
            throw new UnknownObjectColorException();

        if (colorable instanceof Adenylate) 
            return adenylateColor;
        else if (colorable instanceof Guanylate) 
            return guanylateColor;
        else if (colorable instanceof Cytidylate) 
            return cytidylateColor;
        else if (colorable instanceof Thymidylate) 
            return thymidylateColor;
        else if (colorable instanceof Uridylate) 
            return uridylateColor;

        else throw new UnknownObjectColorException();
    }

}
