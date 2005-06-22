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

import java.io.*;
import java.util.*;
import java.awt.*;

public class Base extends Object {
	static final int DEFAULT = 1;
	static final int SELECTED = 2;
	static final int FOCUS = 3;
	static final int FLAGGED = 4;
	int basenumber = 9999;
	String baseletter;
	int x = -1;
	int y = -1;
	int originalx = -1;
	int originaly = -1;
	public static Color DEFAULTFOREGROUND = Color.black;
	public static Color DEFAULTBACKGROUND = Color.white;
	Color foreground = DEFAULTFOREGROUND;
	Color background = DEFAULTBACKGROUND;

	Base(int baseID, String baseTxt, int baseX, int baseY) {
		basenumber = baseID;
		baseletter = baseTxt;
		originalx = baseX;
		originaly = baseY;
		x = originalx;
		y = originaly;
	}

	public String toString() {
		return "Base "+basenumber+"="+baseletter+", pos=["+x+","+y+"]";
	}
	public void setScale (float newscale) {
		x = (int)(originalx * newscale);
		y = (int)(originaly * newscale);
	}

	public void DrawAxes (Graphics g) {
		g.setColor(Color.black);
		g.drawLine(x,y,x+5,y);
		g.drawLine(x,y,x,y-5);
	}

	public void setForeground(Color color) {
		foreground = color;
	}
	
	public void setBackground(Color color) {
		background = color;
	}

	/***************************************************************************
	
					DRAWING ROUTINES
	
	****************************************************************************/
	
	public void clearBase(Graphics g, Dimension baseSize) {
		clearBase(g, baseSize, background);
	}
	
	public void clearBase(Graphics g, Dimension baseSize, Color bg) {
		Color curColor = g.getColor();	
		g.setColor(bg);
		clearBaseInternal(g, baseSize);
		g.setColor(curColor);
	}

	private void clearBaseInternal(Graphics g, Dimension baseSize) {
		g.fillOval(x - baseSize.width/2, y-baseSize.height/2, 1+baseSize.width, 1+baseSize.height);
	}




	public void drawBase(Graphics g, Dimension baseSize, FontMetrics fm) {
		drawBase(g, baseSize, fm, foreground);
	}
	
	private void drawBaseInternal(Graphics g, Dimension baseSize, FontMetrics fm) {
		if (baseSize.width < 2) {
			g.drawLine(x,y, x,y);
		} else if (baseSize.width < 3) {
			g.drawRect(x - baseSize.width/2, y-baseSize.height/2, baseSize.width, baseSize.height);
		} else {
			g.drawString(baseletter, x - (baseSize.width/2) + 3 ,y + (baseSize.height/2) - 4);
		}
	}

	public void drawBase(Graphics g, Dimension baseSize, FontMetrics fm, Color fg) {
		Color curColor = g.getColor();	
		g.setColor(fg);
		drawBaseInternal(g, baseSize, fm);
		g.setColor(curColor);
	}
/*
	public void drawBase(Graphics g, Dimension baseSize, FontMetrics fm, Color fg, int newX, int newY) {
		int oldX = x;
		int oldY = y;
		x = newX;
		y = newY;
		drawBase(g, baseSize, fm, fg);
		x = oldX;
		y = oldY;
	}
*/	
	public void drawBaseSmart(Graphics g, Dimension baseSize, FontMetrics fm, int mode, int newX, int newY) {
		int oldX = x;
		int oldY = y;
		x = newX;
		y = newY;
		if (baseSize.width >= 2) { // must clear the base
			if (mode == DEFAULT) {
				clearBase(g, baseSize, background);					
			} else if (mode == SELECTED) {
				clearBase(g, baseSize, Color.lightGray);					
			} else if (mode == FOCUS) {
				clearBase(g, baseSize, Color.lightGray);					
			} else if (mode == FLAGGED) {
				clearBase(g, baseSize, Color.black);					
			}
		}

		if (mode == DEFAULT) {
			g.setColor(foreground);
		} else if (mode == SELECTED) {
			g.setColor(Color.blue);
		} else if (mode == FOCUS) {
			g.setColor(Color.blue);
		} else if (mode == FLAGGED) {
			g.setColor(Color.white);				
		}
		drawBaseInternal(g, baseSize, fm);
		x = oldX;
		y = oldY;
	}
}
