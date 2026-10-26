package com.thecsdev.shaderboard;

import com.thecsdev.commonmc.api.config.ModConfig;

import static com.thecsdev.shaderboard.Shaderboard.MOD_ID;

/**
 * The configuration {@link ModConfig} for the {@link Shaderboard} mod.
 */
public final class ShaderboardConfig extends ModConfig
{
	// ==================================================
	//private @Expose @SerializedName("client-enabled") boolean enabled = true;
	// ==================================================
	public ShaderboardConfig() { super(MOD_ID); }
	// ==================================================
}
