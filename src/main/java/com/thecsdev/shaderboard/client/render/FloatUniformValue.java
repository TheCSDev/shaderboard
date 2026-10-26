package com.thecsdev.shaderboard.client.render;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.UniformValue;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

/**
 * {@link UniformValue} implementation that reserves space for a single
 * {@code float}.
 * <p>
 * This mod injects one of these into every 'pass' of a post-effect that has
 * bindings defined.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public record FloatUniformValue() implements UniformValue
{
	// ==================================================
	public static final FloatUniformValue INSTANCE = new FloatUniformValue();
	// ==================================================
	//initial value of '0' is a placeholder. the real value is uploaded each frame
	public final @Override void writeTo(Std140Builder builder) { builder.putFloat(0); }
	public final @Override void addSize(Std140SizeCalculator calculator) { calculator.putFloat(); }
	public final @Override UniformValue.@NonNull Type type() { return UniformValue.Type.FLOAT; }
	// ==================================================
}
