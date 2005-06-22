package sstructview.sstruct;
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
970104 RMF	- added support for drag-selection
			- added support for external selection access
*/
import java.applet.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.net.*;

import sstructview.kb.TypedSelection;
import sstructview.rmfutils.OffscreenImage;
import sstructview.sstruct.Base;
import sstructview.sstruct.SecondaryStructure;

public class SStructDisplayer extends OffscreenImage {

	AppletContext context = null;
	SecondaryStructure ss;
	int min = 0;
	int max = 10000;
	private float scale = 1;
	
	private TypedSelection selection = null;
	private Base lastClickedBase = null;
	private Base lastDraggedBase = null;
	
	Vector flaggedBases = null;

	private boolean dragSelecting = false;
	private boolean allowTypeCycling = true;
	
	private Base mouseOverBase = null;
	private Base mouseOverBasePair = null;
	private int mouseOverBaseID = 0;
	
	private Graphics ssGraphic = null;
	private Image ssImage = null;
	
	int rightMargin = 15;
	int bottomMargin = 15;

	Font baseFont;		// font to draw base letters in
	Font infoFont;
	int fontSize;
	public static String defaultFontName = "Courier";
	
	public SStructDisplayer(Container c) {
		super(c);
		init();
	}

	public SStructDisplayer(Container c, Dimension d) {
		super(c, d);
		init();
	}
	
	public void init() {
		ss = null;
		updateFonts();
		selection = new TypedSelection();
		selection.setSelectionType("BASE");
		setAllowTypeCycling(true);
	}

	public void setAllowTypeCycling(boolean allow) {
		allowTypeCycling = allow;
		selection.setAutomaticTypeSwitching(allow);
	}
	
	public boolean allowTypeCycling() {
		return allowTypeCycling;
	}

	public void createSSBuffer (Dimension ssSize) throws java.lang.OutOfMemoryError {
		System.gc();
		System.out.println("createSSBuffer: creating image "+ssSize);
		ssImage = createImage(ssSize.width,ssSize.height);
		if (ssImage == null) {
			System.out.println("createSSBuffer: failed to create offscreen");
		} else {
			System.out.println("createSSBuffer: done creating image "+ssSize);
			System.gc();
			Font f;
			System.out.println("createSSBuffer: getting graphics object");
			ssGraphic = ssImage.getGraphics();
			if (ssImage == null) {
				System.out.println("ssGraphic = null");
			}
			ssGraphic.setFont(baseFont);

			// hack to work around Mac Navigator ignoring bg clear commands
			ssGraphic.setColor(Color.white);
			ssGraphic.fillRect(0,0,ssSize.width,ssSize.height);

			ssGraphic.setColor(Color.black);
			//System.out.println("createSSBuffer: calling paintStructure");
			//paintStructure(ssGraphic,scale);
		}
	}
		
	public synchronized void checkPreview(Graphics g) {
		super.checkPreview(g);
		if (ssImage == null) {
			Dimension ssSize = getSizeOfSSWithMargins();
			createSSBuffer(ssSize);
		}
	}
	

	public synchronized void updatePreview(Graphics g) {
		// g is the onScreen image; most drawning shoud go to previewGraphics
		//System.out.println("updatePreview: just entered");
		//wait(2);
		Dimension d = size();
		
		//System.err.println("In update preview");
		if (forceRepaint()) {
			previewGraphics.setColor(Color.black);		
			//System.out.println("updatePreview: calling clearPreview");
			//clearPreview();
			//System.out.println("updatePreview: done clearPreview");
			//wait(2);
			//System.out.println("updatePreview: calling paintStructure");
			paintStructure(ssGraphic,scale);
			//System.out.println("updatePreview: done paintStructure");
			//System.err.println("Drawing to previewGraphics");
			//wait(2);
			//System.out.println("updatePreview: calling drawImage");
			previewGraphics.drawImage(ssImage, 0, 0, null);
			//System.out.println("updatePreview: done drawImage");
			setForceRepaint(false);
		} else {
			// copy blank secondary structure image
			previewGraphics.drawImage(ssImage, 0, 0, null);
		}
		Font f = ssGraphic.getFont();
		previewGraphics.setFont(f);
		g.setFont(f);
		//System.err.println("Calling updateSelection");
		updateSelection(g);
	}	


/**********************************************************

			SELECTION INFO

 **********************************************************/

