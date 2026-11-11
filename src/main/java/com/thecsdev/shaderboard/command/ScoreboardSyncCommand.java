package com.thecsdev.shaderboard.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.thecsdev.shaderboard.resource.SLanguage;
import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ScoreboardCommand;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minecraft.commands.Commands.literal;

/**
 * Implementation of the {@code /scoreboard objectives shaderboard:sync} command.
 */
public final @ApiStatus.Internal class ScoreboardSyncCommand
{
	// ==================================================
	private ScoreboardSyncCommand() {}
	// ==================================================
	/**
	 * Adds the command branch to {@link ScoreboardCommand}.
	 * @param dispatcher The {@link CommandDispatcher} to extend.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalStateException If vanilla's scoreboard objectives branch is missing.
	 */
	public static final void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher)
			throws NullPointerException, IllegalStateException
	{
		//obtain the vanilla branch
		Objects.requireNonNull(dispatcher);
		final var scoreboard = dispatcher.getRoot().getChild("scoreboard");
		final var objectives = (scoreboard != null) ? scoreboard.getChild("objectives") : null;
		if(objectives == null)
			throw new IllegalStateException("Missing '/scoreboard objectives' command branch");

		//add this mod's branch
		objectives.addChild(literal("shaderboard:sync")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(literal("list").executes(ScoreboardSyncCommand::execute_list))
				.build());
	}
	// ==================================================
	/**
	 * Executes {@code /scoreboard objectives shaderboard:sync list}.
	 * @param context The {@link CommandContext}.
	 */
	private static int execute_list(@NotNull CommandContext<CommandSourceStack> context)
	{
		//obtain the selected names in display order
		final var source = context.getSource();
		final var names  = ScoreboardSyncData.of(source.getServer()).
				getObjectives().stream().sorted().toList();

		//send feedback
		if(names.isEmpty()) {
			source.sendSuccess(() -> Component.literal("")
					.append(SLanguage.CHAT_PREFIX)
					.append(" No objectives are currently manually synced by this mod."),
				false);
		} else {
			source.sendSuccess(() -> Component.literal("")
					.append(SLanguage.CHAT_PREFIX)
					.append(" Manually syncing " + names.size() + " objectives: ")
					.append(Component.literal(String.join(", ", names)).withStyle(ChatFormatting.GRAY)),
				false);
		}

		//return output: the number of selected objectives
		return names.size();
	}
	// ==================================================
}
