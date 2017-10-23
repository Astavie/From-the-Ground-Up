package ftgumod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.Content;
import ftgumod.api.FTGUAPI;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.IStackUtils;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StackUtils implements IStackUtils<Technology> {

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

	public boolean isStackOf(ItemStack ingredient, ItemStack stack) {
		return ingredient.getItem() == stack.getItem() && (ingredient.getMetadata() == OreDictionary.WILDCARD_VALUE || ingredient.getMetadata() == stack.getMetadata()) && (!ingredient.hasTagCompound() || ItemStack.areItemStackTagsEqual(ingredient, stack));
	}

	public Set<ItemPredicate> getItemPredicate(JsonElement element, JsonContext context) {
		Set<ItemPredicate> predicates = new HashSet<>();

		if (element.isJsonArray())
			for (JsonElement json : element.getAsJsonArray())
				predicates.addAll(getItemPredicate(json, context));
		else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (!object.has("type") && object.has("item")) {
				String item = JsonUtils.getString(object, "item");
				if (item.startsWith("#")) {
					Ingredient constant = context.getConstant(item.substring(1));
					if (constant == null)
						throw new JsonSyntaxException("Predicate referenced invalid constant: " + item);
					return Collections.singleton(new ItemPredicate() {

						@Override
						public boolean test(ItemStack item) {
							return constant.test(item);
						}

					});
				}
			}
			return Collections.singleton(ItemPredicate.deserialize(object));
		} else throw new JsonSyntaxException("Expected predicate to be an object or array of objects");

		return predicates;
	}

	@Override
	public ItemStack getParchment(ResourceLocation tech, Parchment type) {
		ItemStack stack = new ItemStack(type == Parchment.IDEA ? Content.i_parchmentIdea : Content.i_parchmentResearch);
		getItemData(stack).setString("FTGU", tech.toString());
		return stack;
	}

	@Nullable
	@Override
	public Technology getTechnology(ItemStack parchment) {
		return TechnologyManager.INSTANCE.technologies.get(new ResourceLocation(getItemData(parchment).getString("FTGU")));
	}

	@Override
	public List<BlockSerializable> getInspected(ItemStack inspector) {
		return ItemMagnifyingGlass.getInspected(inspector);
	}

}
