package de.m_marvin.blueprints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.blueprints.parser.NBTStrructParser;
import de.m_marvin.blueprints.parser.SchemParser;
import de.m_marvin.blueprints.parser.SchemParser.SchemVersion;

public class BlueprintLoader {
	
	public static final NBTStrructParser NBT_PARSER = new NBTStrructParser();
	public static final SchemParser	SCHEM_PARSER = new SchemParser();
	
	public static enum BlueprintFormat {
		NBT,
		SCHEM_SPONGE1,
		SCHEM_SPONGE2,
		SCHEM_SPONGE3;
	}
	
	public static BlueprintFormat formatFromExtension(String extension) {
		if (extension.equalsIgnoreCase("schem")) {
			return BlueprintFormat.SCHEM_SPONGE3;
		} else if (extension.equalsIgnoreCase("nbt")) {
			return BlueprintFormat.NBT;
		} else {
			return null;
		}
	}
	
	public static Blueprint loadBlueprint(File blueprintFile) {
		try {
			if (blueprintFile.getName().endsWith("nbt") || blueprintFile.getName().endsWith("NBT")) {
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
			} else if (blueprintFile.getName().endsWith("schem") || blueprintFile.getName().endsWith("SCHEM")) {
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
			System.err.println("unknown file extension!");
			return null;
		} catch (IOException e) {
			System.err.println("io error while reading blueprint file!");
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean saveBlueprint(Blueprint blueprint, File blueprintFile, BlueprintFormat format) {
		try {
			switch (format) {
			case NBT:
				NBT_PARSER.reset();
				if (!NBT_PARSER.build(blueprint)) {
					System.err.println("failed to build nbt structure nbt!");
					return false;
				}
				if (!NBT_PARSER.write(new FileOutputStream(blueprintFile))) {
					System.err.println("failed to write nbt structure file!");
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
			}
			return true;
		} catch (IOException e) {
			System.err.println("io error while writing blueprint file!");
			e.printStackTrace();
			return false;
		}
	}
	
}
