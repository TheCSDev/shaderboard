package com.thecsdev.shaderboard.world.level.saveddata;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Access to {@link ScoreboardSyncData} that was injected into a
 * {@link MinecraftServer} instance via this mod's {@link Mixin}.
 */
public @ApiStatus.Internal interface ScoreboardSyncDataHolder
{
	// ==================================================
	/**
	 * Returns the selection associated with this server's world.
	 */
	public @NotNull ScoreboardSyncData shaderboard$getScoreboardSyncData();
	// ==================================================
}
