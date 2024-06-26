package de.m_marvin.holostruct.client.commands.arguments;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.m_marvin.holostruct.client.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

/**
 * Argument for blueprint file path
 * @author Marvin Koehler
 */
public class FilePathArgument implements ArgumentType<String> {

	private static final Collection<String> EXAMPLES = Collections.emptyList();
	
	private final String defaultFolder;
	private final Map<String, String> pathSuggestions;
	private final boolean suggestExistingFiles;
	private final boolean allowOnlyExisting;
	
	private FilePathArgument(Map<String, String> pathSuggestions, String defaultFolder, boolean suggestExistingFiles, boolean allowOnlyExisting) {
		 this.suggestExistingFiles = suggestExistingFiles;
		 this.allowOnlyExisting = allowOnlyExisting;
		 this.pathSuggestions = pathSuggestions;
		 this.defaultFolder = defaultFolder;
	}
	
	@SuppressWarnings("resource")
	public static File resolvePath(String path) {
		return new File(Minecraft.getInstance().gameDirectory, path).getAbsoluteFile();
	}
	
	public static FilePathArgument saveBlueprint() {
		return new FilePathArgument(
				ClientConfig.getAdditionalBlueprintFolders(), 
				ClientConfig.DEFAULT_BLUEPRINT_FOLDER.get(), 
				false, false);
	}
	
	public static FilePathArgument loadBlueprint() {
		return new FilePathArgument(
				ClientConfig.getAdditionalBlueprintFolders(), 
				ClientConfig.DEFAULT_BLUEPRINT_FOLDER.get(), 
				true, true);
	}
	
	public static FilePathArgument saveImage() {
		return new FilePathArgument(
				ClientConfig.getAdditionalImageFolders(), 
				ClientConfig.DEFAULT_IMAGE_FOLDER.get(), 
				false, false);
	}

	public static FilePathArgument loadImage() {
		return new FilePathArgument(
				ClientConfig.getAdditionalImageFolders(), 
				ClientConfig.DEFAULT_IMAGE_FOLDER.get(), 
				true, true);
	}
	
	public static String getPath(final CommandContext<?> context, final String name) {
		return context.getArgument(name, String.class);
	}
	
	public boolean isSuggestExistingFiles() {
		return suggestExistingFiles;
	}
	
	public boolean isAllowOnlyExisting() {
		return allowOnlyExisting;
	}
	
	@Override
	public String parse(final StringReader reader) throws CommandSyntaxException {
		String input = reader.readString();
		String[] inputSplit = input.endsWith(":") ? new String[] {input.substring(0, input.length() - 1), ""} : input.split(":");
		String path;
		if (inputSplit.length > 1) {
			String folderPath = this.pathSuggestions.getOrDefault(inputSplit[0], this.defaultFolder);
			String filePath = input.substring(inputSplit[0].length() + 1, input.length());
			path = folderPath + "/" + filePath;
		} else {
			String folderPath = this.defaultFolder;
			String filePath = input;
			path = folderPath + "/" + filePath;
		}
		if (allowOnlyExisting && !resolvePath(path).isFile()) {
			throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("Could not find file!")), Component.translatable("holostruct.commands.argument.filepath.invalid"));
		}
		return path;
	}
	
	@Override
	public boolean equals(final Object o) {
		 if (this == o) return true;
		 if (o instanceof FilePathArgument other) {
		 	return other.isAllowOnlyExisting() == isAllowOnlyExisting() && isSuggestExistingFiles() == isSuggestExistingFiles();
		 }
		 return false;
	}
	
	@Override
	public int hashCode() {
		 int i = 0;
		 if (allowOnlyExisting) i += 1;
		 if (suggestExistingFiles) i += 2;
		 return i;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_118250_, SuggestionsBuilder p_118251_) {
		if (!(p_118250_.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			Set<String> pathList = new HashSet<>();
			this.pathSuggestions.forEach((folderName, path) -> {
				listPaths(resolvePath(path), folderName + ":", pathList, suggestExistingFiles);
			});
			listPaths(resolvePath(this.defaultFolder), "", pathList, suggestExistingFiles);
			return SharedSuggestionProvider.suggest(Stream.of(pathList.toArray()).map((s) -> "\"" + s + "\""), p_118251_);
		}
	}
	
	protected void listPaths(File f, String path, Set<String> paths, boolean listFiles) {
		if (!f.isDirectory()) return;
		if (!listFiles && !path.isEmpty()) paths.add(path);
		for (String entry : f.list()) {
			File filepath = new File(f, entry);
			if (filepath.isFile()) {
				if (listFiles) paths.add(path + entry);
			} else if (filepath.isDirectory()) {
				listPaths(filepath, path + entry + "/", paths, listFiles);
			}
		}
	}
	
	@Override
	public Collection<String> getExamples() {
		 return EXAMPLES;
	}
	
}
