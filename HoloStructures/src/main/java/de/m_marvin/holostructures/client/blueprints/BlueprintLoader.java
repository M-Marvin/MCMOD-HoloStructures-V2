package de.m_marvin.holostructures.client.blueprints;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class BlueprintLoader {
	
	public static interface IFormatLoader {
		public boolean loadFromStream(Blueprint blueprint, InputStream inputStream) throws CommandSyntaxException;
		public boolean saveToStream(Blueprint blueprint, OutputStream outputStream) throws CommandSyntaxException;
	}
	
	public static enum BlueprintFormat {
		NBT("nbt", new LoaderNBT()),SCHEMATIC("schem", new LoaderSchem());
		
		private String fileExtension;
		private IFormatLoader formatLoader;
		
		private BlueprintFormat(String fileExtendion, IFormatLoader loader) {
			this.formatLoader = loader;
			this.fileExtension = fileExtendion;
		}
		
		public String getFileExtension() {
			return fileExtension;
		}
		
		public IFormatLoader getFormatLoader() {
			return formatLoader;
		}
		
		public static BlueprintFormat getFormat(String extension) {
			switch (extension) {
			case "nbt": return NBT;
			case "schem": return SCHEMATIC;
			default: return null;
			}
		}
		
	}
	
	public static Optional<Blueprint> loadBlueprint(InputStream inputStream, BlueprintFormat format) throws CommandSyntaxException {
		Blueprint blueprint = new Blueprint();
		boolean success = format.getFormatLoader().loadFromStream(blueprint, inputStream);	
		return success ? Optional.of(blueprint) : Optional.empty();
	}
	
	public static boolean saveBlueprint(OutputStream outputStream, BlueprintFormat format, Blueprint blueprint) throws CommandSyntaxException {
		return format.getFormatLoader().saveToStream(blueprint, outputStream);
	}
	
}
