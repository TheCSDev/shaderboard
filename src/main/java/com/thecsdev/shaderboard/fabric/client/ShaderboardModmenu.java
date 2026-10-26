package com.thecsdev.shaderboard.fabric.client;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.thecsdev.common.util.annotations.Reflected;
import com.thecsdev.shaderboard.Shaderboard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

/**
 * {@link ModMenuApi} implementation for {@link Shaderboard}.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final @Reflected class ShaderboardModmenu implements ModMenuApi {}
