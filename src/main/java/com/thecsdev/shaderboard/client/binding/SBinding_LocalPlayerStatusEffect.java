package com.thecsdev.shaderboard.client.binding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBinding;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueType;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueTypes;
import com.thecsdev.shaderboard.client.registry.ShaderboardBindingTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * {@link ShaderboardBinding} implementation used for binding local player's status
 * effect values to applicable shaders.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_LocalPlayerStatusEffect extends ShaderboardBinding<SBinding_LocalPlayerStatusEffect.Value>
{
	// ================================================== ==================================================
	//                   SBinding_LocalPlayerStatusEffect IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_LocalPlayerStatusEffect}
	 * instances.
	 */
	public static final MapCodec<SBinding_LocalPlayerStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.and(MobEffect.CODEC.fieldOf("effect").forGetter(SBinding_LocalPlayerStatusEffect::getEffect))
					.apply(instance, SBinding_LocalPlayerStatusEffect::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	private static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.INT, Value::duration)
			.member(ShaderboardValueTypes.INT, Value::amplifier)
			.member(ShaderboardValueTypes.INT, Value::isInfinite)
			.build();
	// ==================================================
	private final @NotNull Holder<MobEffect> effect;
	private final @NotNull Value             value = new Value();
	// ==================================================
	public SBinding_LocalPlayerStatusEffect(@NotNull String uniformName, @NotNull Holder<MobEffect> effect) {
		super(ShaderboardBindingTypes.LOCAL_PLAYER_STATUS_EFFECT, uniformName);
		this.effect = Objects.requireNonNull(effect);
	}
	// ==================================================
	/**
	 * Returns the {@link Holder} of the {@link MobEffect} that this binding passes
	 * the values of.
	 */
	public final @NotNull Holder<MobEffect> getEffect() { return effect; }
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//obtain the local player instance
		final @Nullable var player = Minecraft.getInstance().player;
		if(player == null) return this.value.resetTo0();

		//obtain the status effect instance
		final @Nullable var effect = player.getEffect(this.effect);
		if(effect == null) return this.value.resetTo0();

		//obtain values and return
		this.value.duration   = effect.getDuration();
		this.value.amplifier  = effect.getAmplifier();
		this.value.isInfinite = effect.isInfiniteDuration() ? 1 : 0;
		return this.value;
	}
	// ================================================== ==================================================
	//                                              Value IMPLEMENTATION
	// ================================================== ==================================================\
	/**
	 * Java-side view of the struct passed to GLSL.
	 * @implNote Intentionally mutable, to avoid allocating new object instance each frame.
	 */
	public static final @ApiStatus.Internal class Value
	{
		// ==================================================
		public int duration;
		public int amplifier;
		public int isInfinite;
		// ==================================================
		public Value() { resetTo0(); }
		// ==================================================
		public final int duration() { return duration; }
		public final int amplifier() { return amplifier; }
		public final int isInfinite() { return isInfinite; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public final @NotNull Value resetTo0() {
			this.duration   = 0;
			this.amplifier  = 0;
			this.isInfinite = 0;
			return this;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
