package com.thecsdev.shaderboard.client.render;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.UniformValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * {@link UniformValue} implementation that reserves bytes for
 * {@link ShaderboardValueType#std140Size()}.
 *
 * @param valueType The layout whose size is reserved.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public record ShaderboardUniformValue(@NotNull ShaderboardValueType<?> valueType) implements UniformValue
{
	// ==================================================
	public ShaderboardUniformValue
	{
		//not null requirement
		Objects.requireNonNull(valueType);

		//size requirement
		final int size = valueType.std140Size();
		if(size <= 0 || size % 4 != 0)
			throw new IllegalArgumentException("Expected a positive 4-byte-aligned size; got " + size);
	}
	// ==================================================
	public final @Override UniformValue.@NotNull Type type() {
		//only used by 'UniformValue.CODEC'. this instance is never serialized
		throw new UnsupportedOperationException("Unexpected call to #type().");
	}
	// --------------------------------------------------
	public final @Override void addSize(@NonNull Std140SizeCalculator calculator) {
		//declare how many size this uniform value occupies
		for(int i = 0; i < this.valueType.std140Size(); i += 4)
			calculator.putInt();
	}
	// --------------------------------------------------
	public final @Override void writeTo(@NonNull Std140Builder builder) {
		//assign default values of 0. the real value is uploaded later each frame
		for(int i = 0; i < this.valueType.std140Size(); i += 4)
			builder.putInt(0);
	}
	// ==================================================
}
