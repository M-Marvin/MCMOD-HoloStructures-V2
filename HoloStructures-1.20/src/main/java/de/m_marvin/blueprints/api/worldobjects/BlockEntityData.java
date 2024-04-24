package de.m_marvin.blueprints.api.worldobjects;

import java.util.Objects;

import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.univec.impl.Vec3i;

public class BlockEntityData {
	
	protected Vec3i position;
	protected RegistryName typeName;
	protected TagCompound data;
	
	public BlockEntityData(Vec3i position, RegistryName typeName) {
		this.position = position;
		this.typeName = typeName;
		this.data = new TagCompound();
	}
	
	public void setPosition(Vec3i position) {
		this.position = position;
	}
	
	public Vec3i getPosition() {
		return position;
	}
	
	public void setData(TagCompound data) {
		TypeConverter.BLOCK_ENTITY_META_FILTER.accept(data);
		this.data = data;
	}
	
	public TagCompound getData() {
		return data;
	}

	public RegistryName getTypeName() {
		return typeName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlockEntityData other) {
			return	other.typeName.equals(this.typeName) &&
					other.data.equals(this.data);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.position, this.typeName, this.data);
	}
	
	@Override
	public String toString() {
		return "BlockEntity{name=" + this.typeName + ",pos=" + this.position + ",nbt=" + this.data + "}";
	}
	
}
