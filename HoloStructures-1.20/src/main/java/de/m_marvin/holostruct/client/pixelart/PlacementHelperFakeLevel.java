package de.m_marvin.holostruct.client.pixelart;

import java.util.List;
import java.util.function.Function;

import com.google.common.base.Predicate;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

public class PlacementHelperFakeLevel extends Level {
	
	public static final Function<Level, PlacementHelperFakeLevel> FAKE_LEVEL = Util.memoize(PlacementHelperFakeLevel::new);
	public static final BlockPos FAKE_BLOCK_POS = BlockPos.ZERO;
	
	public static BlockState getStablePlacementState(Level level, Block block, Predicate<BlockState> predicate) {
		PlacementHelperFakeLevel flevel = FAKE_LEVEL.apply(level);
		for (Direction d : Direction.values()) {
			BlockHitResult hit = new BlockHitResult(new Vec3(0.5, 1, 0.5), d, FAKE_BLOCK_POS.relative(Direction.UP), false);
			BlockPlaceContext context = new BlockPlaceContext(flevel, level.players().size() > 0 ? level.players().get(0) : null, InteractionHand.MAIN_HAND, new ItemStack(block), hit);
			BlockState state = block.getStateForPlacement(context);
			if (state != null && predicate.apply(state)) return state;
		}
		return block.defaultBlockState();
	}
	
	private final Level level;
	
	protected PlacementHelperFakeLevel(Level level) {
		super(null, level.dimension(), level.registryAccess(), level.dimensionTypeRegistration(), level.getProfilerSupplier(), true, false, 0, 0);
		this.level = level;
	}

	@Override
	public BlockState getBlockState(BlockPos pPos) {
		if (pPos.equals(FAKE_BLOCK_POS)) {
			return Blocks.STONE.defaultBlockState();
		}
		return Blocks.AIR.defaultBlockState();
	}
	
	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return level.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return level.getFluidTicks();
	}

	@Override
	public ChunkSource getChunkSource() {
		return level.getChunkSource();
	}

	@Override
	public void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData) {}

	@Override
	public void gameEvent(GameEvent pEvent, Vec3 pPosition, Context pContext) {}

	@Override
	public List<? extends Player> players() {
		return level.players();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
		return level.getUncachedNoiseBiome(pX, pY, pZ);
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return level.enabledFeatures();
	}

	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return level.getShade(pDirection, pShade);
	}

	@Override
	public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {}

	@Override
	public void playSeededSound(Player pPlayer, double pX, double pY, double pZ, Holder<SoundEvent> pSound,
			SoundSource pCategory, float pVolume, float pPitch, long pSeed) {}

	@Override
	public void playSeededSound(Player pPlayer, Entity pEntity, Holder<SoundEvent> pSound, SoundSource pCategory,
			float pVolume, float pPitch, long pSeed) {}

	@Override
	public String gatherChunkSourceStats() {
		return level.gatherChunkSourceStats();
	}

	@Override
	public Entity getEntity(int pId) {
		return level.getEntity(pId);
	}

	@Override
	public TickRateManager tickRateManager() {
		return level.tickRateManager();
	}

	@Override
	public MapItemSavedData getMapData(String pMapName) {
		return level.getMapData(pMapName);
	}

	@Override
	public void setMapData(String pMapName, MapItemSavedData pData) {}

	@Override
	public int getFreeMapId() {
		return level.getFreeMapId();
	}

	@Override
	public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {}

	@Override
	public Scoreboard getScoreboard() {
		return level.getScoreboard();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return level.getRecipeManager();
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return null;
	}
	
}
