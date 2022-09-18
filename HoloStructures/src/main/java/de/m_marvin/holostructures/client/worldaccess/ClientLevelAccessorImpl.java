package de.m_marvin.holostructures.client.worldaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

import com.google.common.base.Function;
import com.google.common.collect.Queues;

import de.m_marvin.holostructures.HoloStructures;
import de.m_marvin.holostructures.UtilHelper;
import de.m_marvin.holostructures.client.ClientHandler;
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
import net.minecraft.world.level.BlockAndTintGetter;
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
		if (event.getSenderUUID().equals(Util.NIL_UUID)) {
			if (ClientHandler.getInstance().getBlueprints().isWorking() || pendingCommands.size() > 0) {
				event.setCanceled(true);
			}
			if (pendingCommands.size() > 0) {
				pendingCommands.poll().setResponse(event.getMessage());
			}
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
		if (!hasOPAccess()) return;
		sendCommand("/setblock " + UtilHelper.formatBlockPos(pos) + " " + UtilHelper.formatBlockState(state));
	}
	
	@Override
	public boolean checkBlock(BlockPos pos, BlockState state) {
		if (!hasOPAccess()) return true;
		if (!UtilHelper.checkBlockState(this.level.get().getBlockState(pos), state)) {
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
			
			return UtilHelper.checkBlockState(this.level.get().getBlockState(pos), state);
		}
		return true;
	}
	
	@Override
	public BlockState getBlock(BlockPos pos) {
		return this.level.get().getBlockState(pos);
	}
	
	@Override
	public Optional<Blueprint.EntityData> getBlockEntity(BlockPos pos) {
		if (!hasOPAccess()) return Optional.empty();
		Optional<BlockEntity> blockEntity = Optional.ofNullable(this.level.get().getBlockEntity(pos));
		Function<CompoundTag, CompoundTag> nbtFilter = (nbt) -> {
			nbt.remove("x");
			nbt.remove("y");
			nbt.remove("z");
			nbt.remove("id");
			return nbt;
		};
		if (blockEntity.isPresent()) {
			CommandResponse response = sendCommand("/data get block " + UtilHelper.formatBlockPos(pos));
			Blueprint.EntityData blockEntityData = new Blueprint.EntityData(blockEntity.get().getType().getRegistryName(), 
					() -> response.isPresent() ? Optional.of(nbtFilter.apply(UtilHelper.encryptNBTFromResponse(response.getResponse()))) : Optional.empty());
			return Optional.of(blockEntityData);
		}
		return Optional.empty();
	}
	
	@Override
	public void setBlockEntity(BlockPos pos, Blueprint.EntityData blockEntity) {
		if (!hasOPAccess()) return;
		if (blockEntity.nbt().get().isPresent()) {
			String posString = UtilHelper.formatBlockPos(pos);
			UtilHelper.sequentialCopy(
					blockEntity.nbt().get().get(), 
					(path, value) -> sendCommand("/data modify block " + posString + " " + path + " set value " + value.toString()),
					(path, value) -> sendCommand("/data modify block " + posString + " " + path + " append value " + value.toString()),
					"");
		}
		
	}
	
	@Override
	public Map<Vec3, Blueprint.EntityData> getEntities(BlockPos corner1, BlockPos corner2, Function<Vec3, Vec3> positionMapper) {
		AABB aabb = new AABB(corner1, corner2).inflate(1);
		Function<CompoundTag, CompoundTag> nbtFilter = (nbt) -> {
			nbt.remove("Pos");
			return nbt;
		};
		Map<Vec3, Blueprint.EntityData> map = new HashMap<>();
		this.level.get().getEntities(null, aabb).forEach((entity) -> {
			Vec3 position = positionMapper.apply(entity.position());
			CommandResponse response = hasOPAccess() ? sendCommand("/data get entity " + entity.getUUID().toString()) : new CommandResponse();
			Blueprint.EntityData data = new Blueprint.EntityData(entity.getType().getRegistryName(), () -> response.isPresent() ? Optional.of(nbtFilter.apply(UtilHelper.encryptNBTFromResponse(response.getResponse()))) : Optional.empty());
			map.put(position, data);
		});
		return map;
	}
	
	@Override
	public void addEntity(Vec3 pos, Blueprint.EntityData entity) {
		if (!hasOPAccess()) return;
		if (entity.nbt().get().isPresent()) {
			String pasteTag = "paste_" + entity.hashCode();
			sendCommand("/summon " + entity.type().toString() + " " + UtilHelper.formatVecPos(pos) + " {Tags:[\"" + pasteTag + "\"]}");
			UtilHelper.sequentialCopy(
					entity.nbt().get().get(),
					(path, value) -> {
						sendCommand("/data modify entity @e[tag=" + pasteTag + ",limit=1] " + path + " set value " + value.toString());
					},
					(path, value) -> {
						sendCommand("/data modify entity @e[tag=" + pasteTag + ",limit=1] " + path + " append value " + value.toString());
					},
					"");
		}
	}
	
	@Override
	public BlockAndTintGetter getLevelGetter() {
		return this.level.get();
	}
	
}
