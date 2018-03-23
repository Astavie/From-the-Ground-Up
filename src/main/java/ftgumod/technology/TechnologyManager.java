package ftgumod.technology;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import ftgumod.FTGU;
import ftgumod.api.FTGUAPI;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.ITechnologyManager;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.api.technology.unlock.UnlockCompound;
import ftgumod.api.technology.unlock.UnlockRecipe;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.server.RecipeBookServerImpl;
import ftgumod.util.JsonContextPublic;
import ftgumod.util.SubCollection;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TechnologyManager implements ITechnologyManager, Iterable<Technology> {

	public static final TechnologyManager INSTANCE = new TechnologyManager();

	static {
		FTGUAPI.technologyManager = INSTANCE;
	}

	private final Map<UUID, Map<Technology, AdvancementProgress>> progress = new HashMap<>();

	private final Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();
	private final Collection<Technology> roots = new SubCollection<>(technologies.values(), Technology::isRoot);
	private final Collection<Technology> start = new SubCollection<>(technologies.values(), Technology::researchedAtStart);

	private final Map<ResourceLocation, IUnlock.Factory<?>> unlocks = new HashMap<>();

	private final List<Predicate<? super ITechnology>> removeCallback = new LinkedList<>();
	private final List<Consumer<? super ITechnology>> addCallback = new LinkedList<>();
	private final List<Runnable> createCallback = new LinkedList<>();

	public Map<String, Pair<String, Map<ResourceLocation, String>>> cache;

	private Map<JsonContext, Map<ResourceLocation, String>> loadBuiltin() {
		Map<JsonContext, Map<ResourceLocation, String>> json = new HashMap<>();

		Loader.instance().getActiveModList().forEach(mod -> {
			JsonContextPublic context = new JsonContextPublic(mod.getModId());

			Map<ResourceLocation, String> map = new HashMap<>();
			CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/technologies", root -> {
				Path path = root.resolve("_constants.json");
				if (path != null && Files.exists(path)) {
					BufferedReader reader = null;
					try {
						reader = Files.newBufferedReader(path);
						JsonObject[] array = JsonUtils.fromJson(FTGU.GSON, reader, JsonObject[].class);
						context.loadConstants(array);
					} catch (IOException | JsonParseException e) {
						Technology.getLogger().error("Couldn't read _constants.json from {}", mod.getModId(), e);
						return false;
					} finally {
						IOUtils.closeQuietly(reader);
					}
				}
				return true;
			}, (root, file) -> {
				String relative = root.relativize(file).toString();
				if (!"json".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_") || !relative.contains("/"))
					return true;

				String name = FilenameUtils.removeExtension(relative);
				ResourceLocation id = new ResourceLocation(mod.getModId(), name);

				try {
					map.put(id, new String(Files.readAllBytes(file)));
				} catch (IOException | JsonParseException e) {
					Technology.getLogger().error("Couldn't read technology {} from {}", id, file, e);
					return false;
				}

				return true;
			}, true, true);
			json.put(context, map);
		});
		return json;
	}

	public void unloadProgress(EntityPlayer player) {
		progress.remove(player.getUniqueID());
	}

	public Map<String, Pair<String, Map<ResourceLocation, String>>> getCache() {
		return cache;
	}

	public IUnlock getUnlock(JsonElement element, JsonContext context, ResourceLocation tech) {
		if (element.isJsonArray()) {
			NonNullList<IUnlock> unlocks = NonNullList.create();
			element.getAsJsonArray().forEach(json -> unlocks.add(getUnlock(json, context, tech)));
			return new UnlockCompound(unlocks);
		} else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("type")) {
				ResourceLocation type = new ResourceLocation(JsonUtils.getString(object, "type"));
				if (unlocks.containsKey(type))
					return unlocks.get(type).deserialize(object, context, tech);
			}
			return new UnlockRecipe(CraftingHelper.getIngredient(element, context));
		} else throw new JsonSyntaxException("Expected unlock to be an object or an array of objects");
	}

	public Collection<Technology> getRoots() {
		return roots;
	}

	public Collection<Technology> getStart() {
		return start;
	}

	@Nullable
	@Override
	public Technology getLocked(ItemStack item) {
		if (!item.isEmpty())
			for (Technology t : technologies.values())
				for (IUnlock unlock : t.getUnlock())
					if (unlock.unlocks(item))
						return t;
		return null;
	}

	@Override
	public void removeCallback(Predicate<? super ITechnology> predicate) {
		removeCallback.add(predicate);
	}

	@Override
	public void addCallback(Consumer<? super ITechnology> action) {
		addCallback.add(action);
	}

	@Override
	public void createCallback(Runnable action) {
		createCallback.add(action);
	}

	public AdvancementProgress getProgress(EntityPlayer player, Technology technology) {
		return progress.computeIfAbsent(player.getUniqueID(), uuid -> new HashMap<>()).computeIfAbsent(technology, tech -> {
			AdvancementProgress progress = new AdvancementProgress();
			progress.update(tech.getCriteria(), tech.getRequirements());

			CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
			for (String criterion : progress.getRemaningCriteria())
				if (cap.isResearched(tech.getRegistryName().toString() + "#" + criterion))
					progress.grantCriterion(criterion);

			return progress;
		});
	}

	public void clear() {
		progress.clear();
		technologies.clear();

		createCallback.forEach(Runnable::run);
	}

	public void reload(World world) {
		clear();

		File dir = new File(new File(world.getSaveHandler().getWorldDirectory(), "data"), "technologies");

		cache = new HashMap<>();

		if (dir.exists() && dir.isDirectory()) {
			for (File child : dir.listFiles(File::isDirectory)) {
				File constants = new File(child, "_constants.json");

				String context = "[]";
				if (constants.exists() && constants.isFile())
					try {
						context = new String(Files.readAllBytes(constants.toPath()));
					} catch (IOException e) {
						Technology.getLogger().error("Couldn't read _constants.json from {}", child.getName(), e);
					}

				Map<ResourceLocation, String> techs = new HashMap<>();
				for (File file : FileUtils.listFiles(child, new String[] {"json"}, true)) {
					if (file.getParentFile().equals(child))
						continue;
					ResourceLocation id = new ResourceLocation(child.getName(), FilenameUtils.removeExtension(child.toPath().relativize(file.toPath()).toString().replace('\\', '/')));

					try {
						techs.put(id, new String(Files.readAllBytes(file.toPath())));
					} catch (IOException e) {
						Technology.getLogger().error("Couldn't read technology {} from {}", id, file, e);
					}
				}
				cache.put(child.getName(), Pair.of(context, techs));
			}
		} else
			dir.mkdirs();

		load();
	}

	public void removeFromCache(ResourceLocation tech) {
		if (cache.containsKey(tech.getResourceDomain())) {
			Map<ResourceLocation, String> map = cache.get(tech.getResourceDomain()).getRight();
			map.remove(tech);
			if (map.isEmpty())
				cache.remove(tech.getResourceDomain());
		}
	}

	public void load() {
		Map<JsonContext, Map<ResourceLocation, String>> json = cache.entrySet().stream().collect(Collectors.toMap(entry -> {
			JsonContextPublic context = new JsonContextPublic(entry.getKey());
			try {
				JsonObject[] array = FTGU.GSON.fromJson(entry.getValue().getLeft(), JsonObject[].class);
				context.loadConstants(array);
			} catch (JsonParseException e) {
				Technology.getLogger().error("Couldn't read _constants.json from {}", context.getModId(), e);
			}
			return context;
		}, entry -> entry.getValue().getRight()));

		if (!FTGU.custom)
			loadBuiltin().forEach((context, map) -> {
				if (!json.containsKey(context))
					json.put(context, map);
				else
					map.forEach(json.get(context)::putIfAbsent);
			});

		Map<JsonContext, Map<ResourceLocation, Technology.Builder>> builders = new HashMap<>();
		Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();

		for (Map.Entry<JsonContext, Map<ResourceLocation, String>> domain : json.entrySet()) {
			Map<ResourceLocation, Technology.Builder> map = new HashMap<>();
			for (Map.Entry<ResourceLocation, String> file : domain.getValue().entrySet()) {
				try {
					map.put(file.getKey(), FTGU.GSON.fromJson(file.getValue(), Technology.Builder.class));
				} catch (JsonParseException e) {
					removeFromCache(file.getKey());
					Technology.getLogger().error("Couldn't load technology " + file.getKey(), e);
				}
			}
			builders.put(domain.getKey(), map);
		}

		boolean load = true;
		while (!builders.isEmpty() && load) {
			load = false;

			for (Map.Entry<JsonContext, Map<ResourceLocation, Technology.Builder>> domain : builders.entrySet()) {
				Iterator<Map.Entry<ResourceLocation, Technology.Builder>> iterator = domain.getValue().entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<ResourceLocation, Technology.Builder> entry = iterator.next();

					if (entry.getValue().resolveParent(technologies)) {
						try {
							Technology technology = entry.getValue().build(entry.getKey(), domain.getKey());
							technologies.put(technology.getRegistryName(), technology);
							load = true;
						} catch (JsonParseException e) {
							removeFromCache(entry.getKey());
							Technology.getLogger().error("Couldn't load technology " + entry.getKey(), e);
						}

						iterator.remove();
					}
				}
			}

			if (!load)
				for (Map.Entry<JsonContext, Map<ResourceLocation, Technology.Builder>> domain : builders.entrySet())
					for (Map.Entry<ResourceLocation, Technology.Builder> entry : domain.getValue().entrySet()) {
						removeFromCache(entry.getKey());
						Technology.getLogger().error("Couldn't load technology " + entry.getKey());
					}
		}

		int size = this.technologies.size();
		registerAll(technologies.values().toArray(new Technology[technologies.size()]));

		size = this.technologies.size() - size;
		Technology.getLogger().info("Loaded " + size + " technolog" + (size != 1 ? "ies" : "y"));
	}


	@Override
	public void register(ITechnology value) {
		if (value instanceof Technology) {
			if (_register((Technology) value))
				addCallback.forEach(action -> action.accept(value));
		} else throw new IllegalArgumentException("Technology instance is of unexpected class!");
	}

	private boolean _register(Technology value) {
		if (value == null || value.getRegistryName() == null)
			throw new NullPointerException("Tried to register a technology that is null or has a null registry name");

		for (Predicate<? super ITechnology> predicate : removeCallback)
			if (predicate.test(value))
				return false;

		if (value.hasParent())
			value.getParent().getChildren().add(value);

		technologies.put(value.getRegistryName(), value);
		if (value.start)
			FTGU.PROXY.autoResearch(value);
		return true;
	}

	@Override
	public void registerAll(ITechnology... values) {
		for (ITechnology tech : values)
			register(tech);
	}

	@Override
	public boolean contains(ResourceLocation key) {
		return technologies.containsKey(key);
	}

	@Nullable
	@Override
	public Technology getTechnology(ResourceLocation key) {
		return technologies.get(key);
	}

	@Override
	public TechnologyBuilder createBuilder(ResourceLocation id) {
		return new TechnologyBuilder(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<ITechnology> getTechnologies() {
		return (Collection) technologies.values();
	}

	@Override
	public Set<ResourceLocation> getRegistryNames() {
		return technologies.keySet();
	}

	@Override
	public void sync(EntityPlayerMP player, ITechnology... toasts) {
		PacketDispatcher.sendTo(new TechnologyMessage(player, false, toasts), player);
	}

	@Override
	public void addRecipes(List<IRecipe> recipes, EntityPlayerMP player) {
		RecipeBookServer book = player.recipeBook;
		if (book instanceof RecipeBookServerImpl)
			((RecipeBookServerImpl) book).addRecipes(recipes, player);
		else
			book.add(recipes, player);
	}

	@Override
	public void registerUnlock(ResourceLocation name, IUnlock.Factory<?> factory) {
		unlocks.put(name, factory);
	}

	@Override
	public Iterator<Technology> iterator() {
		return technologies.values().iterator();
	}

	public enum GUI {
		IDEATABLE, RESEARCHTABLE
	}

}
