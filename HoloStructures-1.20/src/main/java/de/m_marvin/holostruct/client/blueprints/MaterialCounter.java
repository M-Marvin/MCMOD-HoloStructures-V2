package de.m_marvin.holostruct.client.blueprints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.m_marvin.blueprints.api.IBlueprintAcessor;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class MaterialCounter {
	
	public static List<ItemStack> countMaterials(IBlueprintAcessor structure) {
		
		// TODO implement item drops and entitiy items
		Map<Item, Integer> map = new HashMap<>();
		
		Vec3i min = structure.getBoundsMin();
		Vec3i max = structure.getBoundsMax();
		
		for (int x = min.x; x < max.x; x++) {
			for (int y = min.y; y < max.y; y++) {
				for (int z = min.z; z < max.z; z++) {
					Vec3i pos = new Vec3i(x, y, z);
					
					if (structure.getBlock(pos) == null || structure.getBlock(pos).isAir()) continue;
					
					BlockState state = TypeConverter.data2blockState(structure.getBlock(pos));
					Item item = state.getBlock().asItem();
					
					if (item == Items.AIR) continue;
					
					map.put(item, map.getOrDefault(item, 0) + 1);
				}
			}
		}
		
		return map.entrySet().stream().map(e -> new ItemStack(e.getKey(), e.getValue())).toList();
		
	}
	
}
