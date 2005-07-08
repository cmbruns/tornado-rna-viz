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

import java.applet.*;
import java.io.*;
import java.util.*;
import java.awt.*;

/* OffscreenImage: deals with showing an arbitrary sized image.
   Features:
   		- buffered offscreen
   		- scrolling by scrollbar or mousecontrol
*/

public class OffscreenImage extends Panel {
	Container myContainer;
	Image preview;
	Dimension previewSize; // size of the offscreen drawing area
	public Graphics previewGraphics;
	public int xorigin = 0;
	public int yorigin = 0;
	
	boolean forceRepaint;
	int prevX = 0;
	int prevY = 0;
	boolean dragging = false;

	public OffscreenImage(Container c) {
		Dimension d;
		d = new Dimension(0,0);
		d = size();
		init(c, d);
	}
	
	public OffscreenImage(Container c, Dimension d) {
		init(c, d);
	}
	
	void init(Container c, Dimension d) {
		this.myContainer = c;		
		previewSize = d;
		//System.out.println("OffscreenImage init: setting preview size to "+d.height+","+d.width);
		forceRepaint = true;
	}
	
	public void setForceRepaint(boolean repaint) {
		forceRepaint = repaint;
	}
	
	public boolean forceRepaint() {
		return forceRepaint;
	}
	
	public Dimension previewSize() {
		return previewSize;
	}
	
/**********************************************************

			preview ROUTINES

 **********************************************************/

	public void clearPreview() {
		if (previewGraphics != null) {
			previewGraphics.setColor(Color.white);
			previewGraphics.fillRect(0, 0, previewSize.width, previewSize.height);
		}
	}
	
	public void createPreview () {
		if ((previewSize.width == 0) & (previewSize.height == 0)) {
			previewSize = size();
		}
		if (previewSize.width + previewSize.height == 0) {
			//System.out.println("createPreview: preview size 0 x 0; no preview created");
		} else {
			//System.out.println("createPreview: creating preview "+previewSize.width+","+previewSize.height);
			preview = createImage(previewSize.width, previewSize.height);
			//System.out.println("createPreview: "+preview.toString());
			//System.out.println("createPreview: created preview "+previewSize.width+","+previewSize.height);
			if (preview == null) {
				//System.out.println("createPreview: failed to create preview");
			} else {
				previewGraphics = preview.getGraphics();
				previewGraphics.setFont(getFont());
				clearPreview();
				forceRepaint = true;
			}
		}
	}
	
	public void updatePreview(Graphics g) { // most should override this.
		if (forceRepaint) {
			System.out.println("OffscreenImage: updatePreview ****");
			clearPreview();
			previewGraphics.setColor(Color.black);		
			for (int y = 10; y < previewSize.height; y = y + 50) {
				for (int x = 10 ; x < previewSize.width ; x = x+50) {
					previewGraphics.drawString(x+","+y, x ,y);
				}
			}
			forceRepaint = false;
		}
	}
	
	public synchronized void checkPreview(Graphics g) {
		if (preview == null) {
			createPreview(); 
		}
	}
	
	public synchronized void paint(Graphics g) {
		checkPreview(g);
		if (preview != null) {
			if (!dragging) {
				//System.out.println("OffscreenImage: paint calling updatePreview");
				updatePreview(g);
			}
			checkOriginBounds();
			//System.out.println("Painting offscreen image: Copying to "+xorigin+","+yorigin);
			copyFromOffscreen(g);
		} else {
			System.out.println("Painting offscreen image: *** couldn't paint the panel because offscreen == null");
		}
	}
	
	public synchronized void copyFromOffscreen(Graphics g) {
		g.drawImage(preview, xorigin, yorigin, Color.white, null);
	}
	
