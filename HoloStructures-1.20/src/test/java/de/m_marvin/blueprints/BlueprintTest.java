
package de.m_marvin.blueprints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import de.m_marvin.blueprints.api.Blueprint;
import de.m_marvin.blueprints.parser.IBlueprintParser;
import de.m_marvin.blueprints.parser.NBTStrructParser;
import de.m_marvin.blueprints.parser.SchemParser;

public class BlueprintTest {
	
	public static void main(String... args) {
		
		File runDir = new File(BlueprintTest.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "..\\..\\runs\\test");
		System.out.println("run dir: " + runDir);
		
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