	private Point findBestLabelLocation(Base bA, Base bB, int offset) {
		// finds a location offset pixels perpendicular to the midpoint between bA and bB 
		int x = 0;
		int y = 0;
		double distAB = Math.sqrt(((bB.x - bA.x)*(bB.x - bA.x))+((bB.y - bA.y)*(bB.y - bA.y)));
		double midPointX = ((bA.x + bB.x) / 2);
		double midPointY = ((bA.y + bB.y) / 2);
		double ADx = (bB.y - bA.y) / distAB;
		double ADy = (-(bB.x - bA.x)) / distAB;
		
		x = (int)(midPointX + (ADx * offset));
		y = (int)(midPointY + (ADy * offset));
		
		Point p = new Point(x,y);
		return p;
	}
	
	private Rectangle findBestLabelRect(Base bA, Base bB, int offset, Dimension d) {
		/* finds a location offset pixels perpendicular to the midpoint between bA and bB 
			returns a Rectangle with dimensions d best suited to label bA.
		*/
		
		int x = 0;
		int y = 0;
		double distAB = Math.sqrt(((bB.x - bA.x)*(bB.x - bA.x))+((bB.y - bA.y)*(bB.y - bA.y)));
		double midPointX = ((bA.x + bB.x) / 2);
		double midPointY = ((bA.y + bB.y) / 2);
		double ADx = (bB.y - bA.y) / distAB;
		double ADy = (-(bB.x - bA.x)) / distAB;
		
		x = (int)(midPointX + (ADx * offset));
		y = (int)(midPointY + (ADy * offset));
		Point p = new Point(x,y);

		/* The label will appear around the base in one of the following positions:
		
				0		1		2
				4		base	6
				8		9		10
				
		*/
		
		int gridPos = 0;
		int rectTop = 0;
		int rectLeft = 0;
		Rectangle r = new Rectangle(d);

		if (x < midPointX - 2) { gridPos = 0; }
		else if (x > midPointX + 2) { gridPos = 2; }
		else { gridPos = 1; }
		
		if (y > midPointY + 2) { gridPos = gridPos + 8; }
		else if (y >= midPointY - 2) { gridPos = gridPos + 4; }
		
		if (x < midPointX - 2) { rectLeft = x - d.width; }
		else if (x > midPointX + 2) { rectLeft = x ; }
		else { rectLeft = x - (d.width / 2); }
		
		if (y < midPointY - 2) { rectTop = y - d.height; }
		else if (y > midPointY + 2) { rectTop = y; }
		else { rectTop = y - (d.height / 2); }

		r.move(rectLeft,rectTop);
		return r;
	}
	
	private void drawBaseInfo3D(Graphics g, Base b, int baseOffset, String promptText, Color txtColor) {
		int vGap = 3;
		int hGap = 2;
			
		g.setFont(infoFont);
		FontMetrics fm = g.getFontMetrics();
		Dimension d = new Dimension(fm.stringWidth(promptText)+(2*hGap), calcFontSize()+(2*vGap));
		Rectangle r;
		if (baseOffset > 0) {
			r = findBestLabelRect(b, ss.getNextBase(b), 10, d);
		} else if (baseOffset == 0) {
			r = findBestLabelRect(ss.getPrevBase(b), ss.getNextBase(b), 10, d);
		} else {
			r = findBestLabelRect(ss.getPrevBase(b), b, 10, d);
		}
		
		g.setColor(Color.lightGray);
		g.fill3DRect(r.x, r.y, r.width, r.height, true);

		g.setColor(txtColor);
		g.drawString(promptText, r.x+hGap+1, r.y+r.height-vGap);
		g.setFont(baseFont);
	}
	

	private void drawBaseInfo(Graphics g, Base b, int baseOffset, String promptText, Color txtColor) {
		FontMetrics fm = g.getFontMetrics();
		Dimension d = new Dimension(fm.stringWidth(promptText), calcFontSize());
		Rectangle r;
		if (baseOffset > 0) {
			r = findBestLabelRect(b, ss.getNextBase(b), 10, d);
		} else if (baseOffset == 0) {
			r = findBestLabelRect(ss.getPrevBase(b), ss.getNextBase(b), 10, d);
		} else {
			r = findBestLabelRect(ss.getPrevBase(b), b, 10, d);
		}

		g.setColor(txtColor);
		g.drawString(promptText, r.x, r.y+r.height);
	}

