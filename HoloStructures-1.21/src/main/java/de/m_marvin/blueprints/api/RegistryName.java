package de.m_marvin.blueprints.api;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Represents an entry in the game registry.
 * Works the same as the ResourceLocation class

 * @author Marvin Köhler
 */
public class RegistryName {
	
	public static final Pattern REGISTRY_NAME_PATTERN = Pattern.compile("([a-z0-9_\\-]{1,})\\:([a-z0-9_\\-\\\\\\/]{1,})");
	
	private String namespace;
	private String name;
	
	public RegistryName(String name) {
		Optional<MatchResult> result = REGISTRY_NAME_PATTERN.matcher(name).results().findAny();
		assert result.isPresent() : "invalid registry name format!";
		this.namespace = result.get().group(1);
		this.name = result.get().group(2);
	}
	
	public RegistryName(String namespace, String name) {
		this.namespace = namespace;
		this.name = name;
	}
	
	public String getNamespace() {
		return namespace;
	}
	public String getName() {
		return name;
	}
	
	public RegistryName copy() {
		return new RegistryName(new String(this.namespace), new String(this.name));
	}
	
	@Override
	public String toString() {
		return this.namespace + ":" + this.name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RegistryName other) {
			return other.namespace.equals(this.namespace) && other.name.equals(this.name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.namespace, this.name);
	}
	
}
