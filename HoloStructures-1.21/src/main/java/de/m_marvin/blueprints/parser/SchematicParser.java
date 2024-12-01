package de.m_marvin.blueprints.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import de.m_marvin.blueprints.api.IBlueprintAcessor;
import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.nbtutility.BinaryParser;
import de.m_marvin.nbtutility.TagType;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.nbtutility.nbt.TagDouble;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.Util;

/**
 * An implementation of {@link IBlueprintParser} for the pre 1.13 .schematic format.
 * 
 * @author Marvin Koehler
 */
public class SchematicParser implements IBlueprintParser {

	public static final Pattern BLOCK_STATE_PARSE_PATTERN = SchemParserSponge1.BLOCK_STATE_PARSE_PATTERN;
	public static final Pattern BLOCK_STATE_PROPERTY_PATTERN = SchemParserSponge1.BLOCK_STATE_PROPERTY_PATTERN;

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
	
	protected TagCompound nbtTag;
	protected final Supplier<Map<Integer, String>> lagacyMap;
	protected final Function<Integer, BlockStateData> lagacyParser;
	protected final Function<BlockStateData, Integer> lagacyBuilder;
	
	public SchematicParser(Supplier<Map<Integer, String>> lagacyMap) {
		this.lagacyMap = lagacyMap;
		this.lagacyParser = Util.memoize(index -> {
			String blockKey = this.lagacyMap.get().get(index);
			if (blockKey == null) return null;
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
			return blockState;
		});
		this.lagacyBuilder = Util.memoize(state -> {
			String blockKey = state2string(state);
			Optional<Integer> lagacyId = this.lagacyMap.get().keySet().stream().filter(i -> this.lagacyMap.get().get(i).equals(blockKey)).findAny();
			return lagacyId.orElseGet(() -> -1);
		});
	}
	
