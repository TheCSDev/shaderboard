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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * {@link ShaderboardValueType}s and factories for composing new ones.
 * @see ShaderboardValueType
 * @see ShaderboardValueWriter
 */
@Environment(EnvType.CLIENT)
public final class ShaderboardValueTypes
{
	// ================================================== ==================================================
	//                              ShaderboardValueTypes IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * A GLSL {@code float}.
	 * <p>
	 * <b>Bytes:</b> 4<br>
	 * <b>Alignment:</b> 4
	 */
	public static final ShaderboardValueType<Float> FLOAT = type(4, 4, ShaderboardValueWriter::putFloat);

	/**
	 * A GLSL {@code int}.
	 * <p>
	 * <b>Bytes:</b> 4<br>
	 * <b>Alignment:</b> 4
	 */
	public static final ShaderboardValueType<Integer> INT = type(4, 4, ShaderboardValueWriter::putInt);

	/**
	 * A GLSL {@code int}. Aka, a boolean encoded as an integer with a value
	 * of {@code 0} or {@code 1}.
	 * <p>
	 * <b>Bytes:</b> 4<br>
	 * <b>Alignment:</b> 4
	 *
	 * @apiNote Declare the GLSL member as an {@link Integer}, not a {@link Boolean}.
	 */
	@ApiStatus.Experimental //cannot decide whether to include, because of SPIR-V
	public static final ShaderboardValueType<Boolean> BOOL = type(4, 4, ShaderboardValueWriter::putBoolean);
	// --------------------------------------------------
	/**
	 * A GLSL {@code vec2}.
	 * <p>
	 * <b>Bytes:</b> 8<br>
	 * <b>Alignment:</b> 8
	 */
	public static final ShaderboardValueType<Vector2fc> VEC2 = type(8, 8, ShaderboardValueWriter::putVec2);

	/**
	 * A GLSL {@code vec3}.
	 * <p>
	 * <b>Bytes:</b> 12<br>
	 * <b>Alignment:</b> 16
	 */
	public static final ShaderboardValueType<Vector3fc> VEC3 = type(12, 16, ShaderboardValueWriter::putVec3);

	/**
	 * A GLSL {@code vec4}.
	 * <p>
	 * <b>Bytes:</b> 16<br>
	 * <b>Alignment:</b> 16
	 */
	public static final ShaderboardValueType<Vector4fc> VEC4 = type(16, 16, ShaderboardValueWriter::putVec4);
	// --------------------------------------------------
	/**
	 * A GLSL {@code ivec2}.
	 * <p>
	 * <b>Bytes:</b> 8<br>
	 * <b>Alignment:</b> 8
	 */
	public static final ShaderboardValueType<Vector2ic> IVEC2 = type(8, 8, ShaderboardValueWriter::putIVec2);

	/**
	 * A GLSL {@code ivec3}.
	 * <p>
	 * <b>Bytes:</b> 12<br>
	 * <b>Alignment:</b> 16
	 */
	public static final ShaderboardValueType<Vector3ic> IVEC3 = type(12, 16, ShaderboardValueWriter::putIVec3);

	/**
	 * A GLSL {@code ivec4}.
	 * <p>
	 * <b>Bytes:</b> 16<br>
	 * <b>Alignment:</b> 16
	 */
	public static final ShaderboardValueType<Vector4ic> IVEC4 = type(16, 16, ShaderboardValueWriter::putIVec4);
	// --------------------------------------------------
	/**
	 * A GLSL {@code mat2}.
	 * <p>
	 * <b>Bytes:</b> 32<br>
	 * <b>Alignment:</b> 16
	 */
	public static final ShaderboardValueType<Matrix2fc> MAT2 = type(32, 16, ShaderboardValueWriter::putMat2);

	/**
	 * A GLSL {@code mat3}.
	 * <p>
	 * <b>Bytes:</b> 48<br>
	 * <b>Alignment:</b> 16
	 */
	public static final ShaderboardValueType<Matrix3fc> MAT3 = type(48, 16, ShaderboardValueWriter::putMat3);

