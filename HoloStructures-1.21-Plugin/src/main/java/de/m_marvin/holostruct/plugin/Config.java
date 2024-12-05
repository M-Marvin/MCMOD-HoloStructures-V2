package de.m_marvin.holostruct.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.entity.Player;

public class Config {
	
	public File configFile;
	
	public Config(File configFile) {
		this.configFile = configFile;
	}
	
	public String getConfigForPlayer(Player player) {
		try {
			InputStream input = new FileInputStream(configFile);
			String config = new String(input.readAllBytes());
			input.close();
			return config;
		} catch (IOException e) {
			System.err.println("Failed to read config file!");
			e.printStackTrace();
			return "";
		}
	}

	public void loadDefaultIfEmpty(String defaultConfig) {
		if (this.configFile.isFile()) return;
		try {
			OutputStream output = new FileOutputStream(this.configFile);
			output.write(defaultConfig.getBytes());
			output.close();
		} catch (IOException e) {
			System.err.println("Could not write config file!");
			e.printStackTrace();
		}
	}
	
}
