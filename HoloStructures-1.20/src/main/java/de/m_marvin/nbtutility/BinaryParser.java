package de.m_marvin.nbtutility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.m_marvin.nbtutility.nbt.ITagBase;

public class BinaryParser {
	
	public static ITagBase read(DataInput input) throws IOException {
		TagType type = TagType.byId(input.readByte());
		input.readUTF(); // why is there an empty string ???
		ITagBase tag = type.create();
		tag.read(input);
		return tag;
	}
	
	public static void write(ITagBase tag, DataOutput output) throws IOException {
		TagType type = tag.getType();
		output.write(tag.getType().getId());
		if (type != TagType.END) output.writeUTF("");
		tag.write(output);
	}
	
	public static <T extends ITagBase> T read(DataInput input, Class<T> rootTagClass) throws IOException {
		ITagBase tag = read(input);
		if (rootTagClass.isInstance(tag)) return rootTagClass.cast(tag);
		try {
			return rootTagClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			return null;
		}
	}
	
	public static <T extends ITagBase> T readCompressed(InputStream input, Class<T> rootTagClass) throws IOException {
		DataInputStream dinput = new DataInputStream(new GZIPInputStream(input));
		ITagBase tag = read(dinput);
		dinput.close();
		if (rootTagClass.isInstance(tag)) return rootTagClass.cast(tag);
		try {
			return rootTagClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			return null;
		}
	}
	
	public static void writeCompressed(ITagBase tag, OutputStream output) throws IOException {
		DataOutputStream doutput = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)));
		write(tag, doutput);
		doutput.close();
	}

	public static byte[] toBytes(ITagBase tag, boolean compressed) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			if (compressed) {
				writeCompressed(tag, buffer);
				return buffer.toByteArray();
			} else {
				write(tag, new DataOutputStream(buffer));
				return buffer.toByteArray();
			}
		} catch (IOException e) {
			System.err.println("failed to write tag to bytes:");
			e.printStackTrace();
			return null;
		}
	}
	
	public static <T extends ITagBase> T fromBytes(byte[] bytes, Class<T> tagClass, boolean compressed) {
		try {
			ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
			if (compressed) {
				return readCompressed(buffer, tagClass);
			} else {
				return read(new DataInputStream(buffer), tagClass);
			}
		} catch (IOException e) {
			System.err.println("failed to read tag from bytes:");
			e.printStackTrace();
			return null;
		}
	}
	
}
