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

import java.util.*;

import org.simtk.molecularstructure.atom.Atom;

public class AtomColorScheme implements ColorScheme {
    private static Color lightGray = new Color(200, 200, 200);
    private static Color darkGray = new Color(80, 80, 80);
    private static Color paleBlue = new Color(140, 150, 255);
    private static Color softRed = new Color(255, 65, 70);
    private static Color warmWhite = new Color(220, 210, 200);
    private static Color deepPurple = new Color(180, 0, 255);
    private static Color warmYellow = new Color(255, 180, 20);
    private static Color pastelMagenta = new Color(255,180,230);
    private static Color darkOrange = new Color(200,90,60);
    
    static AtomColorScheme PALE_CPK_COLORS = new AtomColorScheme();
    static AtomColorScheme CPK_COLORS = new AtomColorScheme();
    static {
        PALE_CPK_COLORS.setColor("C", lightGray);
        PALE_CPK_COLORS.setColor("N", paleBlue);
        PALE_CPK_COLORS.setColor("O", softRed);
    }
    
    private Map<Object, Color> objectColors = new HashMap<Object, Color>();
    
    public AtomColorScheme() {setCpkColors();}
    
    public Color colorOf(Object o) throws UnknownObjectColorException {
        String elementSymbol = null;
        if (o instanceof Atom) {
            elementSymbol = ((Atom)o).getElementSymbol();
            if (objectColors.containsKey(elementSymbol)) 
                return objectColors.get(elementSymbol);
        }
        throw new UnknownObjectColorException();
    }
    
    public void setColor(Object o, Color color) {
        objectColors.put(o, color);
    }
    
    protected void setCpkColors() {
        setColor("C", darkGray);
        setColor("H", Color.white);
        setColor("N", Color.blue);
        setColor("O", Color.red);
        setColor("P", warmYellow);
        setColor("S", Color.yellow);
        
        // Metals - try to keep toward warm colors
        setColor("Mg", pastelMagenta);
        setColor("Mn", pastelMagenta); 
        setColor("Ca", warmWhite); // Like bone
        setColor("Cu", Color.orange); // Like a penny
        setColor("Fe", darkOrange); // Like Rust
        setColor("Zn", Color.gray); // Like buff metal

        // Halogens - try to keep toward cool colors
        setColor("Cl", Color.green); // Like poison gass
        setColor("I", deepPurple); // Like iodine crystals
    }
}
