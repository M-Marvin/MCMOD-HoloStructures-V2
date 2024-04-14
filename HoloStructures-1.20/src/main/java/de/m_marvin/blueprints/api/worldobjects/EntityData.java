package de.m_marvin.blueprints.api.worldobjects;

import java.util.Objects;

import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.univec.impl.Vec3d;

public class EntityData {
	
	protected Vec3d position;
	protected RegistryName entityName;
	protected TagCompound data;
	
	public EntityData(Vec3d position, RegistryName entityName) {
		this.position = position;
		this.entityName = entityName;
	}
	
	public void setData(TagCompound data) {
		this.data = data;
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityData other) {
			return	other.entityName.equals(this.entityName) &&
					other.position.equals(this.position) &&
					other.data.equals(this.data);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.position, this.entityName, this.data);
	}
	
	@Override
	public String toString() {
		return "Entity{name=" + this.entityName + ",pos=" + this.position + ",nbt=" + this.data + "}";
	}
	
}
