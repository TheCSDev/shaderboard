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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * {@link ShaderboardBinding} implementation used for binding local player's food/saturation
 * values to applicable shaders.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_LocalPlayerFood extends ShaderboardBinding<SBinding_LocalPlayerFood.Value>
{
	// ================================================== ==================================================
	//                           SBinding_LocalPlayerFood IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_LocalPlayerFood}
	 * instances.
	 */
	public static final MapCodec<SBinding_LocalPlayerFood> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.apply(instance, SBinding_LocalPlayerFood::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	private static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.INT,   Value::foodLevel)
			.member(ShaderboardValueTypes.FLOAT, Value::saturationLevel)
			.build();
	// ==================================================
	private final @NotNull Value value = new Value();
	// ==================================================
	public SBinding_LocalPlayerFood(@NotNull String uniformName) {
		super(ShaderboardBindingTypes.LOCAL_PLAYER_FOOD, uniformName);
	}
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//obtain player instance
		final @Nullable var player = Minecraft.getInstance().player;

		//update and return the value
		if(player != null) {
			final var foodData         = player.getFoodData();
			this.value.foodLevel       = foodData.getFoodLevel();
			this.value.saturationLevel = foodData.getSaturationLevel();
		} else {
			this.value.resetTo0();
		}
		return this.value;
	}
	// ================================================== ==================================================
	//                                              Value IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Java-side view of the struct passed to GLSL.
	 * @implNote Intentionally mutable, to avoid allocating new object instance each frame.
	 */
	public static final @ApiStatus.Internal class Value
	{
		// ==================================================
		public int   foodLevel;       //4 bytes, offset 0
		public float saturationLevel; //4 bytes, offset 4
		// ==================================================
		public Value() { resetTo0(); /*useless call, but im still paranoid*/ }
		// ==================================================
		public final int foodLevel() { return this.foodLevel; }
		public final float saturationLevel() { return this.saturationLevel; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public final void resetTo0() {
			this.foodLevel       = 0;
			this.saturationLevel = 0;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
