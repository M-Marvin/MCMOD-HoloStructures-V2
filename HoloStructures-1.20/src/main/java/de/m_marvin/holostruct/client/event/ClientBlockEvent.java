package de.m_marvin.holostruct.client.event;

import de.m_marvin.holostruct.mixin.ClientBlockStateDetection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

/**
 * Custom Mixin event for client sided block update detection.
 * Does detect changes on blocks even if thei did not generate any updates.
 * Fired from the {@link ClientBlockStateDetection} mixin.
 * @author Marvin Koehler
 */
public class ClientBlockEvent extends Event {
		
	    private final ClientLevel level;
		private final BlockPos position;
		private final BlockState state;
		
		public ClientBlockEvent(ClientLevel level, BlockPos position, BlockState state) {
			this.level = level;
			this.position = position;
			this.state = state;
		}
		
		public ClientLevel getLevel() {
			return level;
		}
		
		public BlockPos getPosition() {
			return position;
		}
		
		public BlockState getState() {
			return state;
		}
		
		public static class ClientChangeEvent extends ClientBlockEvent {
			
			public ClientChangeEvent(ClientLevel level, BlockPos position, BlockState state) {
				super(level, position, state);
			}
			
		}

		public static class ServerChangeEvent extends ClientBlockEvent {
			
			public ServerChangeEvent(ClientLevel level, BlockPos position, BlockState state) {
				super(level, position, state);
			}
			
		}
		
	}