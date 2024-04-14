package de.m_marvin.blueprints.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.google.common.base.Function;

import de.m_marvin.blueprints.api.IStructAccessor;
import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.nbtutility.TagType;
import de.m_marvin.nbtutility.nbt.ITagBase;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.nbtutility.nbt.TagDouble;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class SchemParserSponge3 {

	public static final Pattern BLOCK_STATE_PARSE_PATTERN = SchemParserSponge2.BLOCK_STATE_PARSE_PATTERN;
	public static final Pattern BLOCK_STATE_PROPERTY_PATTERN = SchemParserSponge2.BLOCK_STATE_PROPERTY_PATTERN;
	public static final Function<TagCompound, TagCompound> BLOCK_ENTITY_DATA_FILTER = SchemParserSponge2.BLOCK_ENTITY_DATA_FILTER;
	
	public static String state2string(BlockStateData state) {
		return SchemParserSponge2.state2string(state);
	}
	
	public static boolean parseSchem(TagCompound nbt, IStructAccessor target) {
		nbt = nbt.getCompound("Schematic");
		
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
			
			if (nbt.has("Metadata", TagType.COMPOUND)) {
				TagCompound metadata = nbt.getCompound("Metadata");
				if (	metadata.has("WEOffsetX", TagType.INT) && 
						metadata.has("WEOffsetY", TagType.INT) && 
						metadata.has("WEOffsetZ", TagType.INT)) {
					offset.addI(new Vec3i(
						metadata.getInt("WEOffsetX"),
						metadata.getInt("WEOffsetY"),
						metadata.getInt("WEOffsetZ")
					));
				}
			}
			
			target.setOffset(offset);
			target.setBounds(new Vec3i(0, 0, 0), size);
			
			TagCompound blockContainer = nbt.getCompound("Blocks");
			
			TagCompound paletteTag = blockContainer.getCompound("Palette");
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
			
			if (blockContainer.has("Data", TagType.INT_ARRAY)) {
				int[] blockData = blockContainer.getIntArray("Data");
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
			} else if (blockContainer.has("Data", TagType.BYTE_ARRAY)) {
				byte[] blockData = blockContainer.getByteArray("Data");
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
			
			for (TagCompound blockEntityTag : blockContainer.getList("BlockEntities", TagCompound.class)) {
				try {
					RegistryName typeName = new RegistryName(blockEntityTag.getString("Id"));
					int[] posArr = blockEntityTag.getIntArray("Pos");
					assert posArr.length == 3 : "invalid pos array, length != 3";
					Vec3i position = new Vec3i(posArr[0], posArr[1], posArr[2]);
					TagCompound data = blockEntityTag.getCompound("Data");
					BlockEntityData blockEntity = new BlockEntityData(position, typeName);
					blockEntity.setData(data);
					target.setBlockEntity(position, blockEntity);
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to load block entity nbt: %s", blockEntityTag.toString());
					e.printStackTrace();
				}
			}
			
			for (TagCompound entityTag : nbt.getList("Entities", TagCompound.class)) {
				try {
					RegistryName entityName = new RegistryName(entityTag.getString("Id"));
					Vec3d position = loadVectorD(entityTag.getList("Pos", TagDouble.class));
					TagCompound data = entityTag.getCompound("Data");
					EntityData entity = new EntityData(position, entityName);
					entity.setData(data);
					target.addEntity(entity);
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to load entity nbt: %s", entityTag.toString());
					e.printStackTrace();
				}
			}
			
			return true;
		} catch (Throwable e) {
			System.err.println("failed to load nbt:");
			e.printStackTrace();
			return false;
		}
	}

	public static boolean buildSchem(TagCompound nbt, IStructAccessor source) {
		
		
		try {
			TagCompound schematicTag = new TagCompound();
			
			Vec3i size = source.getBoundsMax().sub(source.getBoundsMin());
			schematicTag.putShort("Width", (short) size.x);
			schematicTag.putShort("Height", (short) size.y);
			schematicTag.putShort("Length", (short) size.z);
			
			Vec3i offset = source.getOffset();
			schematicTag.putIntArray("Offset", new int[] {offset.x, offset.y, offset.z});
			
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
			TagCompound blockContainer = new TagCompound();
			blockContainer.putList("BlockEntities", blockEntityList);
			blockContainer.putTag("Palette", paletteList);
			
			if (paletteList.getMap().size() <= ((int) Byte.MAX_VALUE - (int) Byte.MIN_VALUE)) {
				byte[] blockDataBytes = new byte[blockData.length];
				for (int i = 0; i < blockData.length; i++) blockDataBytes[i] = (byte) blockData[i];
				blockContainer.putByteArray("Data", blockDataBytes);
			} else {
				blockContainer.putIntArray("Data", blockData);
			}
			
			schematicTag.putTag("Blocks", blockContainer);
			
			List<TagCompound> entityList = new ArrayList<>();
			for (EntityData entity : source.getEntitiesWithin(source.getBoundsMin(), source.getBoundsMax())) {
				try {
					TagCompound entityTag = new TagCompound(entity.getData());
					entityTag.putList("Pos", writeVectorD(entity.getPosition()));
					entityTag.putString("Id", entity.getEntityName().toString());
					entityList.add(entityTag);
				} catch (Throwable e) {
					IBlueprintParser.logWarn(source, "failed to write entity nbt: %s", e.getMessage());
					e.printStackTrace();
				}
			}
			schematicTag.putList("Entities", entityList);
			
			nbt.putTag("Schematic", schematicTag);
			
			return true;
		} catch (Throwable e) {
			IBlueprintParser.logWarn(source, "failed to write nbt structure: %s", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	protected static Vec3d loadVectorD(List<TagDouble> tagList) {
		assert tagList.size() == 3 : "invalid tag list for 3D vector, length != 3";
		return new Vec3d(
					tagList.get(0).getDouble(),
					tagList.get(1).getDouble(),
					tagList.get(2).getDouble()
				);
	}

	protected static List<ITagBase> writeVectorD(Vec3d vec) {
		List<ITagBase> list = new ArrayList<>();
		list.add(new TagDouble(vec.getX()));
		list.add(new TagDouble(vec.getY()));
		list.add(new TagDouble(vec.getZ()));
		return list;
	}

}
