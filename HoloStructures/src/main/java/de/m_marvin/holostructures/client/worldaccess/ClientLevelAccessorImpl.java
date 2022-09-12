package de.m_marvin.holostructures.client.worldaccess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

import com.google.common.base.Function;
import com.google.common.collect.Queues;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.ILevelAccessor;
import de.m_marvin.holostructures.UtilHelper;
import de.m_marvin.holostructures.client.blueprints.Blueprint;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=HoloStructures.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ClientLevelAccessorImpl implements ILevelAccessor {

	public Supplier<ClientLevel> level;
	public Supplier<LocalPlayer> player;
	
	public static class CommandResponse {
		protected Component response = null;
		public boolean isPresent() {
			return this.response != null;
		}
		public Component getResponse() {
			return response;
		}
		public void setResponse(Component response) {
			this.response = response;
		}
	}
	
	public static Queue<CommandResponse> pendingCommands = Queues.newArrayDeque();
	
	@SubscribeEvent
	public static void onChatMessage(ClientChatReceivedEvent event) {
		if (event.getSenderUUID().equals(Util.NIL_UUID) && pendingCommands.size() > 0) {
			event.setCanceled(true);
			pendingCommands.poll().setResponse(event.getMessage());
		}
	}
	
	public ClientLevelAccessorImpl(Supplier<ClientLevel> level, Supplier<LocalPlayer> player) {
		this.level = level;
		this.player = player;
	}
	
	public void sendPackage(Packet<ServerGamePacketListener> clientPackage) {
		this.player.get().connection.send(clientPackage);
	}
	
	public CommandResponse sendCommand(String command) {
		ServerboundChatPacket commandPackage = new ServerboundChatPacket(command);
		sendPackage(commandPackage);
		CommandResponse response = new CommandResponse();
		pendingCommands.add(response);
		return response;
	}
	
	@Override
	public boolean hasServerAccess() {
		return false;
	}
	
	@Override
	public boolean hasOPAccess() {
		return this.player.get().getPermissionLevel() >= 4;
	}
	
	@Override
	public boolean isDoneAccessing() {
		return pendingCommands.size() == 0;
	}
	
	@Override
	public void abbortWaiting() {
		pendingCommands.clear();
	}
	
	@Override
	public CommandSourceStack getChatTarget() {
		return this.player.get().createCommandSourceStack();
	}
	
	public void setBlock(BlockPos pos, BlockState state) {
		sendCommand("/setblock " + UtilHelper.formatBlockPos(pos) + " " + UtilHelper.formatBlockState(state));
	}
	
	@Override
	public boolean checkBlock(BlockPos pos, BlockState state) {
		if (!this.level.get().getBlockState(pos).equals(state)) {
			sendCommand("/setblock " + UtilHelper.formatBlockPos(pos) + " " + UtilHelper.formatBlockState(state));
			
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
	public BlockState getBlock(BlockPos pos) {
		return this.level.get().getBlockState(pos);
	}
	
	@Override
	public Optional<Blueprint.EntityData> getBlockEntity(BlockPos pos) {
		Optional<BlockEntity> blockEntity = Optional.ofNullable(this.level.get().getBlockEntity(pos));
		if (blockEntity.isPresent()) {
			CommandResponse response = sendCommand("/data get block " + UtilHelper.formatBlockPos(pos));
			Blueprint.EntityData blockEntityData = new Blueprint.EntityData(blockEntity.get().getType().getRegistryName(), 
					() -> response.isPresent() ? Optional.of(UtilHelper.encryptNBTFromResponse(response.getResponse())) : Optional.empty());
			return Optional.of(blockEntityData);
		}
		return Optional.empty();
	}
	
	@Override
	public void setBlockEntity(BlockPos pos, Blueprint.EntityData blockEntity) {
		if (blockEntity.nbt().get().isPresent()) {
			// FIXME /data merge does not work for lists (like inventories)
			List<CompoundTag> dataTags = UtilHelper.sizeLimitedCompounds(blockEntity.nbt().get().get(), 200);
			dataTags.forEach((tag) -> sendCommand("/data merge block " + UtilHelper.formatBlockPos(pos) + " " + tag.toString()));
		}
		
	}
	
	@Override
	public Map<Vec3, Blueprint.EntityData> getEntities(BlockPos corner1, BlockPos corner2, Function<Vec3, Vec3> positionMapper) {
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
	public void addEntity(Vec3 pos, Blueprint.EntityData entity) {
		if (entity.nbt().get().isPresent()) {
			// FIXME /data merge does not work for lists (like inventories) and item frames
			List<CompoundTag> dataTags = UtilHelper.sizeLimitedCompounds(entity.nbt().get().get(), 100);
			sendCommand("/summon " + entity.type() + " " + UtilHelper.formatVecPos(pos) + " " + dataTags.get(0).toString());
			if (dataTags.size() > 1) {
				dataTags.forEach((tag) -> sendCommand("/execute positioned " + UtilHelper.formatVecPos(pos) + " run data merge entity @e[type=" + entity.type().toString() + ",distance=..0.1,sort=nearest,limit=1] " + tag.toString()));
			}
		}
	}
	
	@Override
	public BlockGetter getLevelGetter() {
		return this.level.get();
	}
	
}
