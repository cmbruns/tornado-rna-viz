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
 * Created on Dec 9, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure.protein;

import org.simtk.molecularstructure.*;

public class Helix extends SecondaryStructureClass {

    public static HelixType ALPHA = HelixType.RIGHT_HANDED_ALPHA;
    public static HelixType RIGHT_HANDED_ALPHA = HelixType.RIGHT_HANDED_ALPHA;
    public static HelixType RIGHT_HANDED_OMEGA = HelixType.RIGHT_HANDED_OMEGA;
    public static HelixType RIGHT_HANDED_PI = HelixType.RIGHT_HANDED_PI;
    public static HelixType RIGHT_HANDED_GAMMA = HelixType.RIGHT_HANDED_GAMMA;
    public static HelixType RIGHT_HANDED_310 = HelixType.RIGHT_HANDED_310;
    public static HelixType LEFT_HANDED_ALPHA = HelixType.LEFT_HANDED_ALPHA;
    public static HelixType LEFT_HANDED_OMEGA = HelixType.LEFT_HANDED_OMEGA;
    public static HelixType LEFT_HANDED_GAMMA = HelixType.LEFT_HANDED_GAMMA;
    public static HelixType RIBBON_HELIX_27 = HelixType.RIBBON_HELIX_27;
    public static HelixType POLYPROLINE = HelixType.POLYPROLINE;

    // private List residues = new Vector();
    private HelixType helixType = ALPHA;
    
    public void setHelixType(HelixType helixType) {
        this.helixType = helixType;
    }
    
    public HelixType getHelixType() {return this.helixType;}
}

class HelixType {
    static HelixType RIGHT_HANDED_ALPHA = new HelixType();
    static HelixType RIGHT_HANDED_OMEGA = new HelixType();
    static HelixType RIGHT_HANDED_PI = new HelixType();
    static HelixType RIGHT_HANDED_GAMMA = new HelixType();
    static HelixType RIGHT_HANDED_310 = new HelixType();
    static HelixType LEFT_HANDED_ALPHA = new HelixType();
    static HelixType LEFT_HANDED_OMEGA = new HelixType();
    static HelixType LEFT_HANDED_GAMMA = new HelixType();
    static HelixType RIBBON_HELIX_27 = new HelixType();
    static HelixType POLYPROLINE = new HelixType();

    private HelixType() {}
}