	/**
	 * A GLSL {@code mat4}.
	 * <p>
	 * <b>Bytes:</b> 64<br>
	 * <b>Alignment:</b> 16
	 */
	public static final ShaderboardValueType<Matrix4fc> MAT4 = type(64, 16, ShaderboardValueWriter::putMat4);
	// ==================================================
	private ShaderboardValueTypes() {}
	// ==================================================
	/**
	 * Creates a fixed-length array of a {@link ShaderboardValueType}.
	 * @param valueType The value type to turn into an array.
	 * @param length The length of the array.
	 * @throws NullPointerException If a {@link NotNull} argument is {@code null}.
	 * @throws IllegalArgumentException If the array length is not positive.
	 */
	public static <V> @NotNull ShaderboardValueType<List<V>> arrayOf(
			@NotNull ShaderboardValueType<V> valueType, int length)
			throws NullPointerException, IllegalArgumentException
	{
		//requirements
		Objects.requireNonNull(valueType);
		if(length <= 0)
			throw new IllegalArgumentException("Array length must be positive; got " + length);

		//turn into array
		final int alignment = Math.max(16, align(valueType.std140Alignment(), 16));
		final int stride    = align(valueType.std140Size(), alignment);
		final int size      = stride * length;
		return new ShaderboardValueType<>()
		{
			public final @Override int std140Size() { return size; }
			public final @Override int std140Alignment() { return alignment; }
			public final @Override void write(@NotNull ShaderboardValueWriter out, @NotNull List<V> value)
			{
				Objects.requireNonNull(value);
				final int base = out.position();
				for(int i = 0; i < length; i++) {
					out.position(base + i * stride);
					valueType.write(out, value.get(i));
				}
				out.position(base + size);
			}
		};
	}
	// --------------------------------------------------
	/**
	 * Creates a fixed-length {@code float[]} type.
	 * @param length The number of elements. Must be positive.
	 * @throws IllegalArgumentException If {@code length} is not positive.
	 */
	public static @NotNull ShaderboardValueType<float[]> floatArray(int length) throws IllegalArgumentException {
		return primitiveArray(length, 4, (out, value, i) -> out.putFloat(value[i]));
	}

	/**
	 * Creates a fixed-length {@code int[]} type.
	 * @param length The number of elements. Must be positive.
	 * @throws IllegalArgumentException If {@code length} is not positive.
	 */
	public static @NotNull ShaderboardValueType<int[]> intArray(int length) throws IllegalArgumentException {
		return primitiveArray(length, 4, (out, value, i) -> out.putInt(value[i]));
	}

	/**
	 * Creates a fixed-length {@code boolean[]} type, encoded as integers.<br>
	 * Declare the GLSL member as an integer array.
	 * @param length The number of elements. Must be positive.
	 * @throws IllegalArgumentException If {@code length} is not positive.
	 */
	@ApiStatus.Experimental //cannot decide whether to include, because of SPIR-V
	public static @NotNull ShaderboardValueType<boolean[]> boolArray(int length) throws IllegalArgumentException {
		return primitiveArray(length, 4, (out, value, i) -> out.putBoolean(value[i]));
	}
	// --------------------------------------------------
	/**
	 * Creates an opaque, fixed-size byte blob.
	 * <p>
	 * The shader may declare any layout with the same size; Shaderboard does
	 * not interpret the bytes. Values shorter than {@code size} are zero-padded.
	 * @param size The exact number of bytes. Must be positive and a multiple of 4.
	 * @throws IllegalArgumentException If {@code size} is invalid.
	 */
	public static @NotNull ShaderboardValueType<ByteBuffer> raw(int size) throws IllegalArgumentException {
		return raw(size, 4);
	}

