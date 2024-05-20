package de.m_marvin.blueprints.api.worldobjects;

import java.util.Objects;
import java.util.function.Function;

import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.univec.impl.Vec3d;

/**
 * This class represents an entitiy's data.<br>
 * It contains its position, its registry name and its nbt data compound
 * 
 * @author Marvin Koehler
 */
public class EntityData {
	
	public static final Function<TagCompound, TagCompound> ENTITY_META_FILTER = tag -> {
		tag.removeTag("id");
		return tag;
	};
	
	protected Vec3d position;
	protected RegistryName entityName;
	protected TagCompound data;
	
	public EntityData(Vec3d position, RegistryName entityName) {
		this.position = position;
		this.entityName = entityName;
	}

	/**
	 * <b>NOTE</b>: This method filters out some of the fields of the nbt data, since they would be duplicates of the {@link EntityData#position} and {@link EntityData#entityName} fields.
	 */
	public void setData(TagCompound data) {
		this.data = ENTITY_META_FILTER.apply(data);
	}
	
	public TagCompound getData() {
		return data;
	}
	
	public void setPosition(Vec3d position) {
		this.position = position;
	}
	
	public Vec3d getPosition() {
		return position;
	}

	public RegistryName getEntityName() {
		return entityName;
	}

	/**
	 * <b>NOTE</b>: Position is ignored, only nbt and registry name are compared
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityData other) {
			return	other.entityName.equals(this.entityName) &&
					other.data.equals(this.data);
		}
		return false;
	}

	/**
	 * <b>NOTE</b>: Position is ignored, only nbt and registry name are considered
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.entityName, this.data);
	}
	
	@Override
	public String toString() {
		return "Entity{name=" + this.entityName + ",pos=" + this.position + ",nbt=" + this.data + "}";
	}
	
}
