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
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * {@link ShaderboardBinding} implementation used for binding local player's motion
 * states to applicable shaders.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_LocalPlayerMotionState extends ShaderboardBinding<SBinding_LocalPlayerMotionState.Value>
{
	// ================================================== ==================================================
	//                    SBinding_LocalPlayerMotionState IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_LocalPlayerMotionState}
	 * instances.
	 */
	public static final MapCodec<SBinding_LocalPlayerMotionState> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.apply(instance, SBinding_LocalPlayerMotionState::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	private static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.INT, Value::isOnGround)
			.member(ShaderboardValueTypes.INT, Value::isCrouching)
			.member(ShaderboardValueTypes.INT, Value::isSprinting)
			.member(ShaderboardValueTypes.INT, Value::isSwimming)
			.member(ShaderboardValueTypes.INT, Value::isUnderwater)
			.member(ShaderboardValueTypes.INT, Value::isFlying)
			.member(ShaderboardValueTypes.INT, Value::isGliding)
			.member(ShaderboardValueTypes.INT, Value::isRiding)
			.build();
	// ==================================================
	private final @NotNull Value value = new Value();
	// ==================================================
	public SBinding_LocalPlayerMotionState(@NotNull String uniformName) {
		super(ShaderboardBindingTypes.LOCAL_PLAYER_MOTION_STATE, uniformName);
	}
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//obtain the local player instance
		final @Nullable var player = Minecraft.getInstance().player;
		if(player == null) return this.value.resetTo0();

		//obtain values and return
		this.value.isOnGround   = player.onGround()                    ? 1 : 0;
		this.value.isCrouching  = player.isCrouching()                 ? 1 : 0;
		this.value.isSprinting  = player.isSprinting()                 ? 1 : 0;
		this.value.isSwimming   = player.isSwimming()                  ? 1 : 0;
		this.value.isUnderwater = player.isEyeInFluid(FluidTags.WATER) ? 1 : 0;
		this.value.isFlying     = player.getAbilities().flying         ? 1 : 0;
		this.value.isGliding    = player.isFallFlying()                ? 1 : 0;
		this.value.isRiding     = player.isPassenger()                 ? 1 : 0;
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
		public int isOnGround;   //offset 0,  size 4
		public int isCrouching;  //offset 4,  size 4
		public int isSprinting;  //offset 8,  size 4
		public int isSwimming;   //offset 12, size 4
		public int isUnderwater; //offset 16, size 4
		public int isFlying;     //offset 20, size 4
		public int isGliding;    //offset 24, size 4
		public int isRiding;     //offset 28, size 4
		// ==================================================
		public Value() { resetTo0(); }
		// ==================================================
		public final int isOnGround() { return this.isOnGround; }
		public final int isCrouching() { return this.isCrouching; }
		public final int isSprinting() { return this.isSprinting; }
		public final int isSwimming() { return this.isSwimming; }
		public final int isUnderwater() { return this.isUnderwater; }
		public final int isFlying() { return this.isFlying; }
		public final int isGliding() { return this.isGliding; }
		public final int isRiding() { return this.isRiding; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public @NotNull Value resetTo0() {
			this.isOnGround   = 0;
			this.isCrouching  = 0;
			this.isSprinting  = 0;
			this.isUnderwater = 0;
			this.isSwimming   = 0;
			this.isFlying     = 0;
			this.isGliding    = 0;
			this.isRiding     = 0;
			return this;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