	@Override
	public boolean load(InputStream istream) throws IOException {
		this.nbtTag = BinaryParser.readCompressed(istream, TagCompound.class);
		if (!this.nbtTag.has("Blocks", TagType.BYTE_ARRAY)) return false;
		if (!this.nbtTag.has("Data", TagType.BYTE_ARRAY)) return false;
		if (!this.nbtTag.has("TileEntities", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("Entities", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("Width", TagType.SHORT)) return false;
		if (!this.nbtTag.has("Height", TagType.SHORT)) return false;
		if (!this.nbtTag.has("Length", TagType.SHORT)) return false;
		return true;
	}

	@Override
	public boolean write(OutputStream ostream) throws IOException {
		if (!this.nbtTag.has("Blocks", TagType.BYTE_ARRAY)) return false;
		if (!this.nbtTag.has("Data", TagType.BYTE_ARRAY)) return false;
		if (!this.nbtTag.has("TileEntities", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("Entities", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("Width", TagType.SHORT)) return false;
		if (!this.nbtTag.has("Height", TagType.SHORT)) return false;
		if (!this.nbtTag.has("Length", TagType.SHORT)) return false;
		BinaryParser.writeCompressed(this.nbtTag, "Schematic", ostream);
		return true;
	}

	@Override
	public boolean parse(IBlueprintAcessor target) {
		target.clearParseLogs();
		try {
			int width = this.nbtTag.getShort("Width");
			int height = this.nbtTag.getShort("Height");
			int length = this.nbtTag.getShort("Length");
			Vec3i size = new Vec3i(width, height, length);
			target.setBounds(new Vec3i(0, 0, 0), size);
			
			Vec3i weOffset = new Vec3i();
			if (this.nbtTag.has("WEOffsetX") && this.nbtTag.has("WEOffsetY") && this.nbtTag.has("WEOffsetZ")) {
				int weOriginX = -this.nbtTag.getInt("WEOffsetX");
				int weOriginY = -this.nbtTag.getInt("WEOffsetY");
				int weOriginZ = -this.nbtTag.getInt("WEOffsetZ");
				Vec3i origin = new Vec3i(weOriginX, weOriginY, weOriginZ);
				target.setOffset(origin);
				
				int weOffsetX = this.nbtTag.getInt("WEOriginX");
				int weOffsetY = this.nbtTag.getInt("WEOriginY");
				int weOffsetZ = this.nbtTag.getInt("WEOriginZ");
				weOffset = new Vec3i(weOffsetX, weOffsetY, weOffsetZ);
			} else {
				target.setOffset(new Vec3i(0, 0, 0));
			}
			
			byte[] blockBytes = this.nbtTag.getByteArray("Blocks");
			byte[] metaBytes = this.nbtTag.getByteArray("Data");
			
			assert (blockBytes.length != metaBytes.length || blockBytes.length != size.x * size.y * size.z) : "block arrays dont match schematic size!";
			
			int index = 0;
			for (int y = 0; y < size.y; y++) {
				for (int z = 0; z < size.z; z++) {
					for (int x = 0; x < size.x; x++) {
						try {
							Vec3i pos = new Vec3i(x, y, z);
							
							int blockId = Byte.toUnsignedInt(blockBytes[index]);
							int metaId = Byte.toUnsignedInt(metaBytes[index]);
							index++;
							int lagacyIndex = (blockId & 4095) | ((metaId & 15) << 12);
							BlockStateData state = this.lagacyParser.apply(lagacyIndex);
							if (state == null) {
								throw new IllegalArgumentException("unknown lagacy block index "+ blockId + ":" + metaId + " (" + lagacyIndex + ")");
							}
							
							target.setBlock(pos, state);
							
						} catch (Throwable e) {
							IBlueprintParser.logWarn(target, "failed to load block: %s", e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
			
			List<TagCompound> blockEntityTags = this.nbtTag.getList("TileEntities", TagCompound.class);
			for (TagCompound blockEntityTag : blockEntityTags) {
				try {

					RegistryName typeName = new RegistryName(blockEntityTag.getString("id"));
					Vec3i position = new Vec3i(
							-this.nbtTag.getInt("x"),
							-this.nbtTag.getInt("y"),
							-this.nbtTag.getInt("z"));
					
					BlockEntityData blockEntity = new BlockEntityData(size, typeName);
					blockEntity.setData(blockEntityTag);
					
					target.setBlockEntity(position, blockEntity);
					
				} catch (Exception e) {
					IBlueprintParser.logWarn(target, "failed to load block entity nbt: %s", e.getMessage());
					e.printStackTrace();
				}
			}
			
			List<TagCompound> entityTags = this.nbtTag.getList("Entities", TagCompound.class);
			for (TagCompound entityTag : entityTags) {
				try {

					String entityName = entityTag.getString("id");
					RegistryName typeName = new RegistryName(entityName.contains(":") ? entityName.toLowerCase() : "minecraft:" + entityName.toLowerCase());
					Vec3d position = loadVectorD(entityTag.getList("Pos", TagDouble.class)).sub(weOffset);
					
					EntityData entity = new EntityData(position, typeName);
					target.addEntity(entity);
					
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to load entity nbt: %s", e.getMessage());
					e.printStackTrace();
				}
			}
			
			return true;
		} catch (Throwable e) {
			IBlueprintParser.logWarn(target, "failed to parse schematic structure: %s", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void reset() {
		this.nbtTag = new TagCompound();
	}
	
	@Override
	public boolean build(IBlueprintAcessor source) {
		try {
			Vec3i size = source.getBoundsMax().sub(source.getBoundsMin());
			this.nbtTag.putShort("Width", (short) size.x);
			this.nbtTag.putShort("Height", (short) size.y);
			this.nbtTag.putShort("Length", (short) size.z);
			
			Vec3i offset = source.getOffset().sub(source.getBoundsMin());
			this.nbtTag.putInt("WEOffsetX", offset.x);
			this.nbtTag.putInt("WEOffsetY", offset.y);
			this.nbtTag.putInt("WEOffsetZ", offset.z);
			this.nbtTag.putInt("WEOriginX", 0);
			this.nbtTag.putInt("WEOriginY", 0);
			this.nbtTag.putInt("WEOriginZ", 0);
			
			byte[] blockBytes = new byte[size.x * size.y * size.z];
			byte[] metaBytes = new byte[blockBytes.length];
			List<TagCompound> blockEntityTags = new ArrayList<>();
			
			int index = 0;
			for (int y = 0; y < size.y; y++) {
				for (int z = 0; z < size.z; z++) {
					for (int x = 0; x < size.x; x++) {
						try {
							Vec3i pos = new Vec3i(x, y, z).add(source.getBoundsMin());;
							BlockStateData state = source.getBlock(pos);
							if (state == null) state = new BlockStateData(new RegistryName("minecraft:air"));
							
							int lagacyId = this.lagacyBuilder.apply(state);
							int blockId = lagacyId & 4095;
							int metaId = (lagacyId >> 12) & 15;
							blockBytes[index] = (byte) blockId;
							metaBytes[index] = (byte) metaId;
							index++;
							
							BlockEntityData blockEntity = source.getBlockEntity(pos);
							if (blockEntity != null) {
								TagCompound blockEntityTag = new TagCompound(blockEntity.getData());
								blockEntityTag.putString("id", blockEntity.getTypeName().toString());
								blockEntityTag.putInt("x", blockEntity.getPosition().x);
								blockEntityTag.putInt("y", blockEntity.getPosition().y);
								blockEntityTag.putInt("z", blockEntity.getPosition().z);
								blockEntityTags.add(blockEntityTag);
							}
							
						} catch (Throwable e) {
							IBlueprintParser.logWarn(source, "failed to write block nbt: %s", e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
			
			this.nbtTag.putByteArray("Blocks", blockBytes);
			this.nbtTag.putByteArray("Data", metaBytes);
			this.nbtTag.putList("TileEntities", blockEntityTags, TagType.COMPOUND);
			
			// Entities can't be saved easily, since the registry names work different before 1.13
			this.nbtTag.putList("Entities", new ArrayList<TagCompound>(), TagType.COMPOUND);
			
			this.nbtTag.putString("Materials", "Alpha");
			
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
	
}
