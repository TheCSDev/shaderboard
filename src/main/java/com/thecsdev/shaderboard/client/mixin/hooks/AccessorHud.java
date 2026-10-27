package com.thecsdev.shaderboard.client.mixin.hooks;

import net.minecraft.client.gui.Hud;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Comparator;

@Mixin(Hud.class)
public interface AccessorHud
{
	@Accessor("SCORE_DISPLAY_ORDER")
	public static Comparator<PlayerScoreEntry> getScoreDisplayOrder() { throw new AssertionError(); }
}
