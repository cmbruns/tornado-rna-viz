/*
 * Copyright (c) 2005, Stanford University. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of the Stanford University nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */

/*
 * Created on Jul 6, 2005
 *
 */
package org.simtk.moleculargraphics.cartoon;

abstract public class MolecularCartoon {
    // replaced Java 1.5 enum with Java 1.4 compliant
    public static class CartoonType {
        static public CartoonType SPACE_FILLING = new CartoonType();
        static public CartoonType BALL_AND_STICK = new CartoonType();

        // static public CartoonType ROPE_AND_CYLINDER = new CartoonType();
        static public CartoonType DUPLEX_CYLINDER = new CartoonType();
        static public CartoonType NUCLEOTIDE_WEDGE = new CartoonType();

        static public CartoonType RESIDUE_SPHERE = new CartoonType();
        static public CartoonType BOND_STICK = new CartoonType();
        static public CartoonType BACKBONE_TRACE = new CartoonType();
        static public CartoonType NUCLEOTIDE_STICK = new CartoonType();
        static public CartoonType NUCLEOTIDE_STICK_CAPS = new CartoonType();
        static public CartoonType WIRE_FRAME = new CartoonType();
    };
}
