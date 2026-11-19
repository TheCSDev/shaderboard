package com.thecsdev.shaderboard.client.binding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thecsdev.shaderboard.api.client.binding.ShaderboardBinding;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueType;
import com.thecsdev.shaderboard.api.client.binding.value.ShaderboardValueTypes;
import com.thecsdev.shaderboard.client.registry.ShaderboardBindingTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.jspecify.annotations.NonNull;

/**
 * {@link ShaderboardBinding} implementation used for binding {@link Minecraft} client's
 * cursor-related variables to applicable shaders.
 * @since 2.5.0
 */
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class SBinding_CursorInfo extends ShaderboardBinding<SBinding_CursorInfo.Value>
{
	// ================================================== ==================================================
	//                                SBinding_CursorInfo IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * {@link MapCodec} implementation for serializing {@link SBinding_CursorInfo}
	 * instances.
	 */
	public static final MapCodec<SBinding_CursorInfo> CODEC = RecordCodecBuilder.mapCodec(instance ->
			ShaderboardBinding.baseCodec(instance)
					.apply(instance, SBinding_CursorInfo::new)
	);

	/**
	 * {@link ShaderboardValueType} implementation returned by {@link #getValueType()}.
	 */
	private static final ShaderboardValueType<Value> VALUE_TYPE = ShaderboardValueTypes.<Value>struct()
			.member(ShaderboardValueTypes.INT,  Value::isGrabbed)
			.member(ShaderboardValueTypes.VEC2, Value::normalPosition)
			.member(ShaderboardValueTypes.VEC2, Value::scaledPosition)
			.member(ShaderboardValueTypes.VEC2, Value::unscaledPosition)
			.build();
	// ==================================================
	private final @NotNull Value value = new Value();
	// ==================================================
	public SBinding_CursorInfo(@NotNull String uniformName) {
		super(ShaderboardBindingTypes.CURSOR_INFO, uniformName);
	}
	// ==================================================
	public final @Override @NotNull ShaderboardValueType<Value> getValueType() { return VALUE_TYPE; }
	public final @Override @NonNull Value getValue()
	{
		//prerequisites
		final var minecraft = Minecraft.getInstance();
		final var window    = minecraft.getWindow();
		final var mouse     = minecraft.mouseHandler;

		final int    width   = window.getScreenWidth();
		final int    height  = window.getScreenHeight();
		final double normalX = mouse.xpos() / width;
		final double normalY = mouse.ypos() / height;

		this.value.resetTo0();

		//set "is grabbed" and return if needed
		//(aka avoid dividing by invalid window dimensions)
		this.value.isGrabbed = mouse.isMouseGrabbed() ? 1 : 0;
		if(width <= 0 || height <= 0) return this.value;

		//set positions
		this.value.normalPosition.set(
				(float)normalX,
				(float)normalY);
		this.value.scaledPosition.set(
				(float)mouse.getScaledXPos(window),
				(float)mouse.getScaledYPos(window));
		this.value.unscaledPosition.set(
				(float)(normalX * window.getWidth()),
				(float)(normalY * window.getHeight()));

		//return the value
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
		public       int      isGrabbed        = 0;              //offset 0,  size 4
		public final Vector2f normalPosition   = new Vector2f(); //offset 4,  size 8
		public final Vector2f scaledPosition   = new Vector2f(); //offset 12, size 8
		public final Vector2f unscaledPosition = new Vector2f(); //offset 20, size 8
		// ------ padding ------                                 //offset 28, size 4 | Total struct size: 32 bytes
		// ==================================================
		public Value() { resetTo0(); /*useless call 101*/ }
		// ==================================================
		public final int isGrabbed() { return this.isGrabbed; }
		public final @NotNull Vector2fc normalPosition() { return this.normalPosition; }
		public final @NotNull Vector2fc scaledPosition() { return this.scaledPosition; }
		public final @NotNull Vector2fc unscaledPosition() { return this.unscaledPosition; }
		// --------------------------------------------------
		/** Resets all field values to 0. */
		public final void resetTo0() {
			this.isGrabbed = 0;
			this.normalPosition.zero();
			this.scaledPosition.zero();
			this.unscaledPosition.zero();
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
