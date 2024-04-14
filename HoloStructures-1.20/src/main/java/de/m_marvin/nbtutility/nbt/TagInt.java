package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public class TagInt implements ITagBase {

	private int data;
	
	public TagInt() {
		this(0);
	}
	
	public TagInt(int data) {
		this.data = data;
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.data = input.readInt();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeInt(this.data);
	}

	@Override
	public TagType getType() {
		return TagType.INT;
	}
	
	public byte getByte() {
		return (byte) this.data;
	}
	
	public short getShort() {
		return (short) this.data;
	}
	
	public int getInt() {
		return this.data;
	}
	
	public long getLong() {
		return this.data;
	}

	@Override
	public String toString() {
		return Integer.toString(this.data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagInt other) {
			return other.data == this.data;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Integer.hashCode(this.data);
	}
	
}
