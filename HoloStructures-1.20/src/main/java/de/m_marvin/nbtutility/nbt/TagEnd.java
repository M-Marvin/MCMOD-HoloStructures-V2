package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public class TagEnd implements ITagBase {

	public TagEnd() {}
	
	@Override
	public void read(DataInput input) throws IOException {}
	
	@Override
	public void write(DataOutput output) throws IOException {}
	
	@Override
	public TagType getType() {
		return TagType.END;
	}
	
	@Override
	public String toString() {
		return "NULL";
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof TagEnd;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
}