	public synchronized void updateSelection(Graphics g) {
		Font oldFont = g.getFont();
		Base b = null;
		Enumeration e = null;

		FontMetrics fm = previewGraphics.getFontMetrics();	
			
		if (mouseOverBase != null) {
			mouseOverBase.drawBase(previewGraphics, ss.baseSize, fm, Color.blue);
			drawBaseInfo(previewGraphics, mouseOverBase, 0, "#"+mouseOverBase.basenumber, Color.blue);
			mouseOverBasePair = ss.findPairOfBase(mouseOverBase);
			if (mouseOverBasePair != null) {
				mouseOverBasePair.drawBase(previewGraphics, ss.baseSize, fm, Color.blue);
				drawBaseInfo(previewGraphics, mouseOverBasePair, 0, "#"+mouseOverBasePair.basenumber, Color.blue);
			}
		}

		if (selection.size() > 0) {
			
			Vector selectedNames = ss.getDescendents(selection.objects(), "BASE", true);
			Vector selectedBases = ss.getObjects(selectedNames);
			if (scale > 1) {
				e = selectedBases.elements();
				while (e.hasMoreElements()) {
					b = (Base)e.nextElement();
					b.clearBase(previewGraphics, ss.baseSize, Color.lightGray);					
				}

				e = selectedBases.elements();
				previewGraphics.setColor(Color.blue);
				while (e.hasMoreElements()) {
					b = (Base)e.nextElement();
					b.drawBase(previewGraphics, ss.baseSize, fm);					
				}
			} else {
				e = selectedBases.elements();
				while (e.hasMoreElements()) {
					b = (Base)e.nextElement();
					b.clearBase(previewGraphics, ss.baseSize, Color.blue);				
				}
			}
		}

		if (lastClickedBase != null) {
			// since the actual selection can contain multiple objects, we only label
			// the last selected one. There is no way to determine which object this is
			// from the TypedSelection object, so we recalculate it from the lastClickedBase
			if (!selection.isEmpty()) {
				previewGraphics.setFont(infoFont);
				//drawBaseInfo3D(previewGraphics, anchorBase, 0, "Base "+anchorBase.basenumber, Color.blue);
				String selType = selection.selectionType();
				String selName = null;
				if (selType.equals(ss.getType(ss.getName(lastClickedBase)))) {
					selName = ss.getName(lastClickedBase);
				} else {
					selName = ss.getAncestor(ss.getName(lastClickedBase), selType);
				}
				drawBaseInfo3D(previewGraphics, lastClickedBase, 0, selType + " "+selName, Color.blue);
				previewGraphics.setFont(baseFont);
			}
		}
			
	}


/**********************************************************

			EVENT HANDLERS

 **********************************************************/

	public boolean mouseDown(Event evt, int x, int y) {
		Base hitBase = ss.findBase(x,y,xorigin,yorigin);
		if (hitBase == null) {
			return super.mouseDown(evt,x,y);
		} else if (evt.shiftDown()) {
			lastDraggedBase = hitBase;
			selection.selectRange(ss.getName(lastClickedBase), ss.getName(lastDraggedBase));				
		} else if (evt.controlDown()) {

			if (lastClickedBase == null) {
				selection.setSelectionTo(ss.getName(hitBase));
			} else {
				String selType = selection.selectionType();
				String baseName = ss.getName(hitBase);
				if (selType.equals(ss.getType(baseName))) {
					selection.toggleInSelection(baseName);
				} else {
					String newSelection = ss.getAncestor(baseName, selType);
					if (newSelection != null) {
						selection.toggleInSelection(newSelection);
					} else {
						System.out.println("Base "+baseName+" is not part of any "+selType);
					}
				}
			}
			lastClickedBase = hitBase;
		} else if (evt.metaDown() && allowTypeCycling) {

			if (lastClickedBase != hitBase) {
				selection.setSelectionTo(ss.getName(hitBase));
				lastClickedBase = hitBase;
			}
			Vector selectedSets = selection.objects();
			System.out.println("Meta down with type cycling on "+selectedSets);
			String selectionName = (String)selectedSets.elementAt(0);
			System.out.println("Next parent = "+ss.getNextParent(ss.getName(hitBase), selectionName));
			selection.setSelectionTo(ss.getNextParent(ss.getName(hitBase), selectionName));
			System.out.println("Selection = "+selection.objects());

		} else {
			String selectionType = selection.selectionType();
			if (selectionType.equalsIgnoreCase("BASE")) {
				selection.setSelectionTo(ss.getName(hitBase));
				dragSelecting = true;
				lastDraggedBase = hitBase;
			} else {
				selection.setSelectionTo(ss.getParent(ss.getName(hitBase), selectionType));
				dragSelecting = false;
			}
			lastClickedBase = hitBase;
		}
		repaint();
		return true;
	}
				
