package de.m_marvin.blueprints.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import de.m_marvin.blueprints.api.IBlueprintAcessor;
import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.holostruct.client.blueprints.TypeConverter;
import de.m_marvin.nbtutility.TagType;
import de.m_marvin.nbtutility.VarIntUtil;
import de.m_marvin.nbtutility.nbt.ITagBase;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.nbtutility.nbt.TagDouble;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

/**
 * This is part of {@link SchemParser}, this class defines some functions used for the sub-format Sponge-2
 * 
 * @author Marvin Koehler
 */
public class SchemParserSponge2 {

	public static final Pattern BLOCK_STATE_PARSE_PATTERN = SchemParserSponge1.BLOCK_STATE_PARSE_PATTERN;
	public static final Pattern BLOCK_STATE_PROPERTY_PATTERN = SchemParserSponge1.BLOCK_STATE_PROPERTY_PATTERN;
	public static final Function<TagCompound, TagCompound> BLOCK_ENTITY_DATA_FILTER = SchemParserSponge1.BLOCK_ENTITY_DATA_FILTER;
	
	public static String state2string(BlockStateData state) {
		return SchemParserSponge1.state2string(state);
	}
	
	public static boolean parseSchem(TagCompound nbt, IBlueprintAcessor target) {
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
				offset.addI(new Vec3i(-offsetArr[0], -offsetArr[1], -offsetArr[2]));
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
			
			byte[] blockDataBytes = nbt.getByteArray("BlockData");
			int[] blockData = VarIntUtil.toInts(blockDataBytes, size.y * size.x * size.z);
			int counter = 0;
			for (int y = 0; y < size.y; y++) {
				for (int z = 0; z < size.z; z++) {
					for (int x = 0; x < size.x; x++) {
						int id = counter++;
						try {
							BlockStateData blockState = palette[blockData[id]];
							if (blockState == null || blockState.isAir()) continue;
							Vec3i position = new Vec3i(x, y, z).add(target.getBoundsMin());
							target.setBlock(position, blockState);
						} catch (Throwable e) {
							IBlueprintParser.logWarn(target, "failed to load block data entry: pos %d %d %d index %d", x, y, z, id);
							e.printStackTrace();
						}
					}
				}
			}
			
			if (nbt.has("BlockEntities")) {
				for (TagCompound blockEntityTag : nbt.getList("BlockEntities", TagCompound.class)) {
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
			}
			
			if (nbt.has("Entities")) {
				for (TagCompound entityTag : nbt.getList("Entities", TagCompound.class)) {
					try {
						RegistryName entityName = new RegistryName(entityTag.getString("Id"));
						Vec3d position = loadVectorD(entityTag.getList("Pos", TagDouble.class));
						EntityData entity = new EntityData(position, entityName);
						entity.setData(entityTag);
						target.addEntity(entity);
					} catch (Throwable e) {
						IBlueprintParser.logWarn(target, "failed to load entity nbt: %s", entityTag.toString());
						e.printStackTrace();
					}
				}
			}
			
			return true;
		} catch (Throwable e) {
			System.err.println("failed to load nbt:");
			e.printStackTrace();
			return false;
		}
	}

	public static boolean buildSchem(TagCompound nbt, IBlueprintAcessor source) {
		try {
			Vec3i size = source.getBoundsMax().sub(source.getBoundsMin());
			nbt.putShort("Width", (short) size.x);
			nbt.putShort("Height", (short) size.y);
			nbt.putShort("Length", (short) size.z);

			Vec3i offset = source.getOffset().sub(source.getBoundsMin());
			nbt.putIntArray("Offset", new int[] {-offset.x, -offset.y, -offset.z});
			
			Object2IntMap<BlockStateData> palette = new Object2IntArrayMap<>();
			TagCompound paletteList = new TagCompound();
			List<TagCompound> blockEntityList = new ArrayList<>();
			int[] blockData = new int[size.x * size.y * size.z];
			paletteList.putInt(state2string(TypeConverter.AIR_STATE), 0);
			palette.put(TypeConverter.AIR_STATE, 0);
			for (int y = 0; y < size.y; y++) {
				for (int z = 0; z < size.z; z++) {
					for (int x = 0; x < size.x; x++) {
						try {
							Vec3i position = new Vec3i(x, y, z).add(source.getBoundsMin());
							BlockStateData blockState = source.getBlock(position);
							if (blockState == null || blockState.isAir()) continue;
							
							if (!palette.containsKey(blockState)) {
								String blockKey = state2string(blockState);
								int index = palette.size();
								palette.put(blockState, index);
								paletteList.putInt(blockKey, index);
							}
							
							blockData[x + z * size.x + y * size.z * size.x] = palette.getInt(blockState);
							
							BlockEntityData blockEntity = source.getBlockEntity(position);
							if (blockEntity != null) {
								TagCompound blockEntityTag = new TagCompound(blockEntity.getData());
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
			nbt.putList("BlockEntities", blockEntityList, TagType.COMPOUND);
			nbt.putTag("Palette", paletteList);
			nbt.putInt("PaletteMax", paletteList.getMap().size());
			byte[] blockDataBytes = VarIntUtil.toVarIntBytes(blockData);
			nbt.putByteArray("BlockData", blockDataBytes);
			
			List<TagCompound> entityList = new ArrayList<>();
			for (EntityData entity : source.getEntitiesWithin(source.getBoundsMin(), source.getBoundsMax())) {
				try {
					TagCompound entityTag = BLOCK_ENTITY_DATA_FILTER.apply(new TagCompound(entity.getData()));
					entityTag.putList("Pos", writeVectorD(entity.getPosition()));
					entityTag.putString("Id", entity.getEntityName().toString());
					entityList.add(entityTag);
				} catch (Throwable e) {
					IBlueprintParser.logWarn(source, "failed to write entity nbt: %s", e.getMessage());
					e.printStackTrace();
				}
			}
			nbt.putList("Entities", entityList, TagType.COMPOUND);
			
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
