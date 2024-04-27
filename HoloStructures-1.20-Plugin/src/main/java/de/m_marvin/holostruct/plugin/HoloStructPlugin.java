package de.m_marvin.holostruct.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class HoloStructPlugin extends JavaPlugin implements Listener, PluginMessageListener {
	
	public static final String HOLOSTRUCT_GET_CONFIG = "holostruct:querry_access_permissions";
	public static final String HOLOSTRUCT_SET_CONFIG = "holostruct:send_access_permissons";
	public static final String CONFIG_FILE = "holostruct-server.toml";
	
	public Config configuration = new Config(new File(this.getDataFolder(), CONFIG_FILE));
	
	@Override
	public void onEnable() {
		System.out.println("HS2/Permisson Plugin enabled");
		if (!this.getDataFolder().isDirectory()) this.getDataFolder().mkdir();
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, HOLOSTRUCT_SET_CONFIG);
		this.getServer().getMessenger().registerIncomingPluginChannel(this, HOLOSTRUCT_GET_CONFIG, this);
	}
	
	@Override
	public void onDisable() {
		System.out.println("HS2/Permisson Plugin disabled");
		this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, HOLOSTRUCT_SET_CONFIG);
		this.getServer().getMessenger().unregisterIncomingPluginChannel(this, HOLOSTRUCT_GET_CONFIG, this);
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (channel.equals(HOLOSTRUCT_GET_CONFIG)) {
			System.out.println(String.format("HS2/Permissons player '%s' requested permisson config.", player.getName()));
			try {
				// Read default config from incoming message
				DataInput reader = new DataInputStream(new ByteArrayInputStream(message));
				String defaultConfig = UTF8Helper.readString(reader);
				
				// Load default it required
				this.configuration.loadDefaultIfEmpty(defaultConfig);
				
				sendConfigToPlayer(player);
			} catch (IOException e) {
				System.out.println("HS2/Permissons io exception while reading/writing config bytes!");
				e.printStackTrace();
			}
		}
	}
	
	public void sendConfigToPlayer(Player player) {
		try {
			String configString = this.configuration.getConfigForPlayer(player);
			
			// Write current config for the outgoing message
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			DataOutput writer = new DataOutputStream(buffer);
			UTF8Helper.writeString(writer, configString);
			
			// Send message to client
			player.sendPluginMessage(this, HOLOSTRUCT_SET_CONFIG, buffer.toByteArray());
		} catch (IOException e) {
			System.out.println("HS2/Permissons io exception while writing config bytes!");
			e.printStackTrace();
		}
	}
	
}
