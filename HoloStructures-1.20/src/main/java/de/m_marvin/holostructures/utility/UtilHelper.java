package de.m_marvin.holostructures.utility;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.m_marvin.holostructures.client.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

public class UtilHelper {
	
	@SuppressWarnings("resource")
	public static File resolvePath(String path) {
		try {
			String gameDir = Minecraft.getInstance().gameDirectory.getCanonicalPath();
			IntegratedServer worldServer = Minecraft.getInstance().getSingleplayerServer();
			String levelName = worldServer == null ? "null" : worldServer.getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
			String resolvedPath = path.replace("{$worldname$}", levelName);
			return new File(gameDir, resolvedPath);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean checkBlockState(BlockState state, BlockState template) {
		if (state == null || template == null) return false;
		if (state.equals(template)) return true;
		if (state.getBlock() != template.getBlock()) return false;
		
		List<String> ignoredProps = Arrays.asList(Config.FIX_IGNORED_BLOCK_STATE_PROPS.get().split(","));
		
		for (Property<?> prop : state.getProperties()) {
			if (!ignoredProps.contains(prop.getName())) {
				Object value = state.getValue(prop);
				Object valueTemplate = template.getValue(prop);
				if (!value.equals(valueTemplate)) return false;
			}
		}
		
		return true;
	}

	public static BlockPos toBlockPos(Vec3 position) {
		return new BlockPos((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z));
	}
	
}
 