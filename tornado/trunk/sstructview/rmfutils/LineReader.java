package sstructview.rmfutils;
/*************************************************************************

SStructView 1.0                                         December 1, 1996
Copyright © 1996 by The Leland Stanford Junior University.

    Source Code Author: Ramon Felciano
Research Collaborators: Richard O. Chen
                        Russ B. Altman (Principal Investigator).
    Questions/Comments: SStructView@smi.stanford.edu.

If you publish research based on SStructView (source or object code),
please reference our paper:

    R. M. Felciano, R. O. Chen, & R. B. Altman. RNA Secondary
    Structure as a Reusable Interface to Biological Information 
    Resources. 1996. Gene-COMBIS (in press) SMI Tech Report SMI-96-0641

For the latest versions and more information, see

    http://www-smi.stanford.edu/projects/helix/sstructview/

Permission to use, copy, modify, and distribute this software
and its documentation for any purpose and without fee is hereby
granted, provided that the above copyright notices appear in all
copies and that both the above copyright notices and this permission
notice appear in supporting documentation, and that the name of The
Leland Stanford Junior University not be used in advertising or
publicity pertaining to distribution of the software without specific,
written prior permission. This software is made available "as is", and
THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL WARRANTIES,
EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT
LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE LELAND STANFORD JUNIOR
UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE)
OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.

*************************************************************************/

/*
970103 RMF	- added LineListener as constructor param; removed SetListener
			- removed label variable (class shouldn't know about interface elements)
			- LineReader now throws IOException instead of dumping to stdio
*/
import java.applet.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.net.*;

import sstructview.rmfutils.LineListener;

// This thread reads data from the server and prints it on the console
// As usual, the run() method does the interesting stuff.
public class LineReader extends Thread {
	DataInputStream in = null;
	InputStream isRaw = null;
	LineListener listener = null;
	
	public LineReader(URL url, LineListener L) throws IOException {
		super("Client Reader");

		//this.client = c;
		if (url != null) {
			isRaw=url.openStream();
		} else {
			System.out.println("Error: url was empty");
		}
		listener = L;
	}
	
	public LineReader(String filename, LineListener L) throws IOException {
		super("Client Reader");
		listener = L;

		File structureDir = null;
		File inFile = null;
		FileInputStream myfilestream;

		//this.client = c;
		if (filename != null) {
			structureDir = new File("");
			inFile = new File(structureDir, filename);
			isRaw = (InputStream)new FileInputStream(filename);
		} else {
			System.out.println("Error: filename was empty");
		}
	}

	public void run() {
		String line;
		try {
			in = new DataInputStream(isRaw);
			while(true) {
				line = in.readLine();
				if (line == null) { 
					//System.out.println("Server closed connection.");
					break;
				}
				listener.handleString(line); // System.out.println(line);
				// yield();
			}
		}
		catch (IOException e) {
			System.out.println("Reader: " + e);
		}
		finally {
			try {
				if (in != null) {
					in.close();
					isRaw.close();
				}
			}
			catch (IOException e) {
				System.out.println("Reader: " + e);
			}
		}
		listener.handleString(null); // System.out.println(line);
	}

	/*
	public static void main(String argv[])
	{
		Thread reader;

		reader = new LineReader("Dummy.txt", this);

		reader.setPriority(6);
		reader.start();
	}
	*/

}