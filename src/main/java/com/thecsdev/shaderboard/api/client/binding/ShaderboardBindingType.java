package com.thecsdev.shaderboard.api.client.binding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.api.client.registry.SClientRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A "type" of {@link ShaderboardBinding} that {@link Shaderboard} understands.
 * @param <T> {@link ShaderboardBinding} type.
 */
@Environment(EnvType.CLIENT)
public abstract class ShaderboardBindingType<T extends ShaderboardBinding>
{
	// ==================================================
	private final @NotNull Class<T> baseClass;
	// ==================================================
	public ShaderboardBindingType(@NotNull Class<T> baseClass) throws NullPointerException {
		this.baseClass = Objects.requireNonNull(baseClass);
	}
	// ==================================================
	/**
	 * Returns the {@link Class} of the {@link ShaderboardBinding} this
	 * {@link ShaderboardBindingType} represents.
	 */
	public final @NotNull Class<T> getBaseClass() { return this.baseClass; }

	/**
	 * Returns the {@link Identifier} key for this {@link ShaderboardBindingType} in the
	 * {@link SClientRegistries#BINDING_TYPE} registry, or {@code null} if this
	 * {@link ShaderboardBindingType} is not registered.
	 */
	public final @Nullable Identifier getKey() { return SClientRegistries.BINDING_TYPE.getKey(this); }

	/**
	 * Returns the {@link Codec} used for serializing and deserializing
	 * {@link ShaderboardBinding}s of this {@link ShaderboardBindingType}.
	 */
	public abstract @NotNull MapCodec<T> getCodec();
	// ==================================================
}
