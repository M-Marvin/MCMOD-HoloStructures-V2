package de.m_marvin.holostructures;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Queues;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class UtilHelper {

	public static String formatBlockPos(BlockPos pos) {
		return pos.getX() + " " + pos.getY() + " " + pos.getZ();
	}

	public static String formatVecPos(Vec3 pos) {
		return pos.x() + " " + pos.y() + " " + pos.z();
	}
	
	public static String formatBlockState(BlockState state) {
		return state.toString().replace("Block{", "").replace("}", "");
	}
	
	public static CompoundTag encryptNBTFromResponse(Component commandResponse) {
		try {
			String s = commandResponse.getString();
			int i = s.indexOf('{');
			if (i == -1) return new CompoundTag();
			s = s.substring(i, s.length());
			return TagParser.parseTag(s);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			return new CompoundTag();
		}
	}
	
	private static record TagContainer<T extends Tag>(T tag, Supplier<Optional<String>> keyGetter, Function<String, Tag> tagGetter, BiConsumer<String, Tag> tagPutter, Consumer<String> tagRemover) {
		public static TagContainer<CompoundTag> fromCompound(CompoundTag compound) {
			return new TagContainer<CompoundTag>(compound, 
					() -> compound.getAllKeys().stream().findAny(), 
					(key) -> compound.get(key),
					(key, value) -> compound.put(key, value),
					(key) -> compound.remove(key));
		}
		public static TagContainer<ListTag> fromList(ListTag list) {
			return new TagContainer<ListTag>(list, 
					() -> (list.size() > 0) ? Optional.of("0") : Optional.empty(), 
					(key) -> list.get(Integer.parseInt(key)),
					(key, value) -> list.add(value),
					(key) -> list.remove(Integer.parseInt(key)));
		}
	}
		
	public static List<CompoundTag> sizeLimitedCompounds(CompoundTag compoundTag, int maxStringLength) {
		
		List<CompoundTag> splitCompounds = new ArrayList<>();
				
		while (!compoundTag.isEmpty()) {
			
			Deque<String> readTagKey = Queues.newArrayDeque();
			Deque<TagContainer<?>> readTag = Queues.newArrayDeque();
			Deque<TagContainer<?>> writeTag = Queues.newArrayDeque();
			CompoundTag compound = new CompoundTag();
			
			writeTag.offerFirst(TagContainer.fromCompound(compound));
			readTag.offerLast(TagContainer.fromCompound(compoundTag));
			
			while (readTag.size() > 0) {
				Optional<String> key = readTag.peek().keyGetter().get();
				if (key.isPresent()) {
					Tag tag = readTag.peek().tagGetter().apply(key.get());
					if (tag instanceof ListTag readList) {
						readTag.offerFirst(TagContainer.fromList(readList));
						readTagKey.offerFirst(key.get());
						ListTag writeList = new ListTag();
						writeTag.peek().tagPutter().accept(key.get(), writeList);
						writeTag.offerFirst(TagContainer.fromList(writeList));
						continue;
					} else if (tag instanceof CompoundTag readComp) {
						readTag.offerFirst(TagContainer.fromCompound(readComp));
						readTagKey.offerFirst(key.get());
						CompoundTag writeComp = new CompoundTag();
						writeTag.peek().tagPutter().accept(key.get(), writeComp);
						writeTag.offerFirst(TagContainer.fromCompound(writeComp));
						continue;
					} else {
						writeTag.peek().tagPutter().accept(key.get(), readTag.peek().tagGetter().apply(key.get()));
						if (compound.toString().length() <= maxStringLength) {
							readTag.peek().tagRemover().accept(key.get());
						} else {
							writeTag.peek().tagRemover().accept(key.get());
							break;
						}
					}
				} else {
					readTag.poll();
					if (readTagKey.size() > 0) readTag.peek().tagRemover().accept(readTagKey.poll());
					writeTag.poll();
				}
			}
			
			splitCompounds.add(compound);
			
		}
		
		return splitCompounds;
	}
	
	public static void forEachEntry(String path, CompoundTag nbt, BiConsumer<String, Tag> task) {
		nbt.getAllKeys().forEach((key) -> {
			Tag tag = nbt.get(key);
			String path2 = (path.isEmpty() ? "" : path + ".") + key;
			if (tag instanceof CompoundTag compound) {
				forEachEntry(path2, compound, task);
			} else if (tag instanceof ListTag list) {
				forEachEntry(path2, list, task);
			} else {
				task.accept(path2, tag);
			}
		});
	}

	public static void forEachEntry(String path, ListTag nbt, BiConsumer<String, Tag> task) {
		for (int i = 0; i < nbt.size(); i++) {
			Tag tag = nbt.get(i);
			String path2 = (path.isEmpty() ? "" : path + ".") + "[" + i + "]";
			if (tag instanceof CompoundTag compound) {
				forEachEntry(path2, compound, task);
			} else if (tag instanceof ListTag list) {
				forEachEntry(path2, list, task);
			} else {
				task.accept(path2, tag);
			}
		}
	}
	
}
 