package com.thecsdev.shaderboard.api.client.registry;

import com.mojang.serialization.Lifecycle;
import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBinding;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBindingType;
import com.thecsdev.shaderboard.client.registry.ShaderboardBindingTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static com.thecsdev.shaderboard.Shaderboard.MOD_ID;
import static net.minecraft.resources.Identifier.fromNamespaceAndPath;
import static net.minecraft.resources.ResourceKey.createRegistryKey;

/**
 * {@link Shaderboard}'s client-sided registries for adding features to the mod.
 * <p>
 * <b>Important note:</b><br>
 * These {@link Registry}s are <b>NOT</b> registered in the game's <b>ROOT</b>
 * {@link BuiltInRegistries#REGISTRY}! Avoid any and all operations that involve
 * the game's <b>ROOT</b> registry!
 */
@Environment(EnvType.CLIENT)
public final class SClientRegistries
{
	// ==================================================
	private SClientRegistries() {}
	// ==================================================
	/**
	 * {@link Registry} for {@link ShaderboardBindingType}s.<br>
	 * Allows this mod to be capableof recognizing and utilizing
	 * {@link ShaderboardBinding}s.
	 */
	public static final Registry<ShaderboardBindingType<?>> BINDING_TYPE;
	// ==================================================
	public static final void bootstrap() { /*invokes <clinit>*/ }
	static {
		//create registry instances
		BINDING_TYPE = new MappedRegistry<>(createRegistryKey(id("binding_type")), Lifecycle.stable());

		//register binding types
		Registry.register(BINDING_TYPE, id("scoreboard_objective"), ShaderboardBindingTypes.SCOREBOARD_OBJECTIVE);
	}
	// --------------------------------------------------
	/**
	 * Creates an {@link Identifier} instance that uses this mod's
	 * ID as the namespace.
	 * @param path The {@link Identifier#getPath()} value.
	 */
	@Contract("_ -> new")
	private static final @ApiStatus.Internal @NonNull Identifier id(@NotNull String path) {
		return fromNamespaceAndPath(MOD_ID, path);
	}
	// ==================================================
}