	public boolean mouseUp(Event evt, int x, int y) {
		if (dragSelecting) {
			dragSelecting = false;
			repaint();
			return true;
		} else { return super.mouseUp(evt,x,y); }
	}

	public boolean mouseDrag(Event evt, int x, int y) {
		if (dragSelecting) {
			Base hitBase = ss.findBase(x, y, xorigin, yorigin);
			if (hitBase != null) {
				//System.out.println("DRAG extensionBase: "+extensionBase+" anchorBase: "+anchorBase+" hitBase: "+hitBase);
				if (lastDraggedBase == null) {
					drawBases(xorigin, yorigin, lastClickedBase, hitBase, Base.SELECTED);					
				} else if (lastDraggedBase != hitBase) {
					if (lastDraggedBase.basenumber > lastClickedBase.basenumber) {
						if (hitBase.basenumber > lastDraggedBase.basenumber) {
							drawBases(xorigin, yorigin, lastDraggedBase, hitBase, Base.SELECTED);
						} else if (hitBase.basenumber > lastClickedBase.basenumber) {
							drawBases(xorigin, yorigin, hitBase, lastDraggedBase, Base.DEFAULT);
						} else {
							drawBases(xorigin, yorigin, lastClickedBase, lastDraggedBase, Base.DEFAULT);
							drawBases(xorigin, yorigin, lastClickedBase, hitBase, Base.SELECTED);
						}
					} else {
						if (hitBase.basenumber < lastDraggedBase.basenumber) {
							drawBases(xorigin, yorigin, lastDraggedBase, hitBase, Base.SELECTED);
						} else if (hitBase.basenumber < lastClickedBase.basenumber) {
							drawBases(xorigin, yorigin, hitBase, lastDraggedBase, Base.DEFAULT);					
						} else {
							drawBases(xorigin, yorigin, lastClickedBase, lastDraggedBase, Base.DEFAULT);
							drawBases(xorigin, yorigin, lastClickedBase, hitBase, Base.SELECTED);
						}
					}
				}
				lastDraggedBase = hitBase;
				selection.selectRange(ss.getName(lastClickedBase), ss.getName(hitBase));
			}
			return true;
		} else { return super.mouseDrag(evt, x, y); }
	}
	
	public boolean mouseMove(Event evt, int x, int y) {
		Base hitBase = ss.findBase(x,y,xorigin,yorigin);
		if ((hitBase != null) && (hitBase != lastClickedBase)) {
			mouseOverBase = hitBase;
			repaint();
		}
		return false;
	}
	
	public void drawBases(int xOffset, int yOffset, Base startBase, Base endBase, int mode) {
		Graphics g = getGraphics();
		FontMetrics fm = previewGraphics.getFontMetrics();	
		Base b = null;
		Vector v = ss.getBaseRange(startBase,endBase);
		//System.out.println("DrawBases "+startBase+" to "+endBase+" : "+v);
		if (v != null) {
			Enumeration e = v.elements();
			while (e.hasMoreElements()) {
				b = (Base)e.nextElement();
				if ((mode == Base.DEFAULT) && ((flaggedBases != null) && (flaggedBases.contains(b)))) {
					b.drawBaseSmart(g, ss.baseSize, fm, Base.FLAGGED, b.x + xOffset, b.y + yOffset);
				} else {
					b.drawBaseSmart(g, ss.baseSize, fm, mode, b.x + xOffset, b.y + yOffset);
				}				
			}
		}
	}

	public Dimension getSizeOfSSWithMargins() {
		Dimension sizeWMargins;
		if (ss == null) {
			sizeWMargins = new Dimension(50,50);
		} else {
			sizeWMargins = ss.getDimensions();
		}
		if (scale < 1) {
			sizeWMargins.width = (rightMargin*2)+(int)(sizeWMargins.width * 0.5);
			sizeWMargins.height = (bottomMargin*2)+(int)(sizeWMargins.height * 0.5);
		} else {
			sizeWMargins.width = rightMargin+(int)(sizeWMargins.width * scale);
			sizeWMargins.height = bottomMargin+(int)(sizeWMargins.height * scale);
		}
		return sizeWMargins;
	}

