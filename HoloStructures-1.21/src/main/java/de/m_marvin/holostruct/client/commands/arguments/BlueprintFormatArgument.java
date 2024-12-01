package de.m_marvin.holostruct.client.commands.arguments;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.m_marvin.blueprints.BlueprintLoader.BlueprintFormat;
import net.minecraft.commands.SharedSuggestionProvider;

/**
 * Argument for an blueprint file format
 * @author Marvin Koehler
 */
public class BlueprintFormatArgument implements ArgumentType<BlueprintFormat> {

	private static final Collection<String> EXAMPLES = Stream.of(BlueprintFormat.values()).map(format -> format.toString().toLowerCase()).toList();
	
	public static BlueprintFormatArgument format() {
		return new BlueprintFormatArgument();
	}
	
	public static BlueprintFormat getFormat(final CommandContext<?> context, final String name) {
		return context.getArgument(name, BlueprintFormat.class);
	}
	
	@Override
	public BlueprintFormat parse(final StringReader reader) throws CommandSyntaxException {
		String input = reader.readString();
		return BlueprintFormat.valueOf(input.toUpperCase());
	}
	
	@Override
	public boolean equals(final Object o) {
		 if (this == o) return true;
		 if (o instanceof BlueprintFormatArgument) {
		 	return true;
		 }
		 return false;
	}
	
	@Override
	public int hashCode() {
		 return 0;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_118250_, SuggestionsBuilder builder) {
		if (!(p_118250_.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			List<String> formats = Stream.of(BlueprintFormat.values()).map(format -> format.toString().toLowerCase()).toList();
			return SharedSuggestionProvider.suggest(formats, builder);
		}
	}
	
	@Override
	public Collection<String> getExamples() {
		 return EXAMPLES;
	}
	
	
}
