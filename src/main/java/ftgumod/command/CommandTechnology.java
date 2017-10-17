package ftgumod.command;

import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyHandler;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CommandTechnology extends CommandBase {

	public static Technology findTechnology(String id) throws CommandException {
		Technology tech = TechnologyHandler.technologies.get(new ResourceLocation(id));
		if (tech == null)
			throw new CommandException("commands.technology.technologyNotFound", id);
		return tech;
	}

	@Override
	public String getName() {
		return "technology";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.technology.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0)
			throw new WrongUsageException(getUsage(sender));
		else {
			ActionType type = ActionType.byName(args[0]);
			if (type != null) {
				if (args.length < 3)
					throw type.wrongUsage();

				EntityPlayerMP player = getPlayer(server, sender, args[1]);
				Mode mode = Mode.byName(args[2]);

				if (mode == null)
					throw type.wrongUsage();

				perform(sender, args, player, type, mode);
				PacketDispatcher.sendTo(new TechnologyMessage(player, true), player);
			} else if (!args[0].equals("test"))
				throw new WrongUsageException(getUsage(sender));
			else if (args.length == 3)
				testTechnology(sender, getPlayer(server, sender, args[1]), findTechnology(args[2]));
			else if (args.length == 4)
				testCriterion(sender, getPlayer(server, sender, args[1]), findTechnology(args[2]), args[3]);
			else
				throw new WrongUsageException("commands.technology.test.usage");
		}
	}

	private void perform(ICommandSender sender, String[] args, EntityPlayer player, ActionType type, Mode mode) throws CommandException {
		if (mode == Mode.EVERYTHING)
			if (args.length == 3) {
				int affected = type.perform(player, TechnologyHandler.technologies.values());
				if (affected == 0)
					throw mode.fail(type, player.getName());
				mode.success(sender, this, type, player.getName(), affected);
			} else throw mode.usage(type);
		else if (args.length < 4)
			throw mode.usage(type);
		else {
			Technology tech = findTechnology(args[3]);

			if (mode == Mode.ONLY && args.length == 5) {
				if (!tech.getCriteria().keySet().contains(args[4]))
					throw new CommandException("commands.technology.criterionNotFound", tech.getRegistryName(), args[4]);
				if (!type.performCriterion(player, tech, args[4]))
					throw new CommandException(type.translation + ".criterion.failed", tech.getRegistryName(), player.getName(), args[4]);

				notifyCommandListener(sender, this, type.translation + ".criterion.success", tech.getRegistryName(), player.getName(), args[4]);
			} else {
				if (args.length != 4)
					throw mode.usage(type);

				List<Technology> list = getTechnologies(tech, mode);
				int affected = type.perform(player, list);
				if (affected == 0)
					throw mode.fail(type, tech.getRegistryName(), player.getName());
				mode.success(sender, this, type, tech.getRegistryName(), player.getName(), affected);
			}
		}
	}

	private void testCriterion(ICommandSender sender, EntityPlayer player, Technology tech, String criterion) throws CommandException {
		if (!tech.hasCustomUnlock())
			throw new CommandException("commands.technology.criterionNotFound", tech.getRegistryName(), criterion);

		CriterionProgress progress = TechnologyHandler.getProgress(player, tech).getCriterionProgress(criterion);
		if (progress == null)
			throw new CommandException("commands.technology.criterionNotFound", tech.getRegistryName(), criterion);
		if (!progress.isObtained())
			throw new CommandException("commands.technology.test.criterion.notDone", player.getName(), tech.getRegistryName(), criterion);
		notifyCommandListener(sender, this, "commands.technology.test.criterion.success", player.getName(), tech.getRegistryName(), criterion);
	}

	private void testTechnology(ICommandSender sender, EntityPlayer player, Technology tech) throws CommandException {
		if (!tech.isResearched(player))
			throw new CommandException("commands.technology.test.technology.notDone", player.getName(), tech.getRegistryName());
		notifyCommandListener(sender, this, "commands.technology.test.technology.success", player.getName(), tech.getRegistryName());
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length == 1)
			return getListOfStringsMatchingLastWord(args, "grant", "revoke", "test");
		else {
			ActionType type = ActionType.byName(args[0]);
			if (type != null) {
				if (args.length == 2)
					return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				if (args.length == 3)
					return getListOfStringsMatchingLastWord(args, "only", "through", "from", "until", "everything");

				Mode mode = Mode.byName(args[2]);
				if (mode != null && mode != Mode.EVERYTHING) {
					if (args.length == 4)
						return getListOfStringsMatchingLastWord(args, TechnologyHandler.technologies.keySet());
					if (args.length == 5 && mode == Mode.ONLY) {
						Technology tech = TechnologyHandler.technologies.get(new ResourceLocation(args[3]));
						if (tech != null)
							return getListOfStringsMatchingLastWord(args, tech.getCriteria().keySet());
					}
				}
			}

			if (args[0].equals("test")) {
				if (args.length == 2)
					return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				if (args.length == 3)
					return getListOfStringsMatchingLastWord(args, TechnologyHandler.technologies.keySet());
				if (args.length == 4) {
					Technology tech = TechnologyHandler.technologies.get(new ResourceLocation(args[2]));
					if (tech != null)
						return getListOfStringsMatchingLastWord(args, tech.getCriteria().keySet());
				}
			}

			return Collections.emptyList();
		}
	}

	private List<Technology> getTechnologies(Technology tech, Mode mode) {
		List<Technology> list = new LinkedList<>();
		if (mode.parents)
			for (Technology parent = tech.getParent(); parent != null; parent = parent.getParent())
				list.add(parent);

		if (mode.children)
			tech.getChildren(list, false);
		else
			list.add(tech);

		return list;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return args.length > 1 && ("grant".equals(args[0]) || "revoke".equals(args[0]) || "test".equals(args[0])) && index == 1;
	}

	private enum ActionType {
		GRANT("grant") {
			@Override
			protected boolean perform(EntityPlayer player, Technology tech) {
				if (tech.isResearched(player))
					return false;

				tech.setResearched(player);
				tech.announceResearched(player);
				return true;
			}

			@Override
			protected boolean performCriterion(EntityPlayer player, Technology tech, String criterion) {
				return tech.grantCriterion(player, criterion);
			}
		},
		REVOKE("revoke") {
			@Override
			protected boolean perform(EntityPlayer player, Technology tech) {
				if (!tech.hasProgress(player))
					return false;

				tech.removeResearched(player);
				return true;
			}

			@Override
			protected boolean performCriterion(EntityPlayer player, Technology tech, String criterion) {
				return tech.revokeCriterion(player, criterion);
			}
		};

		private final String name, translation;

		ActionType(String name) {
			this.name = name;
			this.translation = "commands.technology." + name;
		}

		private static ActionType byName(String name) {
			for (ActionType type : values())
				if (type.name.equals(name))
					return type;
			return null;
		}

		private CommandException wrongUsage() {
			return new CommandException(translation + ".usage");
		}

		private int perform(EntityPlayer player, Iterable<Technology> techs) {
			int i = 0;

			for (Technology t : techs)
				if (perform(player, t))
					i++;

			return i;
		}

		protected abstract boolean perform(EntityPlayer player, Technology tech);

		protected abstract boolean performCriterion(EntityPlayer player, Technology tech, String criterion);

	}

	private enum Mode {
		ONLY("only", false, false),
		THROUGH("through", true, true),
		FROM("from", false, true),
		UNTIL("until", true, false),
		EVERYTHING("everything", true, true);

		private final String name;
		private final boolean parents;
		private final boolean children;

		Mode(String name, boolean parents, boolean children) {
			this.name = name;
			this.parents = parents;
			this.children = children;
		}

		private static Mode byName(String name) {
			for (Mode mode : values())
				if (mode.name.equals(name))
					return mode;
			return null;
		}

		private CommandException fail(ActionType type, Object... args) {
			return new CommandException(type.translation + "." + this.name + ".failed", args);
		}

		private CommandException usage(ActionType type) {
			return new CommandException(type.translation + "." + this.name + ".usage");
		}

		private void success(ICommandSender sender, ICommand command, ActionType type, Object... args) {
			notifyCommandListener(sender, command, type.translation + "." + this.name + ".success", args);
		}

	}

}
