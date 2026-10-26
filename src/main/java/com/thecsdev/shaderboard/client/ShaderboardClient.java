package com.thecsdev.shaderboard.client;

import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.api.client.registry.SClientRegistries;

/**
 * The main "client" entry-point for this mod, that is executed
 * by all loaders (fabric/neoforge).
 */
public class ShaderboardClient extends Shaderboard
{
	// ==================================================
	public ShaderboardClient()
	{
		//register features
		SClientRegistries.bootstrap();
	}
	// ==================================================
}
