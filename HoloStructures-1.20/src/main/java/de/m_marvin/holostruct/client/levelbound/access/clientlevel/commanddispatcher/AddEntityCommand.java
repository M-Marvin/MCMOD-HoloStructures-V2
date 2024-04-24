package de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher;

import java.util.Locale;

import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.univec.impl.Vec3d;

public class AddEntityCommand extends Command<Boolean> {
	
	private Vec3d position;
	private EntityData data;
	
	public AddEntityCommand(EntityData entityData) {
		this.position = entityData.getPosition();
		this.data = entityData;
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
	public Boolean parseResult(String result) {
		// TODO Auto-generated method stub
		System.out.println("CMD: " + result);
		return null;
	}

}
