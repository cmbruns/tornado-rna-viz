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
 * Created on Sep 12, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.pdb;

import java.io.*;
import java.net.*;
import java.util.zip.*;
import org.simtk.util.*;

public class WebPDB {
    
    static InputStream getWebPDBStream(String rawPdbId) throws IOException {
        return getWebPDBStream(rawPdbId, true);
    }
    
    static InputStream getWebPDBStream(String rawPdbId, boolean useBiologicalUnit) throws IOException {
        URL url = getWebPdbUrl(rawPdbId, useBiologicalUnit); 
        return getWebPDBStream(url);        
    }

    static InputStream getWebPDBStream(URL url) throws IOException {
        InputStream inStream;
        URLConnection urlConnection = url.openConnection();
        inStream = urlConnection.getInputStream();
        
        if ( (url.toString().endsWith(".gz")) )
            inStream = new GZIPInputStream(inStream);
        if ( (url.toString().endsWith(".Z")) )
            inStream = new UncompressInputStream(inStream);
        
        return inStream;        
    }
    
    static URL getWebPdbUrl(String rawPdbId, boolean useBiologicalUnit) {
        // Load PDB molecule from the internet
        String pdbId = rawPdbId.trim().toLowerCase();
        
        // Force ID to be 4 characters
        if (pdbId.length() != 4) return null;
        
        String urlBase;
        String extension;
        String filePrefix = "";
        // if (bioUnitCheckBox.getState()) {
        if (useBiologicalUnit) { // biological unit
            urlBase = "ftp://ftp.rcsb.org/pub/pdb/data/biounit/coordinates/divided/";
            extension = "pdb1.gz";
        }
        else {
            urlBase = "ftp://ftp.rcsb.org/pub/pdb/data/structures/divided/pdb/";
            extension = "ent.Z";
            filePrefix = "pdb";
        }
        
        String division = pdbId.substring(1, 3);
        String fullURLString = urlBase + division + "/" + filePrefix + pdbId + "." + extension;
        
        URL url = null;
        try {
            url = new URL(fullURLString);
        } catch (MalformedURLException exc) { 
            // TODO
        }

        return url;
    }
}
