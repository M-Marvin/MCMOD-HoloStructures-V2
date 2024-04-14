package de.m_marvin.holostructures.client.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.m_marvin.holostructures.HoloStructures;
import net.minecraft.commands.SharedSuggestionProvider;

public class HologramArgument implements ArgumentType<String> {

	private static final Collection<String> EXAMPLES = Arrays.asList("hologram_name");
	
	public static HologramArgument hologram() {
		return new HologramArgument();
	}
	
	public static String getHologram(final CommandContext<?> context, final String name) {
		return context.getArgument(name, String.class);
	}
	
	@Override
	public String parse(final StringReader reader) throws CommandSyntaxException {
		String input = reader.readString();
		return input;
	}
	
	@Override
	public boolean equals(final Object o) {
		 if (this == o) return true;
		 if (o instanceof HologramArgument other) {
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
			return SharedSuggestionProvider.suggest(HoloStructures.CLIENT.HOLOGRAMS.getHolograms().keySet(), builder);
		}
	}
	
	@Override
	public Collection<String> getExamples() {
		 return EXAMPLES;
	}
	
}
