package de.m_marvin.holostructures.client.levelbound.access.clientlevel;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.univec.impl.Vec3i;

public class LevelCommandBuilder {
	
	public static String buildPlaceBlock(Vec3i position, BlockStateData state) {
		StringBuilder psb = new StringBuilder();
		int i = 0;
		for (String prop : state.getProperties().keySet()) {
			psb.append(prop).append("=").append(state.getValue(prop));
			if (++i < state.getProperties().size()) psb.append(",");
		}
		return String.format("/setblock %d %d %d %s{%s}", position.x, position.y, position.z, state.getBlockName().toString(), psb.toString());
	}
	
	public static String buildSetBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		return String.format("/data block merge %d %d %d %d", position.x, position.y, position.z, blockEntity.getData().toString());
	}
	
}
