package de.m_marvin.blueprints.api.worldobjects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.m_marvin.blueprints.api.RegistryName;

/**
 * This class represents an block's data.<br>
 * It contains its registry name and its block state properties as string2string map.
 * 
 * @author Marvin Koehler
 */
public class BlockStateData {
	
	public static final List<RegistryName> AIR_BLOCKS = Arrays.asList(new RegistryName("minecraft:air"), new RegistryName("minecraft:void_air"), new RegistryName("minecraft:cave_air"));
	
	protected RegistryName blockName;
	protected Map<String, String> properties = new HashMap<>();
	
	public BlockStateData(RegistryName blockName) {
		this.blockName = blockName;
	}
	
	public RegistryName getBlockName() {
		return blockName;
	}
	
	public String getValue(String propertyName) {
		return this.properties.getOrDefault(propertyName, "");
	}
	
	public void setValue(String propertyName, String value) {
		this.properties.put(propertyName, value);
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	/**
	 * This method just checks if the blocks registry name is within the list {@link BlockStateData#AIR_BLOCKS}
	 * @return
	 */
	public boolean isAir() {
		return AIR_BLOCKS.contains(this.blockName);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlockStateData other) {
			return	other.blockName.equals(this.blockName) &&
					other.properties.equals(this.properties);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.blockName, this.properties);
	}
	
	@Override
	public String toString() {
		return "BlockState{name=" + this.blockName + ",properties=" + this.properties.toString() + "}";
	}
	
}