	public void setSecondaryStructure(SecondaryStructure s) {
		ss = s;
		selection.setKB(ss);
	}

	public String getSelectionType() {
		return selection.selectionType();
	}
	
	public void setSelectionType(String s) {
		selection.setSelectionType(s);		
		setForceRepaint(true);
		repaint();
	}
	
	public String getSelection () {
		return selection.toString(",");
	}

	public void setSelection(String s) {
		String token = null;
		StringTokenizer st = new StringTokenizer(s,",");
		selection.clearSelection();
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			token = token.trim();
			selection.addToSelection(token);
		}
		setForceRepaint(true);
		repaint();
	}
	
	public Rectangle selectionBoundingBox () {
		Rectangle box = null;
		Vector selectedNames = ss.getDescendents(selection.objects(), "BASE", true);
		Vector selectedBases = ss.getObjects(selectedNames);
		for (Enumeration e = selectedBases.elements(); e.hasMoreElements(); ) {
			Base base = (Base)(e.nextElement());
			if (box == null) {
				box = new Rectangle(base.x, base.y,1,1);
			} else {
				box.add(base.x, base.y);
			}
		}
		return box;
	}
	
	public void zoomToSelection() {
		//Base baseToFind = ss.getBase(Integer.parseInt(s));
		//System.out.println("Zooming to "+baseToFind+" = "+baseToFind.x+","+baseToFind.y);
		Rectangle r = selectionBoundingBox();
		if (r != null) scrollToRect(r); //baseToFind.x, baseToFind.y);
	}
	
	public String getVariable(String varName) {
		return resolveVariable(varName);
	}
	
	public String getFlaggedBases() {
		Enumeration e = flaggedBases.elements();
		String returnString = null;
		while (e.hasMoreElements()) {
			if (returnString == null) {
				returnString = ss.getName((Base)e.nextElement());
			} else {
				returnString = returnString + "," + ss.getName((Base)e.nextElement());
			}
		}
		if (returnString == null) {
			return "";
		} else {
			return returnString;
		}
	}
	
	public void setFlaggedBases(String baseList) {
		StringTokenizer st;
		Base baseToAdd;
		flaggedBases = new Vector();
		
		st = new StringTokenizer(baseList," ,");		
		while (st.hasMoreElements()) {
			try {
				baseToAdd = ss.getBase(Integer.parseInt(st.nextToken()));
				flaggedBases.addElement(baseToAdd);
			} catch (Exception e) {
				System.out.println("Error parsing "+baseList+": "+e.toString());
			}
		}
		setForceRepaint(true);
		repaint();
	}

	public float getScale () {
		return scale;
	}

	public void setScale (float s) throws java.lang.OutOfMemoryError {
	
		Dimension d = size();
		float tempX = -(previewSize().width - d.width);
		float tempY = -(previewSize().height - d.height);
		float relativeXScroll = (xorigin / tempX);
		float relativeYScroll = (yorigin / tempY);

		scale = s;
		updateFonts();

		ssImage = null; 
		setForceRepaint(true);

		Dimension baseSize = new Dimension(calcFontSize(), calcFontSize()+(int)(scale));
		ss.setScale(scale, baseSize);

		Dimension ssSize = getSizeOfSSWithMargins();
		//System.out.println("setScale: Creating buffer of size "+ssSize);
		createSSBuffer(ssSize);
		//System.out.println("setScale: done with createSSBuffer");
		//wait(2);
		//System.out.println("setScale: calling resizePreview");
		resizePreview(ssSize);
		//System.out.println("setScale: done resizePreview");
		//wait(2);
		
		//clearPreview();
		setRelativeScroll(relativeXScroll, relativeYScroll);
		//System.out.println("setScale: repaint");
		//repaint();
		//System.out.println("setScale: done repaint");
	}
	
	private void wait(int seconds) {
		long t1 = System.currentTimeMillis();
		long t2 = t1 + (1000 * seconds);
		System.out.println("Waiting "+seconds+" seconds");
		while (System.currentTimeMillis() < t2) {
		}
	}
	
	public void updateFonts() {
		fontSize = calcFontSize();
		baseFont = new Font(defaultFontName, Font.PLAIN, fontSize);
		infoFont = new Font("Helvetica", Font.PLAIN, fontSize);
		if (ssGraphic != null) { ssGraphic.setFont(baseFont); }
		setFont(baseFont);
	}
	
	public int calcFontSize() {
		// calculates the appropriate font size for the current scaling factor
		if (scale <= 2) {
			return 9;
		} else if (scale <= 3) {
			return 14;
		} else if (scale <= 4) {
			return 16;
		} else if (scale <= 5) {
			return 18;
		} else {
			return 24;
		}
	}

	public void setAppletContext (AppletContext a) {
		context = a;
	}	
	
	public void paintStructure(Graphics g) {
		paintStructure(g,1);
	}
	
	public void paintStructure(Graphics g, float scale) {
		FontMetrics fm = g.getFontMetrics();
		Base b = null;
		Base pair = null;
		System.out.println("Painting complete structure at scale "+scale+" with baseSize "+ss.baseSize);
		if (scale < 1) {
			g.setColor(Color.lightGray);
			for (int i = 1 ; i <= ss.nbases ; i++) {
				b = ss.getBase(i);
				pair = ss.findPairOfBase(b);
				if (pair != null) {
					g.drawLine(b.x, b.y, pair.x, pair.y);
				}
			}

			g.setColor(Color.black);
			for (int i = 1 ; i <= (ss.nbases-1) ; i++) {
				b = ss.getBase(i);
				b.drawBase(g, ss.baseSize, fm);
				//g.drawLine(bases[i].x, bases[i].y, bases[i+1].x, bases[i+1].y);
			}
			

		} else if (scale < 2) {
			g.setColor(Color.lightGray);
			for (int i = 1 ; i <= ss.nbases ; i++) {
				b = ss.getBase(i);
				pair = ss.findPairOfBase(b);
				if (pair != null) {
					g.drawLine(b.x, b.y, pair.x, pair.y);
				}
			}

			g.setColor(Color.black);
			System.out.println("... clearing bases in green");
			for (int i = 1 ; i <= ss.nbases ; i++) { 
				b = ss.getBase(i);
				b.clearBase(g, ss.baseSize, b.background);
			}

			g.setColor(Color.black);
			for (int i = 1 ; i <= ss.nbases ; i++) {
				b = ss.getBase(i);
				b.drawBase(g, ss.baseSize, fm, b.foreground);
				//g.drawRect(bases[i].x, bases[i].y, 2,2);
			}


		} else if (scale >= 2) {
			System.out.println("... drawing base-pair lines");
			g.setColor(Color.black);
			for (int i = 1 ; i <= ss.nbases ; i++) {
				b = ss.getBase(i);
				pair = ss.findPairOfBase(b);
				if (pair != null) {
					g.drawLine(b.x, b.y, pair.x, pair.y);
				}
			}
			
			System.out.println("... drawing base circles");
			g.setColor(Color.white);
			for (int i = 1 ; i <= ss.nbases ; i++) { 
				ss.getBase(i).clearBase(g, ss.baseSize);
			}

			System.out.println("... drawing bases");
			g.setColor(Color.black);
			for (int i = 1 ; i <= ss.nbases ; i++) { 
				ss.getBase(i).drawBase(g, ss.baseSize, fm);
			}

			if (flaggedBases != null) {
				System.out.println("... drawing flagged bases");
				for (int i = 1 ; i <= ss.nbases ; i++) { 
					b = ss.getBase(i);
					if (flaggedBases.contains(b)) {
						b.drawBaseSmart(g, ss.baseSize, fm, Base.FLAGGED, b.x, b.y);
					}
				}
			}
		}
		System.out.println("Done painting complete structure.");
	}
	
	private String Replace(String original, String find, String replaceWith) {
		if (replaceWith == null) {
			replaceWith = "";
		}
		int foundAt = 0;
		int findlength = find.length();
		String newString;
		
		while ((foundAt = original.indexOf(find)) > 0) {
			original = (original.substring(0,foundAt)) + replaceWith + (original.substring(foundAt+findlength));
		}
		return original;
	}
	
	private String resolveVariable(String query) {
		/*
			Legal variables for other objects :
			
				SELECTION					3, 4, 45, 46, 37
											bp1, bp2, bp3
											H1, H2
											
				SELECTION-TYPE				BASE
											BASE-PAIR
											HELIX
															
			Legal variables for SELECTION-TYPE = BASE :
			
				SUBSEQ-START
				SUBSEQ-END
				SUBSEQ-VAL
				
			For backwards compatibility :
			
				PAIR-ID						the ID of the basepair of the last selected base
				PAIR-VAL					the value of the basepair of the last selected base
				GROUP-ID					the name of the parent(s) of the selected object(s)
				
		*/
		String result = null;
		query = query.toUpperCase();
		if (query.equals("SELECTION")) {
			//Vector v = selection.objects();
			//return rangeIDsToString(ss.stringsToObjects(v));
			return selection.toString(",");
			
		} else if (query.equals("SELECTION-TYPE")) {
			return selection.selectionType();
			
		} else if (query.equals("SUBSEQ-START")) {
			System.out.println("Subseq start: "+ String.valueOf(lastClickedBase.basenumber));
			return ((lastDraggedBase == null) && (selection.selectionType().equalsIgnoreCase("BASE"))) ? null : String.valueOf(lastClickedBase.basenumber);

		} else if (query.equals("SUBSEQ-END")) {
			return ((lastDraggedBase == null) && (selection.selectionType().equalsIgnoreCase("BASE"))) ? null : String.valueOf(lastDraggedBase.basenumber);

		} else if (query.equals("SUBSEQ-VAL")) {
			return ((lastDraggedBase == null) || (!selection.selectionType().equalsIgnoreCase("BASE"))) ? null : rangeToString(ss.stringsToObjects(selection.objects()));

		} else if (query.equals("BASE-ID")) {
			return String.valueOf(lastClickedBase.basenumber);

		} else if (query.equals("BASE-VAL")) {
			return lastClickedBase.baseletter;

		} else if (query.equals("PAIR-ID")) {
			Base anchorBasePair = ss.findPairOfBase(lastClickedBase);
			return (anchorBasePair == null) ? null : String.valueOf(anchorBasePair.basenumber);

		} else if (query.equals("PAIR-VAL")) {
			Base anchorBasePair = ss.findPairOfBase(lastClickedBase);
			return (anchorBasePair == null) ? null : anchorBasePair.baseletter;

		} else if (query.equals("GROUP-ID")) {
			return ss.getPrimaryParent(ss.getName(lastClickedBase));

		} else if (query.startsWith("SELECTION-AS-")) {
			String typeName = query.substring(13);
			typeName = typeName.toUpperCase();
			System.out.println("Getting selection as "+typeName);
			Vector sel = selection.objects();
			Vector descendents = ss.getDescendents(sel, typeName, true);
			if (descendents == null) {
				System.out.println("No descendents found for "+selection.toString());
				return null;
			} else {
				return sstructview.kb.SetKB.vectorToStrings(descendents,",");
			}
		} else {
			result = "unknown variable: "+query;
		}
		return result;
	}
	
	public String bindURLVariables(String myBaseURLStr) {
		
		String urlStr = myBaseURLStr;
		int off = -1;
		int off2 = -1;
		String varName = null;
		while ((off = urlStr.indexOf("<--")) > -1) {
			off2 = urlStr.indexOf("-->", off);
			varName = urlStr.substring(off+3, off2);
			urlStr = Replace(urlStr, "<--"+varName+"-->", resolveVariable(varName));	
		}
		
		return urlStr;
	}
	
	protected String rangeToString(Vector v) {
		Enumeration e = null;
		String s = null;
		Base b = null;
		e = v.elements();
		while (e.hasMoreElements()) {
			b = (Base)e.nextElement();
			if (s == null) { s = b.baseletter; } else { s = s + b.baseletter; }		
		}
		return s;
	}
	
	protected String rangeIDsToString(Vector v) {
		Enumeration e = null;
		String s = null;
		Base b = null;
		e = v.elements();
		while (e.hasMoreElements()) {
			b = (Base)e.nextElement();
			if (s == null) { s = Integer.toString(b.basenumber); } else { s = s + " " + Integer.toString(b.basenumber);	}		
		}
		return s;
	}
	
	protected String endpointIDsToString (Vector v) {
		String s = null;
		Base firstBase = (Base)v.firstElement();
		Base lastBase = (Base)v.lastElement();
		if (v.size() > 0) {
			s = firstBase.basenumber + "-" + lastBase.basenumber;
		}
		return s;
	}


}
