package com.thecsdev.shaderboard.resource;

import com.thecsdev.shaderboard.Shaderboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.net.URI;
import java.util.Objects;

import static com.thecsdev.shaderboard.Shaderboard.MOD_ID;
import static net.minecraft.network.chat.Component.literal;

/**
 * {@link Shaderboard}'s language translation keys.
 */
@SuppressWarnings("removal")
public final class SLanguage
{
	// ==================================================
	/**
	 * Prefix {@link Component} used in command output texts.
	 */
	public static final Component CHAT_PREFIX;
	// ==================================================
	private SLanguage() {}
	static
	{
		//initialize command output prefix
		{
			//requirement
			final var sb = Objects.requireNonNull(Shaderboard.getInstance(), "Mod not initialized: " + MOD_ID);

			//create the hover event for the prefix
			final var hoverText = literal(sb.getModName()).withStyle(ChatFormatting.YELLOW)
					.append("\n")
					.append(literal(Shaderboard.getProperty("mod.description")).withStyle(ChatFormatting.DARK_GRAY));
			final var hoverEvent = new HoverEvent.ShowText(hoverText);

			//create the click event for the prefix
			final var clickEvent = new ClickEvent.OpenUrl(URI.create(Shaderboard.getProperty("mod.link.homepage")));

			//initialize main prefix text component
			final var prefix = literal("[" + sb.getModName() + "]");
			prefix.setStyle(prefix.getStyle()
					.applyFormat(ChatFormatting.YELLOW)
					.withHoverEvent(hoverEvent)
					.withClickEvent(clickEvent));
			CHAT_PREFIX = prefix;
		}
	}
	// --------------------------------------------------
	public static final void bootstrap() {/*invokes <clinit>*/}
	// ==================================================
}
