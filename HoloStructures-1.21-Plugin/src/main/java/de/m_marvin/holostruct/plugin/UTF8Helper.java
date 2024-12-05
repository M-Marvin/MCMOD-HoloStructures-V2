package de.m_marvin.holostruct.plugin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/* Copy of mojangs VarInt implementation used for sending UTF strings */

public class UTF8Helper {
	
    protected static int getByteSize(int data) {
        for(int i = 1; i < 5; ++i) {
            if ((data & -1 << i * 7) == 0) {
                return i;
            }
        }

        return 5;
    }
    
    protected static boolean hasContinuationBit(byte data) {
        return (data & 128) == 128;
    }
    
    public static int readVarInt(DataInput buffer) throws IOException {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = buffer.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while(hasContinuationBit(b0));

        return i;
    }

    public static DataOutput writeVarInt(DataOutput buffer, int value) throws IOException {
        while((value & -128) != 0) {
            buffer.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        buffer.writeByte(value);
        return buffer;
    }
    
    public static DataOutput writeString(DataOutput buffer, String string) throws IOException {
    	byte[] strbytes = string.getBytes(StandardCharsets.UTF_8);
		writeVarInt(buffer, strbytes.length);
		buffer.write(strbytes);
		return buffer;
    }
    
    public static String readString(DataInput buffer) throws IOException {
    	byte[] strbytes = new byte[readVarInt(buffer)];
    	buffer.readFully(strbytes);
    	return new String(strbytes, StandardCharsets.UTF_8);
    }
    
}
