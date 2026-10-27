package com.thecsdev.shaderboard.api.client.binding.value;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix2fc;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Writes values into a {@link ByteBuffer} using {@code std140} layout rules.
 * @see ShaderboardValueType
 * @see ShaderboardValueTypes
 */
@Environment(EnvType.CLIENT)
public final class ShaderboardValueWriter
{
	// ==================================================
	private final @NotNull ByteBuffer buffer;
	// ==================================================
	/**
	 * Creates a writer for the given buffer.
	 * @param buffer The buffer to write into, starting at its current position.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public ShaderboardValueWriter(@NotNull ByteBuffer buffer) throws NullPointerException
	{
		//initialize fields
		this.buffer = Objects.requireNonNull(buffer);

		//reorder the buffer where needed
		if(this.buffer.order() != ByteOrder.nativeOrder())
			this.buffer.order(ByteOrder.nativeOrder());
	}
	// ==================================================
	/**
	 * Returns the underlying {@link ByteBuffer} instance.
	 * @apiNote Not recommended to use unless you know what you are doing.
	 */
	public final @NotNull ByteBuffer getBuffer() { return this.buffer; }
	// ==================================================
	/**
	 * Returns the writer's current absolute position.
	 */
	public final int position() { return this.buffer.position(); }

	/**
	 * Moves the writer to an absolute position.
	 * @throws IllegalArgumentException If the position is out of bounds.
	 */
	public final @NotNull ShaderboardValueWriter position(int position) {
		this.buffer.position(position);
		return this;
	}

	/**
	 * Advances the writer to the next multiple of a given integer.
	 * @throws IllegalArgumentException If the alignment is not positive.
	 * @throws IndexOutOfBoundsException If the aligned position exceeds the limit.
	 */
	public final @NotNull ShaderboardValueWriter align(int alignment)
			throws IllegalArgumentException, IndexOutOfBoundsException
	{
		//ensure the given integer is a positive one
		if(alignment <= 0)
			throw new IllegalArgumentException("Alignment must be positive; got " + alignment);

		//calculate the next aligned position
		final int position = this.buffer.position();
		final int aligned  = (position + alignment - 1) / alignment * alignment;

		//ensure the next aligned position is within bounds
		if(aligned > this.buffer.limit())
			throw new IndexOutOfBoundsException("Aligned position " + aligned + " exceeds limit " + this.buffer.limit());

		//set position and return
		this.buffer.position(aligned);
		return this;
	}
	// --------------------------------------------------
	/**
	 * Ensures that the {@link #buffer} has a remaining capacity of X bytes.
	 * @param bytes The needed capacity.
	 * @throws BufferOverflowException If the {@link #buffer} does not have the capacity.
	 */
	private final void assertRemainingBytes(int bytes) throws BufferOverflowException {
		if(this.buffer.remaining() < bytes)
			throw new BufferOverflowException();
	}
	// ==================================================
	/**
	 * Writes a {@code float}.
	 * <p>
	 * <b>Bytes:</b> 4<br>
	 * <b>Alignment:</b> 4
	 */
	public final @NotNull ShaderboardValueWriter putFloat(float value)
	{
		//alignment and assertion
		align(4);
		assertRemainingBytes(4);

		//write and return
		this.buffer.putFloat(value);
		return this;
	}

	/**
	 * Writes an {@code int}.
	 * <p>
	 * <b>Bytes:</b> 4<br>
	 * <b>Alignment:</b> 4
	 */
	public final @NotNull ShaderboardValueWriter putInt(int value)
	{
		//alignment and assertion
		align(4);
		assertRemainingBytes(4);

		//write and return
		this.buffer.putInt(value);
		return this;
	}

