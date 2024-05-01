package de.m_marvin.blueprints.api.worldobjects;

import java.util.Objects;

import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.univec.impl.Vec3i;

/**
 * This class represents an block entitiy's data.<br>
 * It contains its block position, its registry name and its nbt data compound
 * 
 * @author Marvin Koehler
 */
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
	
	/**
	 * <b>NOTE</b>: This method filters out some of the fields of the nbt data, since they would be duplicates of the {@link BlockEntityData#position} and {@link BlockEntityData#registryName} fields.
	 */
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
	
	/**
	 * <b>NOTE</b>: Position is ignored, only nbt and registry name are compared
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlockEntityData other) {
			return	other.typeName.equals(this.typeName) &&
					other.data.equals(this.data);
		}
		return false;
	}

	/**
	 * <b>NOTE</b>: Position is ignored, only nbt and registry name are considered
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.typeName, this.data);
	}
	
	@Override
	public String toString() {
		return "BlockEntity{name=" + this.typeName + ",pos=" + this.position + ",nbt=" + this.data + "}";
	}
	
}
