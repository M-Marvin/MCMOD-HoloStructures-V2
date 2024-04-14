package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public class TagDouble implements ITagBase {

	private double data;
	
	public TagDouble() {
		this(0.0);
	}
	
	public TagDouble(double data) {
		this.data = data;
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.data = input.readDouble();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeDouble(this.data);
	}

	@Override
	public TagType getType() {
		return TagType.DOUBLE;
	}
	
	public float getFloat() {
		return (float) this.data;
	}
	
	public double getDouble() {
		return this.data;
	}

	@Override
	public String toString() {
		return Double.toString(this.data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagDouble other) {
			return other.data == this.data;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Double.hashCode(this.data);
	}
	
}
