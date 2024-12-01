package de.m_marvin.blueprints.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.univec.impl.Vec3i;

/**
 * This class is a container for the blocks, blockentities and entities of a section copied from the world.
 * The data can be accessed trough the methods defined in {@link IStructAccessor}.
 * @author marvi
 *
 */
public class Blueprint implements IBlueprintAcessor {

	protected Vec3i boundsMin;
	protected Vec3i boundsMax;
	protected Vec3i offset;
	protected Multimap<Vec3i, EntityData> pos2entity;
	protected Map<Vec3i, BlockEntityData> pos2blockEntity;
	protected Map<Vec3i, BlockStateData> pos2state;
	
	protected List<String> parsingErrors;
	
	/**
	 * Constructs a new empty blueprint with the dimensions 1x1x1
	 */
	public Blueprint() {
		this.boundsMax = new Vec3i(1, 1, 1);
		this.boundsMin = new Vec3i(0, 0, 0);
		this.offset = new Vec3i(0, 0, 0);
		this.pos2entity = ArrayListMultimap.create();
		this.pos2blockEntity = new HashMap<>();
		this.pos2state = new HashMap<>();
		this.parsingErrors = new ArrayList<>();
	}
	
	/**
	 * Returns a list of errors generatedby the last parsing or writing operation of this blueprint from or to an file
	 * @return A list of strings representing the individual error
	 */
	public List<String> getParsingErrors() {
		return parsingErrors;
	}
	
	@Override
	public void logParseWarn(String errorMessage) {
		this.parsingErrors.add(errorMessage);
	}
	
	@Override
	public void clearParseLogs() {
		this.parsingErrors.clear();
	}
	
	@Override
	public void setBounds(Vec3i min, Vec3i max) {
		this.boundsMin = min;
		this.boundsMax = max;
	}

	@Override
	public Vec3i getBoundsMin() {
		return this.boundsMin;
	}

	@Override
	public Vec3i getBoundsMax() {
		return this.boundsMax;
	}

	@Override
	public void setOffset(Vec3i offset) {
		this.offset = offset;
	}

	@Override
	public Vec3i getOffset() {
		return this.offset;
	}
	
	@Override
	public void setBlock(Vec3i positon, BlockStateData state) {
		this.pos2state.put(positon, state);
	}

	@Override
	public BlockStateData getBlock(Vec3i position) {
		return this.pos2state.get(position);
	}

	@Override
	public void setBlockEntity(Vec3i position, BlockEntityData blockEntity) {
		this.pos2blockEntity.put(position, blockEntity);
	}

	@Override
	public BlockEntityData getBlockEntity(Vec3i position) {
		return this.pos2blockEntity.get(position);
	}

	@Override
	public void addEntity(EntityData entity) {
		Vec3i blockPos = new Vec3i(entity.getPosition());
		this.pos2entity.put(blockPos, entity);
	}
	
	@Override
	public void addEntity(Vec3i blockPos, EntityData entity) {
		this.pos2entity.put(blockPos, entity);
	}
	
	@Override
	public void addEntities(Collection<EntityData> entities) {
		entities.forEach(this::addEntity);
	}
	
	@Override
	public Collection<EntityData> getEntities() {
		return this.pos2entity.values();
	}
	
	@Override
	public Collection<EntityData> getEntitiesOnBlock(Vec3i pos) {
		return this.pos2entity.get(pos);
	}
	
	@Override
	public Collection<EntityData> getEntitiesWithin(Vec3i min, Vec3i max) {
		return this.pos2entity.entries().stream()
			.filter(entry -> isWithin(entry.getKey(), min, max))
			.map(entry -> entry.getValue())
			.toList();
	}
	
	protected boolean isWithin(Vec3i pos, Vec3i min, Vec3i max) {
		return min.min(pos).equals(min) && max.max(pos).equals(max);
	}
	
	@Override
	public void copyTo(IBlueprintAcessor target) {
		target.setBounds(this.boundsMin, this.boundsMax);
		target.setOffset(this.offset);
		for (Vec3i position : this.pos2state.keySet()) {
			target.setBlock(position, this.pos2state.get(position));
		}
		for (Vec3i position : this.pos2blockEntity.keySet()) {
			target.setBlockEntity(position, this.pos2blockEntity.get(position));
		}
		target.addEntities(this.pos2entity.values());
	}
	
}
