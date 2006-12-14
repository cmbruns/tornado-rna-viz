/* Portions copyright (c) 2006 Stanford University and Christopher Bruns
 * Contributors:
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
 * IN NO EVENT SHALL THE AUTHORS, CONTRIBUTORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Created on Nov 1, 2006
 * Original author: Christopher Bruns
 */
package org.simtk.parser;

import org.junit.Test;

public class TestRasmolParser {
    
    // The following test strings are expected to parse
    
    @Test
    public void testColorNum() throws ParseException {
        // Should work
        parseCommand("background [255,255,255]");        
    }

    @Test
    public void testColorName() throws ParseException {
        // Should work
        parseCommand("set background white");        
    }

    @Test
    public void testUpperCaseColorName() throws ParseException {
        // Should work
        parseCommand("bg_color WHITE");        
    }

    @Test
    public void testMixedCaseColorName() throws ParseException {
        // Should work
        parseCommand("background redOrange");        
    }

    // Tests below are expected to fail parsing
    
    @Test(expected= ParseException.class)
    public void testLoneColorName() throws ParseException {
        // Should fail
        parseCommand("white");
    }

    @Test(expected= ParseException.class)
    public void testLoneColorNumber() throws ParseException {
        // Should fail
        parseCommand("[255,255,255]");
    }

    @Test(expected= ParseException.class)
    public void testWrongColorName() throws ParseException {
        // Should fail
        parseCommand("pinkishPhlerb");
    }

    @Test(expected= ParseException.class)
    public void testWrongColorNumber() throws ParseException {
        // Should fail
        parseCommand("[255,255,25x]");
    }

    @Test(expected= ParseException.class)
    public void testNonsenseString() throws ParseException {
        // Should fail
        parseCommand("q,");
    }
    
    private void parseCommand(String command) throws ParseException {
        try {
            new RasmolParser(command);
        }
        catch (TokenMgrError err) {
            throw new ParseException(""+err);
        }
    }
}
