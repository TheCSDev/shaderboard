package com.thecsdev.shaderboard.client.registry;

import com.mojang.serialization.MapCodec;
import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBindingType;
import com.thecsdev.shaderboard.client.binding.ScoreboardBinding;
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
	 * {@link ShaderboardBindingType} for {@link ScoreboardBinding}.
	 */
	public static final ShaderboardBindingType<ScoreboardBinding> SCOREBOARD_OBJECTIVE = new ShaderboardBindingType<>(ScoreboardBinding.class) {
		public final @Override @NotNull MapCodec<ScoreboardBinding> getCodec() { return ScoreboardBinding.CODEC; }
	};
	// ==================================================
}
