package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.google.common.base.Objects;

import de.m_marvin.nbtutility.TagType;

public class TagString implements ITagBase {

	private String data;
	
	public TagString() {
		this("");
	}
	
	public TagString(String data) {
		this.data = data;
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.data = input.readUTF();
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		output.writeUTF(this.data);
	}

	@Override
	public TagType getType() {
		return TagType.STRING;
	}
	
	public String getString() {
		return this.data;
	}

	@Override
	public String toString() {
		return "\"" + this.data + "\"";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagString other) {
			return Objects.equal(this.data, other.data);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.data);
	}
	
}
