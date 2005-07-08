package sstructview;
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

****************************************************************************/

import java.applet.*;
import java.io.*;
import java.awt.*;
import java.net.*;

import sstructview.rmfutils.LineListener;
import sstructview.rmfutils.LineReader;
import sstructview.sstruct.Base;
import sstructview.sstruct.SStructDisplayer;
import sstructview.sstruct.SecondaryStructure;

/***************************************************************************/

/*
970104 RMF	- added button-visibility parameter
			- added check for OutOfMemory error when rescaling image
			- added public getSelection method
*/

public class SStructView extends Applet implements Runnable, LineListener {
    static final long serialVersionUID = 138L;
	static final String copyright = "SStructView 1.2b1 by Ramon M. Felciano";
	Thread monitorThread;
	SStructDisplayer ssPanel;
	String myBaseURLStr;
	String destFrame;
	SecondaryStructure ss;
	Label selectionInfo;
	Label title;
	Button actionButton;
	Button bZoomIn;
	Button bZoomOut;
	TextField findText;
	Button findButton;
	Panel pControls = null;
	static int numLinesRead = 0;
	LineReader readerThread = null;
	String paramDataURL;
	boolean showControls;
	String actionStr = null;
	String flaggedBasesStr = null;
	String initialSelection = null;
	String selectionType = null;
	String defaultForeground = null;
	String defaultBackground = null;
	String defaultFont = null;
	
	public float getScale () {
		return ssPanel.getScale();
	}

//	---------------------------------------------------------------------------
//		¥ setScale
//	---------------------------------------------------------------------------
//		Is this window tilable?
	
	public void setScale(float newScale) {
		float oldScale = getScale();
		if (newScale == oldScale) { return; }
		showStatus("Rescaling image...");
		System.out.println("Rescaling image...");
		try {
			ssPanel.setScale(newScale);
		} catch (java.lang.OutOfMemoryError e) {
			System.out.println("Couldn't rescale image (out of memory); restoring old scale");
			ssPanel.setScale(oldScale);
		} catch (Exception e) {
			System.out.println("Couldn't rescale image: "+e.toString()+"; restoring old scale");
			ssPanel.setScale(oldScale);
		}
		showStatus("");
	}
	
	public void setScale(String newScaleString) {
		Float newScale;
		newScale = new Float(newScaleString);
		ssPanel.setScale(newScale.floatValue());
	}
	
	public String getSelectionType() {
		return ssPanel.getSelectionType();
	}
	
	public void setSelectionType(String s) {
		ssPanel.setSelectionType(s);
	}
	
	public String getSelection() {
		return ssPanel.getSelection();
	}
	
	public void setSelection (String s) {
		ssPanel.setSelection(s);
	}
	
	public String getFlaggedBases() {
		return ssPanel.getFlaggedBases();
	}
	
	public void setFlaggedBases (String s) {
		ssPanel.setFlaggedBases(s);
	}
	
	public String getVariable(String s) {
		return ssPanel.getVariable(s);
	}
	
	public String getActionURL() {
		return ssPanel.bindURLVariables(myBaseURLStr);
	}
	
	public void setActionURL(String newURL) {
		myBaseURLStr = newURL;
	}
	
	private void parseParameters() {
		String btnVis = null;
		destFrame = getParameter("result-frame");
		btnVis = getParameter("show-controls");
		myBaseURLStr = getParameter("action-URL");
		paramDataURL = getParameter("structure-data-URL");
		actionStr = getParameter("action-desc");
		flaggedBasesStr = getParameter("flagged-bases");
		initialSelection = getParameter("initial-selection");
		defaultForeground = getParameter("default-foreground");
		defaultBackground = getParameter("default-background");
		defaultFont = getParameter("base-font");
		selectionType = getParameter("selection-type");
		
		if (btnVis == null) {
			showControls = false;
		} else {
			showControls = (btnVis.equalsIgnoreCase("TRUE"));
		}
	}
	
