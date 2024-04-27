package de.m_marvin.holostruct.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class UtilHelper {
	
	public static BlockPos toBlockPos(Vec3 position) {
		return new BlockPos((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z));
	}
	
}
 