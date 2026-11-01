package com.thecsdev.shaderboard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thecsdev.commonmc.api.config.ModConfig;

import static com.thecsdev.shaderboard.Shaderboard.MOD_ID;

/**
 * The configuration {@link ModConfig} for the {@link Shaderboard} mod.
 */
public final class ShaderboardConfig extends ModConfig
{
	// ==================================================
	private @Expose @SerializedName("client-logLoadingWarnings") boolean logLoadingWarnins = true;
	// ==================================================
	public ShaderboardConfig() { super(MOD_ID); }
	// ==================================================
	/**
	 * Whether to log warnings when loading {@link Shaderboard} bindings whose
	 * configurations may cause issues or unexpected side-effects.
	 */
	public final boolean logBindingConfigWarnins() { return this.logLoadingWarnins; }
	// ==================================================
	/**
	 * Sets the value of {@link #logBindingConfigWarnins()}.
	 * @param value The new value.
	 */
	public final void setLogLoadingWarnins(boolean value) { this.logLoadingWarnins = value; }
	// ==================================================
}
