package de.m_marvin.blueprints.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.google.common.base.Function;

import de.m_marvin.blueprints.api.IStructAccessor;
import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.nbtutility.TagType;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.univec.impl.Vec3i;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class SchemParserSponge1 {

	public static final Pattern BLOCK_STATE_PARSE_PATTERN = Pattern.compile("([A-Za-z0-9_\\-\\:]{1,})\\[([A-Za-z0-9_\\-=\\,]{1,})\\]");
	public static final Pattern BLOCK_STATE_PROPERTY_PATTERN = Pattern.compile("([A-Za-z0-9_\\-]{1,})=([A-Za-z0-9_\\-]{1,})");
	
	public static final Function<TagCompound, TagCompound> BLOCK_ENTITY_DATA_FILTER = (nbt) -> {
		nbt.removeTag("x");
		nbt.removeTag("y");
		nbt.removeTag("z");
		nbt.removeTag("id");
		return nbt;
	};
	
	public static String state2string(BlockStateData state) {
		StringBuilder sb = new StringBuilder();
		sb.append(state.getBlockName().toString());
		if (state.getProperties().size() > 0) {
			sb.append("[");
			int counter = 0;
			for (Entry<String, String> prop : state.getProperties().entrySet()) {
				sb.append(prop.getKey()).append("=").append(prop.getValue());
				if (++counter < state.getProperties().size()) sb.append(",");
			}
			sb.append("]");
		}
		return sb.toString();
	}
	
	public static boolean parseSchem(TagCompound nbt, IStructAccessor target) {
		try {
			Vec3i size = new Vec3i(
					nbt.getShort("Width"),
					nbt.getShort("Height"),
					nbt.getShort("Length")
			);
			
			Vec3i offset = new Vec3i(0, 0, 0);
			if (nbt.has("Offset", TagType.INT_ARRAY)) {
				int[] offsetArr = nbt.getIntArray("Offset");
				assert offsetArr.length == 3 : "invalid offset array, length != 3";
				offset.addI(new Vec3i(offsetArr[0], offsetArr[1], offsetArr[2]));
			}
			
			target.setOffset(offset);
			target.setBounds(new Vec3i(0, 0, 0), size);
			
			TagCompound paletteTag = nbt.getCompound("Palette");
			BlockStateData[] palette = new BlockStateData[paletteTag.getMap().size()];
			for (String blockKey : paletteTag.getMap().keySet()) {
				try {
					Optional<MatchResult> result = BLOCK_STATE_PARSE_PATTERN.matcher(blockKey).results().findAny();
					BlockStateData blockState;
					if (result.isPresent()) {
						RegistryName blockName = new RegistryName(result.get().group(1));
						blockState = new BlockStateData(blockName);
						BLOCK_STATE_PROPERTY_PATTERN.matcher(result.get().group(2)).results().forEach(res -> {
							blockState.setValue(res.group(1), res.group(2));
						});
					} else {
						blockState = new BlockStateData(new RegistryName(blockKey));
					}
					int index = paletteTag.getInt(blockKey);
					palette[index] = blockState;
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to parse palette nbt: %s", blockKey);
					e.printStackTrace();
				}
			}

			if (nbt.has("BlockData", TagType.INT_ARRAY)) {
				int[] blockData = nbt.getIntArray("BlockData");
				int counter = 0;
				for (int y = 0; y < size.y; y++) {
					for (int z = 0; z < size.z; z++) {
						for (int x = 0; x < size.x; x++) {
							int id = counter++;
							try {
								Vec3i position = new Vec3i(x, y, z).add(target.getBoundsMin());
								BlockStateData blockState = palette[blockData[id]];
								target.setBlock(position, blockState);
							} catch (Throwable e) {
								IBlueprintParser.logWarn(target, "failed to load block data entry: pos %d %d %d index %d", x, y, z, id);
								e.printStackTrace();
							}
						}
					}
				}
			} else if (nbt.has("BlockData", TagType.BYTE_ARRAY)) {
				byte[] blockData = nbt.getByteArray("BlockData");
				int counter = 0;
				for (int y = 0; y < size.y; y++) {
					for (int z = 0; z < size.z; z++) {
						for (int x = 0; x < size.x; x++) {
							int id = counter++;
							try {
								Vec3i position = new Vec3i(x, y, z).add(target.getBoundsMin());
								BlockStateData blockState = palette[(int) blockData[id] & 0xff];
								target.setBlock(position, blockState);
							} catch (Throwable e) {
								IBlueprintParser.logWarn(target, "failed to load block data entry: pos %d %d %d index %d", x, y, z, id);
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			for (TagCompound blockEntityTag : nbt.getList("TileEntities", TagCompound.class)) {
				try {
					RegistryName typeName = new RegistryName(blockEntityTag.getString("Id"));
					int[] posArr = blockEntityTag.getIntArray("Pos");
					assert posArr.length == 3 : "invalid pos array, length != 3";
					Vec3i position = new Vec3i(posArr[0], posArr[1], posArr[2]);
					BlockEntityData blockEntity = new BlockEntityData(position, typeName);
					blockEntity.setData(blockEntityTag);
					target.setBlockEntity(position, blockEntity);
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to load block entity nbt: %s", blockEntityTag.toString());
					e.printStackTrace();
				}
			}
			
			return true;
		} catch (Throwable e) {
			IBlueprintParser.logWarn(target, "failed to load nbt structure: %s", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean buildSchem(TagCompound nbt, IStructAccessor source) {
		try {
			Vec3i size = source.getBoundsMax().sub(source.getBoundsMin());
			nbt.putShort("Width", (short) size.x);
			nbt.putShort("Height", (short) size.y);
			nbt.putShort("Length", (short) size.z);
			
			Vec3i offset = source.getOffset();
			nbt.putIntArray("Offset", new int[] {offset.x, offset.y, offset.z});
			
			Object2IntMap<BlockStateData> palette = new Object2IntArrayMap<>();
			TagCompound paletteList = new TagCompound();
			List<TagCompound> blockEntityList = new ArrayList<>();
			int[] blockData = new int[size.x * size.y * size.z];
			int counter = 0;
			for (int y = 0; y < size.y; y++) {
				for (int z = 0; z < size.z; z++) {
					for (int x = 0; x < size.x; x++) {
						try {
							Vec3i position = new Vec3i(x, y, z).add(source.getBoundsMin());
							BlockStateData blockState = source.getBlock(position);
							if (blockState == null) continue;
							
							if (!palette.containsKey(blockState)) {
								String blockKey = state2string(blockState);
								int index = palette.size();
								palette.put(blockState, index);
								paletteList.putInt(blockKey, index);
							}
							
							blockData[counter++] = palette.getInt(blockState);
							
							BlockEntityData blockEntity = source.getBlockEntity(position);
							if (blockEntity != null) {
								TagCompound blockEntityTag = BLOCK_ENTITY_DATA_FILTER.apply(new TagCompound(blockEntity.getData()));
								blockEntityTag.putString("Id", blockEntity.getTypeName().toString());
								blockEntityTag.putIntArray("Pos", new int[] {position.x, position.y, position.z});
								blockEntityList.add(blockEntityTag);
							}
						} catch (Throwable e) {
							IBlueprintParser.logWarn(source, "failed to write block nbt data: %d %d %d", x, y, z);
							e.printStackTrace();
							return false;
						}
					}
				}
			}
			nbt.putList("TileEntities", blockEntityList);
			nbt.putTag("Palette", paletteList);
			nbt.putInt("PaletteMax", paletteList.getMap().size() + 1);
			
			if (paletteList.getMap().size() <= ((int) Byte.MAX_VALUE - (int) Byte.MIN_VALUE)) {
				byte[] blockDataBytes = new byte[blockData.length];
				for (int i = 0; i < blockData.length; i++) blockDataBytes[i] = (byte) blockData[i];
				nbt.putByteArray("BlockData", blockDataBytes);
			} else {
				nbt.putIntArray("BlockData", blockData);
			}
			
			return true;
		} catch (Throwable e) {
			IBlueprintParser.logWarn(source, "failed to write nbt structure: %s", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
}