	/**
	 * Creates an opaque, fixed-size byte blob with an explicit base alignment.
	 * @param size The exact number of bytes. Must be positive and a multiple of 4.
	 * @param alignment The base alignment. Must be positive.
	 * @throws IllegalArgumentException If an argument is invalid.
	 */
	public static @NotNull ShaderboardValueType<ByteBuffer> raw(int size, int alignment)
			throws IllegalArgumentException
	{
		//requirements
		if(size <= 0 || size % 4 != 0)
			throw new IllegalArgumentException("Size must be a positive multiple of 4; got " + size);
		else if(alignment <= 0)
			throw new IllegalArgumentException("Alignment must be positive; got " + alignment);

		//create value type
		return new ShaderboardValueType<>()
		{
			public final @Override int std140Size() { return size; }
			public final @Override int std140Alignment() { return alignment; }
			public final @Override void write(@NotNull ShaderboardValueWriter out, @NotNull ByteBuffer value)
			{
				Objects.requireNonNull(value);
				if(value.remaining() > size)
					throw new IllegalArgumentException("Raw value has " + value.remaining() + " bytes, but the type only allows " + size);
				out.align(alignment);
				out.putBytes(value, size);
			}
		};
	}
	// --------------------------------------------------
	/**
	 * Adapts a value type to a different Java type.
	 * @param delegate The type that performs the actual encoding.
	 * @param mapper Converts {@code V} to the delegate's type.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @apiNote Useful when a binding resolves a natural Java type but needs to
	 *          encode it as another GLSL type.
	 */
	public static <V, M> @NotNull ShaderboardValueType<V> mapped(
			@NotNull ShaderboardValueType<M> delegate, @NotNull Function<V, M> mapper)
			throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(delegate);
		Objects.requireNonNull(mapper);

