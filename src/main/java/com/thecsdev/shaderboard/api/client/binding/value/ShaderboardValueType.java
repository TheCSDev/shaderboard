package com.thecsdev.shaderboard.api.client.binding.value;

import com.thecsdev.shaderboard.api.client.binding.ShaderboardBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.nio.BufferOverflowException;

/**
 * Defines the {@code std140} memory layout of a value resolved by a
 * {@link ShaderboardBinding} and how such values are written.
 *
 * @param <V> The type of the resolved value.
 * @see ShaderboardValueTypes
 */
@Environment(EnvType.CLIENT)
public interface ShaderboardValueType<V>
{
	// ==================================================
	/**
	 * Returns the number of bytes one value occupies in {@code std140} layout.
	 * @implNote This must be a positive multiple of {@code 4}, because every
	 *           {@code std140} type is at least 4-byte aligned.
	 */
	public int std140Size();

	/**
	 * Returns the {@code std140} base alignment of this type.<br>
	 * Used when the type is nested inside an array or a struct.
	 */
	default int std140Alignment() { return 16; }

	/**
	 * Writes {@code value} into the given {@link ShaderboardValueWriter}.
	 * @param out The writer to write into.
	 * @param value The value to encode.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @throws IllegalArgumentException If {@code value} cannot be encoded.
	 * @throws BufferOverflowException If the value exceeds the writer's remaining capacity.
	 */
	public void write(@NotNull ShaderboardValueWriter out, @NotNull V value)
			throws NullPointerException, IllegalArgumentException, BufferOverflowException;
	// ==================================================
}
