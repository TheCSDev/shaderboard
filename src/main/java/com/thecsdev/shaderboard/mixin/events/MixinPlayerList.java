package com.thecsdev.shaderboard.mixin.events;

import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Common-side {@link Mixin} for vanilla's {@link PlayerList}.
 */
@Mixin(PlayerList.class)
public abstract class MixinPlayerList
{
	// ==================================================
	private @Final @Shadow MinecraftServer server;
	// ==================================================
	/**
	 * Sends {@link Objective}s manually selected for syncing, after vanilla's
	 * initial scoreboard sync.
	 */
	@Inject(method = "updateEntireScoreboard", at = @At("RETURN"))
	private void shaderboard$onUpdateEntireScoreboard(ServerScoreboard scoreboard, ServerPlayer player, CallbackInfo ci)
	{
		//send selected objectives not already included by vanilla's display-based sync
		for(final var name : ScoreboardSyncData.of(this.server).getObjectives())
		{
			final var objective = scoreboard.getObjective(name);
			if(objective == null || scoreboard.getObjectiveDisplaySlotCount(objective) > 0)
				continue; //ignore missing objectives and those already sent by vanilla

			//send only to this player, without restarting global tracking
			for(final var packet : scoreboard.getStartTrackingPackets(objective))
				player.connection.send(packet);
		}
	}
	// ==================================================
}
