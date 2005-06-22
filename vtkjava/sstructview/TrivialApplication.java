package sstructview;
import java.net.*;
import java.io.*;
import java.util.*;

import sstructview.kb.TypedSelection;
import sstructview.sstruct.Base;
import sstructview.sstruct.SecondaryStructure;

public class TrivialApplication {
	
	public static void loadFile(String location, SecondaryStructure SecStruct) {
		DataInputStream in = null;
		InputStream isRaw = null;
		System.out.println("Reading "+location);
		try { 
			if (!location.startsWith("/")) {
				URL url = null;
				System.out.println("URL : " + location);
				try {
					url = new URL(location);
				} catch (MalformedURLException e) { 
					System.out.println(e.toString());
				}
				if (url != null) {
					isRaw=url.openStream();
				} else {
					System.out.println("Error: url was empty");
				}
			} else { // starts with backslash so assume local filename
				File structureDir = null;
				File inFile = null;
				FileInputStream myfilestream;

				//this.client = c;
				if (location != null) {
					structureDir = new File("");
					inFile = new File(structureDir, location);
					isRaw = (InputStream)new FileInputStream(location);
				} else {
					System.out.println("Error: filename was empty");
				}
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}

		String line;
		try {
			in = new DataInputStream(isRaw);
			while(true) {
				line = in.readLine();
				if (line == null) { 
					//System.out.println("Server closed connection.");
					break;
				}
				//System.out.println(line);
				SecStruct.addObjectFromStr(line);
			}
		} catch (IOException e) {
			System.out.println("Reader: " + e);
		} finally {
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
	}
	

	public static void main(String args[]) {
		SecondaryStructure ss = new SecondaryStructure();
		String paramDataURL = "http://helix-web.stanford.edu/sstructview/data/e.coli16s.coord";
		//String paramDataURL = "/Phoenix/SStructView/Source Code/SStructAppApplet/v1.1/e.coli.small";
		//String paramDataURL = "/Phoenix/SStructView/www-sstructview/v1.1/e.coli16s.coord";
		loadFile(paramDataURL, ss);
		//ss.dump();
		
		long start = 0;
		long end = 0;
		Vector v = null;
	
		System.gc();
	
		start = System.currentTimeMillis();
		v = ss.getBaseRange("120","1080");
		end = System.currentTimeMillis();
		System.out.println("Objects accessed via Strings; Milliseconds: "+(end - start));
		v = null;
		System.gc();
		
		start = System.currentTimeMillis();
		v = ss.getBaseRange(120,1080);
		end = System.currentTimeMillis();
		System.out.println("Objects accessed via Ints; Milliseconds: "+(end - start));
		v = null;
		System.gc();

		start = System.currentTimeMillis();
		v = ss.getRange(120,1080);
		end = System.currentTimeMillis();
		System.out.println("Names accessed via Ints; Milliseconds: "+(end - start));
		v = null;
		System.gc();

		start = System.currentTimeMillis();
		v = ss.getRangeObjects(120,1080);
		end = System.currentTimeMillis();
		System.out.println("Objects accessed via Ints with Array;  Milliseconds: "+(end - start));

/*
		String testNames[] = {"23","25","bp1","H1","H2","ALL-BASE-PAIRS","ALL-HELICES"};
		String curName = null;
		for (int i = 1; i < testNames.length; i++) {
			curName = testNames[i-1];
			System.out.println("  getPrimaryParent "+curName+" : "+ss.getPrimaryParent(curName));
			System.out.println("        getParents "+curName+" : "+ss.getParents(curName));
			System.out.println("      getAncestors "+curName+" : "+ss.getAncestors(curName));
			System.out.println("getPrimarySiblings "+curName+" : "+ss.getPrimarySiblings(curName));
			System.out.println("       getSiblings "+curName+" : "+ss.getSiblings(curName));
			System.out.println("       getChildren "+curName+" : "+ss.getChildren(curName));
			System.out.println("         getLeaves "+curName+" : "+ss.getLeaves(curName));
			System.out.println("                     getDescendents "+curName+" : "+ss.getDescendents(curName, null, false));
			System.out.println("          getDescendents w. Parents "+curName+" : "+ss.getDescendents(curName, null, true));
			System.out.println("     getDescendents BASE w. Parents "+curName+" : "+ss.getDescendents(curName, "BASE", true));
			System.out.println("getDescendents BASE-PAIR w. Parents "+curName+" : "+ss.getDescendents(curName, "BASE-PAIR", true));
			System.out.println("           getType "+curName+" : "+ss.getType(curName));
		}
		
		System.out.println("findBase 1 : "+ss.getBase(1));
		System.out.println("findPairOfBase 1 : "+ss.findPairOfBase(ss.getBase(1)));
		System.out.println("findPairOfBase 25 : "+ss.findPairOfBase(ss.getBase(25)));
		System.out.println("findPairOfBase 10 : "+ss.findPairOfBase(ss.getBase(10)));
		System.out.println("getAncestor of 23 of type HELIX : "+ss.getAncestor("23","HELIX"));
		
		System.out.println("getElementAt 8 : "+ss.getElementAt(8));
		System.out.println("getElementAt 9 : "+ss.getElementAt(9));
		System.out.println("getElementAt 10 : "+ss.getElementAt(10));
		System.out.println("getElementAt 11 : "+ss.getElementAt(11));
		System.out.println("getNextBase 10 : "+ss.getNextBase(ss.getBase(10)));
		System.out.println("getPrevBase 10 : "+ss.getPrevBase(ss.getBase(10)));	
		

		TypedSelection selection = new TypedSelection();
		selection.setKB(ss);
		selection.setAutomaticTypeSwitching(true);
		selection.setSelectionTo("10");
		System.out.println(selection.toString());
		
		selection.setSelectionTo("bp1");
		System.out.println(selection.toString());
		
		selection.addToSelection("bp2");
		System.out.println(selection.toString());
		
		selection.addToSelection("bp3");
		System.out.println(selection.toString());
		
		selection.removeFromSelection("bp2");
		System.out.println(selection.toString());
		
		selection.removeFromSelection("bp3");
		System.out.println(selection.toString());
		
		selection.addToSelection("bp3");
		System.out.println(selection.toString());
		
		selection.addToSelection("H2");
		System.out.println(selection.toString());
		
		selection.selectRange("4","10");
		System.out.println(selection.toString());
		
		selection.selectRange("20","12");
		System.out.println(selection.toString());
*/
	}

}
