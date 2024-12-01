
package de.m_marvin.blueprints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.blueprints.parser.IBlueprintParser;
import de.m_marvin.blueprints.parser.NBTStrructParser;
import de.m_marvin.blueprints.parser.SchemParser;
import de.m_marvin.blueprints.parser.SchematicParser;

public class BlueprintTest {
	
	/*
	 * This is (horrible) spaghetti helper code and has nothing to do with the mod. xD
	 */
	
	public static void main(String... args) throws JsonSyntaxException, JsonIOException, IOException {
		
		File runDir = new File(BlueprintTest.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "..\\..\\runs\\test");
		System.out.println("run dir: " + runDir);
		
	}
	
	public static void renameMappings(File runDir) throws JsonSyntaxException, JsonIOException, IOException {
		
		//File mappingsF = new File(runDir, "files/legacy.json");
		File renamedF = new File(runDir, "files/legacy2.json");
		File outputFIle = new File(runDir, "files/legacy_renamed.json");
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		//JsonObject mappings = gson.fromJson(new InputStreamReader(new FileInputStream(mappingsF)), JsonObject.class);
		JsonObject renamed = gson.fromJson(new InputStreamReader(new FileInputStream(renamedF)), JsonObject.class);
		JsonObject renamed2 = renamed.get("blocks").getAsJsonObject();
		
		JsonObject output = new JsonObject();
		
		for (String rk : renamed2.keySet()) {

			int blockId = Integer.parseInt(rk.split(":")[0]);
			int metaId = Integer.parseInt( rk.split(":")[1]);
			
			int index = blockId | (metaId << 12);
			
			output.addProperty(String.valueOf(index), renamed2.get(rk).getAsString());
			
		}
		
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFIle));
		gson.toJson(output, writer);
		writer.close();
		
	}
	
	public static void loadSaveSCHEMATIC_Test(File runDir) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		File[] schemFiles = new File(runDir, "files/schematics/").listFiles();
		File lagacyMap = new File(runDir, "files/lagacy.json");
		
		Gson gson = new Gson();
		JsonObject mapJson = gson.fromJson(new InputStreamReader(new FileInputStream(lagacyMap)), JsonObject.class);
		
		Map<Integer, String> mappings = mapJson.keySet().stream().collect(Collectors.toMap(name -> mapJson.get(name).getAsInt(), name -> name));
		
		IBlueprintParser parser = new SchematicParser(() -> mappings);
		
		for (File schemFile : schemFiles) {
			if (schemFile.isDirectory()) continue;
			File outFile = new File(schemFile.getParentFile(), "/res/" + schemFile.getName());
			System.out.println("\nDEBUG try load file " + schemFile.getName());
			
			try {
				if (!parser.load(new FileInputStream(schemFile))) {
					System.err.println("failed to load schem file!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("schem file loaded");
			
			Blueprint blueprint = new Blueprint();
			
			if (!parser.parse(blueprint)) {
				System.err.println("failed to parse schem file!");
			}
			
			System.out.println("Blueprint loaded");
			
			parser.reset();
			
			if (!parser.build(blueprint)) {
				System.err.println("failed to build schem file!");
			}
			
			System.out.println("schem file build");
			
			try {
				if (!parser.write(new FileOutputStream(outFile))) {
					System.err.println("failed to write schem file!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("schem file written");
			
		}
	}
	
	public static void loadSaveSCHEM_Test(File runDir) {
		File[] schemFiles = new File(runDir, "files/schems/").listFiles();
		
		IBlueprintParser parser = new SchemParser();
		
		for (File schemFile : schemFiles) {
			if (schemFile.isDirectory()) continue;
			File outFile = new File(schemFile.getParentFile(), "/res/" + schemFile.getName() + ".nbt");
			System.out.println("\nDEBUG try load file " + schemFile.getName());
			
			try {
				if (!parser.load(new FileInputStream(schemFile))) {
					System.err.println("failed to load schem file!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("schem file loaded");
			
			Blueprint blueprint = new Blueprint();
			
			if (!parser.parse(blueprint)) {
				System.err.println("failed to parse schem file!");
			}
			
			System.out.println("Blueprint loaded");
			
			parser.reset();
			
			if (!parser.build(blueprint)) {
				System.err.println("failed to build schem file!");
			}
			
			System.out.println("schem file build");
			
			try {
				if (!parser.write(new FileOutputStream(outFile))) {
					System.err.println("failed to write schem file!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("schem file written");
			
		}
		
	}
	
	public static void loadSaveNBT_Test(File runDir) {
		File testFile = new File(runDir, "files/test.nbt");
		File testFileCopy = new File(runDir, "files/test_copy.nbt");
		
		try {
			File fin = new File(runDir, "files/test.schem");
			File fout = new File(runDir, "files/out.bin");
			
			InputStream in = new GZIPInputStream(new FileInputStream(fin));
			OutputStream out = new FileOutputStream(fout);
			out.write(in.readAllBytes());
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
		
		IBlueprintParser parser = new NBTStrructParser();
		
		try {
			if (!parser.load(new FileInputStream(testFile))) {
				System.err.println("This file is not a valid blueprint file!");
				System.exit(-1);
			}
		} catch (IOException e) {
			System.err.println("IO Exception!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		Blueprint blueprint = new Blueprint();
		
		if (!parser.parse(blueprint)) {
			System.err.println("Failed to parse blueprint file!");
			System.exit(-1);
		}
		
		
		System.out.println("Blueprint loaded");
		
		
		parser.reset();
		if (!parser.build(blueprint)) {
			System.err.println("Failed to build blueprint file!");
			System.exit(-1);
		}
		
		try {
			if (!parser.write(new FileOutputStream(testFileCopy))) {
				System.err.println("Could not write copy file!");
				System.exit(-1);
			}
		} catch (IOException e) {
			System.err.println("IO Exception!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		System.out.println("Blueprint saved");
	}
	
}
