package com.thecsdev.shaderboard.client.binding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBinding;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueType;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueTypes;
import com.thecsdev.shaderboard.client.registry.ShaderboardBindingTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * {@link ShaderboardBinding} implementation used for binding local player's attribute
 * values to applicable shaders.
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class SBinding_LocalPlayerAttribute extends ShaderboardBinding<SBinding_LocalPlayerAttribute.Value>
{
	// ================================================== ==================================================
	//                      SBinding_LocalPlayerAttribute IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_LocalPlayerAttribute}
	 * instances.
	 */
	public static final MapCodec<SBinding_LocalPlayerAttribute> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.and(Attribute.CODEC.fieldOf("attribute").forGetter(SBinding_LocalPlayerAttribute::getAttribute))
					.apply(instance, SBinding_LocalPlayerAttribute::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	private static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.FLOAT, Value::baseValue)
			.member(ShaderboardValueTypes.FLOAT, Value::value)
			.build();
	// ==================================================
	private final @NotNull Holder<Attribute> attribute;
	private final @NotNull Value             value = new Value();
	// ==================================================
	public SBinding_LocalPlayerAttribute(@NotNull String uniformName, @NotNull Holder<Attribute> attribute)
	{
		//initialize
		super(ShaderboardBindingTypes.LOCAL_PLAYER_ATTRIBUTE, uniformName);
		this.attribute = Objects.requireNonNull(attribute);

		//log warning if needed
		if(!attribute.value().isClientSyncable() && Shaderboard.getConfig().logBindingConfigWarnins())
			Shaderboard.LOGGER.warn(
					"Attribute '{}' is not syncable with client; value of '{}' may differ from one on the server-side.",
					attribute.getRegisteredName(),
					uniformName);
	}
	// ==================================================
	/**
	 * Returns the attribute this binding passes the value of.
	 */
	public final @NotNull Holder<Attribute> getAttribute() { return this.attribute; }
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//obtain the local player instance
		final @Nullable var player = Minecraft.getInstance().player;
		if(player == null) return this.value.resetTo0();

		//obtain the local player's attribute
		final @Nullable var attribute = player.getAttribute(this.attribute);
		if(attribute == null) return this.value.resetTo0();

		//obtain values and return
		this.value.baseValue = (float) attribute.getBaseValue();
		this.value.value     = (float) attribute.getValue();
		return this.value;
	}
	// ================================================== ==================================================
	//                                              Value IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Java-side view of the struct passed to GLSL.
	 * @implNote Intentionally mutable, to avoid allocating new object instance each frame.
	 */
	public static final @ApiStatus.Internal class Value
	{
		// ==================================================
		public float baseValue;
		public float value;
		// ==================================================
		public Value() { resetTo0(); }
		// ==================================================
		public final float baseValue() { return baseValue; }
		public final float value() { return value; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public final @NotNull Value resetTo0() {
			this.baseValue = 0;
			this.value     = 0;
			return this;
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
