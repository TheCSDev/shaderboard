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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * {@link ShaderboardBinding} implementation used for binding local player's breathing
 * values to applicable shaders.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_LocalPlayerAirSupply extends ShaderboardBinding<SBinding_LocalPlayerAirSupply.Value>
{
	// ================================================== ==================================================
	//                      SBinding_LocalPlayerAirSupply IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_LocalPlayerAirSupply}
	 * instances.
	 */
	public static final MapCodec<SBinding_LocalPlayerAirSupply> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.apply(instance, SBinding_LocalPlayerAirSupply::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	private static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.INT, Value::airSupply)
			.member(ShaderboardValueTypes.INT, Value::maxAirSupply)
			.build();
	// ==================================================
	private final @NotNull Value value = new Value();
	// ==================================================
	public SBinding_LocalPlayerAirSupply(@NotNull String uniformName) {
		super(ShaderboardBindingTypes.LOCAL_PLAYER_AIR_SUPPLY, uniformName);
	}
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//obtain player instance
		final @Nullable var player = Minecraft.getInstance().player;

		//update and return the value
		if(player != null) {
			this.value.maxAirSupply = player.getMaxAirSupply();
			this.value.airSupply    = Mth.clamp(player.getAirSupply(), 0, this.value.maxAirSupply);
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
		public int airSupply;    //4 bytes, offset 0
		public int maxAirSupply; //4 bytes, offset 4
		// ==================================================
		public Value() { resetTo0(); /*whatever, ill keep it*/ }
		// ==================================================
		public final int airSupply() { return this.airSupply; }
		public final int maxAirSupply() { return this.maxAirSupply; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public final void resetTo0() {
			this.airSupply    = 0;
			this.maxAirSupply = 0;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
