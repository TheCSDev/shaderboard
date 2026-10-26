package com.thecsdev.shaderboard.client.mixin.events;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.thecsdev.shaderboard.client.render.ShaderboardManager;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Client-side {@link Mixin} for vanilla's {@link PostPass}.
 * <p>
 * Every frame, right before a post-pass is added to the frame graph, this
 * mixin asks {@link ShaderboardManager} to write the currently resolved
 * binding values into the pass's uniform buffers.
 * <p>
 * This is what makes the values dynamic instead of staying at the initial
 * value of {@code 0}.
 */
@Mixin(PostPass.class)
public abstract class MixinPostPass
{
	// ==================================================
	private @Final @Shadow String                 name;
	private @Final @Shadow Map<String, GpuBuffer> customUniforms;
	// ==================================================
	@Inject(method = "addToFrame", at = @At("HEAD"))
	private void updateBindings(CallbackInfo ci) {
		//(the pass name encodes the post-effect id and pass index,
		//which lets us find its bindings)
		ShaderboardManager.updatePostPassBindingsCallback(this.name, this.customUniforms);
	}
	// ==================================================
}