	public void init() {
		parseParameters();
		if (defaultForeground != null) {
			Base.DEFAULTFOREGROUND = SecondaryStructure.getColorNamed(defaultForeground);
		}
		if (defaultBackground != null) {
			Base.DEFAULTBACKGROUND = SecondaryStructure.getColorNamed(defaultBackground);
		}
		if (defaultFont != null) {
			SStructDisplayer.defaultFontName = defaultFont;
		}
		
		setLayout(new BorderLayout());
		pControls = new Panel();
		
		pControls.add(new Label("Zoom:",Label.RIGHT));
		bZoomIn = new Button("In");
		bZoomOut = new Button("Out");
		pControls.add(bZoomIn);
		pControls.add(bZoomOut);
		pControls.add(new Button("Reset"));

		findText = new TextField("", 10);
		findButton = new Button("Find");
		pControls.add(findText);
		pControls.add(findButton);

		/*
		pControls.add(new Label("View:",Label.RIGHT));
		pControls.add(new Button("Overview"));
		pControls.add(new Button("Small"));
		pControls.add(new Button("Medium"));
		pControls.add(new Button("Large"));
		*/
	
		System.out.println("actionStr");
		if (actionStr != null) {
			pControls.add(actionButton = new Button(actionStr));
		}
		System.out.println("Font");
		Font f = getFont();
		Font defaultFont = new Font("Dialog", Font.PLAIN, 10);
		setFont(defaultFont);

		System.out.println("labels");
		add("North",new Label(copyright ,Label.CENTER));
		title = new Label("Initializing",Label.CENTER);
		title.setFont(defaultFont);
		add("Center",title);
		
		System.out.println("new SS");
		ss = new SecondaryStructure();
	 	System.out.println("Reading layout data from "+paramDataURL);
	 
	 	System.out.println("Reading layout data from \""+paramDataURL.substring(paramDataURL.lastIndexOf("/")+1)+"\"");
	 	title.setText("Reading layout data from \""+paramDataURL.substring(paramDataURL.lastIndexOf("/")+1)+"\"");
	 	
		System.out.println("new SSDisplayer");
		ssPanel = new SStructDisplayer(this);
		
		/*
			Create a lineReader that will asynchronously read the structure text file
			
		*/
		System.out.println("creating a readerThread");
		debugStr("INIT creating new thread readerThread");

		String s = paramDataURL;//.toUpperCase();
		try { 
			if (!s.startsWith("/")) {
				URL url = null;
				try { url = new URL(paramDataURL); } catch (MalformedURLException e) { debugStr(e.toString());}
				readerThread = new LineReader(url, this);
			} else { // starts with backslash so assume local filename
				readerThread = new LineReader(paramDataURL, this);
			}
		} catch (IOException e) {
			debugStr(e.toString());
		}
		
		readerThread.setPriority(3);	// set to a low priority to encourage screen updates
		readerThread.start();

		/*
			Create a monitor thread that will update the screen as the data file is
			being read by the lineReader, and display the SS after it is done.
			
		*/
		debugStr("INIT creating new thread monitorThread");
		monitorThread = new Thread(this);
		monitorThread.setPriority(1);
		monitorThread.start();
	}

