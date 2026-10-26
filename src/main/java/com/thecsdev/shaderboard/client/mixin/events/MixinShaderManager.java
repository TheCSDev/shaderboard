package com.thecsdev.shaderboard.client.mixin.events;

import com.google.common.collect.ImmutableMap;
import com.thecsdev.shaderboard.client.render.ShaderboardManager;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Client-side {@link Mixin} for vanilla's {@link ShaderManager}.
 */
@Mixin(ShaderManager.class)
public abstract class MixinShaderManager
{
	// ==================================================
	/**
	 * At the start of every resource reload, scan post-effect files for
	 * {@code "shaderboard:bindings"} and rebuild the binding registry.
	 */
	@Inject(method = "reload", at = @At("HEAD"))
	private void onReloadBegin(
			PreparableReloadListener.SharedState currentReload,
			Executor taskExecutor,
			PreparableReloadListener.PreparationBarrier preparationBarrier,
			Executor reloadExecutor,
			CallbackInfoReturnable<CompletableFuture<Void>> cir)
	{
		//the 'SharedState' gives us the new 'ResourceManager' for this reload,
		//which is where the post-effect files will be read from
		ShaderboardManager.reloadBeginCallback(currentReload.resourceManager());
	}
	// --------------------------------------------------
	/**
	 * When vanilla stores a parsed {@link PostChainConfig}, we add to that config
	 * one uniform block per binding, so the shader's declared blocks actually
	 * exist and have GPU buffers.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Redirect(
			method = "loadPostChain",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;",
					remap = false
			)
	) // FIXME - Look into an alt. solution that isn't @Redirect
	private static ImmutableMap.Builder onLoadPostChain(
			ImmutableMap.Builder builder, Object key, Object value)
	{
		if(value instanceof PostChainConfig pcConfig)
			return builder.put(key, ShaderboardManager.loadPostChainCallback((Identifier)key, pcConfig));
		return builder.put(key, value);
	}
	// ==================================================
}
