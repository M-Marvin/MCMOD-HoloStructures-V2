package de.m_marvin.holostruct.client.pixelart;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public record BlockConfiguration(MapColor color, MapColor.Brightness brightness, BlockState state, int rgb) {
	
}
