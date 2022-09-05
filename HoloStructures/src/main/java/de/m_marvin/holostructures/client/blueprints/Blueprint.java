package de.m_marvin.holostructures.client.blueprints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.ILevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Blueprint {
	
	protected String name;
	protected BlockPos origin;
	protected Vec3i size;
	protected List<BlockState> states;
	protected Map<BlockPos, Integer> statemap;
	protected Map<BlockPos, EntityData> blockentities;
	protected MultiValuedMap<Vec3, EntityData> entities;
	
	public static record EntityData(ResourceLocation type, Supplier<Optional<CompoundTag>> nbt) {}
	
	public Blueprint() {
		this.name = "";
		this.origin = BlockPos.ZERO;
		this.states = new ArrayList<>();
		this.statemap = new HashMap<>();
		this.blockentities = new HashMap<>();
		this.entities = new ArrayListValuedHashMap<>();
	}
	
	public static Blueprint createBlueprint(ILevelAccessor accessor, BlockPos corner1, BlockPos corner2, boolean includeEntities) {
		Vec3i from = new Vec3i(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
		Vec3i to = new Vec3i(Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));
		Blueprint blueprint = new Blueprint();
		
		blueprint.size = to.subtract(from);
		for (int x = from.getX(); x <= to.getX(); x++) {
			for (int z = from.getZ(); z <= to.getZ(); z++) {
				for (int y = from.getY(); y <= to.getY(); y++) {
					BlockPos position = new BlockPos(x, y, z);
					BlockState state = accessor.getBlock(position);
					int stateId = blueprint.mapState(state);
					blueprint.setState(position.subtract(from), stateId);
					
					if (state.hasBlockEntity()) {
						Optional<EntityData> blockEntity = accessor.getBlockEntity(position);
						if (blockEntity.isPresent()) blueprint.addBlockEntity(position.subtract(from), blockEntity.get());
					}
				}
			}
		}
		
		if (includeEntities) {
			Map<Vec3, EntityData> entities = accessor.getEntities(corner1, corner2, (position) -> position.subtract(from.getX(), from.getY(), from.getZ()));
			entities.forEach(blueprint::addEntity);
		}
		
		return blueprint;
	}
	
	public boolean pasteBlueprint(ILevelAccessor accessor, BlockPos originPosition, boolean pasteEntities) {
		Vec3i from = originPosition.subtract(this.origin);
		Vec3i to = from.offset(this.size);
		
		for (int placeInstable = 0; placeInstable <= 1; placeInstable++) {
			for (int x = from.getX(); x <= to.getX(); x++) {
				for (int z = from.getZ(); z <= to.getZ(); z++) {
					for (int y = from.getY(); y <= to.getY(); y++) {
						BlockPos position = new BlockPos(x, y, z);
						BlockState state = this.resolveState(this.getState(position.subtract(from)));
						if (state.isCollisionShapeFullBlock(accessor.getLevelGetter(), position) != placeInstable > 0) {
							accessor.setBlock(position, state);
						}
					}
				}
			}
		}
		
		int waitingTime = 0;
		while (!accessor.isDoneAccessing()) {
			try {
				waitingTime += 100;
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			if (waitingTime > 2000) {
				accessor.abbortWaiting();
			}
		}
		
		/* State validation section, trys to fix misconnected blockstates */
		
		List<BlockPos> invalidStates = new ArrayList<BlockPos>();
		for (int x = from.getX(); x <= to.getX(); x++) {
			for (int z = from.getZ(); z <= to.getZ(); z++) {
				for (int y = from.getY(); y <= to.getY(); y++) {
					BlockPos position = new BlockPos(x, y, z);
					BlockState state = this.resolveState(this.getState(position.subtract(from)));
					if (!accessor.checkBlock(position, state)) invalidStates.add(position);
				}
			}
		}
		
		boolean validStates = false;
		boolean validated = false;
		while (!validStates && HoloStructures.getInstance().getBlueprints().isWorking()) {
			validStates = validated;
			validated = true;
			Random random = new Random();
			List<BlockPos> list = new ArrayList<>();
			list.addAll(invalidStates);
			for (int i = 0; i < invalidStates.size(); i++) {
				int r = random.nextInt(list.size());
				BlockPos position = list.remove(r);
				BlockState state = this.resolveState(this.getState(position.subtract(from)));
				if (!accessor.checkBlock(position, state)) validated = false;
			}
		}
		
		/* End of validation section */
		
		this.blockentities.forEach((pos, blockEntity) -> accessor.setBlockEntity(pos.offset(from), blockEntity));
		
		if (pasteEntities && this.entities.size() > 0) {
			this.entities.keySet().forEach((position) -> {
				this.entities.get(position).forEach((entity) -> accessor.addEntity(position, entity));
			});
		}
		
		return validated;
	}
	
	public int mapState(BlockState state) {
		if (this.states.contains(state)) {
			return this.states.indexOf(state);
		} else {
			this.states.add(state);
			return this.states.size() - 1;
		}
	}
	
	public void setState(BlockPos pos, int stateId) {
		this.statemap.put(pos, stateId);
	}
	
	public int getState(BlockPos pos) {
		return this.statemap.getOrDefault(pos, 0);
	}
	
	public BlockState resolveState(int stateId) {
		if (stateId > this.states.size() - 1) return Blocks.AIR.defaultBlockState();
		return this.states.get(stateId);
	}
	
	public void addBlockEntity(BlockPos pos, EntityData entityData) {
		this.blockentities.put(pos, entityData);
	}
	
	public void addEntity(Vec3 pos, EntityData entity) {
		this.entities.put(pos, entity);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setOrigin(BlockPos origin) {
		this.origin = origin;
	}
	
	public BlockPos getOrigin() {
		return origin;
	}
		
}
