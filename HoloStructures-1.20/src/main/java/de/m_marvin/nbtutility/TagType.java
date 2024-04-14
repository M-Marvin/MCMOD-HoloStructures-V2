package de.m_marvin.nbtutility;

import java.util.function.Supplier;

import de.m_marvin.nbtutility.nbt.ITagBase;
import de.m_marvin.nbtutility.nbt.TagByte;
import de.m_marvin.nbtutility.nbt.TagByteArray;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.nbtutility.nbt.TagDouble;
import de.m_marvin.nbtutility.nbt.TagEnd;
import de.m_marvin.nbtutility.nbt.TagFloat;
import de.m_marvin.nbtutility.nbt.TagInt;
import de.m_marvin.nbtutility.nbt.TagIntArray;
import de.m_marvin.nbtutility.nbt.TagList;
import de.m_marvin.nbtutility.nbt.TagLong;
import de.m_marvin.nbtutility.nbt.TagShort;
import de.m_marvin.nbtutility.nbt.TagString;

public enum TagType {
	
	END(		(byte) 0x00, TagEnd::new),
	BYTE(		(byte) 0x01, TagByte::new),
	SHORT(		(byte) 0x02, TagShort::new),
	INT(		(byte) 0x03, TagInt::new),
	LONG(		(byte) 0x04, TagLong::new),
	FLOAT(		(byte) 0x05, TagFloat::new),
	DOUBLE(		(byte) 0x06, TagDouble::new),
	BYTE_ARRAY(	(byte) 0x07, TagByteArray::new),
	STRING(		(byte) 0x08, TagString::new),
	TAG_LIST(	(byte) 0x09, TagList::new),
	COMPOUND(	(byte) 0x0A, TagCompound::new),
	INT_ARRAY(	(byte) 0x0B, TagIntArray::new);
	
	private final byte id;
	private final Supplier<ITagBase> supplier;
	
	private TagType(byte id, Supplier<ITagBase> supplier) {
		this.id = id;
		this.supplier = supplier;
	}
	
	public byte getId() {
		return id;
	}
	
	public static TagType byId(byte id) {
		for (TagType t : values()) if (t.getId() == id) return t;
		return END;
	}
	
	public ITagBase create() {
		return this.supplier.get();
	}
	
}
