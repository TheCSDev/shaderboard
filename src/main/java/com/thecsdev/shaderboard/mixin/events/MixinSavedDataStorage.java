package com.thecsdev.shaderboard.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.thecsdev.shaderboard.world.level.saveddata.ScoreboardSyncData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;

/**
 * Common-side {@link Mixin} for vanilla's {@link SavedDataStorage}.
 */
@Mixin(SavedDataStorage.class)
@SuppressWarnings("ConstantValue") //inaccurate static analysis
public abstract class MixinSavedDataStorage
{
	// ==================================================
	private @Shadow Path getDataFile(Identifier id) { throw new AssertionError(); }
	// ==================================================
	/**
	 * Leaves {@link ScoreboardSyncData} unchanged for its own version-aware
	 * {@link Codec} to read.
	 * @see ScoreboardSyncData#CODEC
	 */
	@WrapOperation(method = "readTagFromDisk", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/datafix/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;II)Lnet/minecraft/nbt/CompoundTag;"))
	private CompoundTag shaderboard$onUpdateSavedData(
			@Nullable DataFixTypes type, DataFixer fixer, CompoundTag tag,
			int fromVersion, int toVersion,
			Operation<CompoundTag> original,
			@Local(argsOnly=true) Path dataFile)
	{
		//bypass vanilla data migrations only for this mod's scoreboard sync data file
		if(type == null && dataFile.equals(getDataFile(ScoreboardSyncData.TYPE.id())))
			return tag;

		//all other files retain vanilla's migration behavior and other wrappers
		return original.call(type, fixer, tag, fromVersion, toVersion);
	}
	// ==================================================
}
