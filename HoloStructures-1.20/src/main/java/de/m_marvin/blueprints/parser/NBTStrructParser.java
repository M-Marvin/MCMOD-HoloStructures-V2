package de.m_marvin.blueprints.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.m_marvin.blueprints.api.IStructAccessor;
import de.m_marvin.blueprints.api.RegistryName;
import de.m_marvin.blueprints.api.worldobjects.BlockEntityData;
import de.m_marvin.blueprints.api.worldobjects.BlockStateData;
import de.m_marvin.blueprints.api.worldobjects.EntityData;
import de.m_marvin.nbtutility.BinaryParser;
import de.m_marvin.nbtutility.TagType;
import de.m_marvin.nbtutility.nbt.ITagBase;
import de.m_marvin.nbtutility.nbt.TagCompound;
import de.m_marvin.nbtutility.nbt.TagDouble;
import de.m_marvin.nbtutility.nbt.TagInt;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class NBTStrructParser implements IBlueprintParser {

	protected TagCompound nbtTag;
	protected int dataVersion;
	
	@Override
	public boolean load(InputStream istream) throws IOException {
		this.nbtTag = BinaryParser.readCompressed(istream, TagCompound.class);
		if (!this.nbtTag.has("DataVersion", TagType.INT)) return false;
		if (!this.nbtTag.has("size", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("palette", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("entities", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("blocks", TagType.TAG_LIST)) return false;
		this.dataVersion = this.nbtTag.getInt("DataVersion");
		return true;
	}

	@Override
	public boolean write(OutputStream ostream) throws IOException {
		if (!this.nbtTag.has("DataVersion", TagType.INT)) return false;
		if (!this.nbtTag.has("size", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("palette", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("entities", TagType.TAG_LIST)) return false;
		if (!this.nbtTag.has("blocks", TagType.TAG_LIST)) return false;
		BinaryParser.writeCompressed(this.nbtTag, ostream);
		return true;
	}
	
	public int getDataVersion() {
		return dataVersion;
	}
	
	public void setDataVersion(int dataVersion) {
		this.dataVersion = dataVersion;
	}

	@Override
	public boolean parse(IStructAccessor target) {
		target.clearParseLogs();
		try {
			Vec3i size = loadVectorI(this.nbtTag.getList("size", TagInt.class));
			target.setBounds(new Vec3i(0, 0, 0), size);
			
			List<TagCompound> paletteList = this.nbtTag.getList("palette", TagCompound.class);
			BlockStateData[] palette = new BlockStateData[paletteList.size()];
			int index = 0;
			for (TagCompound entry : paletteList) {
				int id = index++;
				try {
					RegistryName blockName = new RegistryName(entry.getString("Name"));
					BlockStateData blockState = new BlockStateData(blockName);
					if (entry.has("Properties", TagType.COMPOUND)) {
						TagCompound properties = entry.getCompound("Properties");
						for (String prop : properties.getMap().keySet()) {
							blockState.setValue(prop, properties.getString(prop));
						}
					}
					palette[id] = blockState;
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to load block entry from nbt: index %d", id);
					e.printStackTrace();
				}
			}
			
			for (TagCompound blockTag : this.nbtTag.getList("blocks", TagCompound.class)) {
				try {
					Vec3i position = loadVectorI(blockTag.getList("pos", TagInt.class));
					int stateIndex = blockTag.getInt("state");
					assert stateIndex < palette.length : "state palette index out of bounds!";
					target.setBlock(position, palette[stateIndex]);
					
					if (blockTag.has("nbt", TagType.COMPOUND)) {
						TagCompound nbt = blockTag.getCompound("nbt");
						RegistryName typeName = new RegistryName(nbt.getString("id"));
						BlockEntityData blockEntity = new BlockEntityData(position, typeName);
						blockEntity.setData(nbt);
						target.setBlockEntity(position, blockEntity);
					}
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to load block entry from nbt: %s", blockTag);
					e.printStackTrace();
				}
			}
			
			for (TagCompound entityTag : this.nbtTag.getList("entities", TagCompound.class)) {
				try {
					TagCompound nbt = entityTag.getCompound("nbt");
					RegistryName entityName = new RegistryName(nbt.getString("id"));
					Vec3d position = loadVectorD(entityTag.getList("pos", TagDouble.class));
					EntityData entity = new EntityData(position, entityName);
					entity.setData(nbt);
					target.addEntity(entity);
				} catch (Throwable e) {
					IBlueprintParser.logWarn(target, "failed to load entity from nbt: %s", entityTag.toString());
					e.printStackTrace();
				}
			}
			
			return true;
		} catch (Throwable e) {
			IBlueprintParser.logWarn(target, "failed to parse nbt structure: %s", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void reset() {
		this.nbtTag = new TagCompound();
	}
	
	@Override
	public boolean build(IStructAccessor source) {
		source.clearParseLogs();
		try {
			
			Vec3i size = source.getBoundsMax().sub(source.getBoundsMin());
			this.nbtTag.putList("size", writeVectorI(size));
			
			Object2IntMap<BlockStateData> palette = new Object2IntArrayMap<>();
			List<TagCompound> paletteList = new ArrayList<>();
			List<TagCompound> blockList = new ArrayList<>();
			for (int x = 0; x < size.x; x++) {
				for (int z = 0; z < size.z; z++) {
					for (int y = 0; y < size.y; y++) {
						try {
							Vec3i position = new Vec3i(x, y, z).add(source.getBoundsMin());
							BlockStateData blockState = source.getBlock(position);
							if (blockState == null) continue;
							
							if (!palette.containsKey(blockState)) {
								TagCompound paletteTag = new TagCompound();
								paletteTag.putString("Name", blockState.getBlockName().toString());
								if (blockState.getProperties().size() > 0) {
									TagCompound propTag = new TagCompound();
									for (Entry<String, String> prop : blockState.getProperties().entrySet()) {
										propTag.putString(prop.getKey(), prop.getValue());
									}
									paletteTag.putTag("Properties", propTag);
								}
								palette.put(blockState, paletteList.size());
								paletteList.add(paletteTag);
							}
							
							TagCompound blockTag = new TagCompound();
							blockTag.putInt("state", palette.getInt(blockState));
							blockTag.putList("pos", writeVectorI(position));
							BlockEntityData blockEntity = source.getBlockEntity(position);
							if (blockEntity != null) {
								TagCompound blockEntityTag = blockEntity.getData();
								blockEntityTag.putString("id", blockEntity.getTypeName().toString());
								blockTag.putTag("nbt", blockEntity.getData());
								
							}
							blockList.add(blockTag);
						} catch (Throwable e) {
							IBlueprintParser.logWarn(source, "failed to write block nbt data: pos %d %d %d", x, y, z);
							e.printStackTrace();
							return false;
						}
					}
				}
			}
			this.nbtTag.putList("blocks", blockList);
			this.nbtTag.putList("palette", paletteList);
			
			List<TagCompound> entityList = new ArrayList<>();
			for (EntityData entity : source.getEntitiesWithin(source.getBoundsMin(), source.getBoundsMax())) {
				try {
					TagCompound entityTag = new TagCompound();
					entityTag.putList("pos", writeVectorD(entity.getPosition()));
					entityTag.putList("blockPos", writeVectorI(new Vec3i(entity.getPosition())));
					entityTag.putTag("nbt", entity.getData());
					entityList.add(entityTag);
				} catch (Throwable e) {
					IBlueprintParser.logWarn(source, "failed to write entity nbt data: entity position %.2f %.2f %.2f", entity.getPosition().x, entity.getPosition().y, entity.getPosition().z);
					e.printStackTrace();
					return false;
				}
			}
			this.nbtTag.putList("entities", entityList);
			
			this.nbtTag.putInt("DataVersion", this.dataVersion);
			
			return true;
		} catch (Throwable e) {
			IBlueprintParser.logWarn(source, "failed to write nbt structure: %s", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	protected Vec3d loadVectorD(List<TagDouble> tagList) {
		assert tagList.size() == 3 : "invalid tag list for 3D vector, length != 3";
		return new Vec3d(
					tagList.get(0).getDouble(),
					tagList.get(1).getDouble(),
					tagList.get(2).getDouble()
				);
	}

	protected List<ITagBase> writeVectorD(Vec3d vec) {
		List<ITagBase> list = new ArrayList<>();
		list.add(new TagDouble(vec.getX()));
		list.add(new TagDouble(vec.getY()));
		list.add(new TagDouble(vec.getZ()));
		return list;
	}
	
	protected Vec3i loadVectorI(List<TagInt> tagList) {
		assert tagList.size() == 3 : "invalid tag list for 3D vector, length != 3";
		return new Vec3i(
					tagList.get(0).getInt(),
					tagList.get(1).getInt(),
					tagList.get(2).getInt()
				);
	}

	protected List<ITagBase> writeVectorI(Vec3i vec) {
		List<ITagBase> list = new ArrayList<>();
		list.add(new TagInt(vec.getX()));
		list.add(new TagInt(vec.getY()));
		list.add(new TagInt(vec.getZ()));
		return list;
	}
	
}
