package com.thecsdev.shaderboard.client.mixin.hooks;

import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.FileToIdConverter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShaderManager.class)
public interface AccessorShaderManager
{
	@Accessor("POST_CHAIN_ID_CONVERTER")
	public static FileToIdConverter getPostChainIdConverter() { throw new AssertionError(); }
}
