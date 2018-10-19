package ftgumod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.Content;
import ftgumod.api.FTGUAPI;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.IStackUtils;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StackUtils implements IStackUtils {

	public static final StackUtils INSTANCE = new StackUtils();

	static {
		FTGUAPI.stackUtils = INSTANCE;
	}

	public NBTTagCompound getItemData(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		return nbt;
	}

	@Override
	public boolean isStackOf(ItemStack ingredient, ItemStack stack) {
		return ingredient.getItem() == stack.getItem() && (ingredient.getItemDamage() == OreDictionary.WILDCARD_VALUE || ingredient.getItemDamage() == stack.getItemDamage()) && (!ingredient.hasTagCompound() || ItemStack.areItemStackTagsEqual(ingredient, stack));
	}

	@Override
	public ItemPredicate getItemPredicate(JsonElement element, JsonContextPublic context) {
		Set<ItemPredicate> predicates = new HashSet<>();

		if (element.isJsonPrimitive()) {
			String item = element.getAsString();
			if (item.startsWith("#")) {
				ItemPredicate constant = context.getPredicate(item.substring(1));
				if (constant == null)
					throw new JsonSyntaxException("Predicate referenced invalid constant: " + item);
				return constant;
			}
			JsonObject object = new JsonObject();
			object.add("item", element);
			return ItemPredicate.deserialize(object);
		}
		if (element.isJsonArray())
			for (JsonElement json : element.getAsJsonArray())
				predicates.add(getItemPredicate(json, context));
		else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (!object.has("type") && object.has("item")) {
				String item = JsonUtils.getString(object, "item");
				if (item.startsWith("#")) {
					ItemPredicate constant = context.getPredicate(item.substring(1));
					if (constant == null)
						throw new JsonSyntaxException("Predicate referenced invalid constant: " + item);
					return constant;
				}
			}
			return ItemPredicate.deserialize(object);
		} else throw new JsonSyntaxException("Expected predicate to be an object or an array of objects");

		return new ItemPredicate() {


			@Override
			public boolean test(ItemStack item) {
				return predicates.stream().anyMatch(p -> p.test(item));
			}

		};
	}

	@Override
	public ItemStack getParchment(ITechnology tech, Parchment type) {
		ItemStack stack = new ItemStack(type == Parchment.IDEA ? Content.i_parchmentIdea : Content.i_parchmentResearch);
		getItemData(stack).setString("FTGU", tech.getRegistryName().toString());
		return stack;
	}

	@Nullable
	@Override
	public Technology getTechnology(ItemStack parchment) {
		return TechnologyManager.INSTANCE.getTechnology(new ResourceLocation(getItemData(parchment).getString("FTGU")));
	}

	@Override
	public List<BlockSerializable> getInspected(ItemStack inspector) {
		return ItemMagnifyingGlass.getInspected(inspector);
	}

}
