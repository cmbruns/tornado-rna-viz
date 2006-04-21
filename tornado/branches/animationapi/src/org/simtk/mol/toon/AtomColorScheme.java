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
package org.simtk.mol.toon;

import java.awt.*;

import org.simtk.molecularstructure.atom.*;

public class AtomColorScheme implements ColorScheme {
    private Color lightGray = new Color(230, 230, 230);
    private Color paleBlue = new Color(140, 150, 255);
    private Color softRed = new Color(255, 65, 70);
    private Color warmWhite = new Color(220, 210, 200);
    private Color deepPurple = new Color(180, 0, 255);
    private Color warmYellow = new Color(255, 180, 20);
    
    private Color defaultColor = Color.white;
    private Color defaultAtomColor = Color.pink;

    public Color color(Object o) {
        Color answer = defaultColor;

        if (o instanceof Atom) {
            answer = defaultAtomColor;

            Atom atom = (Atom) o;
            String e = atom.getElementSymbol();

            if (e.equals("C")) answer = lightGray;
            else if (e.equals("H")) answer = Color.white;
            else if (e.equals("N")) answer = paleBlue;
            else if (e.equals("O")) answer = softRed;
            else if (e.equals("P")) answer = warmYellow;
            else if (e.equals("S")) answer = Color.yellow;
            else if (e.equals("Cl")) answer = Color.green;
            else if (e.equals("Ca")) answer = warmWhite;
            else if (e.equals("Cu")) answer = Color.orange;
            else if (e.equals("Zn")) answer = Color.gray;
            else if (e.equals("I")) answer = deepPurple;

        }
        return answer;
    }
}
