package org.simtk.moleculargraphics.cartoon;
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

import java.awt.Color;
import java.util.*;

/*
 * Created on Jul 20, 2006
 * Original author: Christopher Bruns
 */

/**
 * Color scheme that blends a particular highlight color with that of
 * another color scheme.
 */
public class HighlightColorScheme implements ColorScheme {
    ColorScheme baseScheme;
    Color highlightColor;
    Map<Color, Color> storedColors = new HashMap<Color, Color>();
    double highlightProportion = 0.50;

    public HighlightColorScheme(Color highlightColor, ColorScheme baseScheme) {
        this.baseScheme = baseScheme;
        this.highlightColor = highlightColor;
    }

    public Color colorOf(Object colorable) throws UnknownObjectColorException {
        Color base = baseScheme.colorOf(colorable);
        if (storedColors.containsKey(base)) return storedColors.get(base);

        int red = (int)(
            highlightColor.getRed() * highlightProportion + 
            base.getRed() * (1.0 - highlightProportion));
        int green = (int)(
                highlightColor.getGreen() * highlightProportion + 
                base.getGreen() * (1.0 - highlightProportion));
        int blue = (int)(
                highlightColor.getBlue() * highlightProportion + 
                base.getBlue() * (1.0 - highlightProportion));
        
        Color answer = new Color(red, green, blue);
        storedColors.put(base, answer);

        return answer;
    }

}
