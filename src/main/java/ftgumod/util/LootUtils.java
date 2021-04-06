package ftgumod.util;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class LootUtils {

	private static final Field field = ReflectionHelper.findField(LootTable.class, "pools", "field_186466_c");

	@SuppressWarnings("unchecked")
	public static void addLootPools(LootTableManager manager, LootTable table, ResourceLocation pools) {
		LootTable extra = manager.getLootTableFromLocation(pools);
		try {
			for (LootPool pool : (List<LootPool>) field.get(extra))
				((List<LootPool>) field.get(table)).add(pool);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
