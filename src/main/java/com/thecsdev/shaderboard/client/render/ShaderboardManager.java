package com.thecsdev.shaderboard.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.serialization.JsonOps;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBinding;
import com.thecsdev.shaderboard.client.mixin.hooks.AccessorShaderManager;
import io.netty.util.internal.UnstableApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.util.*;

import static com.thecsdev.shaderboard.Shaderboard.LOGGER;

/**
 * The main runtime manager/controller responsible for keeping track of
 * {@link ShaderboardBinding}s and giving them functionality.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ShaderboardManager
{
	// ==================================================
	/**
	 * {@link FileToIdConverter} instance that turns {@code post_effect/xxx.json}
	 * locations into an {@link Identifier}s.
	 * @see ShaderManager
	 */
	public static final FileToIdConverter POST_CHAIN_ID_CONVERTER = AccessorShaderManager.getPostChainIdConverter();
	// --------------------------------------------------
	/**
	 * The vanilla root JSON key used to declare a post-effect's pass list.
	 */
	@UnstableApi //vanilla game could change this name at any moment
	private static final String JSONKEY_PASSES = "passes";

	/**
	 * The root JSON key used to declare bindings in a post-effect JSON file.
	 */
	public static final String JSONKEY_BINDINGS = "shaderboard:bindings";
	// --------------------------------------------------
	/**
	 * All known bindings, keyed by post-effect {@link Identifier} and then by
	 * pass index.
	 * <p>
	 * Replaced entirely on every resource reload, hence {@code volatile}.
	 */
	private static volatile @NotNull Map<Identifier, Map<Integer, List<ShaderboardBinding>>> BINDINGS = Map.of();
	// ==================================================
	private ShaderboardManager() {}
	// ==================================================
	/**
	 * Internal callback method is invoked the moment the game's
	 * {@link ShaderManager} begins (re/)loading.
	 * <p>
	 * Scans post-effect JSON files for {@link ShaderboardBinding} definitions,
	 * and (re/)builds the bindings map (aka {@link #BINDINGS}).
	 *
	 * @param resourceManager The game's {@link ResourceManager}.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	@ApiStatus.Internal
	public static final void reloadBeginCallback(@NotNull ResourceManager resourceManager)
			throws NullPointerException
	{
		//create a new temp. map to collect bindings into
		final var collected = new HashMap<Identifier, Map<Integer, List<ShaderboardBinding>>>();

		//begin collecting bindings
		// ---------- obtain a collection post-effect resources
		final var entries = resourceManager.listResources(
				POST_CHAIN_ID_CONVERTER.prefix(),    //the "post_effect" folder
				id -> id.getPath().endsWith(".json") //.json files inside
		).entrySet();

		//iterate post-effect resources, and collect bindings
		for(final var entry : entries)
		try
		{
			// ---------- read and parse the resource's json
			JsonElement jsonElement;
			try(final var reader = entry.getValue().openAsReader()) {
				jsonElement = StrictJsonParser.parse(reader);
			}

			//ensure the resource's json is a 'JsonObject'
			if(!jsonElement.isJsonObject()) continue;
			final var json = jsonElement.getAsJsonObject();

			//read/parse defined bindings from the json
			final var bindings_root    = parseBindings(json, entry.getKey());                    //binding definitions found in json's root
			final var bindings_perPass = new LinkedHashMap<Integer, List<ShaderboardBinding>>(); //binding definitions found in individual post-effect passes

			//in addition to bindings declared in the json's root, each post-effect's
			//"pass" can have its own bindings defined as well. we read and parse
			//those "per pass" bindings here
			int passCount = 0;
			if(json.has(JSONKEY_PASSES) && json.get(JSONKEY_PASSES).isJsonArray())
			{
				//obtain vanilla passes for the post-effect
				final var passes = json.getAsJsonArray(JSONKEY_PASSES);
				passCount = passes.size();

				//iterate each pass, and read/parse its custom bindings if any
				for(int i = 0; i < passCount; i++) {
					//skip 'pass' if it's not a 'JsonObject'
					if(!passes.get(i).isJsonObject()) continue;
					//read/parse its custom bindings for this mod, if any
					final var passBindings = parseBindings(passes.get(i).getAsJsonObject(), entry.getKey());
					//put found bindings into the map
					if(!passBindings.isEmpty()) bindings_perPass.put(i, passBindings);
				}
			}

			//if we found no custom bindings defined anywhere, we can just skip
			//to the next post-effect then
			if(bindings_root.isEmpty() && bindings_perPass.isEmpty()) continue;

			// ---------- merge the collected bindings into one unified map
			final var effectId        = POST_CHAIN_ID_CONVERTER.fileToId(entry.getKey()); //ex. "minecraft:blur"
			final var bindings_merged = new HashMap<Integer, List<ShaderboardBinding>>(); //map for merging bindings

			//iterate passes, merge bindings for each pass
			for(int i = 0; i < passCount; i++)
			{
				//prepare
				final var passBindings = new ArrayList<>(bindings_root);
				final var overrides    = bindings_perPass.get(i);
				//merge
				if(overrides != null)       passBindings.addAll(overrides);
				//submit
				if(!passBindings.isEmpty()) bindings_merged.put(i, passBindings);
			}

			//after collection of bindings is complete, we submit them to the
			//collection of collected bindings
			if(!bindings_merged.isEmpty())
				collected.put(effectId, bindings_merged);
		}
		catch (Exception e) {
			LOGGER.error(
					"Failed to scan post-effect JSON resource '{}' for '{}' definitions",
					entry.getKey(), JSONKEY_BINDINGS, e);
		}

		//finally, reassign the main map
		BINDINGS = Collections.unmodifiableMap(collected);
	}
	// --------------------------------------------------
	/**
	 * Reads a {@link #JSONKEY_BINDINGS} {@link JsonArray} from a {@link JsonObject},
	 * if present.
	 * @param postEffectJson A post-effect's resource JSON data.
	 * @param resourceId ID of the resource the JSON came from. Used solely for logging.
	 * @throws NullPointerException If the argument is {@code null}.
	 */
	private static @NotNull List<ShaderboardBinding> parseBindings(
			@NotNull JsonObject postEffectJson,
			@NotNull Identifier resourceId) throws NullPointerException
	{
		//not-null requirements
		Objects.requireNonNull(postEffectJson);

		//obtain the json array for bindings, and ensure it is an array
		if(!postEffectJson.has(JSONKEY_BINDINGS)) return List.of();
		final var json_bindings = postEffectJson.get(JSONKEY_BINDINGS);
		if(!json_bindings.isJsonArray()) return List.of();

		//parse defined bindings individually, then return them
		final var result = new ArrayList<ShaderboardBinding>();
		for(final var json_binding : json_bindings.getAsJsonArray()) {
			ShaderboardBinding.CODEC.parse(JsonOps.INSTANCE, json_binding)
					.resultOrPartial(message -> LOGGER.error("Failed to parse bindings for '{}' because: {}", resourceId, message))
					.ifPresent(result::add);
		}
		return result;
	}
	// ==================================================
	/**
	 * Expands a vanilla's {@link PostChainConfig} by injecting one uniform
	 * block per declared {@link ShaderboardBinding} into each pass.
	 * @param effectId The post-effect's {@link Identifier}.
	 * @param config The vanilla-parsed {@link PostChainConfig}.
	 * @return A new config with the binding groups added, or the original config
	 *         if the post-effect has no bindings.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	@ApiStatus.Internal
	public static @NotNull PostChainConfig loadPostChainCallback(
			@NotNull Identifier effectId, @NotNull PostChainConfig config)
			throws NullPointerException
	{
		//not-null requirements
		Objects.requireNonNull(effectId);
		Objects.requireNonNull(config);

		//if there are no bindings for this post-effect, there is nothing to do
		final var effectBindings = BINDINGS.get(effectId);
		if(effectBindings == null || effectBindings.isEmpty())
			return config;

		//inject binding uniform groups into each pass that declares bindings
		boolean changed = false;
		final var newPasses = new ArrayList<PostChainConfig.Pass>(config.passes().size());
		for(int i = 0; i < config.passes().size(); i++)
		{
			//obtain this pass and its bindings
			final var pass         = config.passes().get(i);
			final var passBindings = effectBindings.get(i);
			if(passBindings == null || passBindings.isEmpty()) {
				newPasses.add(pass);
				continue;
			}

			//copy the pass's uniform groups, and add ours on top
			final var uniforms = new HashMap<>(pass.uniforms());
			for(final var binding : passBindings)
			{
				//never clobber a vanilla uniform group that happens to share the name
				if(uniforms.containsKey(binding.getUniformName())) {
					LOGGER.warn(
							"Post-effect '{}' already has a uniform group named '{}'; skipping binding.",
							effectId, binding.getUniformName());
					continue;
				}
				uniforms.put(binding.getUniformName(), List.of(FloatUniformValue.INSTANCE));
				changed = true;
			}

			//rebuild the pass with the augmented uniform map
			newPasses.add(new PostChainConfig.Pass(pass.vertexShaderId(), pass.fragmentShaderId(), pass.inputs(), pass.outputTarget(), uniforms));
		}

		//return the result depending on if a change took place
		if(!changed) return config;
		return new PostChainConfig(config.internalTargets(), newPasses);
	}
	// --------------------------------------------------
	/**
	 * Writes the current value of every {@link ShaderboardBinding} belonging to
	 * the post-effect a pass belongs to into that pass's uniform buffers.
	 * <p>
	 * Invoked once per pass per frame, right before the pass is submitted to the
	 * frame graph. The GPU writes are queued through the device's command
	 * encoder, exactly like vanilla's {@code GlobalSettingsUniform}.
	 * @param passName The pass's name, formatted as {@code <post-effect-id>/<pass-index>}.
	 * @param customUniforms The pass's uniform group buffers, keyed by group name.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	@ApiStatus.Internal
	public static void updatePostPassBindingsCallback(
			@NotNull String passName, @NotNull Map<String, GpuBuffer> customUniforms)
			throws NullPointerException
	{
		//not-null requirements
		Objects.requireNonNull(passName);
		Objects.requireNonNull(customUniforms);
		if(customUniforms.isEmpty()) return;

		//split "<post-effect-id>/<pass-index>" into its two parts
		final int lastSlash = passName.lastIndexOf('/');
		if(lastSlash <= 0 || lastSlash == passName.length() - 1) return;
		final var effectId = Identifier.tryParse(passName.substring(0, lastSlash));
		if(effectId == null) return;
		final int passIndex;
		passIndex = Integer.parseInt(passName.substring(lastSlash + 1));

		//obtain the bindings for this specific pass
		final var effectBindings = BINDINGS.get(effectId);
		if(effectBindings == null) return;
		final var passBindings = effectBindings.get(passIndex);
		if(passBindings == null || passBindings.isEmpty()) return;

		//write each binding's resolved value into its uniform buffer
		for(final var binding : passBindings)
		{
			var buffer = customUniforms.get(binding.getUniformName());
			if(buffer == null) continue;

			//vanilla creates these buffers with 'USAGE_UNIFORM' only, making them
			//impossible to update after creation. swap in a buffer we can write to
			if((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0) {
				buffer = RenderSystem.getDevice().createBuffer(
						() -> "Shaderboard dynamic binding",
						GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
						4L);
				customUniforms.put(binding.getUniformName(), buffer);
			}

			//resolve and upload the value
			try(final var stack = MemoryStack.stackPush()) {
				//a binding is a single std140 float, so exactly 4 bytes
				final var data = Std140Builder.onStack(stack, 4).putFloat(binding.resolve()).get();
				RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
			}
		}
	}
	// ==================================================
}