	/**
	 * Writes an {@code int}. Its value 0 or 1.
	 * <p>
	 * <b>Bytes:</b> 4<br>
	 * <b>Alignment:</b> 4
	 */
	@ApiStatus.Experimental //cannot decide whether to include, because of SPIR-V
	public final @NotNull ShaderboardValueWriter putBoolean(boolean value) {
		return putInt(value ? 1 : 0);
	}
	// --------------------------------------------------
	/**
	 * Writes a {@code vec2}.
	 * <p>
	 * <b>Bytes:</b> 8<br>
	 * <b>Alignment:</b> 8
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putVec2(@NotNull Vector2fc value) throws NullPointerException
	{
		//assertion and alignment
		Objects.requireNonNull(value);
		align(8);

		//write and return
		putFloat(value.x());
		putFloat(value.y());
		return this;
	}

	/**
	 * Writes a {@code vec3}.
	 * <p>
	 * <b>Bytes:</b> 12<br>
	 * <b>Alignment:</b> 16
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putVec3(@NotNull Vector3fc value) throws NullPointerException
	{
		//assertion and alignment
		Objects.requireNonNull(value);
		align(16);

		//write and return
		putFloat(value.x());
		putFloat(value.y());
		putFloat(value.z());
		return this;
	}

	/**
	 * Writes a {@code vec4}.
	 * <p>
	 * <b>Bytes:</b> 16<br>
	 * <b>Alignment:</b> 16
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putVec4(@NotNull Vector4fc value) throws NullPointerException
	{
		//assertion and alignment
		Objects.requireNonNull(value);
		align(16);

		//write and return
		putFloat(value.x());
		putFloat(value.y());
		putFloat(value.z());
		putFloat(value.w());
		return this;
	}
	// --------------------------------------------------
	/**
	 * Writes an {@code ivec2}.
	 * <p>
	 * <b>Bytes:</b> 8<br>
	 * <b>Alignment:</b> 8
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putIVec2(@NotNull Vector2ic value) throws NullPointerException
	{
		//assertion and alignment
		Objects.requireNonNull(value);
		align(8);

		//write and return
		putInt(value.x());
		putInt(value.y());
		return this;
	}

	/**
	 * Writes an {@code ivec3}.
	 * <p>
	 * <b>Bytes:</b> 12<br>
	 * <b>Alignment:</b> 16
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putIVec3(@NotNull Vector3ic value) throws NullPointerException
	{
		//assertion and alignment
		Objects.requireNonNull(value);
		align(16);

		//write and return
		putInt(value.x());
		putInt(value.y());
		putInt(value.z());
		return this;
	}

	/**
	 * Writes an {@code ivec4}.
	 * <p>
	 * <b>Bytes:</b> 16<br>
	 * <b>Alignment:</b> 16
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putIVec4(@NotNull Vector4ic value) throws NullPointerException
	{
		//assertion and alignment
		Objects.requireNonNull(value);
		align(16);

		//write and return
		putInt(value.x());
		putInt(value.y());
		putInt(value.z());
		putInt(value.w());
		return this;
	}
	// --------------------------------------------------
	/**
	 * Writes a {@code mat2}.
	 * <p>
	 * <b>Bytes:</b> 32<br>
	 * <b>Alignment:</b> 16
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putMat2(@NotNull Matrix2fc value) throws NullPointerException
	{
		//not null requirement
		Objects.requireNonNull(value);

		//align, write, and return
		align(16);
		putFloat(value.m00());
		putFloat(value.m01());
		align(16);
		putFloat(value.m10());
		putFloat(value.m11());
		return this;
	}

	/**
	 * Writes a {@code mat3}.
	 * <p>
	 * <b>Bytes:</b> 48<br>
	 * <b>Alignment:</b> 16
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putMat3(@NotNull Matrix3fc value) throws NullPointerException
	{
		//not null requirement
		Objects.requireNonNull(value);

		//align, write, and return
		align(16);
		putFloat(value.m00());
		putFloat(value.m01());
		putFloat(value.m02());
		align(16);
		putFloat(value.m10());
		putFloat(value.m11());
		putFloat(value.m12());
		align(16);
		putFloat(value.m20());
		putFloat(value.m21());
		putFloat(value.m22());
		align(16); //trailing column padding, so the next member starts at +48
		return this;
	}

	/**
	 * Writes a {@code mat4}.
	 * <p>
	 * <b>Bytes:</b> 64<br>
	 * <b>Alignment:</b> 16
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	public final @NotNull ShaderboardValueWriter putMat4(@NotNull Matrix4fc value) throws NullPointerException
	{
		//not null requirement
		Objects.requireNonNull(value);

		//align, write, and return
		align(16);
		putFloat(value.m00());
		putFloat(value.m01());
		putFloat(value.m02());
		putFloat(value.m03());
		putFloat(value.m10());
		putFloat(value.m11());
		putFloat(value.m12());
		putFloat(value.m13());
		putFloat(value.m20());
		putFloat(value.m21());
		putFloat(value.m22());
		putFloat(value.m23());
		putFloat(value.m30());
		putFloat(value.m31());
		putFloat(value.m32());
		putFloat(value.m33());
		return this;
	}
	// ==================================================
	/**
	 * Writes up to a certain amount of bytes from a given source, zero-ing out any
	 * remaining space.
	 * @param source The source whyose bytes are to be written.
	 * @param maxAmount Maximum amount of bytes to write.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 * @throws IllegalArgumentException If the integer argument is negative.
	 */
	public final @NotNull ShaderboardValueWriter putBytes(@NotNull ByteBuffer source, int maxAmount)
			throws NullPointerException, IllegalArgumentException
	{
		//requirements
		Objects.requireNonNull(source);
		if(maxAmount < 0)
			throw new IllegalArgumentException("Max amount count cannot be negative; got " + maxAmount);
		assertRemainingBytes(maxAmount);

		//write bytes
		final var src   = source.duplicate();
		final int count = Math.min(maxAmount, src.remaining());
		src.limit(src.position() + count);
		this.buffer.put(src);
		return putZero(maxAmount - count);
	}

	/**
	 * Writes X amount of {@code (byte) 0} at the current position.
	 * @param amount The amount of zeroes to write.
	 * @throws IllegalArgumentException If {@code bytes} is negative.
	 */
	public final @NotNull ShaderboardValueWriter putZero(int amount) throws IllegalArgumentException
	{
		//requirements
		if(amount < 0)
			throw new IllegalArgumentException("Amount cannot be negative; got " + amount);
		assertRemainingBytes(amount);

		//write zeros
		for(int i = 0; i < amount; i++)
			this.buffer.put((byte) 0);
		return this;
	}

	/**
	 * Writes {@code (byte) 0} from the current position up to a given target position.
	 * @param targetPosition Buffer position to stop writing zeros at.
	 * @throws IllegalArgumentException If the target position is out of bounds.
	 */
	public final @NotNull ShaderboardValueWriter putZeroTo(int targetPosition) throws IllegalArgumentException {
		return putZero(targetPosition - this.buffer.position());
	}
	// ==================================================
	/**
	 * Sets the buffer's limit to the current position, then rewinds its position
	 * back to {@code 0}.
	 * @return {@link #getBuffer()}
	 */
	public final @NotNull ByteBuffer flip() {
		this.buffer.limit(this.buffer.position());
		this.buffer.position(0);
		return this.buffer;
	}
	// ==================================================
}
