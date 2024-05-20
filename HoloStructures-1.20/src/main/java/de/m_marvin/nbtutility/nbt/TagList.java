package de.m_marvin.nbtutility.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.m_marvin.nbtutility.TagType;

public class TagList implements ITagBase {

	private TagType fallback;
	private TagType type;
	private List<ITagBase> data; 
	
	public TagList() {
		this(new ArrayList<>());
	}

	public TagList(TagType fallbackType) {
		this(new ArrayList<>(), fallbackType);
	}

	public TagList(List<? extends ITagBase> data) {
		this(data, TagType.END);
	}
	
	public TagList(List<? extends ITagBase> data, TagType fallbackType) {
		this.data = new ArrayList<>();
		this.data.addAll(data);
		this.fallback = fallbackType;
		boolean b = checkConsistency();
		assert b : "inconsistent list types!";
	}
	
	protected boolean checkConsistency() {
		if (this.data.isEmpty()) {
			this.type = this.fallback;
			return true;
		} else {
			this.type = this.data.get(0).getType();
			for (ITagBase entry : this.data) {
				if (entry.getType() != this.type) return false;
			}
			return true;
		}
	}
	
	@Override
	public void read(DataInput input) throws IOException {
		this.type = TagType.byId(input.readByte());
		int len = input.readInt();
		
		if (this.type == TagType.END && len > 0) {
			throw new IOException("missing type on tag list!");
		} else {
			this.data.clear();
			for (int i = 0; i < len; i++) {
				ITagBase item = this.type.create();
				item.read(input);
				this.data.add(item);
			}
		}
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
		boolean b = checkConsistency();
		assert b : "inconsistent list types!";
		output.writeByte(this.type.getId());
		output.writeInt(this.data.size());
		for (int i = 0; i < this.data.size(); i++) {
			this.data.get(i).write(output);
		}
	}
	
	@Override
	public TagType getType() {
		return TagType.TAG_LIST;
	}
	
	public TagType getListType() {
		return this.type;
	}
	
	public List<ITagBase> getList() {
		return this.data;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < this.data.size(); i++) {
			sb.append(this.data.get(i).toString());
			if (i < this.data.size() - 1) sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagList other) {
			return this.data.equals(other.data);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.data.hashCode();
	}
	
}
