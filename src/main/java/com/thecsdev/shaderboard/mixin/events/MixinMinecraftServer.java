package com.thecsdev.shaderboard.mixin.events;

import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Common-side {@link Mixin} for vanilla's {@link MinecraftServer}.
 */
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{
	// ==================================================
	/**
	 * Restores manual scoreboard synchronization after vanilla loads the world.
	 */
	@Inject(method = "createLevels", at = @At("RETURN"))
	private void shaderboard$onCreateLevels(CallbackInfo ci)
	{
		//obtain the loaded scoreboard and saved selection
		final var server     = (MinecraftServer)(Object)this;
		final var scoreboard = server.getScoreboard();
		final var data       = ScoreboardSyncData.of(server);

		//iterate a copy so stale names can be removed from the saved selection
		for(final var name : List.copyOf(data.getObjectives()))
		{
			//obtain objective from name
			final var objective = scoreboard.getObjective(name);

			//ignore objectives that no longer exist
			if(objective == null) {
				data.removeObjective(name);
				continue;
			}

			//start tracking manually synced objectives, if not already tracked by vanilla
			if(scoreboard.getObjectiveDisplaySlotCount(objective) == 0)
				scoreboard.startTrackingObjective(objective);
		}
	}
	// ==================================================
}
