package de.m_marvin.nbtutility;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;
import java.util.Arrays;

public class VarIntUtil {
	
	public static byte[] toVarIntBytes(int[] iarr) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(iarr.length);
		for (int i : iarr) {
			while ((i & -128) != 0) {
				buffer.write(i & 127 | 128);
				i >>>= 7;
			}
			buffer.write(i);
		}
		return buffer.toByteArray();
	}
	
	public static int[] toInts(byte[] viarr, int capacity) {
		IntBuffer buffer = IntBuffer.wrap(new int[capacity]);
		int i = 0;
		int in = 0;
		for (byte b : viarr) {
			i |= (b & 127) << (7 * in);
			in++;
			if ((b & 128) == 0) {
				buffer.put(i);
				i = 0;
				in = 0;
			}
		}
		return Arrays.copyOf(buffer.array(), buffer.position());
	}
	
}
