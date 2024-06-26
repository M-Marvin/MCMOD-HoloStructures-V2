package de.m_marvin.blueprints.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import de.m_marvin.blueprints.api.IBlueprintAcessor;
import de.m_marvin.nbtutility.BinaryParser;
import de.m_marvin.nbtutility.TagType;
import de.m_marvin.nbtutility.nbt.TagCompound;
import net.minecraft.SharedConstants;

/**
 * An implementation of {@link IBlueprintParser} for the .schem format.
 * Supports the sub-formats Sponge-1 up to Sponge-3
 * 
 * @author Marvin Koehler
 */
public class SchemParser implements IBlueprintParser {
	
	public static final Pattern BLOCK_STATE_PARSE_PATTERN = Pattern.compile("([a-z0-9_\\-\\:]{1,})\\[([a-z0-9_\\-=\\,]{1,})\\]");
	public static final Pattern BLOCK_STATE_PROPERTY_PATTERN = Pattern.compile("([a-z0-9_\\-]{1,})=([a-z0-9_\\-]{1,})");
	
	public static enum SchemVersion {
		SPONGE1(1, SchemParserSponge1::parseSchem, SchemParserSponge1::buildSchem),
		SPONGE2(2, SchemParserSponge2::parseSchem, SchemParserSponge2::buildSchem),
		SPONGE3(3, SchemParserSponge3::parseSchem, SchemParserSponge3::buildSchem);
		
		private final int version;
		private final BiFunction<TagCompound, IBlueprintAcessor, Boolean> parser;
		private final BiFunction<TagCompound, IBlueprintAcessor, Boolean> builder;
		
		private SchemVersion(int version, BiFunction<TagCompound, IBlueprintAcessor, Boolean> parser, BiFunction<TagCompound, IBlueprintAcessor, Boolean> builder) {
			this.version = version;
			this.parser = parser;
			this.builder = builder;
		}
		
		public int getVersion() {
			return version;
		}
		
		public BiFunction<TagCompound, IBlueprintAcessor, Boolean> getParser() {
			return parser;
		}
		
		public BiFunction<TagCompound, IBlueprintAcessor, Boolean> getBuilder() {
			return builder;
		}
		
		public static SchemVersion byVersion(int version) {
			for (SchemVersion ver : values()) if (ver.version == version) return ver;
			return null;
		}
	}
	
	protected TagCompound nbtTag;
	protected int dataVersion;
	protected SchemVersion version;
	
	@Override
	public boolean load(InputStream istream) throws IOException {
		this.nbtTag = BinaryParser.readCompressed(istream, TagCompound.class);
		TagCompound rootTag = this.nbtTag;
		if (this.nbtTag.has("Schematic", TagType.COMPOUND)) rootTag = this.nbtTag.getCompound("Schematic");
		if (!rootTag.has("Version", TagType.INT)) return false;
		if (rootTag.has("Metadata", TagType.COMPOUND)) loadMetaTag(rootTag.getCompound("Metadata"));
		this.version = SchemVersion.byVersion(rootTag.getInt("Version"));
		if (this.version == null) return false;
		return true;
	}

	@Override
	public boolean write(OutputStream ostream) throws IOException {
		if (this.version == null) {
			System.err.println("no schem version set!");
			return false;
		}
		TagCompound rootTag = this.nbtTag;
		if (this.nbtTag.has("Schematic", TagType.COMPOUND)) rootTag = this.nbtTag.getCompound("Schematic");
		rootTag.putInt("DataVersion", this.dataVersion);
		rootTag.putInt("Version", this.version.getVersion());
		rootTag.putTag("Metadata", makeMetaTag());
		BinaryParser.writeCompressed(this.nbtTag, this.version == SchemVersion.SPONGE3 ? "" : "Schematic", ostream);
		return true;
	}
	
	public int getDataVersion() {
		return dataVersion;
	}
	
	public void setDataVersion(int dataVersion) {
		this.dataVersion = dataVersion;
	}
	
	public SchemVersion getSchemVersion() {
		return version;
	}
	
	public void setSchemVersion(SchemVersion version) {
		this.version = version;
	}
	
	@Override
	public boolean parse(IBlueprintAcessor target) {
		target.clearParseLogs();
		if (this.version == null) {
			System.err.println("no schem version set!");
			return false;
		} else {
			return this.version.getParser().apply(this.nbtTag, target);
		}
	}
	
	@Override
	public void reset() {
		this.nbtTag = new TagCompound();
		this.dataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
	}
	
	@Override
	public boolean build(IBlueprintAcessor source) {
		source.clearParseLogs();
		if (this.version == null) {
			System.err.println("no schem version set!");
			return false;
		} else {
			return this.version.getBuilder().apply(this.nbtTag, source);
		}
	}
	
	protected void loadMetaTag(TagCompound nbt) {}
	
	protected TagCompound makeMetaTag() {
		TagCompound nbt = new TagCompound();
		return nbt;
	}
	
}
