package de.m_marvin.holostruct.client.levelbound.access.clientlevel;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher.Command;
import de.m_marvin.holostruct.client.levelbound.access.clientlevel.commanddispatcher.Command.Response;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.locale.Language;

public class ClientCommandDispatcher {
	
	public static final Supplier<ClientPacketListener> PACKAGE_LISTENER = () -> Minecraft.getInstance().getConnection();
	
	private Map<Pattern, String> languageReverseMap = new HashMap<>();
	private Queue<Command<?>> commandQueue = new ArrayDeque<>();
	
	public void reloadReverseMap(Language language) {
		this.languageReverseMap = language.getLanguageData().entrySet().stream()
				.filter(entry -> entry.getKey().contains("command") || entry.getKey().contains("argument"))
				.collect(Collectors.toMap(entry -> {
					StringBuilder regexBuilder = new StringBuilder();
					
					String translation = entry.getValue();
					Matcher m = FORMAT_PATTERN.matcher(translation);
					
					int literalCount = 0;
					int parseEnd = 0;
					while (m.find()) {
						int i1 = m.start();
						int i2 = m.end();
						regexBuilder.append(Pattern.quote(translation.substring(parseEnd, i1)));
						literalCount += i1 - parseEnd;
						parseEnd = i2;
						
						if (m.group(2).equals("%")) regexBuilder.append("%");
						if (m.group(1) != null) {
							String indexName = "index" + m.group(1);
							regexBuilder.append(String.format("(?<%s>.+)", indexName));
						} else {
							regexBuilder.append("(.+)");
						}
					}
					regexBuilder.append(Pattern.quote(translation.substring(parseEnd, translation.length())));
					literalCount += translation.length() - parseEnd;
					
					return literalCount > 0 ? Pattern.compile(regexBuilder.toString()) : null;
				}, entry -> {
					return entry.getKey();
				}, (a, b) -> a));
		this.languageReverseMap.remove(null);
	}
	
	public <T> CompletableFuture<T> startDispatch(Command<T> command) {
		new CompletableFuture<>();
		synchronized (this.commandQueue) {
			PACKAGE_LISTENER.get().sendCommand(command.command());
			this.commandQueue.add(command);
			return command.startDispatch(System.currentTimeMillis());
		}
	}

	private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
	private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("\\((?:\\?<index([0-9]+)>|)\\.\\+\\)");
	
	public boolean handleSysteMessage(String message) {
		
		for (Pattern pattern : this.languageReverseMap.keySet()) {
			Matcher matcher = pattern.matcher(message);
			if (matcher.matches()) {
				String keyName = this.languageReverseMap.get(pattern);
				String patternStr = pattern.pattern();
				
				List<String> values = null;
				try {
					Map<Integer, String> name2value = GROUP_NAME_PATTERN.matcher(patternStr).results()
							.mapToInt(m -> Integer.parseInt(m.group(1)))
							.boxed()
							.collect(Collectors.toMap(index -> (int) index, index -> {
								return matcher.group("index" + index);
							}, (a, b) -> a));
					values = name2value.keySet().stream().sorted().map(k -> name2value.get(k)).toList();
				} catch (NumberFormatException e) {
					values = IntStream.range(1, matcher.groupCount() + 1).mapToObj(k -> matcher.group(k)).toList();
				}
				
				synchronized (this.commandQueue) {
					Command<?> command = this.commandQueue.peek();
					Response response = new Response(keyName, values);
					if (command != null) {
						while (this.commandQueue.peek().isOutdated(System.currentTimeMillis()))
							this.commandQueue.poll();
						if (command.tryAccept(response)) {
							this.commandQueue.poll();
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
}
