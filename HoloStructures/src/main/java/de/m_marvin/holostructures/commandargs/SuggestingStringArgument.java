package de.m_marvin.holostructures.commandargs;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;

public class SuggestingStringArgument implements ArgumentType<String> {
	
	protected final StringType type;
	protected Supplier<String[]> suggestions;
	
	public SuggestingStringArgument(final StringType type, Supplier<String[]> suggestions) {
		this.type = type;
		this.suggestions = suggestions;
	}
	
	public static SuggestingStringArgument wordSuggesting(Supplier<String[]> suggestions) {
		return new SuggestingStringArgument(StringType.SINGLE_WORD, suggestions);
	}

	public static SuggestingStringArgument stringSuggesting(Supplier<String[]> suggestions) {
		return new SuggestingStringArgument(StringType.QUOTABLE_PHRASE, suggestions);
	}

	public static SuggestingStringArgument greedyStringwordSuggesting(Supplier<String[]> suggestions) {
		return new SuggestingStringArgument(StringType.GREEDY_PHRASE, suggestions);
	}
	
	public static String getString(final CommandContext<?> context, final String name) {
		return context.getArgument(name, String.class);
	}

	public StringType getType() {
		return type;
	}

	@Override
	public String parse(final StringReader reader) throws CommandSyntaxException {
		if (type == StringType.GREEDY_PHRASE) {
			final String text = reader.getRemaining();
			reader.setCursor(reader.getTotalLength());
			return text;
		} else if (type == StringType.SINGLE_WORD) {
			return reader.readUnquotedString();
		} else {
			return reader.readString();
		}
	}

	@Override
	public String toString() {
		return "suggesting_string()";
	}

	@Override
	public Collection<String> getExamples() {
		return type.getExamples();
	}

	public static String escapeIfRequired(final String input) {
		for (final char c : input.toCharArray()) {
			if (!StringReader.isAllowedInUnquotedString(c)) {
				return escape(input);
			}
		}
		return input;
	}

	private static String escape(final String input) {
		final StringBuilder result = new StringBuilder("\"");

		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (c == '\\' || c == '"') {
				result.append('\\');
			}
			result.append(c);
		}

		result.append("\"");
		return result.toString();
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
