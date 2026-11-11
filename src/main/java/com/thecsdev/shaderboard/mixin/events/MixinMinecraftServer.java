package com.thecsdev.shaderboard.mixin.events;

import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncDataHolder;
import com.thecsdev.shaderboard.world.scores.ScoreboardSyncManager;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

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
	private @Unique @NotNull ScoreboardSyncData shaderboard$syncData = new ScoreboardSyncData();

	/**
	 * Whether loading finished, allowing the {@link ScoreboardSyncData} to
	 * be saved.
	 */
	private @Unique boolean shaderboard$syncDataLoaded;
	// ==================================================
	public final @Override @NotNull ScoreboardSyncData shaderboard$getScoreboardSyncData() {
		return this.shaderboard$syncData;
	}
	// ==================================================
	/**
	 * Loads {@link ScoreboardSyncData} after vanilla loads a world/level.
	 */
	@SuppressWarnings("DataFlowIssue") //not. this is a Mixin
	@Inject(method = "createLevels", at = @At("RETURN"))
	private void shaderboard$onCreateLevels(CallbackInfo ci)
	{
		//load the file's data
		final var filePath = ScoreboardSyncData.getFile((MinecraftServer)(Object)this);
		try {
			this.shaderboard$syncData       = ScoreboardSyncData.load(filePath);
			this.shaderboard$syncDataLoaded = true; //must go after load
		} catch(IOException e) {
			throw new IllegalStateException("Unable to load file at: " + filePath, e);
		}

		//restore tracking for the loaded selection
		ScoreboardSyncManager.restoreObjectives((MinecraftServer)(Object)this);
	}
	// --------------------------------------------------
	/**
	 * Saves {@link ScoreboardSyncData} when vanilla saves the world/level.
	 */
	@SuppressWarnings("DataFlowIssue") //not. this is a Mixin
	@Inject(method = "saveAllChunks", at = @At("RETURN"))
	private void shaderboard$onSaveAllChunks(boolean silent, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir)
	{
		//don't save if loading never finished first
		if(!this.shaderboard$syncDataLoaded) return;

		//save the file's data
		final var filePath = ScoreboardSyncData.getFile((MinecraftServer)(Object)this);
		try { this.shaderboard$syncData.save(filePath); }
		catch(IOException e) { throw new IllegalStateException("Unable to save file at: " + filePath, e); }
	}
	// ==================================================
}
