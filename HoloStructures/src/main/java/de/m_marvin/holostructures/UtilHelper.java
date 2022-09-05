package de.m_marvin.holostructures;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

import com.google.common.collect.Queues;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
	
	public static List<CompoundTag> splitCompound(CompoundTag compoundTag, int maxStringLength) {
		
//		Predicate<Character> openBraketTest = (jsonChar) -> jsonChar == '{' || jsonChar == '[';
//		Predicate<Character> closeBraketTest = (jsonChar) -> jsonChar == '}' || jsonChar == ']';
//		
//		String jsonString = compoundTag.toString();
//		
//		int openBrakets = 0;
//		int lastCompleteTagIndex = 0;
//		for (int i = 0; i < jsonString.length(); i++) {
//			char jsonChar = jsonString.charAt(i);
//			if (openBraketTest.test(jsonChar)) openBrakets++;
//			if (closeBraketTest.test(jsonChar)) openBrakets--;
//			if ()
//			
//		}
		
		CompoundTag compound = new CompoundTag();
		
		Deque<CompoundTag> readTag = Queues.newArrayDeque();
		Deque<CompoundTag> writeTag = Queues.newArrayDeque();
		
		writeTag.offerFirst(compound);
		readTag.offerLast(compoundTag);
		
		compoundTag.getAllKeys().forEach((key) -> { // TODO Correct itteration
			Tag tag = compoundTag.get(key);
			if (tag instanceof CompoundTag readComp) {
				readTag.offerLast(readComp);
				CompoundTag writeComp = new CompoundTag();
				writeTag.peek().put(key, writeComp);
				writeTag.offerFirst(writeComp);
				continue;
			} else {
				writeTag.peek().put(key, readTag.peek().get(key));
			}
		});
		
		System.out.println(compoundTag);
		System.out.println(tag);
		
		// TODO
		return new ArrayList<>();
		
	}
	
}
 