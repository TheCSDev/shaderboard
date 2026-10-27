package com.thecsdev.shaderboard.client.binding;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBinding;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueType;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueTypes;
import com.thecsdev.shaderboard.client.mixin.hooks.AccessorHud;
import com.thecsdev.shaderboard.client.registry.ShaderboardBindingTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * {@link ShaderboardBinding} implementation used for binding scoreboard objective
 * values to applicable shaders.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ScoreboardBinding extends ShaderboardBinding<Integer>
{
	// ================================================== ==================================================
	//                                  ScoreboardBinding IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link ScoreboardBinding} instances.
	 */
	public static final MapCodec<ScoreboardBinding> CODEC = RecordCodecBuilder.mapCodec(instance ->
		ShaderboardBinding.baseCodec(instance) //must inherit superclass's CODEC's properties
				.and(Codec.STRING.fieldOf("objective").forGetter(ScoreboardBinding::getObjective))
				.and(Target.CODEC.fieldOf("target").forGetter(ScoreboardBinding::getTarget))
				.apply(instance, ScoreboardBinding::new)
	);
	// ==================================================
	private final @NotNull String objective;
	private final @NotNull Target target;
	// ==================================================
    public ScoreboardBinding(@NotNull String uniformName, @NotNull String objective, @NotNull Target target)
		    throws NullPointerException, IllegalArgumentException
    {
		super(ShaderboardBindingTypes.SCOREBOARD_OBJECTIVE, uniformName);
		this.objective = Objects.requireNonNull(objective);
		this.target    = Objects.requireNonNull(target);
	}
	// ==================================================
	public final @Override @NotNull ScoreboardBinding clone() {
		return new ScoreboardBinding(getUniformName(), getObjective(), getTarget());
	}
	public final @Override @NotNull ShaderboardValueType<Integer> getValueType() {
		return ShaderboardValueTypes.INT;
	}
	// ==================================================
	/**
	 * Returns the name of the scoreboard objective whose value is to be bound.
	 */
	public final @NotNull String getObjective() { return this.objective; }

	/**
	 * Returns the {@link Target} whose scoreboard objective value is to be bound.
	 */
	public final @NotNull Target getTarget() { return this.target; }
	// ==================================================
	public final @Override @NotNull Integer getValue()
	{
		//resolving requires presence of a loaded level and player
		final var client = Minecraft.getInstance();
		if(client.level == null || client.player == null)
			return 0;

		//obtain scoreboard and objective
		final var scoreboard = client.level.getScoreboard();
		final var objective  = scoreboard.getObjective(this.getObjective());
		if(objective == null) return 0;

		//obtain objective's scores in the correct order
		//(this should ideally match order in which gui 'sidebar' displays scores)
		final var scores = scoreboard.listPlayerScores(objective)
				.stream()
				.filter(input -> !input.isHidden())
				.sorted(AccessorHud.getScoreDisplayOrder())
				.toList();

		//resolve result
		final var localPlayerName = client.player.getScoreboardName();
		return switch(getTarget().type()) {
			case TOP_PLAYER -> scores.isEmpty() ? 0 : scores.getFirst().value();
			case LOCAL_PLAYER -> Iterables
					.tryFind(scores, s -> s.owner().equalsIgnoreCase(localPlayerName))
					.transform(PlayerScoreEntry::value)
					.or(0);
			case PLAYER -> Iterables
					.tryFind(scores, s -> s.owner().equalsIgnoreCase(target.name()))
					.transform(PlayerScoreEntry::value)
					.or(0);
		};
	}
	// ================================================== ==================================================
	//                                             Target IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Describes whose scoreboard objective value a {@link ScoreboardBinding} resolves to.
	 * @param type Which kind of score holder this target refers to.
	 * @param name The target's name. Only used by {@link Type#PLAYER};
	 *             {@code null} for the other types.
	 */
	public record Target(@NotNull Type type, @Nullable String name)
	{
		// ==================================================
		/**
		 * The kind of score holder a {@link Target} refers to.
		 */
		public enum Type implements StringRepresentable
		{
			// --------------------------------------------------
			/** The player whose client this is. */
			LOCAL_PLAYER("local_player"),

			/** A specific player, identified by {@link Target#name}. */
			PLAYER("player"),

			/** The player with the highest score on the objective. */
			TOP_PLAYER("top_player");
			// --------------------------------------------------
			private final @NotNull String id;
			Type(@NotNull String id) { this.id = id; }
			@Override public @NotNull String getSerializedName() { return this.id; }
			// --------------------------------------------------
		}
		// ==================================================
		/**
		 * The {@link Codec} for {@link Target}s.
		 * <p>
		 * Expects one of:
		 * <pre>{@code
		 * { "type": "local_player" }
		 * { "type": "player", "name": "Player123" }
		 * { "type": "top_player" }
		 * }</pre>
		 * The {@code "name"} field is optional and only applies to {@link Type#PLAYER};
		 * it is {@code null} for the other types.
		 */
		public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				StringRepresentable.fromEnum(Type::values).fieldOf("type").forGetter(Target::type),
				Codec.STRING.optionalFieldOf("name").forGetter(t -> Optional.ofNullable(t.name()))
		).apply(instance, (type, name) -> new Target(type, name.orElse(null))));
	}
	// ================================================== ==================================================
}
