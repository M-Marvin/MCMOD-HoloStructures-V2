package de.m_marvin.holostructures;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class UtilHelper {

	public static String formatBlockPos(BlockPos pos) {
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
	
}
 