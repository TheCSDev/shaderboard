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
 * {@link ShaderboardBinding} implementation used for binding local player's health
 * values to applicable shaders.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_LocalPlayerHealth extends ShaderboardBinding<SBinding_LocalPlayerHealth.Value>
{
	// ================================================== ==================================================
	//                         SBinding_LocalPlayerHealth IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_LocalPlayerHealth}
	 * instances.
	 */
	public static final MapCodec<SBinding_LocalPlayerHealth> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.apply(instance, SBinding_LocalPlayerHealth::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	private static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.FLOAT, Value::health)
			.member(ShaderboardValueTypes.FLOAT, Value::maxHealth)
			.member(ShaderboardValueTypes.FLOAT, Value::absorption)
			.build();
	// ==================================================
	private final @NotNull Value value = new Value();
	// ==================================================
	public SBinding_LocalPlayerHealth(@NotNull String uniformName) {
		super(ShaderboardBindingTypes.LOCAL_PLAYER_HEALTH, uniformName);
	}
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//obtain player instance
		final @Nullable var player = Minecraft.getInstance().player;

		//update and return the value
		if(player != null) {
			this.value.health     = player.getHealth();
			this.value.maxHealth  = player.getMaxHealth();
			this.value.absorption = player.getAbsorptionAmount();
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
		public float health;     //4 bytes, offset 0. | Player health points.
		public float maxHealth;  //4 bytes, offset 4. | Player's max number of health points.
		public float absorption; //4 bytes, offset 8. | Player's additional health / absorption points.
		// ------ padding ------ //4 bytes, offset 12 | Total struct size: 16 bytes
		// ==================================================
		public Value() { resetTo0(); /*useless call, but im paranoid*/ }
		// ==================================================
		public final float health() { return this.health; }
		public final float maxHealth() { return this.maxHealth; }
		public final float absorption() { return this.absorption; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public final void resetTo0() {
			this.health     = 0;
			this.maxHealth  = 0;
			this.absorption = 0;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
