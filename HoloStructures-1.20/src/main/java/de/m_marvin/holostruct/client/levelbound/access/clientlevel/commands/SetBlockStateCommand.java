package de.m_marvin.holostruct.client.levelbound.access.clientlevel.commands;

import java.util.Optional;

import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.univec.impl.Vec3i;

public class SetBlockStateCommand extends Command<Boolean> {
	
	private final Vec3i position;
	private final BlockStateData data;
	
	public SetBlockStateCommand(Vec3i pos, BlockStateData blockState) {
		this.position = pos;
		this.data = blockState;
	}
	
	@Override
	public String command() {
		StringBuilder sb = new StringBuilder();
		this.data.getProperties().forEach((key, value) -> sb.append(key).append("=").append(value).append(","));
		String propertyString = sb.toString().substring(0, Math.max(0, sb.toString().length() - 1));
		
		return String.format("setblock %d %d %d %s[%s]", 
				this.position.x,
				this.position.y,
				this.position.z,
				this.data.getBlockName().toString(),
				propertyString);
	}

	@Override
	public Optional<Boolean> tryParseResponse(Response response) {
		if (response.getResponseKey().equals("commands.setblock.success")) {
			return Optional.of(true);
		} else if (response.getResponseKey().equals("commands.setblock.failed")) {
			return Optional.of(true);
		} else if (response.getResponseKey().equals("argument.pos.unloaded")) {
			return Optional.of(false);
		}
		
		return Optional.empty();
	}
	
}
