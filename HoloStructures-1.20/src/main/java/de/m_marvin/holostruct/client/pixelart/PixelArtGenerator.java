package de.m_marvin.holostruct.client.pixelart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.joml.Random;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.holostruct.HoloStruct;
import de.m_marvin.holostruct.client.ClientConfig;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;

public class PixelArtGenerator {
	
	/** Image loaded to convert to pixel art */
	protected BufferedImage inputImage;
	/** Resulting image using the available map colors */
	protected BufferedImage outputImage;
	/** List of blocks that the user wants not to use */
	protected List<Block> blacklistConfig = new ArrayList<>();
	protected List<Block> blacklistUser = new ArrayList<>();
	/** List of blocks that the user wants to prefer */
	protected List<Block> whitelistConfig = new ArrayList<>();
	protected List<Block> whitelistUser = new ArrayList<>();
	/** If the current pixel art should be build using 3D shading */
	protected boolean useShadowing = false;
	/** List of blocks required for this image */
	protected Map<Block, Integer> blockList = new HashMap<>();
	/** Image color to block configuration map */
	protected Map<Integer, BlockConfiguration> imageMap = new ConcurrentHashMap<>();
	/** Map color to block configuration map */
	public Multimap<Integer, BlockConfiguration> colorMap = Multimaps.newMultimap(new HashMap<>(), () -> new HashSet<>());
	/** Block sorted map of possible configurations */
	protected Map<Block, List<BlockConfiguration>> blockColors = new HashMap<>();
	
	protected final Random random = new Random();
	
	protected void rebuildColorMap() {
		resetLists();
		
		this.colorMap.clear();
		this.blockColors.clear();
		
		@SuppressWarnings("resource")
		BlockGetter level = Minecraft.getInstance().level;
		
		BuiltInRegistries.BLOCK.entrySet().stream()
			.map(Map.Entry::getValue)
			.flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
			.forEach(state -> {
				MapColor color = state.getMapColor(level, BlockPos.ZERO);
				Stream.of(MapColor.Brightness.LOW, MapColor.Brightness.NORMAL, MapColor.Brightness.HIGH)
					.forEach(brightness -> {
						int rgb = calculateRGBfromMapColor(color, brightness);
						if (!this.blockColors.containsKey(state.getBlock())) this.blockColors.put(state.getBlock(), new ArrayList<>());
						BlockConfiguration config = new BlockConfiguration(color, brightness, state, rgb);
						this.blockColors.get(state.getBlock()).add(config);
						this.colorMap.put(config.rgb(), config);
					});
			});
	}
	
	protected int calculateRGBfromMapColor(MapColor color, MapColor.Brightness brightness) {
		int bgr = color.calculateRGBColor(brightness);
		return 0xFF000000 | ((bgr & 0xFF) << 16) | (((bgr >> 8) & 0xFF) << 8) | ((bgr >> 16) & 0xFF);
	}
	
