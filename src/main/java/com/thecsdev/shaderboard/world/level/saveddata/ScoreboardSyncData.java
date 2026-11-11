package com.thecsdev.shaderboard.world.level.saveddata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FormattingStyle;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.thecsdev.shaderboard.Shaderboard.MOD_ID;

/**
 * Holds the names of scoreboard {@link Objective}s selected for background
 * synchronization in a given world/level.
 */
public final @ApiStatus.Internal class ScoreboardSyncData
{
	// ==================================================
	/**
	 * Current version of this mod's saved-data format.
	 * Unversioned data is version {@code 0}.
	 */
	public static final int FORMAT_VERSION = 1;
	// --------------------------------------------------
	/**
	 * {@link Codec} implementation for serializing {@link ScoreboardSyncData}.
	 * Reads versions {@code 0} and {@code 1}, which share the same objectives format,
	 * and writes the current {@link #FORMAT_VERSION}.
	 */
	public static final Codec<ScoreboardSyncData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, FORMAT_VERSION).optionalFieldOf("version", 0).forGetter(_ -> FORMAT_VERSION),
			Codec.STRING.listOf().fieldOf("objectives").forGetter(data -> data.getObjectives().stream().sorted().toList())
	).apply(instance, (_, objectives) -> new ScoreboardSyncData(objectives)));
	// --------------------------------------------------
	/**
	 * {@link Gson} instance used to write JSON files.
	 * @see #save(Path)
	 */
	private static final Gson GSON = new GsonBuilder()
			.setFormattingStyle(FormattingStyle.PRETTY.withIndent("\t"))
			.create();
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
	public final boolean addObjective(@NotNull String name) throws NullPointerException {
		return this.objectives.add(Objects.requireNonNull(name));
	}

	/**
	 * Removes a scoreboard {@link Objective}'s name from the synchronization selection.
	 * @param name The name of the {@link Objective} to deselect.
	 * @return {@code true} if the name was in the collection of added names.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final boolean removeObjective(@NotNull String name) throws NullPointerException {
		return this.objectives.remove(Objects.requireNonNull(name)); //ignore if already not present
	}
	// ==================================================
	/**
	 * Saves {@link ScoreboardSyncData} to a file at a given {@link Path}.
	 * @param file The destination file {@link Path}.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalArgumentException If {@link Path} does not end with {@code .json}.
	 * @throws IOException If encoding or an I/O operation fails.
	 */
	public final void save(@NotNull Path file)
			throws NullPointerException, IllegalArgumentException, IOException
	{
		//requirements
		Objects.requireNonNull(file);
		if(file.getFileName() == null || !file.getFileName().toString().endsWith(".json"))
			throw new IllegalArgumentException("Expected a '.json' file path; got '" + file + "'");

		//encode the data
		final var data = CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow(IOException::new);

		//create parent directories
		final var targetFile = file.toAbsolutePath();
		Files.createDirectories(targetFile.getParent());

		//create a temp JSON file in the same directory
		//(this is done for redundancy, so i/o errors don't corrupt the real target file mid-write)
		final var tempFile = Files.createTempFile(
				targetFile.getParent(),
				targetFile.getFileName() + ".",
				".tmp");
		//write the encoded data to the file
		try {
			//write JSON data file
			try(final var writer = Files.newBufferedWriter(tempFile)) {
				GSON.toJson(data, writer);
			}
			//replace the real target file with the temp file
			Files.move(
					tempFile, targetFile,
					StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(JsonIOException e) { throw new IOException("Unable to write JSON to '" + file + "'", e); }
		finally { Files.deleteIfExists(tempFile); }
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
		Objects.requireNonNull(server);
		return ((ScoreboardSyncDataHolder)server).shaderboard$getScoreboardSyncData();
	}
	// --------------------------------------------------
	/**
	 * Returns the {@link Path} of the {@link File} where {@link ScoreboardSyncData}
	 * is saved for a given {@link MinecraftServer}.
	 * @param server The {@link MinecraftServer} whose world/level holds the data.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public static final @NotNull Path getFile(@NotNull MinecraftServer server) throws NullPointerException {
		return Objects.requireNonNull(server)
				.getWorldPath(LevelResource.DATA)
				.resolve(MOD_ID)
				.resolve("scoreboard_sync.json");
	}

	/**
	 * Loads {@link ScoreboardSyncData} from a given file {@link Path}.
	 * @param file The source file {@link Path}.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @throws IllegalArgumentException If {@link Path} does not end with {@code .json}.
	 * @throws IOException If reading or decoding fails.
	 */
	public static final @NotNull ScoreboardSyncData load(@NotNull Path file)
			throws NullPointerException, IllegalArgumentException, IOException
	{
		//requirements
		Objects.requireNonNull(file);
		if(file.getFileName() == null || !file.getFileName().toString().endsWith(".json"))
			throw new IllegalArgumentException("Expected a '.json' file path; got '" + file + "'");

		//if file doesn't exist, we use new empty data
		else if(Files.notExists(file))
			return new ScoreboardSyncData();

		//read the JSON file and parse the data
		try(final var reader = Files.newBufferedReader(file)) {
			return CODEC
					.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader))
					.getOrThrow(IOException::new);
		}
		catch(JsonParseException e) { throw new IOException("Unable to read JSON from '" + file + "'", e); }
	}
	// ==================================================
}
