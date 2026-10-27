package com.thecsdev.shaderboard.api.client.binding;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.thecsdev.shaderboard.api.client.registry.SClientRegistries.BINDING_TYPE;

/**
 * A single named binding, parsed from a shader JSON file's {@code "shaderboard:bindings"}
 * object.
 * <p>
 * The binding's {@code name} becomes the name of a GLSL uniform block inside shader.
 * Shaderboard injects the block into the pass's uniform layout and keeps the underlying
 * GPU buffer updated with the resolved value every frame.
 *
 * @param <V> The type of value this binding resolves.
 */
@Environment(EnvType.CLIENT)
public abstract class ShaderboardBinding<V> implements Cloneable
{
	// ==================================================
	/**
	 * {@link Codec} instance for handling {@link ShaderboardBinding} serialization.
	 */
	public static final Codec<? extends ShaderboardBinding<?>> CODEC = BINDING_TYPE.byNameCodec().partialDispatch(
			"type",
			binding -> DataResult.success(binding.getType()),
			bindingType -> DataResult.success(bindingType.getCodec()));

	/**
	 * Builds the shared {@link Codec} fragment for all {@link ShaderboardBinding}s.<br>
	 * Includes the {@code "uniform_name"} field that every binding type requires.
	 * @param <T> The concrete binding type.
	 * @param instance The codec instance.
	 * @throws NullPointerException If the argument is {@code null}.
	 * @apiNote Use {@code .and(...)} for chaining.
	 */
	protected static <T extends ShaderboardBinding<?>> P1<Mu<T>, String> baseCodec(@NotNull Instance<T> instance)
			throws NullPointerException
	{
		Objects.requireNonNull(instance);
		return instance.group(
				Codec.STRING.fieldOf("uniform_name")
						.forGetter(ShaderboardBinding::getUniformName)
		);
	}
	// ==================================================
	private final @NotNull String                    uniformName;
	private final @NotNull ShaderboardBindingType<?> type;
	// ==================================================
	public ShaderboardBinding(@NotNull ShaderboardBindingType<?> type, @NotNull String uniformName)
			throws NullPointerException, IllegalArgumentException
	{
		//argument validity checks
		Objects.requireNonNull(type);
		if(type.getBaseClass() != getClass())
			throw new IllegalArgumentException("Provided ShaderboardBindingType doesn't represent this ShaderboardBinding's Class");

		//assign field values
		this.uniformName = Objects.requireNonNull(uniformName);
		this.type        = type;
	}
	// ==================================================
	@SuppressWarnings({"MethodDoesntCallSuperMethod", "unchecked"})
	public @Virtual @NotNull ShaderboardBinding<V> clone() throws IllegalStateException {
		final var type = (ShaderboardBindingType<ShaderboardBinding<V>>) getType();
		final var json = type.getCodec().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow();
		final var copy = type.getCodec().codec().parse(JsonOps.INSTANCE, json).getOrThrow();
		if(getClass() != copy.getClass()) throw new IllegalStateException("Class mismatch after #clone()");
		return copy;
	}
	// ==================================================
	/**
	 * Returns the name of the variable GLSL will use to access the value of this binding.
	 */
	@Contract(pure = true)
	public final @NotNull String getUniformName() { return this.uniformName; }

	/**
	 * Returns the size of this binding's uniform block, in bytes.
	 * @see ShaderboardValueType#std140Size()
	 */
	@Contract(pure = true)
	public final int getUniformSize() { return getValueType().std140Size(); }
	// --------------------------------------------------
	/**
	 * Returns the {@link ShaderboardBindingType} of this {@link ShaderboardBinding}.
	 */
	@Contract(pure = true)
	public final @NotNull ShaderboardBindingType<?> getType() { return this.type; }

	/**
	 * Returns the {@code std140} layout of the value this binding resolves.
	 * @implNote The returned value <b>must be</b> constant!
	 */
	@Contract(pure = true)
	public abstract @NotNull ShaderboardValueType<V> getValueType();
	// --------------------------------------------------
	/**
	 * Resolves the value of this {@link ShaderboardBinding}.<br>
	 * This value is then passed to a shader using this binding.
	 */
	@Contract(pure = true)
	public abstract @NotNull V getValue();
	// ==================================================
}
