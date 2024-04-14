package de.m_marvin.holostructures.blueprints;

import java.util.HashMap;
import java.util.Map;

import de.m_marvin.blueprints.api.Blueprint;

public class BlueprintManager {
	
	private Map<String, Blueprint> loadedBlueprints = new HashMap<>();
	
	public Blueprint getLoadedBlueprint(String name) {
		return this.loadedBlueprints.get(name);
	}
	
	public void setLoadedBlueprint(String name, Blueprint blueprint) {
		this.loadedBlueprints.put(name, blueprint);
	}
	
	public void unloadBlueprint(String name) {
		this.loadedBlueprints.remove(name);
	}
	
	public Map<String, Blueprint> getLoadedBlueprints() {
		return loadedBlueprints;
	}
	
}
