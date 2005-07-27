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
 * Created on Jul 19, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.moleculargraphics.commands;

import java.io.*;
import java.text.*;

import org.simtk.molecularstructure.MoleculeCollection;
import org.simtk.util.LineCommand;

public class LoadFileCommand implements LineCommand {

    public Object execute(String[] args, Object target) throws ParseException {
        String fileName = null;
        MoleculeCollection molecules = (MoleculeCollection) target;
        
        try {
            if (args.length == 1) {
                loadPDBFile(args[0], molecules);
            }
            else {
                if (args[0].equals("pdb")) {
                    loadPDBFile(args[1], molecules);
                }
                else throw new ParseException("Unknown file format : '" + args[0] + "'", 0);
            }
        }
        catch (FileNotFoundException exc) {
            throw new ParseException("File not found: " + fileName, 0);
        }
        catch (IOException exc) {
            throw new ParseException("Problem reading file: " + fileName, 0);
        }
        
        return molecules;
    }

    private void loadPDBFile(String fileName, MoleculeCollection molecules) throws FileNotFoundException, IOException {
        molecules.loadPDBFormat(fileName);
    }
}
