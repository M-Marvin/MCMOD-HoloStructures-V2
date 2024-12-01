package de.m_marvin.blueprints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.blueprints.parser.IBlueprintParser;
import de.m_marvin.blueprints.parser.NBTStrructParser;
import de.m_marvin.blueprints.parser.SchemParser;
import de.m_marvin.blueprints.parser.SchemParser.SchemVersion;
import de.m_marvin.blueprints.parser.SchematicParser;

/**
 * An class combining all imnplementations of {@link IBlueprintParser}.
 * Automatically identifies the format of an file and selects the required parser.
 * 
 * @author Marvin Koehler
 */
public class BlueprintLoader {
	
	public static Map<Integer, String> LAGACY_STATE_MAP = new HashMap<>();
	
	public static final NBTStrructParser NBT_PARSER = new NBTStrructParser();
	public static final SchemParser	SCHEM_PARSER = new SchemParser();
	public static final SchematicParser SCHEMATIC_PARSER = new SchematicParser(() -> LAGACY_STATE_MAP);
	
	/**
	 * The supported formats of {@link BluprintLoader}
	 * @author Marvin Koehler
	 */
	public static enum BlueprintFormat {
		NBT("nbt"),
		SCHEMATIC_LAGACY("schematic"),
		SCHEM_SPONGE1("schem"),
		SCHEM_SPONGE2("schem"),
		SCHEM_SPONGE3("schem");
		
		private final String extension;
		
		private BlueprintFormat(String extenstion) {
			this.extension = extenstion;
		}
		
		public String getExtension() {
			return extension;
		}
	}
	
	/**
	 * Returns the format used for the file extension.
	 * In case multiple sup-formats exist for this extension, the newest one is returned.
	 * @param The file name extension
	 * @return extension The format used for this extension
	 */
	public static BlueprintFormat formatFromExtension(String extension) {
		if (extension.equalsIgnoreCase(BlueprintFormat.SCHEM_SPONGE3.getExtension())) {
			return BlueprintFormat.SCHEM_SPONGE3;
		} else if (extension.equalsIgnoreCase(BlueprintFormat.NBT.getExtension())) {
			return BlueprintFormat.NBT;
		} else if (extension.equalsIgnoreCase(BlueprintFormat.SCHEMATIC_LAGACY.getExtension())) {
			return BlueprintFormat.SCHEMATIC_LAGACY;
		} else {
			return null;
		}
	}
	
	/**
	 * Tries to load the file as {@link Blueprint}.
	 * @param blueprintFile The schematic file
	 * @return The blueprint or null if an error occurred.
	 */
	public static Blueprint loadBlueprint(File blueprintFile) {
		try {
			String[] s = blueprintFile.getName().split("\\.");
			BlueprintFormat format = s.length > 0 ? formatFromExtension(s[s.length - 1]) : null;
			
			if (format != null) {
				switch (format) {
				case NBT: {
					if (!NBT_PARSER.load(new FileInputStream(blueprintFile))) {
						System.err.println("failed to load nbt structure file!");
						return null;
					}
					Blueprint blueprint = new Blueprint();
					if (!NBT_PARSER.parse(blueprint)) {
						System.err.println("failed to parse blueprint nbt!");
						return null;
					}
					return blueprint;
				}
				case SCHEM_SPONGE1:
				case SCHEM_SPONGE2:
				case SCHEM_SPONGE3: {
					if (!SCHEM_PARSER.load(new FileInputStream(blueprintFile))) {
						System.err.println("failed to load schem structure file!");
						return null;
					}
					Blueprint blueprint = new Blueprint();
					if (!SCHEM_PARSER.parse(blueprint)) {
						System.err.println("failed to parse blueprint nbt!");
						return null;
					}
					return blueprint;
				}
				case SCHEMATIC_LAGACY: {
					if (!SCHEMATIC_PARSER.load(new FileInputStream(blueprintFile))) {
						System.err.println("failed to load schematic structure file!");
						return null;
					}
					Blueprint blueprint = new Blueprint();
					if (!SCHEMATIC_PARSER.parse(blueprint)) {
						System.err.println("failed to parse blueprint nbt!");
						return null;
					}
					return blueprint;
				}
				}
			}
			
			System.err.println("unknown file extension!");
			return null;
		} catch (IOException e) {
			System.err.println("io error while reading blueprint file!");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Tries to write the blueprint to an file with the specified format.
	 * @param blueprint The blueprint to save to the file
	 * @param blueprintFile The file path and name
	 * @param format The format for to save the blueprint with, should match the files extension for compatibility reasons.
	 * @return true if the file could successfully been written
	 */
	public static boolean saveBlueprint(Blueprint blueprint, File blueprintFile, BlueprintFormat format) {
		try {
			if (format == null) {
				String[] s = blueprintFile.getName().split("\\.");
				format = s.length > 0 ? formatFromExtension(s[s.length - 1]) : null;
			} else {
				String[] s = blueprintFile.getName().split("\\.");
				if (!s[s.length - 1].equalsIgnoreCase(format.getExtension())) {
					blueprintFile = new File(blueprintFile.toString() + "." + format.getExtension());
				}
			}
			
			if (format != null) {
				switch (format) {
				case NBT:
					NBT_PARSER.reset();
					if (!NBT_PARSER.build(blueprint)) {
						System.err.println("failed to build nbt structure nbt!");
						return false;
					}
					if (!NBT_PARSER.write(new FileOutputStream(blueprintFile))) {
						System.err.println("failed to write schem structure file!");
						return false;
					}
					break;
				case SCHEM_SPONGE1:
				case SCHEM_SPONGE2:
				case SCHEM_SPONGE3:
					switch (format) {
					case SCHEM_SPONGE1: SCHEM_PARSER.setSchemVersion(SchemVersion.SPONGE1); break;
					case SCHEM_SPONGE2: SCHEM_PARSER.setSchemVersion(SchemVersion.SPONGE2); break;
					case SCHEM_SPONGE3: SCHEM_PARSER.setSchemVersion(SchemVersion.SPONGE3); break;
					default: return false;
					}
					SCHEM_PARSER.reset();
					if (!SCHEM_PARSER.build(blueprint)) {
						System.err.println("failed to build schem nbt!");
						return false;
					}
					if (!SCHEM_PARSER.write(new FileOutputStream(blueprintFile))) {
						System.err.println("failed to write schem file!");
						return false;
					}
					break;
				case SCHEMATIC_LAGACY:
					SCHEMATIC_PARSER.reset();
					if (!SCHEMATIC_PARSER.build(blueprint)) {
						System.err.println("failed to build nbt structure nbt!");
						return false;
					}
					if (!SCHEMATIC_PARSER.write(new FileOutputStream(blueprintFile))) {
						System.err.println("failed to write schematic structure file!");
						return false;
					}
					break;
				}
				return true;
			}

			System.err.println("unspecified file extension!");
			return false;
		} catch (IOException e) {
			System.err.println("io error while writing blueprint file!");
			e.printStackTrace();
			return false;
		}
	}
	
}
