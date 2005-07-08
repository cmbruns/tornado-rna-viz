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
 * Created on Jun 9, 2005
 *
 */
package org.simtk.util;

/*
 * @(#)UncompressInputStream.java           0.3-2 18/06/1999
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1999  Ronald Tschal�r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 */

import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;


/**
 * This class decompresses an input stream containing data compressed with
 * the unix "compress" utility (LZC, a LZW variant). This code is based
 * heavily on the <var>unlzw.c</var> code in <var>gzip-1.2.4</var> (written
 * by Peter Jannesen) and the original compress code.
 *
 * @version 0.3-2  18/06/1999
 * @author  Ronald Tschal�r
 */
public class UncompressInputStream extends FilterInputStream
{
    /**
     * @param is the input stream to decompress
     * @exception IOException if the header is malformed
     */
    public UncompressInputStream(InputStream is)  throws IOException
    {
    super(is);
    parse_header();
    }


    byte[] one = new byte[1];
    public synchronized int read() throws IOException
    {
    int b = in.read(one, 0, 1);
    if (b == 1)
        return (one[0] & 0xff);
    else
        return -1;
    }


    // string table stuff
    private static final int TBL_CLEAR = 0x100;
    private static final int TBL_FIRST = TBL_CLEAR + 1;

    private int[]  tab_prefix;
    private byte[] tab_suffix;
    private int[]  zeros = new int[256];
    private byte[] stack;

    // various state
    private boolean block_mode;
    private int  n_bits;
    private int  maxbits;
    private int  maxmaxcode;
    private int  maxcode;
    private int  bitmask;
    private int  oldcode;
    private byte finchar;
    private int  stackp;
    private int  free_ent;

    // input buffer
    private byte[]  data = new byte[10000];
    private int     bit_pos = 0, end = 0, got = 0;
    private boolean eof  = false;
    private static final int EXTRA = 64;


    public synchronized int read(byte[] buf, int off, int len)
        throws IOException
    {
    if (eof)  return -1;
    int start = off;

    /* Using local copies of various variables speeds things up by as
     * much as 30% !
     */
    int[]  l_tab_prefix = tab_prefix;
    byte[] l_tab_suffix = tab_suffix;
    byte[] l_stack      = stack;
    int    l_n_bits     = n_bits;
    int    l_maxcode    = maxcode;
    int    l_maxmaxcode = maxmaxcode;
    int    l_bitmask    = bitmask;
    int    l_oldcode    = oldcode;
    byte   l_finchar    = finchar;
    int    l_stackp     = stackp;
    int    l_free_ent   = free_ent;
    byte[] l_data       = data;
    int    l_bit_pos    = bit_pos;


    // empty stack if stuff still left

    int s_size = l_stack.length - l_stackp;
    if (s_size > 0)
    {
        int num = (s_size >= len) ? len : s_size ;
        System.arraycopy(l_stack, l_stackp, buf, off, num);
        off += num;
        len -= num;
        l_stackp += num;
    }

    if (len == 0)
    {
        stackp = l_stackp;
        return off-start;
    }


    // loop, filling local buffer until enough data has been decompressed

    main_loop: do
    {
        if (end < EXTRA)  fill();

        int bit_in = (got > 0) ? (end - end%l_n_bits)<<3 :
                     (end<<3)-(l_n_bits-1);

        while (l_bit_pos < bit_in)
        {
        // check for code-width expansion

        if (l_free_ent > l_maxcode)
        {
            int n_bytes = l_n_bits << 3;
            l_bit_pos = (l_bit_pos-1) +
                n_bytes - (l_bit_pos-1+n_bytes) % n_bytes;

            l_n_bits++;
            l_maxcode = (l_n_bits==maxbits) ? l_maxmaxcode :
                              (1<<l_n_bits) - 1;

            if (debug)
            System.err.println("Code-width expanded to " + l_n_bits);

            l_bitmask = (1<<l_n_bits)-1;
            l_bit_pos = resetbuf(l_bit_pos);
            continue main_loop;
        }


        // read next code

        int pos = l_bit_pos>>3;
        int code = (((l_data[pos]&0xFF) | ((l_data[pos+1]&0xFF)<<8) |
                ((l_data[pos+2]&0xFF)<<16))
                >> (l_bit_pos & 0x7)) & l_bitmask;
        l_bit_pos += l_n_bits;


        // handle first iteration

        if (l_oldcode == -1)
        {
            if (code >= 256)
            throw new IOException("corrupt input: " + code +
                          " > 255");
            l_finchar = (byte) (l_oldcode = code);
            buf[off++] = l_finchar;
            len--;
            continue;
        }


        // handle CLEAR code

        if (code == TBL_CLEAR && block_mode)
        {
            System.arraycopy(zeros, 0, l_tab_prefix, 0, zeros.length);
            l_free_ent = TBL_FIRST - 1;

            int n_bytes = l_n_bits << 3;
            l_bit_pos = (l_bit_pos-1) +
                n_bytes - (l_bit_pos-1+n_bytes) % n_bytes;
            l_n_bits  = INIT_BITS;
            l_maxcode = (1 << l_n_bits) - 1;
            l_bitmask = l_maxcode;

            if (debug)  System.err.println("Code tables reset");

            l_bit_pos = resetbuf(l_bit_pos);
            continue main_loop;
        }


        // setup

        int incode = code;
        l_stackp = l_stack.length;


        // Handle KwK case

        if (code >= l_free_ent)
        {
            if (code > l_free_ent)
            throw new IOException("corrupt input: code=" + code +
                          ", free_ent=" + l_free_ent);
            
            l_stack[--l_stackp] = l_finchar;
            code = l_oldcode;
        }


        // Generate output characters in reverse order

        while (code >= 256)
        {
            l_stack[--l_stackp] = l_tab_suffix[code];
            code = l_tab_prefix[code];
        }
        l_finchar  = l_tab_suffix[code];
        buf[off++] = l_finchar;
        len--;


        // And put them out in forward order

        s_size = l_stack.length - l_stackp;
        int num = (s_size >= len) ? len : s_size ;
        System.arraycopy(l_stack, l_stackp, buf, off, num);
        off += num;
        len -= num;
        l_stackp += num;


        // generate new entry in table

        if (l_free_ent < l_maxmaxcode)
        {
            l_tab_prefix[l_free_ent] = l_oldcode;
            l_tab_suffix[l_free_ent] = l_finchar;
            l_free_ent++;
        }


        // Remember previous code

        l_oldcode = incode;


        // if output buffer full, then return

        if (len == 0)
        {
            n_bits   = l_n_bits;
            maxcode  = l_maxcode;
            bitmask  = l_bitmask;
            oldcode  = l_oldcode;
            finchar  = l_finchar;
            stackp   = l_stackp;
            free_ent = l_free_ent;
            bit_pos  = l_bit_pos;

            return off-start;
        }
        }

        l_bit_pos = resetbuf(l_bit_pos);
    } while (got > 0);

    n_bits   = l_n_bits;
    maxcode  = l_maxcode;
    bitmask  = l_bitmask;
    oldcode  = l_oldcode;
    finchar  = l_finchar;
    stackp   = l_stackp;
    free_ent = l_free_ent;
    bit_pos  = l_bit_pos;

    eof = true;
    return off-start;
    }


