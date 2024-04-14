package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public class TagByte implements ITagBase {
	
	private byte data;
	
	public TagByte() {
		this((byte) 0);
	}

	public TagByte(byte data) {
		this.data = data;
	}

	@Override
	public void read(DataInput input) throws IOException {
		this.data = input.readByte();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeByte(this.data);
	}
	
	@Override
	public TagType getType() {
		return TagType.BYTE;
	}
	
	public byte getByte() {
		return this.data;
	}
	
	public short getShort() {
		return this.data;
	}
	
	public int getInt() {
		return this.data;
	}
	
	public long getLong() {
		return this.data;
	}
	
	@Override
	public String toString() {
		return Byte.toString(this.data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagByte other) {
			return other.data == this.data;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Byte.hashCode(this.data);
	}
	
}
