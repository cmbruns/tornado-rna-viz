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
 * Created on Dec 22, 2005
 * Original author: Christopher Bruns
 */
package org.simtk.pdb;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.zip.GZIPInputStream;

import javax.swing.SwingUtilities;

import org.simtk.gui.MonitoredProcess;
import org.simtk.molecularstructure.MoleculeCollection;
import org.simtk.mvc.SimpleObservable;
import org.simtk.util.UncompressInputStream;

abstract class LoadPDBProcess extends Thread implements MonitoredProcess {
    private volatile boolean m_isSuccessful = false; // Only set true in case of success
    private volatile boolean m_isFailed = false;
    private volatile SimpleObservable m_loadMoleculeObservable;
    protected volatile MoleculeCollection molecules = null;
    // private volatile InputStream m_inputStream;
    private volatile boolean isCancelled = false;
    
    LoadPDBProcess(SimpleObservable loadMoleculeObservable) {
        m_loadMoleculeObservable = loadMoleculeObservable;
    }
    
    abstract InputStream getInputStream() throws IOException;

    public synchronized void cancelLoad() {
        isCancelled = true;
        try {
            getInputStream().close();
        } catch (IOException exc) {}
        interrupt();
    }
    
    protected void loadMolecules() throws IOException, InterruptedException {
        molecules = new MoleculeCollection();
        molecules.loadPDBFormat(getInputStream());
    }
    
    public synchronized MoleculeCollection getMolecules() {return molecules;}
    
    public void run() {
        try {
            loadMolecules();
            
            if (isCancelled)
                reportFailure();
            else
                reportSuccess();
            
        } catch (IOException exc) {
            reportFailure();
        }
        catch (InterruptedException exc) {  
            reportFailure();
        }
    }
    
    private void reportSuccess() {
        m_isSuccessful = true;
        m_isFailed = false;        
        // Deliver molecules in the event thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                m_loadMoleculeObservable.setChanged();
                m_loadMoleculeObservable.notifyObservers(LoadPDBProcess.this);                    
            }
        });
    }
    
    private void reportFailure() {
        m_isFailed = true;
        m_isSuccessful = false;
        // Deliver molecules in the event thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                m_loadMoleculeObservable.setChanged();
                m_loadMoleculeObservable.notifyObservers(LoadPDBProcess.this);                    
            }
        });        
    }
    
    // MonitoredProcess interface
    public synchronized void abort() {
        cancelLoad();
    }   
    
    public int getProgress() {return 0;} // TODO
    public int getMinimum() {return 0;} // TODO
    public int getMaximum() {return 0;} // TODO
    public boolean isFailed() {return m_isFailed;}
    public boolean isSuccessful() {return ( (m_isSuccessful) && (!isCancelled) );}
}

public class LoadPdbUrlProcess extends LoadPDBProcess {
    protected URL url;
    private String pdbId = null;
    
    protected LoadPdbUrlProcess(URL url, SimpleObservable loadMoleculeObservable) {
        super(loadMoleculeObservable);
        this.url = url;
    }
    protected InputStream getInputStream() throws IOException {
        if (url == null) throw new IOException("Missing URL");
        URLConnection urlConnection = url.openConnection();
        if (urlConnection == null) throw new IOException("Unable to open URL: "+url);
        InputStream inStream = urlConnection.getInputStream();
        
        if ( (url.toString().endsWith(".gz")) )
            inStream = new GZIPInputStream(inStream);
        else if ( (url.toString().endsWith(".Z")) )
            inStream = new UncompressInputStream(inStream);
        
        return inStream;        
    }
    public void setPdbId(String pdbId) {this.pdbId = pdbId;}
    
    protected void loadMolecules() throws IOException, InterruptedException {
        super.loadMolecules();

        String fileName = url.getFile();

        // strip off all but the file name (no path)
        String fileSep = System.getProperty("file.separator");
        int pathEnd = fileName.lastIndexOf(fileSep);
        if (pathEnd >= 0) fileName = fileName.substring(pathEnd + 1);

        molecules.setInputStructureFileName(url.getFile());
        if (this.pdbId != null) molecules.setPdbId(this.pdbId);
    }
}
