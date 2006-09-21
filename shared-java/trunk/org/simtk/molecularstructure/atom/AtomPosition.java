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
 * Created on Jul 27, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.molecularstructure.atom;

import java.text.ParseException;

import org.simtk.geometry3d.*;

public class AtomPosition {
    private Vector3D coordinates = null;
    private char pdbAltLoc = ' ';
    private double temperatureFactor = 0.0;
    private double occupancy = 0.0;
    
    public AtomPosition(Vector3D coordinates) {setCoordinates(coordinates);}
    
    public AtomPosition(String pdbLine)
    throws ParseException
    {   
        // PDB Fields: A applies to Atom, R to Residue, M to molecule
        //A    1 -  6        Record name     "HETATM" or "ATOM  "
        //
        //A    7 - 11        Integer         serial         Atom serial number.
        //
        //A   13 - 16        Atom            name           Atom name.
        //
        //A   17             Character       altLoc         Alternate location indicator.
        //
        //R   18 - 20        Residue name    resName        Residue name.
        //
        //M   22             Character       chainID        Chain identifier.
        //
        //R   23 - 26        Integer         resSeq         Residue sequence number.
        //
        //R   27             AChar           iCode          Code for insertion of residues.
        //
        //A   31 - 38        Real(8.3)       x              Orthogonal coordinates for X.
        //
        //A   39 - 46        Real(8.3)       y              Orthogonal coordinates for Y.
        //
        //A   47 - 54        Real(8.3)       z              Orthogonal coordinates for Z.
        //
        //A   55 - 60        Real(6.2)       occupancy      Occupancy.
        //
        //A   61 - 66        Real(6.2)       tempFactor     Temperature factor.
        //
        //?   73 - 76        LString(4)      segID          Segment identifier;
        //                                                  left-justified.
        //
        //A   77 - 78        LString(2)      element        Element symbol; right-justified.
        //
        //A   79 - 80        LString(2)      charge         Charge on the atom.
        setCoordinates( new Vector3DClass(
                (new Double(pdbLine.substring(30,38).trim())).doubleValue(),
                (new Double(pdbLine.substring(38,46).trim())).doubleValue(),
                (new Double(pdbLine.substring(46,54).trim())).doubleValue() ));

        setOccupancy(
                new Double( pdbLine.substring(54,60).trim() ).doubleValue());

        setTemperatureFactor(
                new Double( pdbLine.substring(60,66).trim() ).doubleValue());

        setPdbAltLoc(pdbLine.charAt(16));
    }
        
    public Vector3D getCoordinates() {return this.coordinates;}
    public double getOccupancy() {return this.occupancy;}
    public double getTemperatureFactor() {return this.temperatureFactor;}
    public char getPdbAltLoc() {return this.pdbAltLoc;}
    
    public void setCoordinates(Vector3D v) {
        this.coordinates = v;
    }
    
    private void setPdbAltLoc(char c) {
        this.pdbAltLoc = c;
    }

    private void setTemperatureFactor(double b) {
        this.temperatureFactor = b;
    }

    private void setOccupancy(double o) {
        this.occupancy = o;
    }
}