	public void run() {
		while ((readerThread.isAlive()) && (numLinesRead > -1)) {
			//try { monitorThread.sleep(200); } catch (InterruptedException e) {}
			//debugStr("Read "+numLinesRead+" lines.");
		}
		
		//debugStr("Structure loaded: "+ss.nbases+" bases");
		removeAll();
		
		try {
			ssPanel.setAppletContext(this.getAppletContext());
		} catch (java.lang.NullPointerException E) {
			// fails if we are running the applet from an application (not browser)
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		ssPanel.setSecondaryStructure(ss);
		if (flaggedBasesStr != null) {
			ssPanel.setFlaggedBases(flaggedBasesStr);
		}
		add("Center", ssPanel);
		if (showControls) {
			add("South", pControls);
		}
		ssPanel.setScale((float)0.5);
		showStatus("Done.");
		layout();
		if (selectionType != null) {
			setSelection(selectionType);
		}
		if (initialSelection != null) {
			setSelection(initialSelection);
		}
		if (showControls) {
			pControls.layout();
		}
	}
	
	public void handleString(String inputStr) {
		if (inputStr == null) {
			numLinesRead = -1;
		} else {
			ss.addObjectFromStr(inputStr);
		}
	}
	
	private void doFind() {
		String pattern = findText.getText();
		if ((pattern != null) && (pattern != "")) {
			System.out.println("Finding text "+pattern);
			//setScale((float)6.0);
			setSelection(pattern);
			ssPanel.zoomToSelection(); // (0,0);
		}
	}
	
	private void doAction() {
		String dataStr=null;
		InputStream isRaw;
		DataInputStream is;
		Applet a;
		String urlStr;
		AppletContext context;
		
		context = getAppletContext();
		urlStr = ssPanel.bindURLVariables(myBaseURLStr);
		debugStr("ACTION:"+urlStr);
		
		URL url=null;

		try {
			url=new URL(urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		if (url != null) {
			if (context != null) {
				if (destFrame != null) {
					context.showDocument(url,destFrame);
				} else {
					context.showDocument(url);
				}
			} else {
				try {
					isRaw=url.openStream();
					is=new DataInputStream(isRaw);
					try {
						dataStr = "dummy";					
			 			while ((dataStr != null) | (is.available() > 0)) {
			 				dataStr = is.readLine();
							debugStr(dataStr);
			 				// if (dataStr !=null) { addBaseFromStr(dataStr); } 				
			 			}
					} catch (java.io.IOException e) {
						debugStr("Error: "+e.toString());
					}
					is.close();
					isRaw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			debugStr("Error: bad url "+urlStr);
		}
	}
	
	public boolean action(Event evt, Object arg) {
	
		if (evt.target instanceof Button) {
			if ("Redraw".equals(arg)) {
				ssPanel.repaint();
				return true;
			} else if (evt.target == actionButton) {
				doAction();
				return true;
			} else if (evt.target == findButton) {
				doFind();
				return true;
			} else if (evt.target == bZoomIn) {
				float curScale = getScale();
				setScale((float)(curScale * 1.5));
				return true;
			} else if (evt.target == bZoomOut) {
				float curScale = getScale();
				setScale((float)(curScale / 1.5));
				return true;
			}
		}
		if ("Reset".equals(arg)) {
			setScale((float)0.5);
			return true;
		}
		if ("Overview".equals(arg)) {
			setScale((float)0.5);
			return true;
		}
		if ("Small".equals(arg)) {
			setScale((float)1);
			return true;
		}
		if ("Medium".equals(arg)) {
			setScale((float)2);
			return true;
		}
		if ("Large".equals(arg)) {
			setScale((float)3);
			return true;
		}
		if ("Extra Large".equals(arg)) {
			setScale((float)6);
			return true;
		}
		if ("Dummy1".equals(arg)) {
			return true;
		}
		if ("Dummy2".equals(arg)) {
			return true;
		}
		return super.handleEvent(evt);
	}
	
	public String getAppletInfo() {
		String s = new String("SStructView 1.2b2 by Ramon M. Felciano, Copyright (c) 1996-1997 All Rights Reserved");
		return s;
	}
	
	public String[][] getParameterInfo() {
		String pinfo[][] = {
				{"structure-data-URL",    "url",    "location of secondary structure coordinate file"},
				{"flagged-bases", "string",	"list of base ids to be drawn in reverse text"},
				{"initial-selection", "string",	"list of base ids to be selected when first drawn"},
				{"selection-type", "string",	"type of objects that can be selected, default: base"},
				{"show-controls", "string", "whether to show zoom/action buttoms (optional, default: false)"},
				{"action-URL", "url", "query url to call with user selection as parameters (optional)"},
				{"action-desc",   "string",     "label for the action-URL button (optional)"},
				{"result-display-frame",   "string",     "name of Netscape frame in which to display results (optional)"}
			};
		return pinfo;
	}
	


	public void start() {
		if (monitorThread != null) {
			if (monitorThread.isAlive()) {
				debugStr("START resuming thread monitorThread");
				monitorThread.resume(); // ******************************* Workaround for MacNetscape 3.0b5?
			} else {
				debugStr("START monitorThread not alive.");
			}
		} else {
			debugStr("START monitorThread is null.");
		}

		if (readerThread != null) {
			if (readerThread.isAlive()) {
				debugStr("START resuming readerThread.");
				readerThread.resume();
			} else {
				debugStr("START readerThread not alive.");
			}
		} else {
			debugStr("START readerThread is null.");
		}
	}
	
	public void stop() {
		if (readerThread != null && readerThread.isAlive()) {
			debugStr("STOP suspending readerThread.");
			readerThread.suspend();
		} else {
			debugStr("STOP readerThread is null or dead.");
		}
		if (monitorThread != null && monitorThread.isAlive()) {
			debugStr("STOP suspending monitorThread.");
			monitorThread.suspend();
		} else {
			debugStr("STOP monitorThread is null or dead.");
		}
	}
	
	public void resize (int width, int height) {
		debugStr("RESIZE: "+width+","+height);
	}
	
	public void resize (Dimension d) {
		debugStr("RESIZE: "+d);
	}
	
	public void finalize() {
		if (readerThread != null) {
			if (readerThread.isAlive()) { debugStr("FINALIZE readerThread is alive."); } 
			else { debugStr("FINALIZE readerThread not alive."); }
		} else { debugStr("FINALIZE readerThread is null."); }

		if (monitorThread != null) {
			if (monitorThread.isAlive()) { debugStr("FINALIZE monitorThread is alive."); } 
			else { debugStr("FINALIZE monitorThread not alive."); }
		} else { debugStr("FINALIZE monitorThread is null."); }
	}
	
	public void destroy() {
		if (readerThread != null) {
			if (readerThread.isAlive()) { debugStr("DESTROY readerThread is alive."); } 
			else { debugStr("DESTROY readerThread not alive."); }
		} else { debugStr("DESTROY readerThread is null."); }

		if (monitorThread != null) {
			if (monitorThread.isAlive()) { debugStr("DESTROY monitorThread is alive."); } 
			else { debugStr("DESTROY monitorThread not alive."); }
		} else { debugStr("DESTROY monitorThread is null."); }
		if (readerThread != null) readerThread = null;
		if (monitorThread != null) monitorThread = null;
	}
	
	private void debugStr(String s) {
		System.out.println("App mode : "+s);
	}
	
	
	
}
