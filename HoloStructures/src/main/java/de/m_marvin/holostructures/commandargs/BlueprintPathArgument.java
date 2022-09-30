package de.m_marvin.holostructures.commandargs;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.m_marvin.holostructures.UtilHelper;
import de.m_marvin.holostructures.client.Config;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;

public class BlueprintPathArgument implements ArgumentType<String> {

	private static final Collection<String> EXAMPLES = Arrays.asList("not_aviable");
	
	private final boolean suggestExistingFiles;
	private final boolean allowOnlyExisting;
	
	private BlueprintPathArgument(boolean suggestExistingFiles, boolean allowOnlyExisting) {
		 this.suggestExistingFiles = suggestExistingFiles;
		 this.allowOnlyExisting = allowOnlyExisting;
	}
	
	public static BlueprintPathArgument save() {
		return new BlueprintPathArgument(false, false);
	}
	
	public static BlueprintPathArgument load() {
		return new BlueprintPathArgument(true, false);
	}

	public static BlueprintPathArgument loadOnlyExisting() {
		return new BlueprintPathArgument(true, true);
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
		String[] inputSplit = input.split(":");
		String path;
		if (inputSplit.length > 1) {
			String folderPath = Config.getFolder(inputSplit[0]);
			String filePath = input.substring(inputSplit[0].length() + 1, input.length());
			path = folderPath + "/" + filePath;
		} else {
			String folderPath = Config.getDefaultFolder();
			String filePath = input;
			path = folderPath + "/" + filePath;
		}
		if (allowOnlyExisting && !UtilHelper.resolvePath(path).isFile()) {
			throw new SimpleCommandExceptionType(new TranslatableComponent("argument.blueprintpath.invalid")).create();
		}
		return path;
	}
	
	@Override
	public boolean equals(final Object o) {
		 if (this == o) return true;
		 if (o instanceof BlueprintPathArgument other) {
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
			Config.getAdditionalFolders().forEach((folderName, path) -> {
				listPaths(UtilHelper.resolvePath(path), folderName + ":", pathList, suggestExistingFiles);
			});
			listPaths(UtilHelper.resolvePath(Config.getDefaultFolder()), "", pathList, suggestExistingFiles);
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