		//create the type
		return new ShaderboardValueType<>()
		{
			public final @Override int std140Size() { return delegate.std140Size(); }
			public final @Override int std140Alignment() { return delegate.std140Alignment(); }
			public final @Override void write(@NotNull ShaderboardValueWriter out, @NotNull V value) {
				Objects.requireNonNull(value);
				delegate.write(out, mapper.apply(value));
			}
		};
	}
	// --------------------------------------------------
	/**
	 * Starts building a struct type.
	 * <p>
	 * A struct maps to a sequence of GLSL block members. Its size is rounded
	 * up to a multiple of 16, as required by {@code std140}.
	 */
	public static <V> @NotNull StructBuilder<V> struct() { return new StructBuilder<>(); }
	// ==================================================
	/**
	 * Creates a simple scalar/vector/matrix type.
	 * @param size Value type's {@code std140} size.
	 * @param alignment Value type's {@code std140} alignment.
	 * @param writer Value writer.
	 * @throws NullPointerException If the {@link NotNull} argument is {@code null}.
	 */
	private static <V> @NotNull ShaderboardValueType<V> type(
			int size, int alignment, @NotNull BiConsumer<ShaderboardValueWriter, V> writer)
			throws NullPointerException
	{
		//not null requirements
		Objects.requireNonNull(writer);

		//create the type
		return new ShaderboardValueType<>()
		{
			public final @Override int std140Size() { return size; }
			public final @Override int std140Alignment() { return alignment; }
			public final @Override void write(@NotNull ShaderboardValueWriter out, @NotNull V value) {
				Objects.requireNonNull(value);
				writer.accept(out, value);
			}
		};
	}
	// --------------------------------------------------
	/**
	 * Creates a fixed-length array of a primitive/opaque Java array.
	 * @param length Array length.
	 * @param elementSize {@code std140} size of a single element in the array.
	 */
	private static <V> @NotNull ShaderboardValueType<V> primitiveArray(
			int length, int elementSize, @NotNull ShaderboardValueTypes.ArrayElementWriter<V> writer)
	{
		//requirements
		if(length <= 0)
			throw new IllegalArgumentException("Array length must be positive; got " + length);

		//create the type
		final int stride = align(elementSize, 16);
		final int size   = stride * length;
		return new ShaderboardValueType<>()
		{
			public final @Override int std140Size() { return size; }
			public final @Override void write(@NotNull ShaderboardValueWriter out, @NotNull V value) {
				Objects.requireNonNull(value);
				final int base = out.position();
				for(int i = 0; i < length; i++) {
					out.position(base + i * stride);
					writer.write(out, value, i);
				}
				out.position(base + size);
			}
		};
	}

	/**
	 * Helper for {@link #primitiveArray(int, int, ArrayElementWriter)}.
	 */
	@ApiStatus.Internal
	private @FunctionalInterface interface ArrayElementWriter<V> {
		void write(@NotNull ShaderboardValueWriter out, @NotNull V array, int index);
	}
	// --------------------------------------------------
	/**
	 * Rounds an integer up to the nearest multiple of the specified alignment.
	 * @param value the value to align (must be non-negative).
	 * @param alignment the alignment boundary to round up to (must be positive).
	 */
	private static int align(int value, int alignment) {
		return (value + alignment - 1) / alignment * alignment;
	}
	// ================================================== ==================================================
	//                                      StructBuilder IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Builder for building {@link ShaderboardValueType} that encodes an object
	 * as an {@code std140} struct.
	 */
	public static final class StructBuilder<V>
	{
		// ==================================================
		private final List<Member> members = new ArrayList<>();
		// ==================================================
		private StructBuilder() {}
		// ==================================================
		/**
		 * Appends a member to the struct.
		 * @param type The member's value type.
		 * @param getter Extracts the member from the struct value.
		 * @throws NullPointerException If an argument is {@code null}.
		 */
		@SuppressWarnings("unchecked")
		public final <M> @NotNull StructBuilder<V> member(@NotNull ShaderboardValueType<M> type, @NotNull Function<V, M> getter)
				throws NullPointerException
		{
			//not null requirements
			Objects.requireNonNull(type);
			Objects.requireNonNull(getter);

			//add member and return
			this.members.add(new Member((ShaderboardValueType<Object>) type, (Function<Object, Object>) getter));
			return this;
		}
		// --------------------------------------------------
		/**
		 * Builds the struct type.
		 * @throws IllegalStateException If the struct has no members.
		 */
		public final @NotNull ShaderboardValueType<V> build() throws IllegalStateException
		{
			//requirements
			if(this.members.isEmpty())
				throw new IllegalStateException("A struct needs at least one member");

			//compute each member's offset using std140 alignment rules
			final int[] offsets = new int[this.members.size()];
			int end = 0, maxAlignment = 16;

			for(int i = 0; i < this.members.size(); i++)
			{
				final var member          = this.members.get(i);
				final int memberAlignment = member.type.std140Alignment();

				offsets[i]   = align(end, memberAlignment);
				end          = offsets[i] + member.type.std140Size();
				maxAlignment = Math.max(maxAlignment, memberAlignment);
			}

			//a struct's alignment is that of its most-aligned member, rounded up to vec4
			final int          alignment = align(maxAlignment, 16);
			final int          size      = align(end, alignment);
			final List<Member> members   = List.copyOf(this.members);

			//create the type
			return new ShaderboardValueType<>()
			{
				public final @Override int std140Size() { return size; }
				public final @Override int std140Alignment() { return alignment; }
				public final @Override void write(@NotNull ShaderboardValueWriter out, @NotNull V value)
				{
					Objects.requireNonNull(value);
					final int base = out.position();
					for(int i = 0; i < members.size(); i++)
					{
						final var member = members.get(i);
						out.position(base + offsets[i]);
						member.type.write(out, member.getter.apply(value));
					}
					out.position(base + size);
				}
			};
		}
		// ==================================================
		private record Member(@NotNull ShaderboardValueType<Object> type, @NotNull Function<Object, Object> getter) {}
		// ==================================================
	}
	// ================================================== ==================================================
}