	public boolean loadImage(File imageFile) {
		try {
			this.inputImage = ImageIO.read(imageFile);
			if (this.inputImage.getWidth() != 128 || this.inputImage.getHeight() != 128) {
				this.inputImage = null;
				return false;
			}
			this.outputImage = new BufferedImage(this.inputImage.getWidth(), this.inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean saveImage(File imageFile) {
		if (this.outputImage == null) return false;
		try {
			ImageIO.write(this.outputImage, "PNG", imageFile);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void rebuild() {
		if (this.blockColors.isEmpty()) rebuildColorMap();
		
		Set<Integer> imageColors = new HashSet<>();
		
		for (int px = 0; px < 128; px++) {
			for (int py = 0; py < 128; py++) {
				imageColors.add(this.inputImage.getRGB(px, py));
			}
		}
		
		this.imageMap.clear();
		imageColors.stream().parallel().forEach(color -> {
			BlockConfiguration configuration = findConfigurationForColor(color);
			if (configuration == null) {
				HoloStruct.LOGGER.warn("could not find valid configuration for pixel color " + Integer.toHexString(color));
				return;
			}
			this.imageMap.put(color, configuration);
		});
		
		this.blockList.clear();
		for (int px = 0; px < 128; px++) {
			for (int py = 0; py < 128; py++) {
				int originalPixel = this.inputImage.getRGB(px, py);
				BlockConfiguration configuration = this.imageMap.get(originalPixel);
				int blockCount = this.blockList.getOrDefault(configuration.state().getBlock(), 0);
				this.blockList.put(configuration.state().getBlock(), ++blockCount);
				this.outputImage.setRGB(px, py, configuration.rgb());
			}
		}
	}
	
	protected void placePixelBlocks(Blueprint target, int pxX, int pxZ, int pxY, BlockStateData state, BlockStateData fillerState, int heightChange, int scale) {
		
		for (int bX = 0; bX < scale / 2; bX++) {
			for (int bZ = 0; bZ < scale; bZ++) {
				
				target.setBlock(new Vec3i(pxX * scale + bX, pxY * scale, pxZ * scale + bZ), state);
				target.setBlock(new Vec3i(pxX * scale + bX, pxY * scale - 1, pxZ * scale + bZ), fillerState);
				
			}
		}
		
		if (heightChange != 0 || scale == 1) {

			target.setBlock(new Vec3i(pxX * scale + scale / 2, pxY * scale, pxZ * scale), state);
			target.setBlock(new Vec3i(pxX * scale + scale / 2, pxY * scale - 1, pxZ * scale), fillerState);
			
		}
		
	}
	
	public Blueprint buildBlueprint(int mapScale) {
		
		if (this.inputImage != null && this.imageMap != null) {

			int scale = (int) Math.pow(2, mapScale - 1);
			
			Blueprint blueprint = new Blueprint();
			int minY = 0;
			int maxY = 0;
			
			BlockStateData stoneState = new BlockStateData(new RegistryName("minecraft:stone"));
			BlockStateData airState = TypeConverter.AIR_STATE;
			
			int[] compressionOffset = new int[128];
			for (int x = 0; x < 128; x++) {
				int y = 0;
				int ly = 0;
				int hy = 0;
				for (int z = 0; z < 128; z++) {

					int pixelColor = this.inputImage.getRGB(x, z);
					BlockConfiguration config = this.imageMap.get(pixelColor);
					int heightChange = config.brightness() == Brightness.NORMAL ? 0 : config.brightness() == Brightness.HIGH ? 1 : -1;
					y += heightChange;
					
					if (y > hy) hy = y;
					if (y < ly) ly = y;
					
				}
				
				compressionOffset[x] = -(hy - ly) / 2 - ly;
			}
			
			for (int x = 0; x < 128; x++) {
				int y = compressionOffset[x];
				
				placePixelBlocks(blueprint, x, -1, y, stoneState, airState, 0, scale);
				
				for (int z = 0; z < 128; z++) {
					int pixelColor = this.inputImage.getRGB(x, z);
					BlockConfiguration config = this.imageMap.get(pixelColor);
					BlockStateData block = TypeConverter.blockState2data(config.state());
					int heightChange = config.brightness() == Brightness.NORMAL ? 0 : config.brightness() == Brightness.HIGH ? 1 : -1;
					y += heightChange;
					
					placePixelBlocks(blueprint, x, z, y, block, stoneState, heightChange, scale);
					
					if (y < minY) minY = y;
					if (y > maxY) maxY = y;
				}
			}
			
			blueprint.setBounds(new Vec3i(0, minY, -1), new Vec3i(128, maxY + 1, 128));
			
			return blueprint;
			
		}
		
		return null;
		
	}
	
	protected BlockConfiguration findConfigurationForColor(int rgb) {
		BlockConfiguration configuration = null;
		double div = Double.MAX_VALUE;
		
		for (Block prefered : this.whitelistUser) {
			if (this.blacklistUser.contains(prefered)) continue;
			
			for (BlockConfiguration config : this.blockColors.get(prefered)) {
				
				if (config.brightness() != Brightness.NORMAL && !this.useShadowing) continue;
				
				double mapDiv = (
						Math.abs((rgb >> 16 & 0xFF) - (config.rgb() >> 16 & 0xFF)) + 
						Math.abs((rgb >> 8 & 0xFF) - (config.rgb() >> 8 & 0xFF)) + 
						Math.abs((rgb & 0xFF) - (config.rgb() & 0xFF))
					) / 3.0;
				
				if (mapDiv < div) {
					div = mapDiv;
					configuration = config;
				}
				
			}
			
		}
		
		for (Block prefered : this.whitelistConfig) {
			if (this.blacklistConfig.contains(prefered)) continue;
			if (this.blacklistUser.contains(prefered)) continue;

			for (BlockConfiguration config : this.blockColors.get(prefered)) {

				if (config.brightness() != Brightness.NORMAL && !this.useShadowing) continue;
				
				double mapDiv = (
						Math.abs((rgb >> 16 & 0xFF) - (config.rgb() >> 16 & 0xFF)) + 
						Math.abs((rgb >> 8 & 0xFF) - (config.rgb() >> 8 & 0xFF)) + 
						Math.abs((rgb & 0xFF) - (config.rgb() & 0xFF))
					) / 3.0;
				
				if (mapDiv < div) {
					div = mapDiv;
					configuration = config;
				} else if (Math.abs(div - mapDiv) < 0.0001) {
					if (config.state().equals(prefered.defaultBlockState())) configuration = config;
				}
				
			}
		}
		
		if (configuration != null) {

			BlockConfiguration fconfig = configuration;
			@SuppressWarnings("resource")
			BlockState preferedStableState = PlacementHelperFakeLevel.getStablePlacementState(
					Minecraft.getInstance().level, 
					configuration.state().getBlock(), 
					state -> {
						for (BlockConfiguration config : this.blockColors.get(fconfig.state().getBlock())) {
							if (config.state().equals(state)) return true;
						}
						return false;
					});
			
			for (BlockConfiguration config : this.blockColors.get(configuration.state().getBlock())) {
				if (config.state().equals(preferedStableState) && config.color() == configuration.color() && config.brightness() == configuration.brightness()) {
					configuration = config;
					break;
				}
			}
			
		}
		
		return configuration;
	}
	
	public Map<Block, Integer> getBlockList() {
		return blockList;
	}
	
	public List<Block> getBlacklist() {
		return blacklistUser;
	}
	
	public List<Block> getWhitelist() {
		return whitelistUser;
	}
	
	public void setUseShadowing(boolean useShadowing) {
		this.useShadowing = useShadowing;
	}
	
	public boolean isUseShadowing() {
		return useShadowing;
	}
	
	public void resetLists() {
		this.whitelistConfig.clear();
		this.whitelistConfig.addAll(ClientConfig.DEFAULT_BLOCK_WHITELIST.get().stream().map(name -> BuiltInRegistries.BLOCK.get(new ResourceLocation(name))).toList());
		this.blacklistConfig.clear();
		this.blacklistConfig.addAll(ClientConfig.DEFAULT_BLOCK_BLACKLIST.get().stream().map(name -> BuiltInRegistries.BLOCK.get(new ResourceLocation(name))).toList());
	}
	
	public void updatePreview() {
		HoloStruct.CLIENT.PIXELART_PREVIEW.setPreviewImage(this.outputImage);
	}
	
	public void hidePreview() {
		HoloStruct.CLIENT.PIXELART_PREVIEW.setPreviewImage(null);
	}
	
}
