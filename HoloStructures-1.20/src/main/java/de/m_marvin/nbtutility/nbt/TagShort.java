package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public class TagShort implements ITagBase {

	private short data;
	
	public TagShort() {
		this((short) 0);
	}
	
	public TagShort(short data) {
		this.data = data;
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.data = input.readShort();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeShort(this.data);
	}

	@Override
	public TagType getType() {
		return TagType.SHORT;
	}
	
	public byte getByte() {
		return (byte) this.data;
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
		return Short.toString(this.data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagShort other) {
			return other.data == this.data;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Short.hashCode(this.data);
	}
	
}
