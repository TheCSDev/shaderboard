package com.thecsdev.shaderboard.world.scores;

import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Manages background synchronization of selected scoreboard {@link Objective}s.
 * @see ScoreboardSyncData
 */
public final @ApiStatus.Internal class ScoreboardSyncManager
{
	// ==================================================
	private ScoreboardSyncManager() {}
	// ==================================================
	/**
	 * Selects an {@link Objective} for background synchronization.
	 * @param server The {@link MinecraftServer} that owns the {@link Objective}.
	 * @param objective The {@link Objective} to synchronize.
	 * @return {@code true} if the {@link Objective} was not already selected.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @apiNote Call this on the server thread.
	 */
	public static final boolean addObjective(@NotNull MinecraftServer server, @NotNull Objective objective)
			throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(server);
		Objects.requireNonNull(objective);

		//add the objective to the saved selection
		if(!ScoreboardSyncData.of(server).addObjective(objective.getName()))
			return false; //ignore if already selected

		//start tracking only if vanilla is not already synchronizing via a display slot
		//(objectives with a display slot are already synced by the vanilla game)
		final var scoreboard = server.getScoreboard();
		if(scoreboard.getObjectiveDisplaySlotCount(objective) == 0)
			scoreboard.startTrackingObjective(objective);
		return true;
	}
	// --------------------------------------------------
	/**
	 * Deselects an {@link Objective} from background synchronization.
	 * @param server The {@link MinecraftServer} whose selection to update.
	 * @param name The name of the {@link Objective} to deselect.
	 * @return {@code true} if the {@link Objective} was in the saved selection.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @apiNote Call this on the server thread.
	 */
	public static final boolean removeObjective(@NotNull MinecraftServer server, @NotNull String name)
			throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(server);
		Objects.requireNonNull(name);

		//remove the objective from the saved selection
		if(!ScoreboardSyncData.of(server).removeObjective(name))
			return false; //ignore if not selected

		//stop tracking only if vanilla is not still synchronizing the objective
		//(objectives with a display slot are still synced by the vanilla game)
		final var scoreboard = server.getScoreboard();
		final var objective  = scoreboard.getObjective(name);
		if(objective != null && scoreboard.getObjectiveDisplaySlotCount(objective) == 0)
			scoreboard.stopTrackingObjective(objective);
		return true;
	}
	// ==================================================
}
