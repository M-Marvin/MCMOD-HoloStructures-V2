package de.m_marvin.holostructures.commandargs;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Direction;

public class DirectionArgumentType implements ArgumentType<Direction> {
	
	private static final Collection<String> EXAMPLES = Arrays.asList("north", "east", "up");
	
	private final Direction[] values;
	
	private DirectionArgumentType(final Direction[] values) {
		 this.values = values;
	}
	
	public static DirectionArgumentType directions() {
		return directions(Direction.values());
	}
	
	public static DirectionArgumentType directions(Direction... values) {
		return new DirectionArgumentType(values);
	}
	
	public static Direction getDirection(final CommandContext<?> context, final String name) {
		 return context.getArgument(name, Direction.class);
	}
	
	public Direction[] getValues() {
		return values;
	}
	
	@Override
	public Direction parse(final StringReader reader) throws CommandSyntaxException {
		 Direction result = Direction.byName(reader.readString());
		 if (result == null) result = Direction.NORTH;
		 return result;
	}
	
	@Override
	public boolean equals(final Object o) {
		 if (this == o) return true;
		 if (o instanceof DirectionArgumentType other) {
		 	return other.getValues().equals(this.values);
		 }
		 return false;
	}
	
	@Override
	public int hashCode() {
		 return this.values.hashCode();
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_118250_, SuggestionsBuilder p_118251_) {
		if (!(p_118250_.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			return SharedSuggestionProvider.suggest(Stream.of(getValues()).map((d) -> d.getName()), p_118251_);
		}
	}
	
	@Override
	public Collection<String> getExamples() {
		 return EXAMPLES;
	}
	
}
