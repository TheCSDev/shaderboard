package com.thecsdev.shaderboard.fabric;

import com.thecsdev.shaderboard.fabric.client.ShaderboardFabricClient;
import com.thecsdev.shaderboard.fabric.server.ShaderboardFabricServer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

public final class ShaderboardFabric implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer
{
	// ==================================================
	public final @Override void onInitializeClient() { new ShaderboardFabricClient(); }
	public final @Override void onInitializeServer() { new ShaderboardFabricServer(); }
	// --------------------------------------------------
	public final @Override void onInitialize() {}
	// ==================================================
}
