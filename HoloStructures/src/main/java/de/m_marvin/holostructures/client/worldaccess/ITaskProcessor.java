package de.m_marvin.holostructures.client.worldaccess;

import com.google.common.base.Optional;
import com.mojang.brigadier.CommandDispatcher;

import de.m_marvin.holostructures.ILevelAccessor;
import de.m_marvin.holostructures.client.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;

public interface ITaskProcessor {
	
	public default Optional<ILevelAccessor> getAccessor() {
		return ClientHandler.getInstance().getLevelAccessor();
	}
	
	@SuppressWarnings("resource")
	public default boolean hasOperator() {
		return Minecraft.getInstance().player.getPermissionLevel() >= 4;
	}
	
	public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher);
	public boolean canOperate();
	
}
