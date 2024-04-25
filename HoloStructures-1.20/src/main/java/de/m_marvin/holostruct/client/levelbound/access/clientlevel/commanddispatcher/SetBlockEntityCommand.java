package de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher;

import java.util.Optional;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.univec.impl.Vec3i;

public class SetBlockEntityCommand extends Command<Boolean> {
	
	private final BlockEntityData data;
	
	public SetBlockEntityCommand(BlockEntityData blockEntity) {
		this.data = blockEntity;
	}
	
	@Override
	public String command() {
		return String.format("data merge block %d %d %d %s",
				this.data.getPosition().x,
				this.data.getPosition().y,
				this.data.getPosition().z,
				this.data.getData().toString());
	}

	@Override
	public Optional<Boolean> tryParseResponse(Response response) {
		if (response.getResponseKey().equals("commands.data.block.invalid")) {
			return Optional.of(false);
		} else if (response.getResponseKey().equals("commands.data.block.modified")) {
			Vec3i position = new Vec3i(
					Integer.parseInt(response.getResponseValues().get(0)),
					Integer.parseInt(response.getResponseValues().get(1)),
					Integer.parseInt(response.getResponseValues().get(2)));
			
			return Optional.of(position.equals(this.data.getPosition()));
		}
		
		return Optional.empty();
	}
	
}
