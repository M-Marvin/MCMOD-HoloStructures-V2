package de.m_marvin.holostruct.client.blueprints;

import java.util.HashMap;
import java.util.Map;

import de.m_marvin.blueprints.api.Blueprint;

/**
 * This class manages the loading and saving of blueprints.
 * Contains a map holding all currently loaded blueprints with their names.
 * @author Marvin Koehler
 */
public class BlueprintManager {
	
	private Map<String, Blueprint> loadedBlueprints = new HashMap<>();
	
	/**
	 * Get the blueprint stored under the name.
	 * @param name The name of the blueprint
	 * @return The blueprint or null if none exists
	 */
	public Blueprint getLoadedBlueprint(String name) {
		return this.loadedBlueprints.get(name);
	}
	
	/**
	 * Stores the blueprint under the name.
	 * @param name The name for the blueprint
	 * @param blueprint The blueprint to store
	 */
	public void setLoadedBlueprint(String name, Blueprint blueprint) {
		this.loadedBlueprints.put(name, blueprint);
	}
	
	/**
	 * Discard the blueprint stored under the name.
	 * @param name The name of the blueprint
	 */
	public void unloadBlueprint(String name) {
		this.loadedBlueprints.remove(name);
	}
	
	/**
	 * Gets the map of names and blueprints.
	 * @return A name to blueprint map of all loaded blueprints
	 */
	public Map<String, Blueprint> getLoadedBlueprints() {
		return loadedBlueprints;
	}
	
}