	public synchronized void update(Graphics g) {
		// if offscreen map is smaller than our display area, it will be centered so
		// we need to clear it and fill the background with white.
		Dimension d = size();
		
/*
		if (previewSize.width < d.width) {
			g.setColor(Color.white);
			g.fillRect(0, 0, xorigin, d.height);
			g.fillRect(d.width - xorigin, 0, d.width, d.height);
		}
		
		if (previewSize.height < d.height) {
			g.setColor(Color.white);
			g.fillRect(0,0,d.width,yorigin);
			g.fillRect(0,d.height-yorigin,d.width,d.height);
		}
*/
		paint(g);  // don't erase the bg since offscreen blitz will take care of that
	}


/**********************************************************

			SCROLLING AND SIZING

 **********************************************************/

	public void scroll(int scrollX, int scrollY) {
		xorigin = xorigin + scrollX;
		yorigin = yorigin + scrollY;
		repaint();
	}
	
	public void scrollTo(int newCenterX, int newCenterY) {
		Dimension d = this.getSize();
		int panelCenterX = d.width / 2;
		int panelCenterY = d.height / 2;
		//xorigin = newCenterX - previewCenterX;
		//yorigin = newCenterY - previewCenterY;
		int newxorigin = panelCenterX - newCenterX;
		int newyorigin = panelCenterY - newCenterY;
		System.out.println("Scrolling to "+newCenterX+","+newCenterY);
		System.out.println("Preview size w="+previewSize.width+", h="+previewSize.height);
		System.out.println("Origin offset = "+xorigin+","+yorigin);
		xorigin = newxorigin;
		yorigin = newyorigin;
		repaint();
	}
	
	public void scrollToRect(Rectangle r) {
		int rCenterX = r.x + (r.width / 2);
		int rCenterY = r.y + (r.height / 2);
		scrollTo(rCenterX, rCenterY);
	}
	
	private void checkOriginBounds() {
		Dimension d = size();
		int maxScrollAreaX = (previewSize.width > d.width ? -(previewSize.width - d.width) : 0);
		int maxScrollAreaY = (previewSize.height > d.height ? -(previewSize.height - d.height) : 0);
		
		if (previewSize.width > d.width) {
			// there is room to scroll
			if (xorigin > 0) {
				xorigin = 0;
			} else {
				if (xorigin < maxScrollAreaX) {
					xorigin = maxScrollAreaX;				
				} else {
					// xorigin = xorigin;
				}
			}
		} else {
			// image is smaller than canvas, so we center it.
			xorigin = (d.width / 2) - (previewSize.width / 2);
		}
		
		if (previewSize.height > d.height) {
			if (yorigin > 0) {
				yorigin = 0;
			} else {
				if (yorigin < maxScrollAreaY) {
					yorigin = maxScrollAreaY;				
				} else {
					// yorigin = yorigin;
				}
			}
		} else {
			// image is smaller than canvas, so we center it.
			yorigin = (d.height / 2) - (previewSize.height / 2);
		}
	}
	
	public void setRelativeScroll(float relativeXScroll, float relativeYScroll) {
		Dimension d = size();
		float tempX = -(previewSize.width - d.width);
		float tempY = -(previewSize.height - d.height);
		
		xorigin = (int)(relativeXScroll * tempX);
		yorigin = (int)(relativeYScroll * tempY);
		repaint();
	}
	
	public Point getScroll() {
		Point p;
		p = new Point(xorigin,yorigin);
		return p;
	}

	public void resizePreview(Dimension d) {
		previewSize = d;
		createPreview();
	}
	

/**********************************************************

			EVENT HANDLERS

 **********************************************************/

	public boolean mouseDown(Event evt, int x, int y) {
		//if (evt.shiftDown()) {
			prevX = x;
			prevY = y;
			dragging = true;
			return true;
		//} else { return false; }
	}

	public boolean mouseUp(Event evt, int x, int y) {
		if (dragging) {
			prevX = 0;
			prevY = 0;
			dragging = false;
			return true;
		} else { return false; }
	}
	
	public boolean mouseDrag(Event evt, int x, int y) {
		if (dragging) {
			scroll(x - prevX, y - prevY);
			prevX = x;
			prevY = y;
			return true;
		} else { return false; }
	}
}
