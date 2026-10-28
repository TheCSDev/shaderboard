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

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_LocalPlayerXp extends ShaderboardBinding<SBinding_LocalPlayerXp.Value>
{
	// ================================================== ==================================================
	//                             SBinding_LocalPlayerXp IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_LocalPlayerXp}
	 * instances.
	 */
	public static final MapCodec<SBinding_LocalPlayerXp> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.apply(instance, SBinding_LocalPlayerXp::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	public static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.INT,   Value::expLevel)
			.member(ShaderboardValueTypes.FLOAT, Value::expProgress)
			.member(ShaderboardValueTypes.INT,   Value::expTotal)
			.build();
	// ==================================================
	private final @NotNull Value value = new Value();
	// ==================================================
	public SBinding_LocalPlayerXp(@NotNull String uniformName) {
		super(ShaderboardBindingTypes.LOCAL_PLAYER_XP, uniformName);
	}
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//obtain player instance
		final @Nullable var player = Minecraft.getInstance().player;

		//update and return the value
		if(player != null) {
			this.value.expLevel    = player.experienceLevel;
			this.value.expProgress = player.experienceProgress;
			this.value.expTotal    = player.totalExperience;
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
		public int   expLevel;    //4 bytes, offset 0
		public float expProgress; //4 bytes, offset 4
		public int   expTotal;    //4 bytes, offset 8
		// ------ padding ------  //4 bytes, offset 12 | Total struct size: 16 bytes
		// ==================================================
		public Value() { resetTo0(); /*useless call. less paranoid by now but i'll keep it */ }
		// ==================================================
		public final int expLevel() { return this.expLevel; }
		public final float expProgress() { return this.expProgress; }
		public final int expTotal() { return this.expTotal; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public final void resetTo0() {
			this.expLevel    = 0;
			this.expProgress = 0;
			this.expTotal    = 0;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