    /**
     * Moves the unread data in the buffer to the beginning and resets
     * the pointers.
     */
    private final int resetbuf(int bit_pos)
    {
    int pos = bit_pos >> 3;
    System.arraycopy(data, pos, data, 0, end-pos);
    end -= pos;
    return 0;
    }


    private final void fill()  throws IOException
    {
    got = in.read(data, end, data.length-1-end);
    if (got > 0)  end += got;
    }


    public synchronized long skip(long num)  throws IOException
    {
    byte[] tmp = new byte[(int) num];
    int got = read(tmp, 0, (int) num);

    if (got > 0)
        return (long) got;
    else
        return 0L;
    }


    public synchronized int available()  throws IOException
    {
    if (eof)  return 0;

    return in.available();
    }


    private static final int LZW_MAGIC      = 0x1f9d;
    private static final int MAX_BITS       = 16;
    private static final int INIT_BITS      = 9;
    private static final int HDR_MAXBITS    = 0x1f;
    private static final int HDR_EXTENDED   = 0x20;
    private static final int HDR_FREE       = 0x40;
    private static final int HDR_BLOCK_MODE = 0x80;

    private void parse_header()  throws IOException
    {
    // read in and check magic number 

    int t = in.read();
    if (t < 0)  throw new EOFException("Failed to read magic number");
    int magic = (t & 0xff) << 8;
    t = in.read();
    if (t < 0)  throw new EOFException("Failed to read magic number");
    magic += t & 0xff;
    if (magic != LZW_MAGIC)
        throw new IOException("Input not in compress format (read " +
                  "magic number 0x" +
                  Integer.toHexString(magic) + ")");


    // read in header byte

    int header = in.read();
    if (header < 0)  throw new EOFException("Failed to read header");

    block_mode = (header & HDR_BLOCK_MODE) > 0;
    maxbits    = header & HDR_MAXBITS;

    if (maxbits > MAX_BITS)
        throw new IOException("Stream compressed with " + maxbits +
                  " bits, but can only handle " + MAX_BITS +
                  " bits");

    if ((header & HDR_EXTENDED) > 0)
        throw new IOException("Header extension bit set");

    if ((header & HDR_FREE) > 0)
        throw new IOException("Header bit 6 set");

    if (debug)
    {
        System.err.println("block mode: " + block_mode);
        System.err.println("max bits:   " + maxbits);
    }


    // initialize stuff

    maxmaxcode = 1 << maxbits;
    n_bits     = INIT_BITS;
    maxcode    = (1 << n_bits) - 1;
    bitmask    = maxcode;
    oldcode    = -1;
    finchar    = 0;
    free_ent   = block_mode ? TBL_FIRST : 256;

    tab_prefix = new int[1 << maxbits];
    tab_suffix = new byte[1 << maxbits];
    stack      = new byte[1 << maxbits];
    stackp     = stack.length;

    for (int idx=255; idx>=0; idx--)
        tab_suffix[idx] = (byte) idx;
    }


    private static final boolean debug = false;

    public static void main (String args[])  throws Exception
    {
    if (args.length != 1)
    {
        System.err.println("Usage: UncompressInputStream <file>");
        System.exit(1);
    }

    InputStream in =
            new UncompressInputStream(new FileInputStream(args[0]));

    byte[] buf = new byte[100000];
    int tot = 0;
    long beg = System.currentTimeMillis();

    while (true)
    {
        int got = in.read(buf);
        if (got < 0)  break;
        System.out.write(buf, 0, got);
        tot += got;
    }

    long end = System.currentTimeMillis();
    System.err.println("Decompressed " + tot + " bytes");
    System.err.println("Time: " + (end-beg)/1000. + " seconds");
    }
}
