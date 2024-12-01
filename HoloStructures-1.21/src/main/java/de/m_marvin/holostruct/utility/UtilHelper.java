package de.m_marvin.holostruct.utility;

import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

public class UtilHelper {
	
	public static BlockPos toBlockPos(Vec3 position) {
		return new BlockPos((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z));
	}
	
	public static Vec3d rotate(Vec3d pos, Rotation rotation) {
        switch(rotation) {
	        case NONE:
	        default:
	            return pos;
	        case CLOCKWISE_90:
	            return new Vec3d(-pos.getZ(), pos.getY(), pos.getX());
	        case CLOCKWISE_180:
	            return new Vec3d(-pos.getX(), pos.getY(), -pos.getZ());
	        case COUNTERCLOCKWISE_90:
	            return new Vec3d(pos.getZ(), pos.getY(), -pos.getX());
	    }
	}
	
	public static float rotate(float yrot, Rotation rotation) {
        switch(rotation) {
	        case NONE:
	        default:
	            return yrot;
	        case CLOCKWISE_90:
	            return yrot + 90;
	        case CLOCKWISE_180:
	            return yrot + 180;
	        case COUNTERCLOCKWISE_90:
	            return yrot - 90;
	    }
	}
	
}
 