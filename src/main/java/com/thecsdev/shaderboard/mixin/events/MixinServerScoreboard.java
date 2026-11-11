package com.thecsdev.shaderboard.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Common-side {@link Mixin} for vanilla's {@link ServerScoreboard}.
 */
@Mixin(ServerScoreboard.class)
public abstract class MixinServerScoreboard
{
	// ==================================================
	private @Final @Shadow MinecraftServer server;
	private @Final @Shadow Set<Objective> trackedObjectives;
	// ==================================================
	/**
	 * Removes deleted {@link Objective}s from the saved synchronization selection.
	 */
	@Inject(method = "onObjectiveRemoved", at = @At("RETURN"))
	private void shaderboard$onObjectiveRemoved(Objective objective, CallbackInfo ci) {
		ScoreboardSyncData.of(this.server).removeObjective(objective.getName());
	}
	// --------------------------------------------------
	/**
	 * Restores synchronization of manually selected {@link Objective}s after their
	 * {@link DisplaySlot} is cleared or assigned to another {@link Objective}.
	 */
	@WrapOperation(method = "setDisplayObjective", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/ServerScoreboard;stopTrackingObjective(Lnet/minecraft/world/scores/Objective;)V"))
	private void shaderboard$onStopTrackingDisplayedObjective(
			ServerScoreboard scoreboard, Objective old, Operation<Void> original)
	{
		//let vanilla and other wrappers perform their original operations
		original.call(scoreboard, old);

		//do not restore an objective that is being deleted
		if(scoreboard.getObjective(old.getName()) != old)
			return;

		//restore manual synchronization unless another wrapper already retained it
		if(ScoreboardSyncData.of(this.server).getObjectives().contains(old.getName()) && !this.trackedObjectives.contains(old))
			scoreboard.startTrackingObjective(old);
	}
	// ==================================================
}
