package ftgumod.technology;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import ftgumod.FTGU;
import ftgumod.api.FTGUAPI;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.ITechnologyManager;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.api.technology.unlock.UnlockCompound;
import ftgumod.api.technology.unlock.UnlockRecipe;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.server.RecipeBookServerImpl;
import ftgumod.util.StackUtils;
import ftgumod.util.SubCollection;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBookServer;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.crafting.CraftingHelper;
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

	public static ICommandSender player;

	static {
		FTGUAPI.technologyManager = INSTANCE;
	}

	private final Map<UUID, Map<Technology, TechnologyProgress>> progress = new HashMap<>();

	private final Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();
	private final Collection<Technology> roots = new SubCollection<>(technologies.values(), Technology::isRoot);
	private final Collection<Technology> start = new SubCollection<>(technologies.values(),
			Technology::researchedAtStart);

	private final Map<ResourceLocation, IUnlock.Factory<?>> unlocks = new HashMap<>();
	private final Map<ResourceLocation, IResearchRecipe.Factory<?>> puzzles = new HashMap<>();

	private final List<Predicate<? super ITechnology>> removeCallback = new LinkedList<>();
	private final List<Consumer<? super ITechnology>> addCallback = new LinkedList<>();
	private final List<Runnable> createCallback = new LinkedList<>();

	public Map<String, Pair<String, Map<ResourceLocation, String>>> cache;

	private Map<JsonContextPublic, Map<ResourceLocation, String>> loadBuiltin() {
		Map<JsonContextPublic, Map<ResourceLocation, String>> json = new HashMap<>();

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
						error("Couldn't read _constants.json from {}", mod.getModId(), e);
						return false;
					} finally {
						IOUtils.closeQuietly(reader);
					}
				}
				return true;
			}, (root, file) -> {
				String relative = root.relativize(file).toString();
				if (!"json".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_")
						|| !relative.contains("/"))
					return true;

				String name = FilenameUtils.removeExtension(relative);
				ResourceLocation id = new ResourceLocation(mod.getModId(), name);

				try {
					map.put(id, new String(Files.readAllBytes(file)));
				} catch (IOException | JsonParseException e) {
					error("Couldn't read technology {} from {}", id, file, e);
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

	public IUnlock getUnlock(JsonElement element, JsonContextPublic context, ResourceLocation tech) {
		if (element.isJsonArray()) {
			NonNullList<IUnlock> unlocks = NonNullList.create();
			element.getAsJsonArray().forEach(json -> unlocks.add(getUnlock(json, context, tech)));
			return new UnlockCompound(unlocks);
		} else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("type")) {
				ResourceLocation type = new ResourceLocation(context.appendModId(JsonUtils.getString(object, "type")));
				if (unlocks.containsKey(type))
					return unlocks.get(type).deserialize(object, context, tech);
			}
		}
		return new UnlockRecipe(StackUtils.INSTANCE.getItemPredicate(element, context));
	}

	public IResearchRecipe getPuzzle(JsonElement element, JsonContextPublic context, ResourceLocation technology) {
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("type")) {
				ResourceLocation type = new ResourceLocation(context.appendModId(JsonUtils.getString(object, "type")));
				if (puzzles.containsKey(type))
					return puzzles.get(type).deserialize(object, context, technology);
				throw new JsonSyntaxException("Unknown puzzle type " + type);
			} else
				throw new JsonSyntaxException("IPuzzle missing required field 'type'");
		} else
			throw new JsonSyntaxException("Expected puzzle to be an object");
	}

	public Collection<Technology> getRoots() {
		return roots;
	}

	public Collection<Technology> getStart() {
		return start;
	}

	@Override
	public boolean isLocked(ItemStack stack, @Nullable EntityPlayer player) {
		boolean tech = false;
		if (!stack.isEmpty()) {
			for (Technology t : technologies.values()) {
				for (IUnlock unlock : t.getUnlock()) {
					if (unlock.unlocks(stack)) {
						if (player == null)
							return true;
						if (t.isResearched(player))
							return false;
						tech = true;
						break;
					}
				}
			}
		}
		return tech;
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

	public TechnologyProgress getProgress(EntityPlayer player, Technology technology) {
		return progress.computeIfAbsent(player.getUniqueID(), uuid -> new HashMap<>()).computeIfAbsent(technology,
				tech -> {
					TechnologyProgress progress = new TechnologyProgress();

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

	public void reload(File data) {
		clear();

		cache = new HashMap<>();
		load(new File(FTGU.folder, "technologies"));
		load(new File(data, "technologies"));

		load();
	}

	private void load(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			for (File child : dir.listFiles(File::isDirectory)) {
				File constants = new File(child, "_constants.json");

				String context = null;
				if (constants.exists() && constants.isFile()) {
					try {
						context = new String(Files.readAllBytes(constants.toPath()));
					} catch (IOException e) {
						error("Couldn't read _constants.json from {}", child.getName(), e);
					}
				}

				Map<ResourceLocation, String> techs = new HashMap<>();
				for (File file : FileUtils.listFiles(child, new String[] { "json" }, true)) {
					if (file.getParentFile().equals(child))
						continue;
					ResourceLocation id = new ResourceLocation(child.getName(), FilenameUtils
							.removeExtension(child.toPath().relativize(file.toPath()).toString().replace('\\', '/')));

					try {
						techs.put(id, new String(Files.readAllBytes(file.toPath())));
					} catch (IOException e) {
						error("Couldn't read technology {} from {}", id, file, e);
					}
				}

				if (cache.containsKey(child.getName())) {
					cache.get(child.getName()).getRight().forEach(techs::putIfAbsent);
					if (context == null)
						context = cache.get(child.getName()).getLeft();
				}
				cache.put(child.getName(), Pair.of(context == null ? "[]" : context, techs));
			}
		} else
			dir.mkdirs();
	}

	public void removeFromCache(ResourceLocation tech) {
		if (cache.containsKey(tech.getNamespace())) {
			Map<ResourceLocation, String> map = cache.get(tech.getNamespace()).getRight();
			map.remove(tech);
			if (map.isEmpty())
				cache.remove(tech.getNamespace());
		}
	}

	public void load() {
		Map<JsonContextPublic, Map<ResourceLocation, String>> json = cache.entrySet().stream()
				.collect(Collectors.toMap(entry -> {
					JsonContextPublic context = new JsonContextPublic(entry.getKey());
					try {
						JsonObject[] array = FTGU.GSON.fromJson(entry.getValue().getLeft(), JsonObject[].class);
						context.loadConstants(array);
					} catch (JsonParseException e) {
						error("Couldn't read _constants.json from {}", context.getModId(), e);
					}
					return context;
				}, entry -> entry.getValue().getRight()));

		if (!FTGU.custom) {
			loadBuiltin().forEach((context, map) -> {
				if (!json.containsKey(context))
					json.put(context, map);
				else
					map.forEach(json.get(context)::putIfAbsent);
			});
		}

		Map<JsonContextPublic, Map<ResourceLocation, Technology.Builder>> builders = new HashMap<>();
		Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();

		for (Map.Entry<JsonContextPublic, Map<ResourceLocation, String>> domain : json.entrySet()) {
			Map<ResourceLocation, Technology.Builder> map = new HashMap<>();
			for (Map.Entry<ResourceLocation, String> file : domain.getValue().entrySet()) {
				try {
					map.put(file.getKey(), FTGU.GSON.fromJson(file.getValue(), Technology.Builder.class));
				} catch (JsonParseException e) {
					removeFromCache(file.getKey());
					error("Couldn't load technology " + file.getKey(), e);
				}
			}
			builders.put(domain.getKey(), map);
		}

		boolean load = true;
		while (!builders.isEmpty() && load) {
			load = false;

			for (Map.Entry<JsonContextPublic, Map<ResourceLocation, Technology.Builder>> domain : builders.entrySet()) {
				Iterator<Map.Entry<ResourceLocation, Technology.Builder>> iterator = domain.getValue().entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Map.Entry<ResourceLocation, Technology.Builder> entry = iterator.next();

					if (entry.getValue().resolveParent(technologies)) {
						try {
							Technology technology = entry.getValue().build(entry.getKey(), domain.getKey());
							technologies.put(technology.getRegistryName(), technology);
							load = true;
						} catch (JsonParseException e) {
							removeFromCache(entry.getKey());
							error("Couldn't load technology " + entry.getKey(), e);
						}

						iterator.remove();
					}
				}
			}

			if (!load) {
				for (Map.Entry<JsonContextPublic, Map<ResourceLocation, Technology.Builder>> domain : builders
						.entrySet()) {
					for (Map.Entry<ResourceLocation, Technology.Builder> entry : domain.getValue().entrySet()) {
						removeFromCache(entry.getKey());
						error("Couldn't load technology " + entry.getKey());
					}
				}
			}
		}

		registerAll(technologies.values().toArray(new Technology[technologies.size()]));

		int size = this.technologies.size();
		info("Loaded " + size + " technolog" + (size != 1 ? "ies" : "y"));
	}

	private static void printToPlayer(String string) {
		if (player != null) {
			player.sendMessage(new TextComponentString(string)
					.setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true)));
		}
	}

	private static void error(String string, Object p1, Object p2, Exception e) {
		Technology.getLogger().error(string, p1, p2, e);
		printToPlayer(String.format(string, p1, p2) + "\n " + e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	private static void error(String string, Object p1, Exception e) {
		Technology.getLogger().error(string, p1, e);
		printToPlayer(String.format(string, p1) + "\n " + e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	private static void error(String string, Exception e) {
		Technology.getLogger().error(string, e);
		printToPlayer(string + "\n " + e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	private static void error(String string) {
		Technology.getLogger().error(string);
		printToPlayer(string);
	}

	private static void info(String string) {
		Technology.getLogger().info(string);
		printToPlayer(string);
	}

	@Override
	public void register(ITechnology value) {
		if (value instanceof Technology) {
			if (_register((Technology) value))
				addCallback.forEach(action -> action.accept(value));
		} else
			throw new IllegalArgumentException("Technology instance is of unexpected class");
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

	@Override
	public boolean contains(ITechnology value) {
		return technologies.containsValue(value);
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
	public void registerPuzzle(ResourceLocation name, IResearchRecipe.Factory<?> factory) {
		puzzles.put(name, factory);
	}

	@Override
	public Iterator<Technology> iterator() {
		return technologies.values().iterator();
	}

	public enum GUI {
		IDEATABLE, RESEARCHTABLE
	}

}
