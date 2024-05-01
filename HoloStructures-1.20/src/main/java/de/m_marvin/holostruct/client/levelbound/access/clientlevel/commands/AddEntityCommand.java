package de.m_marvin.holostruct.client.levelbound.access.clientlevel.commands;

import java.util.Locale;
import java.util.Optional;

import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.univec.impl.Vec3d;

public class AddEntityCommand extends Command<Boolean> {
	
	private final Vec3d position;
	private final EntityData data;
	private final String entityDisplayName;
	
	public AddEntityCommand(EntityData entityData) {
		this.position = entityData.getPosition();
		this.data = entityData;
		this.entityDisplayName = TypeConverter.data2entity(data).getDisplayName().getString();
		
	}
	
	@Override
	public String command() {
		return String.format(Locale.US, "summon %s %f %f %f %s", 
				this.data.getEntityName().toString(), 
				this.position.x, 
				this.position.y, 
				this.position.z, 
				this.data.getData() == null ? "{}" : this.data.getData().toString());
	}
	
	@Override
	public Optional<Boolean> tryParseResponse(Response response) {
		if (response.getResponseKey().equals("commands.summon.success")) {
			String entityName = response.getResponseValues().get(0);
			if (entityName.equals(this.entityDisplayName)) {
				System.out.println("Summoned entity " + entityName);
				return Optional.of(true);
			}
		}
		return Optional.empty();
	}

}
