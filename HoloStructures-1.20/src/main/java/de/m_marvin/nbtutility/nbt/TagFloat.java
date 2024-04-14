package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public class TagFloat implements ITagBase {
	
	private float data;
	
	public TagFloat() {
		this(0.0F);
	}
	
	public TagFloat(float data) {
		this.data = data;
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.data = input.readFloat();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeFloat(this.data);
	}

	@Override
	public TagType getType() {
		return TagType.FLOAT;
	}
	
	public float getFloat() {
		return this.data;
	}
	
	public double getDouble() {
		return this.data;
	}

	@Override
	public String toString() {
		return Float.toString(this.data);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagFloat other) {
			return other.data == this.data;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Float.hashCode(this.data);
	}
	
}
