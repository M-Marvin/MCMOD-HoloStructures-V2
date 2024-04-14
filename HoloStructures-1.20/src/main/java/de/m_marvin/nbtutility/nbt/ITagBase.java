package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.m_marvin.nbtutility.TagType;

public interface ITagBase {
	
	public void read(DataInput input) throws IOException;
	public void write(DataOutput output) throws IOException;
	public TagType getType();
	
}
