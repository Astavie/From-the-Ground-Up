package ftgumod.api.util.predicate;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Streams;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.oredict.OreDictionary;

public class ItemMod extends ItemPredicate {

    private static final Map<String, ItemMod> map = new HashMap<>();

    private final String modid;

    public ItemMod(String modid) {
        super(Streams.stream(Item.REGISTRY).filter(i -> i.getRegistryName().getNamespace().equals(modid))
                .map(i -> new ItemStack(i, 1, OreDictionary.WILDCARD_VALUE)).toArray(ItemStack[]::new));
        this.modid = modid;
    }

    @Override
    public boolean apply(ItemStack item) {
        return item.getItem().getRegistryName().getNamespace().equals(modid);
    }

    public static class Factory implements ItemPredicate.Factory {

        @Override
        public ItemPredicate apply(JsonObject json) {
            String modid = JsonUtils.getString(json, "modid");
            ItemMod item = map.get(modid);
            if (item == null) {
                item = new ItemMod(modid);
                map.put(modid, item);
            }
            return item;
        }

    }

}
