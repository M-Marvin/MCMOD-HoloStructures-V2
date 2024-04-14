package de.m_marvin.holostructures.client.levelbound.access.clientlevel;

import java.util.Collection;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostructures.client.levelbound.access.ILevelAccessor;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.client.Minecraft;

public class ClientLevelAccessorImpl implements ILevelAccessor {

	private Minecraft minecraft;
	
	public ClientLevelAccessorImpl(Minecraft minecraft) {
		this.minecraft = minecraft;
	}
	
	@Override
	public void setBlock(Vec3i positon, BlockStateData state) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public BlockStateData getBlock(Vec3i position) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public BlockEntityData getBlockEntity(Vec3i position) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addEntity(EntityData entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addEntity(Vec3i blockPos, EntityData entity) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addEntities(Collection<EntityData> entities) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntities() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
}
