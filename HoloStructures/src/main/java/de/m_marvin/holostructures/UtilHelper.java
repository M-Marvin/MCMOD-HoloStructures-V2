package de.m_marvin.holostructures;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.m_marvin.holostructures.client.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

public class UtilHelper {

	public static BlockPos loadBlockPos(CompoundTag nbt, String name) {
		ListTag posArr = nbt.getList(name, 3);
		if (posArr.size() != 3) {
			throw new IllegalArgumentException("Tag " + name + " (length=" + posArr.size() + ") is not a valid BlockPos!");
		}
		return new BlockPos(posArr.getInt(0), posArr.getInt(1), posArr.getInt(2));
	}

	public static ListTag saveBlockPos(Vec3i pos) {
		ListTag tag = new ListTag();
		tag.add(IntTag.valueOf(pos.getX()));
		tag.add(IntTag.valueOf(pos.getY()));
		tag.add(IntTag.valueOf(pos.getZ()));
		return tag;
	}

	public static Vec3 loadVecPos(CompoundTag nbt, String name) {
		ListTag posArr = nbt.getList(name, 6);
		if (posArr.size() != 3) throw new IllegalArgumentException("Tag " + name + " (length=" + posArr.size() + ") is not a valid BlockPos!");
		return new Vec3(posArr.getDouble(0), posArr.getDouble(1), posArr.getDouble(2));
	}

	public static ListTag saveVecPos(Vec3 pos) {
		ListTag nbt = new ListTag();
		nbt.add(DoubleTag.valueOf(pos.x()));
		nbt.add(DoubleTag.valueOf(pos.y()));
		nbt.add(DoubleTag.valueOf(pos.z()));
		return nbt;
	}

	public static String formatBlockPos(Vec3i pos) {
		return pos.getX() + " " + pos.getY() + " " + pos.getZ();
	}

	public static String formatVecPos(Vec3 pos) {
		return pos.x() + " " + pos.y() + " " + pos.z();
	}
	
	public static String formatBlockState(BlockState state) {
		return state.toString().replace("Block{", "").replace("}", "");
	}
	
	public static CompoundTag encryptNBTFromResponse(Component commandResponse) {
		try {
			String s = commandResponse.getString();
			int i = s.indexOf('{');
			if (i == -1) return new CompoundTag();
			s = s.substring(i, s.length());
			return TagParser.parseTag(s);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			return new CompoundTag();
		}
	}
	
	public static void sequentialCopy(CompoundTag sourceTag, BiConsumer<String, Tag> tagTarget, BiConsumer<String, Tag> listTarget, String targetPath) {
		sourceTag.getAllKeys().forEach((key) -> {
			Tag value = sourceTag.get(key);
			if (value instanceof CompoundTag subSource) {
				sequentialCopy(subSource, tagTarget, listTarget, appandTagPath(targetPath, key));
			} else if (value instanceof ListTag subSource) {
				tagTarget.accept(appandTagPath(targetPath, key), new ListTag());
				subSource.forEach((entryTag) -> {
					listTarget.accept(appandTagPath(targetPath, key) ,entryTag);
				});
			} else {
				tagTarget.accept(appandTagPath(targetPath, key), value);
			}
		});		
	}
	
	public static String appandTagPath(String path, String s) {
		return path + (path.isEmpty() ? "" : ".") + s;
	}
	
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
	
}
 