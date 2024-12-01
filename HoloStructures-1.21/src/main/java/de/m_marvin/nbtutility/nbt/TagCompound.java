package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.m_marvin.nbtutility.TagType;

public class TagCompound implements ITagBase {

	private Map<String, ITagBase> data;

	public TagCompound() {
		this(new HashMap<>());
	}
	
	public TagCompound(Map<String, ITagBase> data) {
		this.data = data;
	}
	
	public TagCompound(TagCompound tag) {
		this.data = new HashMap<>();
		this.data.putAll(tag.data);
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.data.clear();
		TagType type;
		while ((type = TagType.byId(input.readByte())) != TagType.END) {
			String key = input.readUTF();
			ITagBase item = type.create();
			item.read(input);
			this.data.put(key, item);
		}
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		for (Entry<String, ITagBase> item : this.data.entrySet()) {
			if (item.getValue().getType() == TagType.END) continue;
			output.writeByte(item.getValue().getType().getId());
			output.writeUTF(item.getKey());
			item.getValue().write(output);
		}
		output.writeByte(TagType.END.getId());
	}
	
	@Override
	public TagType getType() {
		return TagType.COMPOUND;
	}
	
	public Map<String, ITagBase> getMap() {
		return this.data;
	}
	
	public int getEntryCount() {
		return this.data.size();
	}
	
	public boolean has(String key) {
		return this.data.containsKey(key);
	}

	public boolean has(String key, TagType type) {
		if (!this.data.containsKey(key)) return false;
		return this.data.get(key).getType() == type; 
	}
	
	public ITagBase get(String key) {
		return this.data.get(key);
	}

	public <T extends ITagBase> T get(String key, Class<T> tagClass) {
		ITagBase tag = this.data.get(key);
		if (tagClass.isInstance(tag)) return tagClass.cast(tag);
		return null;
	}
	
	public <T extends ITagBase> T getOr(String key, T def) {
		@SuppressWarnings("unchecked")
		T tag = get(key, (Class<T>) def.getClass());
		return tag == null ? def : tag;
	}
	
	public TagCompound getCompound(String key) {
		return get(key, TagCompound.class);
	}
	
	public void putTag(String key, ITagBase tag) {
		this.data.put(key, tag);
	}
	
	public void removeTag(String key) {
		this.data.remove(key);
	}
	
	public byte getByte(String key) {
		TagByte tag = get(key, TagByte.class);
		return tag == null ? (byte) 0 : tag.getByte();
	}
	
	public void putByte(String key, byte value) {
		this.data.put(key, new TagByte(value));
	}
	
	public short getShort(String key) {
		TagShort tag = get(key, TagShort.class);
		return tag == null ? (short) 0 : tag.getShort();
	}

	public void putShort(String key, short value) {
		this.data.put(key, new TagShort(value));
	}
	
	public int getInt(String key) {
		TagInt tag = get(key, TagInt.class);
		return tag == null ? 0 : tag.getInt();
	}

	public void putInt(String key, int value) {
		this.data.put(key, new TagInt(value));
	}
	
	public long getLong(String key) {
		TagLong tag = get(key, TagLong.class);
		return tag == null ? 0L : tag.getLong();
	}

	public void putLong(String key, long value) {
		this.data.put(key, new TagLong(value));
	}
	
	public float getFloat(String key) {
		TagFloat tag = get(key, TagFloat.class);
		return tag == null ? 0.0F : tag.getFloat();
	}

	public void putFloat(String key, float value) {
		this.data.put(key, new TagFloat(value));
	}
	
	public double getDouble(String key) {
		TagDouble tag = get(key, TagDouble.class);
		return tag == null ? 0.0 : tag.getDouble();
	}

	public void putDouble(String key, double value) {
		this.data.put(key, new TagDouble(value));
	}
	
	public String getString(String key) {
		TagString tag = get(key, TagString.class);
		return tag == null ? null : tag.getString();
	}

	public void putString(String key, String value) {
		if (value == null) 
			this.data.remove(key);
		else
			this.data.put(key, new TagString(value));
	}
	
	public byte[] getByteArray(String key) {
		TagByteArray tag = get(key, TagByteArray.class);
		return tag == null ? null : tag.getArray();
	}

	public void putByteArray(String key, byte[] value) {
		if (value == null) 
			this.data.remove(key);
		else
			this.data.put(key, new TagByteArray(value));
	}
	
	public int[] getIntArray(String key) {
		TagIntArray tag = get(key, TagIntArray.class);
		return tag == null ? null : tag.getArray();
	}

	public void putIntArray(String key, int[] value) {
		if (value == null) 
			this.data.remove(key);
		else
			this.data.put(key, new TagIntArray(value));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ITagBase> List<T> getList(String key, Class<T> listTagClass) {
		TagList tag = get(key, TagList.class);
		if (tag == null) {
			return null;
		} else if (listTagClass.isInstance(tag.getListType().create())) {
			return (List<T>) tag.getList();
		} else if (tag.getListType() == TagType.END) {
			return new ArrayList<>();
		} else {
			return null;
		}
	}

	public List<ITagBase> getList(String key) {
		return getList(key, ITagBase.class);
	}
	
	public <T extends ITagBase> void putList(String key, List<T> list) {
		this.data.put(key, new TagList(list));
	}

	public <T extends ITagBase> void putList(String key, List<T> list, TagType fallbackType) {
		this.data.put(key, new TagList(list, fallbackType));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int i = 0;
		for (String key : this.data.keySet()) {
			sb.append("\"").append(key).append("\": ").append(this.data.get(key).toString());
			if (i < this.data.size() - 1) sb.append(", ");
			i++;
		}
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagCompound other) {
			return this.data.equals(other.data);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.data.hashCode();
	}
	
}
