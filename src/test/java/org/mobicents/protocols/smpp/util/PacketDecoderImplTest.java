/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.smpp.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import org.mobicents.protocols.smpp.Address;
import org.mobicents.protocols.smpp.util.PacketDecoderImpl;
import org.mobicents.protocols.smpp.util.SMPPDate;

@Test
public class PacketDecoderImplTest {
    /**
     * ASCII string containing "ABCD!~<nul>E".
     */
    private static final byte[] asciiBytes = {
            0x41, 0x42, 0x43, 0x44, 0x21, 0x7e, 0, 0x45,
    };

    public void testReadCStringSucceedsAtZero() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 0);
        String s = decoder.readCString();
        assertEquals(s, "ABCD!~");
        assertEquals(decoder.getParsePosition(), 7);
    }
    
    public void testReadCStringSucceedsAtNonZero() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 4);
        String s = decoder.readCString();
        assertEquals(s, "!~");
        assertEquals(decoder.getParsePosition(), 7);
    }
    
    public void testReadCStringParsesAZeroLengthString() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 6);
        String s = decoder.readCString();
        assertEquals(s, "");
        assertEquals(decoder.getParsePosition(), 7);
    }
    
    public void testReadCStringExceptionsWhenNoNullByte() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 7);
        try {
            decoder.readCString();
            fail("should have failed with ArrayIndexOutOfBounds");
        } catch (ArrayIndexOutOfBoundsException x) {
            // success!
        }
    }
    
    public void testReadStringSucceedsAtZero() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 0);
        String s = decoder.readString(3);
        assertEquals(s, "ABC");
        assertEquals(decoder.getParsePosition(), 3);
    }
    
    public void testReadStringSucceedsAtNonZero() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 3);
        String s = decoder.readString(3);
        assertEquals(s, "D!~");
        assertEquals(decoder.getParsePosition(), 6);
    }
    
    public void testReadStringParsesNulByte() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 2);
        String s = decoder.readString(6);
        String expected = "CD!~" + new Character((char) 0).toString() + "E";
        assertEquals(s, expected);
        assertEquals(s.charAt(4), '\u0000');
        assertEquals(decoder.getParsePosition(), 8);
    }
    
    public void testReadStringParsesZeroLength() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 3);
        String s = decoder.readString(0);
        assertEquals(s, "");
        assertEquals(decoder.getParsePosition(), 3);
    }
    
    public void testReadStringExceptionsWhenNotEnoughBytes() throws Exception {
        PacketDecoderImpl decoder = new PacketDecoderImpl(asciiBytes, 1);
        try {
            decoder.readString(9);
            fail("should have thrown ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException x) {
            // success
        }
    }
    
    public void testReadUInt1Succeeds() throws Exception {
        byte[] integer = new byte[] { 56 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(integer);
        int value = decoder.readUInt1();
        assertEquals(value, 56);
        assertEquals(decoder.getParsePosition(), 1);
    }

    public void testReadUInt1ParsesHighIntegerCorrectly() throws Exception {
        byte[] integer = new byte[] { (byte) 0xa2 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(integer);
        int value = decoder.readUInt1();
        assertEquals(value, 0xa2);
        assertEquals(decoder.getParsePosition(), 1);
    }
    
    public void testReadUInt1ThrowsExceptionOnInsufficientBytes() throws Exception {
        try {
            byte[] integer = new byte[] { 56 };
            PacketDecoderImpl decoder = new PacketDecoderImpl(integer, 1);
            decoder.readUInt1();
            fail("should have thrown ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException x) {
            // success
        }
    }
    
    public void testReadUInt2Succeeds() throws Exception {
        byte[] integer = new byte[] { 0, 0, 0, (byte) 0xa2, (byte) 0x94, 1, 2 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(integer, 3);
        int value = decoder.readUInt2();
        assertEquals(value, 0xa294);
        assertEquals(decoder.getParsePosition(), 5);
    }
    
    public void testReadUInt2ThrowsExceptionOnInsufficientBytes() throws Exception {
        try {
            byte[] integer = new byte[] { 0, 0, 0x73 };
            PacketDecoderImpl decoder = new PacketDecoderImpl(integer, 2);
            decoder.readUInt2();
            fail("should have thrown ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException x) {
            // success
        }
    }
    
    public void testReadUInt4Succeeds() throws Exception {
        byte[] integer = new byte[] { 0, 0, (byte) 0xff, 0x23, 0x1a, (byte) 0x8a };
        PacketDecoderImpl decoder = new PacketDecoderImpl(integer, 2);
        long value = decoder.readUInt4();
        assertEquals(value, 0xff231a8aL);
        assertEquals(decoder.getParsePosition(), 6);
    }
    
    public void testReadUInt4ThrowsExceptionOnInsufficientBytes() throws Exception {
        try {
            byte[] integer = new byte[] { 0, 0, 0x73, 0x1a, 0x2b };
            PacketDecoderImpl decoder = new PacketDecoderImpl(integer, 2);
            decoder.readUInt4();
            fail("should have thrown ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException x) {
            // success
        }
    }
    
    public void testReadAddressSucceeds() throws Exception {
        byte[] bytes = new byte[] { 1, 2, 0x31, 0x32, 0x33, 0 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(bytes);
        Address address = decoder.readAddress();
        assertEquals(address, new Address(1, 2, "123"));
        assertEquals(decoder.getParsePosition(), 6);
    }
    
    public void testReadNullAddressSucceeds() throws Exception {
        byte[] bytes = new byte[] { 1, 2, 3, 4, 0, 0, 0 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(bytes, 4);
        Address address = decoder.readAddress();
        assertEquals(address, new Address());
        assertEquals(decoder.getParsePosition(), 7);
    }
    
    public void testReadDateSucceeds() throws Exception {
        byte[] bytes = new byte[17];
        bytes[16] = 0;
        System.arraycopy("080118161504000+".getBytes("US-ASCII"), 0, bytes, 0, 16);
        PacketDecoderImpl decoder = new PacketDecoderImpl(bytes);
        SMPPDate date = decoder.readDate();
        assertNotNull(date);
        assertEquals(decoder.getParsePosition(), 17);
    }
    
    public void testReadNullDateSucceeds() throws Exception {
        byte[] bytes = new byte[] { 1, 2, 3, 0 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(bytes, 3);
        SMPPDate date = decoder.readDate();
        assertNull(date);
    }
    
    public void testReadBytesSucceeds() throws Exception {
        byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(bytes, 4);
        byte[] parsedArray = decoder.readBytes(4);
        assertEquals(parsedArray.length, 4);
        assertEquals(parsedArray[0], bytes[4]);
        assertEquals(parsedArray[1], bytes[5]);
        assertEquals(parsedArray[2], bytes[6]);
        assertEquals(parsedArray[3], bytes[7]);
        assertEquals(decoder.getParsePosition(), 8);
    }
    
    public void testReadZeroBytesSucceeds() throws Exception {
        byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
        PacketDecoderImpl decoder = new PacketDecoderImpl(bytes, 6);
        byte[] parsedArray = decoder.readBytes(0);
        assertNotNull(parsedArray);
        assertEquals(parsedArray.length, 0);
    }
}
