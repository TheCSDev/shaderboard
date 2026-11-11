package com.thecsdev.shaderboard.world.level.saveddata;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.thecsdev.shaderboard.Shaderboard.MOD_ID;

/**
 * Holds the names of scoreboard {@link Objective}s selected for background
 * synchronization in a given world/level.
 */
public final @ApiStatus.Internal class ScoreboardSyncData extends SavedData
{
	// ==================================================
	/**
	 * {@link Codec} implementation for serializing {@link ScoreboardSyncData}.
	 * Stores the selected {@link Objective} names under the {@code objectives} field.
	 */
	public static final Codec<ScoreboardSyncData> CODEC = Codec.STRING.listOf()
			.fieldOf("objectives").codec()
			.xmap(ScoreboardSyncData::new, data -> data.getObjectives().stream().sorted().toList());
	// --------------------------------------------------
	/**
	 * {@link SavedDataType} describing this data's storage identity, factory,
	 * and {@link Codec}.
	 */
	public static final SavedDataType<ScoreboardSyncData> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(MOD_ID, "scoreboard_sync"),
			ScoreboardSyncData::new,
			CODEC,
			DataFixTypes.SAVED_DATA_SCOREBOARD);
	// ==================================================
	private final @NotNull Set<String> objectives = new HashSet<>();
	// ==================================================
	public ScoreboardSyncData() {}
	// --------------------------------------------------
	/**
	 * Creates a {@link ScoreboardSyncData} instance and adds all provided
	 * {@link Objective} names from the {@link Collection}.
	 * @param objectives The names of {@link Objective}s to select.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @see #addObjective(String)
	 */
	private ScoreboardSyncData(@NotNull Collection<String> objectives) throws NullPointerException {
		this.objectives.addAll(Objects.requireNonNull(objectives));
	}
	// ==================================================
	/**
	 * Returns a read-only view of the names of scoreboard {@link Objective}s
	 * selected for background synchronization.
	 * @apiNote This is a live view of a {@link Collection}, not an independent copy.
	 */
	public final @NotNull Collection<String> getObjectives() {
		return Collections.unmodifiableSet(this.objectives);
	}
	// ==================================================
	/**
	 * Adds a scoreboard {@link Objective}'s name to the synchronization selection.
	 * @param name The name of the {@link Objective} to select.
	 * @return {@code true} if the name was not in the collection of added names.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean addObjective(@NotNull String name) throws NullPointerException
	{
		//add the name
		if(!this.objectives.add(Objects.requireNonNull(name)))
			return false; //ignore if already added

		//mark as dirty and return
		setDirty();
		return true;
	}

	/**
	 * Removes a scoreboard {@link Objective}'s name from the synchronization selection.
	 * @param name The name of the {@link Objective} to deselect.
	 * @return {@code true} if the name was in the collection of added names.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean removeObjective(@NotNull String name) throws NullPointerException
	{
		//remove the name
		if(!this.objectives.remove(Objects.requireNonNull(name)))
			return false; //ignore if already not present

		//mark as dirty and return
		setDirty();
		return true;
	}
	// ==================================================
	/**
	 * Returns the {@link ScoreboardSyncData} associated with the given
	 * {@link MinecraftServer}'s world.
	 * @param server The {@link MinecraftServer} whose saved data to obtain.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @apiNote Call this on the server thread.
	 */
	public static final @NotNull ScoreboardSyncData of(@NotNull MinecraftServer server) throws NullPointerException {
		return Objects.requireNonNull(server).getDataStorage().computeIfAbsent(TYPE);
	}
	// ==================================================
}
