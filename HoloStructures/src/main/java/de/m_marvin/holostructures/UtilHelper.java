package de.m_marvin.holostructures;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
					() -> (list.size() > 0) ? Optional.of(String.valueOf(new Random().nextInt(list.size()))) : Optional.empty(), 
					(key) -> list.get(Integer.parseInt(key)),
					(key, value) -> list.add(value),
					(key) -> list.remove(Integer.parseInt(key)));
		}
	}
	
	
	public static List<CompoundTag> splitCompound(CompoundTag compoundTag, int maxStringLength) {

		System.out.println("Split " + compoundTag);
		
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
						readTag.offerLast(TagContainer.fromList(readList));
						readTagKey.offerLast(key.get());
						ListTag writeList = new ListTag();
						writeTag.peek().tagPutter().accept(key.get(), writeList);
						writeTag.offerLast(TagContainer.fromList(writeList));
						continue;
					} else if (tag instanceof CompoundTag readComp) {
						readTag.offerLast(TagContainer.fromCompound(readComp));
						readTagKey.offerLast(key.get());
						CompoundTag writeComp = new CompoundTag();
						writeTag.peek().tagPutter().accept(key.get(), writeComp);
						writeTag.offerFirst(TagContainer.fromCompound(writeComp));
						continue;
					} else {
						writeTag.peek().tagPutter().accept(key.get(), readTag.peek().tagGetter().apply(key.get()));
						if (writeTag.peek().toString().length() <= maxStringLength) {
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
			
			System.out.println("-> " + compound);
			splitCompounds.add(compound);
			
		}
		
		System.out.println("=> " + compoundTag);
		
		// TODO
		return new ArrayList<>();
		
	}
	

//	System.out.println("Split " + compoundTag);
//	
//	List<CompoundTag> splitCompounds = new ArrayList<>();
//			
//	while (!compoundTag.isEmpty()) {
//		
//		Deque<String> readTagKey = Queues.newArrayDeque();
//		Deque<CompoundTag> readTag = Queues.newArrayDeque();
//		Deque<CompoundTag> writeTag = Queues.newArrayDeque();
//		
//		CompoundTag compound = new CompoundTag();
//		
//		writeTag.offerFirst(compound);
//		readTag.offerLast(compoundTag);
//		
//		while (readTag.size() > 0) {
//			Optional<String> key = readTag.peek().getAllKeys().stream().findAny();
//			if (key.isPresent()) {
//				Tag tag = readTag.peek().get(key.get());
//				if (tag instanceof ListTag readList) {
//					
//				} else if (tag instanceof CompoundTag readComp) {
//					readTag.offerLast(readComp);
//					readTagKey.offerLast(key.get());
//					CompoundTag writeComp = new CompoundTag();
//					writeTag.peek().put(key.get(), writeComp);
//					writeTag.offerFirst(writeComp);
//					continue;
//				} else {
//					writeTag.peek().put(key.get(), readTag.peek().get(key.get()));
//					if (writeTag.peek().toString().length() <= maxStringLength) {
//						readTag.peek().remove(key.get());
//					} else {
//						writeTag.peek().remove(key.get());
//						break;
//					}
//				}
//			} else {
//				readTag.poll();
//				if (readTagKey.size() > 0) readTag.peek().remove(readTagKey.poll());
//				writeTag.poll();
//			}
//		}
//		
//		System.out.println("-> " + compound);
//		splitCompounds.add(compound);
//		
//	}
//	
//	System.out.println("=> " + compoundTag);
//	
//	// TODO
//	return new ArrayList<>();
	
	
}
 