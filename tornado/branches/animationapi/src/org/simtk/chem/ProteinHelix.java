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
package org.simtk.chem;

public class ProteinHelix extends RangeSecondaryStructure {

    public static ProteinHelixType ALPHA = ProteinHelixType.RIGHT_HANDED_ALPHA;
    public static ProteinHelixType RIGHT_HANDED_ALPHA = ProteinHelixType.RIGHT_HANDED_ALPHA;
    public static ProteinHelixType RIGHT_HANDED_OMEGA = ProteinHelixType.RIGHT_HANDED_OMEGA;
    public static ProteinHelixType RIGHT_HANDED_PI = ProteinHelixType.RIGHT_HANDED_PI;
    public static ProteinHelixType RIGHT_HANDED_GAMMA = ProteinHelixType.RIGHT_HANDED_GAMMA;
    public static ProteinHelixType RIGHT_HANDED_310 = ProteinHelixType.RIGHT_HANDED_310;
    public static ProteinHelixType LEFT_HANDED_ALPHA = ProteinHelixType.LEFT_HANDED_ALPHA;
    public static ProteinHelixType LEFT_HANDED_OMEGA = ProteinHelixType.LEFT_HANDED_OMEGA;
    public static ProteinHelixType LEFT_HANDED_GAMMA = ProteinHelixType.LEFT_HANDED_GAMMA;
    public static ProteinHelixType RIBBON_HELIX_27 = ProteinHelixType.RIBBON_HELIX_27;
    public static ProteinHelixType POLYPROLINE = ProteinHelixType.POLYPROLINE;

    // private List residues = new Vector();
    private ProteinHelixType helixType = ALPHA;
    
    public ProteinHelix(Residue start, Residue end, ProteinHelixType helixType) {
        super(start, end);
        this.helixType = helixType;
    }

    public ProteinHelixType getHelixType() {return this.helixType;}
}

