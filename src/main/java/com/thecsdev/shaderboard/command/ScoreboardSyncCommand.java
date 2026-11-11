package com.thecsdev.shaderboard.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.thecsdev.shaderboard.resource.SLanguage;
import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import com.thecsdev.shaderboard.world.scores.ScoreboardSyncManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ScoreboardCommand;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
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
				.then(literal("add")
						.then(argument("objective", ObjectiveArgument.objective())
								.executes(ScoreboardSyncCommand::execute_add)))
				.then(literal("remove")
						.then(argument("objective", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(
										ScoreboardSyncData.of(context.getSource().getServer()).getObjectives(), builder))
								.executes(ScoreboardSyncCommand::execute_remove)))
				.build());
	}
	// ==================================================
	/**
	 * Executes {@code /scoreboard objectives shaderboard:sync list}.
	 * @param context The {@link CommandContext}.
	 * @return The number of manually synced objectives.
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
					.append(" Manually syncing " + names.size() + " objective(s): ")
					.append(Component.literal(String.join(", ", names)).withStyle(ChatFormatting.GRAY)),
				false);
		}

		//return output: the number of selected objectives
		return names.size();
	}
	// --------------------------------------------------
	/**
	 * Executes {@code /scoreboard objectives shaderboard:sync add}.
	 * @param context The {@link CommandContext}.
	 * @throws CommandSyntaxException If the {@link Objective} does not exist.
	 * @return {@code 0} if already syncing and {@code 1} if syncing began.
	 */
	private static int execute_add(@NotNull CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException
	{
		//obtain arguments
		final var source    = context.getSource();
		final var objective = ObjectiveArgument.getObjective(context, "objective");

		//start synchronizing the objective if not already
		if(!ScoreboardSyncManager.addObjective(source.getServer(), objective)) {
			source.sendFailure(Component.literal("")
					.append(SLanguage.CHAT_PREFIX)
					.append(" Already manually syncing: ")
					.append(objective.getFormattedDisplayName()));
			return 0; //"0" indicates that it's already manually syncing
		}
		else {
			source.sendSuccess(() -> Component.literal("")
					.append(SLanguage.CHAT_PREFIX)
					.append(" Now manually syncing: ")
					.append(objective.getFormattedDisplayName()),
				true);
			return 1; //"1" indicates success
		}
	}
	// --------------------------------------------------
	/**
	 * Executes {@code /scoreboard objectives shaderboard:sync remove}.
	 * @param context The {@link CommandContext}.
	 * @return {@code 0} if already not syncing and {@code 1} if removed from the syncing selection.
	 */
	private static int execute_remove(@NotNull CommandContext<CommandSourceStack> context)
	{
		//obtain arguments
		final var source = context.getSource();
		final var name   = StringArgumentType.getString(context, "objective");

		//stop manually synchronizing the objective if selected
		if(!ScoreboardSyncManager.removeObjective(source.getServer(), name)) {
			source.sendFailure(Component.literal("")
					.append(SLanguage.CHAT_PREFIX)
					.append(" Already not manually syncing: ")
					.append(Component.literal(name).withStyle(ChatFormatting.GRAY)));
			return 0; //"0" indicates that it was not selected
		}
		else {
			source.sendSuccess(() -> Component.literal("")
					.append(SLanguage.CHAT_PREFIX)
					.append(" No longer manually syncing: ")
					.append(Component.literal(name).withStyle(ChatFormatting.GRAY)),
				true);
			return 1; //"1" indicates success
		}
	}
	// ==================================================
}
