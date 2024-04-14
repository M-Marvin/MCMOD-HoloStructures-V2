package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import de.m_marvin.nbtutility.TagType;

public class TagIntArray implements ITagBase {
	
	private int[] data;
	
	public TagIntArray() {
		this(new int[0]);
	}
	
	public TagIntArray(int[] data) {
		this.data = data;
	}

	@Override
	public void read(DataInput input) throws IOException {
		int len = input.readInt();
		this.data = new int[len];
		for (int i = 0; i < len; i++) this.data[i] = input.readInt();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeInt(this.data.length);
		for (int i = 0; i < this.data.length; i++) output.writeInt(this.data[i]);
	}

	@Override
	public TagType getType() {
		return TagType.INT_ARRAY;
	}
	
	public int[] getArray() {
		return this.data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < this.data.length; i++) {
			sb.append(Integer.toString(this.data[i]));
			if (i < this.data.length - 1) sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagIntArray other) {
			return Objects.equals(this.data, other.data);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.data);
	}
	
}
