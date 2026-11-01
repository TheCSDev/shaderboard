package com.thecsdev.shaderboard.client.registry;

import com.mojang.serialization.MapCodec;
import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBindingType;
import com.thecsdev.shaderboard.client.binding.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Holds {@link ShaderboardBindingType}s natively provided by this
 * {@link Shaderboard} mod.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ShaderboardBindingTypes
{
	// ==================================================
	/**
	 * {@link ShaderboardBindingType} for {@link SBinding_Scoreboard}.
	 */
	public static final ShaderboardBindingType<SBinding_Scoreboard> SCOREBOARD_OBJECTIVE = new ShaderboardBindingType<>(SBinding_Scoreboard.class) {
		public final @Override @NotNull MapCodec<SBinding_Scoreboard> getCodec() { return SBinding_Scoreboard.CODEC; }
	};

	/**
	 * {@link ShaderboardBindingType} for {@link SBinding_LocalPlayerHealth}.
	 */
	public static final ShaderboardBindingType<SBinding_LocalPlayerHealth> LOCAL_PLAYER_HEALTH = new ShaderboardBindingType<>(SBinding_LocalPlayerHealth.class) {
		public final @Override @NotNull MapCodec<SBinding_LocalPlayerHealth> getCodec() { return SBinding_LocalPlayerHealth.CODEC; }
	};

	/**
	 * {@link ShaderboardBindingType} for {@link SBinding_LocalPlayerFood}.
	 */
	public static final ShaderboardBindingType<SBinding_LocalPlayerFood> LOCAL_PLAYER_FOOD = new ShaderboardBindingType<>(SBinding_LocalPlayerFood.class) {
		public final @Override @NotNull MapCodec<SBinding_LocalPlayerFood> getCodec() { return SBinding_LocalPlayerFood.CODEC; }
	};

	/**
	 * {@link ShaderboardBindingType} for {@link SBinding_LocalPlayerXp}.
	 */
	public static final ShaderboardBindingType<SBinding_LocalPlayerXp> LOCAL_PLAYER_XP = new ShaderboardBindingType<>(SBinding_LocalPlayerXp.class) {
		public final @Override @NotNull MapCodec<SBinding_LocalPlayerXp> getCodec() { return SBinding_LocalPlayerXp.CODEC; }
	};

	/**
	 * {@link ShaderboardBindingType} for {@link SBinding_LocalPlayerAirSupply}.
	 */
	public static final ShaderboardBindingType<SBinding_LocalPlayerAirSupply> LOCAL_PLAYER_AIR_SUPPLY = new ShaderboardBindingType<>(SBinding_LocalPlayerAirSupply.class) {
		public final @Override @NotNull MapCodec<SBinding_LocalPlayerAirSupply> getCodec() { return SBinding_LocalPlayerAirSupply.CODEC; }
	};

	/**
	 * {@link ShaderboardBindingType} for {@link SBinding_LocalPlayerAttribute}.
	 */
	public static final ShaderboardBindingType<SBinding_LocalPlayerAttribute> LOCAL_PLAYER_ATTRIBUTE = new ShaderboardBindingType<>(SBinding_LocalPlayerAttribute.class) {
		public final @Override @NotNull MapCodec<SBinding_LocalPlayerAttribute> getCodec() { return SBinding_LocalPlayerAttribute.CODEC; }
	};

	/**
	 * {@link ShaderboardBindingType} for {@link SBinding_LocalPlayerStatusEffect}.
	 */
	public static final ShaderboardBindingType<SBinding_LocalPlayerStatusEffect> LOCAL_PLAYER_STATUS_EFFECT = new ShaderboardBindingType<>(SBinding_LocalPlayerStatusEffect.class) {
		public final @Override @NotNull MapCodec<SBinding_LocalPlayerStatusEffect> getCodec() { return SBinding_LocalPlayerStatusEffect.CODEC; }
	};
	// ==================================================
}
