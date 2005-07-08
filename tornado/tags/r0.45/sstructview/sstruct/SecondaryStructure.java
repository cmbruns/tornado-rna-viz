package sstructview.sstruct;

import java.applet.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.net.*;

import sstructview.kb.SetAndSequenceKB;
import sstructview.sstruct.Base;

public class SecondaryStructure extends SetAndSequenceKB {
	int nbases = -1;
	int minX = 0;
	int minY = 0;
	int maxX = 100;
	int maxY = 100;
	Dimension baseSize;
	
	private static Hashtable colors = new Hashtable();
	static {
		colors.put("black",Color.black);
		colors.put("blue",Color.blue);
		colors.put("cyan",Color.cyan);
		colors.put("darkGray",Color.darkGray);
		colors.put("gray",Color.gray);
		colors.put("green",Color.green);
		colors.put("lightGray",Color.lightGray);
		colors.put("magenta",Color.magenta);
		colors.put("orange",Color.orange);
		colors.put("pink",Color.pink);
		colors.put("red",Color.red);
		colors.put("white",Color.white);
		colors.put("yellow",Color.yellow);
	}
	
	int snapToDistance = 8;	

	public SecondaryStructure() {
		baseSize = new Dimension(1,1);
	}

	public Dimension getDimensions() {
		Dimension d = new Dimension(maxX-minX,maxY-minY);
		return d;
	}
	
	public void setScale (float newscale, Dimension newSize) {
		if (newscale < 1) {
			baseSize.width = 1;
			baseSize.height = 1;
		} else if (newscale < 1.5) {
			baseSize.width = 2;
			baseSize.height = 2;
		} else {
			baseSize.width = newSize.width;
			baseSize.height = newSize.height;
		}

		for (int i = 1 ; i <= nbases ; i++) {
			getBase(i).setScale(newscale);
		}

		snapToDistance = (int)(newSize.height * 1.5);
		if (snapToDistance < 2) { snapToDistance = 2; }
	}

	public static Color getColorNamed(String name) {
		Color color = (Color)(colors.get(name));
		if ((color == null) && (name.indexOf(",") >= 0)) {
			// define a new color
			StringTokenizer st = new StringTokenizer(name,",");
			int red = Integer.parseInt(st.nextToken());
			int green = Integer.parseInt(st.nextToken());
			int blue = Integer.parseInt(st.nextToken());
			color = new Color(red,green,blue);
			colors.put(name, color);
		}
		if (color == null) {
			System.err.println("Could not find or define color "+name+"; defaulting to black.");
			color = Color.black;
		}
		return color;
	}
	
	public void addObjectFromStr(String str) {		
		StringTokenizer st;
		String	objectType;
		String	objectID;
		st = new StringTokenizer(str);
		
		try {
			objectType = st.nextToken();
			objectType.toUpperCase();
			objectID = st.nextToken();
			if (objectType.equals("BASE")) {
			
				String baseletter = st.nextToken();
				int baseX = Integer.parseInt(st.nextToken());
				int baseY = Integer.parseInt(st.nextToken());
				Base b = new Base(Integer.parseInt(objectID), baseletter, baseX, baseY);
				if (st.hasMoreTokens()) {
					b.setBackground(getColorNamed(st.nextToken()));
					if (st.hasMoreTokens()) {
						b.setForeground(getColorNamed(st.nextToken()));
					}
				}
				addBase(b);
				
			} else { // assume group type
				
				String baseList = st.nextToken(""); // skip until end of line
				StringTokenizer listOfCommaTokens = new StringTokenizer(baseList,"	, ");
				String s = null;
				addInstance(objectID, objectType, objectID);
				while (listOfCommaTokens.hasMoreTokens()) {
					s = listOfCommaTokens.nextToken();
					addChild(objectID, s);
				}
			}
		} catch (Exception e) {
			System.out.println("Error parsing "+str+": "+e.toString());
		}
	 }

	public void addBase(Base b) {
		String name = String.valueOf(b.basenumber);
		addInstance(name, "BASE", b);
		addElementToSeq(name, b, b.basenumber - 1);
		if (b.basenumber > nbases) { nbases = b.basenumber; }
 		if (b.x < minX) { minX = b.x; }
 		if (b.y < minY) { minY = b.y; }
 		if (b.x > maxX) { maxX = b.x; }
 		if (b.y > maxY) { maxY = b.y; }
	}
	
	public Base getBase(int baseID) {
		Base b = (Base)getObjectAt(baseID-1);
		return b;
	}
	
	public Base findBase(int x, int y, int xoffset, int yoffset) {
		double bestdist = Double.MAX_VALUE;
		int closest = -1;
		x = x - xoffset;
		y = y - yoffset;
		for (int i = 1 ; i <= nbases ; i++) {
			Base b = getBase(i);
			double dist = (b.x - x) * (b.x - x) + (b.y - y) * (b.y - y);
			if (dist <= bestdist) {
				closest = i;
				bestdist = dist;
			}
		}
		if (bestdist <= snapToDistance) { // must click within N pixels 
			return getBase(closest);
		} else {
			return null;
		}
	}

	public Base getNextBase(Base b) {
		String baseName = getNextInSeq(getName(b));
		Base bNext = (Base)getObject(baseName);
		if (bNext == null) {
			return b; // for backwards compat; should be null as well
		} else {
			return bNext;
		}
	}
	
	public Base getPrevBase(Base b) {
		String baseName = getPrevInSeq(getName(b));
		Base bPrev = (Base)getObject(baseName);
		if (bPrev == null) {
			return b; // for backwards compat; should be null as well
		} else {
			return bPrev;
		}
	}
	

	public Vector getSiblingBases(Base b) {
		// gets all bases that are part of this group or in subgroups of this group.
		String immediateParent = getPrimaryParent(getName(b));
		if (immediateParent == null) {
			return null;
		} else {
			Vector sibsAsStrings = getLeaves(immediateParent);
			return stringsToObjects(sibsAsStrings);
		}
	}
	
	public Vector getSiblingBases(Base b, String type) {
		String typedParent = getParent(getName(b), type);
		if (typedParent == null) {
			return null;
		} else {
			Vector sibsAsStrings = getLeaves(typedParent);
			return stringsToObjects(sibsAsStrings);			
		}
	}

	public Base findPairOfBase(Base b) {
		Vector sibs = getSiblingBases(b);
		if (sibs == null) {
			return null;
		} else if (sibs.size() == 2) { // ignore groups > 2; should really make sure group is BASEPAIR
			if (sibs.elementAt(0) == b) {
				return (Base)sibs.elementAt(1);
			} else {
				return (Base)sibs.elementAt(0);
			}
		} else {
			return null;
		}
	}

	public Vector getBaseRange (Base anchor, Base extension) {
		// returns a Vector with all the bases between anchor and extension inclusive
		Vector v = getRangeObjects(anchor.basenumber-1, extension.basenumber-1);
		return v;
	}
	
	public Vector getBaseRange (String anchor, String extension) {
		// returns a Vector with all the bases between anchor and extension inclusive
		Vector v = getRangeObjects(anchor, extension);
		return v;
	}
	public Vector getBaseRange (int anchor, int extension) {
		// returns a Vector with all the bases between anchor and extension inclusive
		Vector v = getRangeObjects(anchor, extension);
		return v;
	}
	
}
