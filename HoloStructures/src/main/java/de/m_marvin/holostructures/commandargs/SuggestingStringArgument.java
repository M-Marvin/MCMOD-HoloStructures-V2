package de.m_marvin.holostructures.commandargs;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;

public class SuggestingStringArgument extends StringArgumentType {
	
	private Supplier<String[]> suggestions;
	
	public SuggestingStringArgument setSuggestions(Supplier<String[]> suggestions) {
		this.suggestions = suggestions;
		return this;
	}
	
	public static SuggestingStringArgument wordSuggesting(Supplier<String[]> suggestions) {
		return new SuggestingStringArgument().
	}

	public static SuggestingStringArgument stringSuggesting(Supplier<String[]> suggestions) {
		return new SuggestingStringArgument(suggestions, StringType.QUOTABLE_PHRASE);
	}

	public static SuggestingStringArgument greedyStringwordSuggesting(Supplier<String[]> suggestions) {
		return new SuggestingStringArgument(suggestions, StringType.GREEDY_PHRASE);
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			return SharedSuggestionProvider.suggest(Stream.of(this.suggestions.get()), builder);
		}
	}
	
}
