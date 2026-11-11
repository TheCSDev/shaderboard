package com.thecsdev.shaderboard.mixin.events;

import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncDataHolder;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Common-side {@link Mixin} for vanilla's {@link MinecraftServer}.
 */
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ScoreboardSyncDataHolder
{
	// ==================================================
	/**
	 * {@link ScoreboardSyncData} injected in this {@link MinecraftServer}.
	 * @see ScoreboardSyncDataHolder
	 */
	private @Unique final @NotNull ScoreboardSyncData shaderboard$syncData = new ScoreboardSyncData();
	// ==================================================
	public final @Override @NotNull ScoreboardSyncData shaderboard$getScoreboardSyncData() {
		return this.shaderboard$syncData;
	}
	// ==================================================
}
