package com.thecsdev.shaderboard.client.binding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link ShaderboardBinding} implementation used for binding scoreboard objective
 * values to applicable shaders.
 * <p>
 * Java-side values use a reusable {@code int[]} for both scalar and array targets.
 * Scalar targets still expose a single GLSL {@code int}, not an array.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_Scoreboard extends ShaderboardBinding<int[]>
{
	// ================================================== ==================================================
	//                                  SBinding_Scoreboard IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_Scoreboard} instances.
	 */
	public static final MapCodec<SBinding_Scoreboard> CODEC = RecordCodecBuilder.mapCodec(instance ->
		ShaderboardBinding.baseCodec(instance) //must inherit superclass's CODEC's properties
				.and(Codec.STRING.fieldOf("objective").forGetter(SBinding_Scoreboard::getObjective))
				.and(Target.CODEC.fieldOf("target").forGetter(SBinding_Scoreboard::getTarget))
				.apply(instance, SBinding_Scoreboard::new)
	);
	// ==================================================
	private final @NotNull String                       objective;
	private final @NotNull Target                       target;
	private final @NotNull ShaderboardValueType<int[]>  valueType;
	private final @NotNull Comparator<PlayerScoreEntry> scoreOrder;
	private final int @NotNull []                       value;
	// ==================================================
	public SBinding_Scoreboard(@NotNull String uniformName, @NotNull String objective, @NotNull Target target)
			throws NullPointerException, IllegalArgumentException
	{
		super(ShaderboardBindingTypes.SCOREBOARD_OBJECTIVE, uniformName);
		this.objective    = Objects.requireNonNull(objective);
		this.target       = Objects.requireNonNull(target).validate().getOrThrow(IllegalArgumentException::new);
		final var isArray = (target.type() == Target.Type.ARRAY);
		this.value        = new int[isArray ? Objects.requireNonNull(target.count()) : 1];
		this.valueType    = isArray
				? ShaderboardValueTypes.intArray(this.value.length)
				: ShaderboardValueTypes.mapped(ShaderboardValueTypes.INT, scores -> scores[0]);
		this.scoreOrder   = target.createComparator();
	}
	// ==================================================
	public final @Override @NotNull SBinding_Scoreboard clone() {
		return new SBinding_Scoreboard(getUniformName(), getObjective(), getTarget().clone());
	}
	public final @Override @NotNull ShaderboardValueType<int[]> getValueType() {
		return this.valueType;
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
	public final @Override int @NotNull [] getValue()
	{
		//resolving requires presence of a loaded level and player
		final var client = Minecraft.getInstance();
		if(client.level == null || client.player == null)
			return resetValue();

		//obtain scoreboard and objective
		final var scoreboard = client.level.getScoreboard();
		final var objective  = scoreboard.getObjective(this.getObjective());
		if(objective == null) return resetValue();
		return resolveValue(scoreboard.listPlayerScores(objective), client.player.getScoreboardName());
	}
	// ==================================================
	/**
	 * Populates the {@link #value} array with the scoreboard objective
	 * values of the given entries.
	 * @param entries The scoreboard entries to resolve values from.
	 * @param localPlayerName The name of the player whose client this is.
	 */
	@ApiStatus.Internal
	private final int @NotNull [] resolveValue(
			@NotNull Collection<PlayerScoreEntry> entries,
			@NotNull String localPlayerName)
	{
		//clean the slate for the new frame
		resetValue();

		//obtain scores
		final var scores = entries.stream()
				.filter(score -> this.target.includeHidden() || !score.isHidden())
				.filter(score -> switch(this.target.type()) {
					case LOCAL_PLAYER       -> score.owner().equalsIgnoreCase(localPlayerName);
					case PLAYER             -> score.owner().equalsIgnoreCase(this.target.name());
					case TOP_PLAYER, ARRAY  -> true;
				})
				.sorted(this.scoreOrder)
				.skip(this.target.offset() != null ? this.target.offset() : 0)
				.limit(this.value.length)
				.iterator();

		//populate values and return
		for(int i = 0; scores.hasNext(); i++)
			this.value[i] = scores.next().value();
		return this.value;
	}
	// --------------------------------------------------
	/**
	 * Resets the {@link #value} array to default values.
	 */
	@ApiStatus.Internal
	private int @NotNull [] resetValue() { Arrays.fill(this.value, 0); return this.value; }
	// ================================================== ==================================================
	//                                             Target IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Describes whose scoreboard objective value a {@link SBinding_Scoreboard} resolves to.
	 * @param type Type of score holder this target refers to.
	 * @param name When {@link Type#PLAYER}; the target plauer's name..
	 * @param includeHidden Whether hidden scores can be targeted.
	 * @param count When {@link Type#ARRAY}; the array's length.
	 * @param sort When {@link Type#ARRAY}; how the array is sorted.
	 * @param offset How many entries to skip after sorting.
	 */
	public record Target(
			@NotNull Type type, @Nullable String name, boolean includeHidden,
			@Nullable Integer count, @Nullable List<Sort> sort, @Nullable Integer offset)
	{
		// ==================================================
		/**
		 * When {@link Type#ARRAY}; maximum array length.
		 */
		public static final int MAX_COUNT = 128;
		// --------------------------------------------------
		/**
		 * {@link Codec} for selection settings. Defaults preserve existing scalar targets.
		 */
		public static final Codec<Target> CODEC = RecordCodecBuilder.<Target>create(instance -> instance.group(
				StringRepresentable.fromEnum(Type::values).fieldOf("type").forGetter(Target::type),
				Codec.STRING.optionalFieldOf("name").forGetter(t -> Optional.ofNullable(t.name())),
				Codec.BOOL.optionalFieldOf("include_hidden", false).forGetter(Target::includeHidden),
				Codec.INT.optionalFieldOf("count").forGetter(t -> Optional.ofNullable(t.count())),
				Sort.CODEC.listOf().optionalFieldOf("sort").forGetter(t -> Optional.ofNullable(t.sort())),
				Codec.INT.optionalFieldOf("offset").forGetter(t -> Optional.ofNullable(t.offset()))
		).apply(instance, (type, name, includeHidden, count, sort, offset) -> new Target(
				type, name.orElse(null), includeHidden, count.orElse(null),
				sort.orElse(null), offset.orElse(null)))
		).validate(Target::validate);
		// ==================================================
		public Target {
			Objects.requireNonNull(type);
			if(sort != null) sort = List.copyOf(sort);
		}
		// --------------------------------------------------
		/**
		 * Ensures that {@code this} {@link Target}'s configuration is valid.
		 */
		@ApiStatus.Internal
		private final @NotNull DataResult<Target> validate()
		{
			//when 'type' is 'player', target player name must be defined
			if(this.type == Type.PLAYER && this.name == null)
				return DataResult.error(() -> "Target type is 'player', but 'name' is not defined");

			//when 'type' is 'array', array size must be defined
			if(this.type == Type.ARRAY) {
				if(this.count == null || this.count < 1 || this.count > MAX_COUNT)
					return DataResult.error(() -> "Target type is 'array', but 'count' is not between 1 and " + MAX_COUNT + " (inclusive); got " + this.count);
			}
			//when 'type' is not 'array', 'count' and 'sort' must not be defined
			else if(this.count != null || this.sort != null)
				return DataResult.error(() -> "Target type is not 'array', yet 'count' or 'sort' is defined");

			//handle presence of 'offset'
			if(this.offset != null) {
				//only 'array' and 'top_player' support 'offset'
				if(this.type != Type.ARRAY && this.type != Type.TOP_PLAYER)
					return DataResult.error(() -> "Target type is not 'array' or 'top_player', yet 'offset' is defined");
				//offset must not be negative
				if(this.offset < 0)
					return DataResult.error(() -> "Target 'offset' must not be negative");
			}

			//handle presence of 'sort'
			if(this.sort != null) {
				//the array of sorting keys must not be empty
				if(this.sort.isEmpty())
					return DataResult.error(() -> "Target 'sort' array must contain at least one sorting rule key");
				//the array of sorting keys must not repeat a sort key
				if(this.sort.stream().map(Sort::by).distinct().count() != this.sort.size())
					return DataResult.error(() -> "Target 'sort' array must not repeat a sorting rule key");
			}

			//validation passed. return this target
			return DataResult.success(this);
		}
		// ==================================================
		public final @NotNull @Override Target clone() {
			return new Target(this.type, this.name, this.includeHidden, this.count, this.sort, this.offset);
		}
		// ==================================================
		/**
		 * Builds the complete {@link Comparator} used by {@link #resolveValue(Collection, String)}.
		 */
		private final @NotNull Comparator<PlayerScoreEntry> createComparator()
		{
			//prepare the complete ordering for this target
			Comparator<PlayerScoreEntry> result;

			//if sorting rules are defined, apply them in their declared order
			//each subsequent rule only compares entries tied by all previous rules
			if(this.sort != null) {
				result = this.sort.getFirst().createComparator();
				for(int i = 1; i < this.sort.size(); i++)
					result = result.thenComparing(this.sort.get(i).createComparator());
			}
			//otherwise, use vanilla's sidebar order
			else result = AccessorHud.getScoreDisplayOrder();

			//break any remaining ties using player name
			if(this.type == Type.ARRAY)
				result = result.thenComparing(PlayerScoreEntry::owner);

			//return the result
			return result;
		}
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
			TOP_PLAYER("top_player"),

			/** A fixed-size array of sorted scores. */
			ARRAY("array");
			// --------------------------------------------------
			private final @NotNull String id;
			Type(@NotNull String id) { this.id = id; }
			@Override public final @NotNull String getSerializedName() { return this.id; }
			// --------------------------------------------------
		}
		// ==================================================
	}
	// ================================================== ==================================================
	//                                               Sort IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Used for sorting scoreboard entries when a {@link Target} is of
	 * type {@link Target.Type#ARRAY}.
	 */
	@ApiStatus.Internal
	public record Sort(@NotNull By by, @NotNull Order order)
	{
		// ==================================================
		/**
		 * {@link Codec} implemnetation for serializing {@link Sort} instances.
		 */
		public static final Codec<Sort> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				StringRepresentable.fromEnum(By::values).fieldOf("by").forGetter(Sort::by),
				StringRepresentable.fromEnum(Order::values).fieldOf("order").forGetter(Sort::order)
		).apply(instance, Sort::new));
		// ==================================================
		public Sort {
			Objects.requireNonNull(by);
			Objects.requireNonNull(order);
		}
		// ==================================================
		/**
		 * Creates a {@link Comparator} used for sorting {@link PlayerScoreEntry}
		 * instances according to this sort key.
		 */
		public final @NotNull Comparator<PlayerScoreEntry> createComparator()
		{
			//create comparator for "sort by"
			final Comparator<PlayerScoreEntry> comparator = switch(this.by) {
				case NAME  -> Comparator.comparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
				case VALUE -> Comparator.comparingInt(PlayerScoreEntry::value);
			};
			//handle sort order, then return
			return (this.order == Order.DESCENDING) ? comparator.reversed() : comparator;
		}
		// ==================================================
		/**
		 * The way in which to sort entries.
		 */
		@ApiStatus.Internal
		public enum By implements StringRepresentable
		{
			// --------------------------------------------------
			NAME("name"),
			VALUE("value");
			// --------------------------------------------------
			private final @NotNull String id;
			By(@NotNull String id) { this.id = id; }
			public final @Override @NotNull String getSerializedName() { return this.id; }
			// --------------------------------------------------
		}
		// --------------------------------------------------
		/**
		 * Direction in which to sort entries.
		 */
		@ApiStatus.Internal
		public enum Order implements StringRepresentable
		{
			// --------------------------------------------------
			ASCENDING("ascending"),
			DESCENDING("descending");
			// --------------------------------------------------
			private final @NotNull String id;
			Order(@NotNull String id) { this.id = id; }
			public final @Override @NotNull String getSerializedName() { return this.id; }
			// --------------------------------------------------
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
