package com.thecsdev.shaderboard.client.registry;

import com.mojang.serialization.MapCodec;
import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBindingType;
import com.thecsdev.shaderboard.client.binding.SBinding_LocalPlayerHealth;
import com.thecsdev.shaderboard.client.binding.SBinding_Scoreboard;
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
	// ==================================================
}
