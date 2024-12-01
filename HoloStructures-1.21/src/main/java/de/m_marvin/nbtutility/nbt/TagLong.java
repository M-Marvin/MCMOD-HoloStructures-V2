package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public class TagLong implements ITagBase {

	private long data;
	
	public TagLong() {
		this(0L);
	}
	
	public TagLong(long data) {
		this.data = data;
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.data = input.readLong();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeLong(this.data);
	}

	@Override
	public TagType getType() {
		return TagType.LONG;
	}
	
	public byte getByte() {
		return (byte) this.data;
	}
	
	public short getShort() {
		return (short) this.data;
	}
	
	public int getInt() {
		return (int) this.data;
	}
	
	public long getLong() {
		return this.data;
	}

	@Override
	public String toString() {
		return Long.toString(this.data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagLong other) {
			return other.data == this.data;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(this.data);
	}
	
}
