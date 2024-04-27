package de.m_marvin.holostruct.plugin;

import org.bukkit.entity.Player;

public class Config {
	
	public String config = null;
	
	public String getConfigForPlayer(Player player) {
		return this.config;
	}

	public void loadDefaultIfEmpty(String defaultConfig) {
		if (this.config == null) {
			this.config = defaultConfig;
		}
	}
	
}
