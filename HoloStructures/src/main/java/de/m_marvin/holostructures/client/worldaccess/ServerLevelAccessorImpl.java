package de.m_marvin.holostructures.client.worldaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Function;

import de.m_marvin.holostructures.ILevelAccessor;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import de.m_marvin.holostructures.client.blueprints.Blueprint.EntityData;
import de.m_marvin.holostructures.server.worldaccess.ServerLevelAccessHandler;
import de.m_marvin.holostructures.server.worldaccess.ServerLevelAccessHandler.RespondingMSG;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ServerLevelAccessorImpl implements ILevelAccessor {
	
	public static final String PROTOCOL_VERSION = "1.0";
	
	public Supplier<ClientLevel> level;
	public Supplier<LocalPlayer> player;
	public SimpleChannel network;

	public static class PackageResponse<T> {
		protected T response = null;
		public boolean isPresent() {
			return this.response != null;
		}
		public T getResponse() {
			return response;
		}
		public void setResponse(T response) {
			this.response = response;
		}
	}

	public static HashMap<Integer, PackageResponse<?>> pendingPackages = new HashMap<>();
	
	public ServerLevelAccessorImpl(SimpleChannel networkAccess, Supplier<ClientLevel> level, Supplier<LocalPlayer> player) {
		this.level = level;
		this.player = player;
		this.network = networkAccess;
	}
	
	public <T> PackageResponse<T> sendPackage(RespondingMSG<T> message) {
		this.network.send(PacketDistributor.SERVER.noArg(), message);
		PackageResponse<T> response = new PackageResponse<>();
		pendingPackages.put(message.hashCode(), response);
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public <T> void handleResponse(RespondingMSG<T> message) {
		PackageResponse<T> response = (PackageResponse<T>) pendingPackages.get(message.hashCode());
		if (response != null && message.getResponse().isPresent()) {
			response.response = message.getResponse().get();
			pendingPackages.remove(message.hashCode());
		}
	}
	
	public SimpleChannel getNetwork() {
		return network;
	}
	
	@Override
	public CommandSourceStack getChatTarget() {
		return this.player.get().createCommandSourceStack();
	}

	@Override
	public BlockGetter getLevelGetter() {
		return this.level.get();
	}

	@Override
	public boolean hasServerAccess() {
		return true;
	}

	@Override
	public boolean hasOPAccess() {
		return player.get().getPermissionLevel() >= 4;
	}

	@Override
	public boolean isDoneAccessing() {
		return pendingPackages.size() == 0;
	}
	
	@Override
	public void abbortWaiting() {
		pendingPackages.clear();
	}
	
	@Override
	public void setBlock(BlockPos pos, BlockState state) {
		sendPackage(new ServerLevelAccessHandler.WASetBlockState(pos, state));
	}

	@Override
	public BlockState getBlock(BlockPos pos) {
		return this.level.get().getBlockState(pos);
	}

	@Override
	public boolean checkBlock(BlockPos pos, BlockState state) {
		if (!this.level.get().getBlockState(pos).equals(state)) {
			sendPackage(new ServerLevelAccessHandler.WASetBlockState(pos, state));
			
			// Wait for response of the command to prevent a "overload" of the server with commands
			int waitingTime = 0;
			while (!isDoneAccessing()) {
				try {
					waitingTime += 1;
					Thread.sleep(1);
				} catch (ThreadDeath | InterruptedException e) {
					e.printStackTrace();
					break;
				}
				if (waitingTime > 2000) {
					abbortWaiting();
					return false;
				}
			}
			
			return this.level.get().getBlockState(pos).equals(state);
		}
		return true;
	}

	@Override
	public Optional<EntityData> getBlockEntity(BlockPos pos) {
		Optional<BlockEntity> blockEntity = Optional.ofNullable(this.level.get().getBlockEntity(pos));
		if (blockEntity.isPresent()) {
			PackageResponse<CompoundTag> response = sendPackage(new ServerLevelAccessHandler.WAGetBlockEntity(pos));
			Blueprint.EntityData blockEntityData = new Blueprint.EntityData(blockEntity.get().getType().getRegistryName(), 
					() -> response.isPresent() ? Optional.of(response.getResponse()) : Optional.empty());
			return Optional.of(blockEntityData);
		}
		return Optional.empty();
	}

	@Override
	public void setBlockEntity(BlockPos pos, EntityData data) {
		if (data.nbt().get().isPresent()) {
			sendPackage(new ServerLevelAccessHandler.WASetBlockEntity(pos, data));
		}
	}

	@Override
	public Map<Vec3, EntityData> getEntities(BlockPos corner1, BlockPos corner2, Function<Vec3, Vec3> positionMapper) {
		AABB aabb = new AABB(corner1, corner2);
		Function<CompoundTag, CompoundTag> nbtFilter = (nbt) -> {
			nbt.remove("Pos");
			return nbt;
		};
		Map<Vec3, Blueprint.EntityData> map = new HashMap<>();
		this.level.get().getEntities(null, aabb).forEach((entity) -> {
			Vec3 position = positionMapper.apply(entity.position());
			Blueprint.EntityData data = new Blueprint.EntityData(entity.getType().getRegistryName(), () -> Optional.of(nbtFilter.apply(entity.serializeNBT())));
			map.put(position, data);
		});
		return map;
	}

	@Override
	public void addEntity(Vec3 pos, EntityData entity) {
		if (entity.nbt().get().isPresent()) {
			sendPackage(new ServerLevelAccessHandler.WAAddEntity(pos, entity));
		}
	}

}
