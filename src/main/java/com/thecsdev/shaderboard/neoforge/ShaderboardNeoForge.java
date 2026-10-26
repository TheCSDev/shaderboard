package com.thecsdev.shaderboard.neoforge;

import com.thecsdev.shaderboard.Shaderboard;
import com.thecsdev.shaderboard.neoforge.client.ShaderboardNeoClient;
import com.thecsdev.shaderboard.neoforge.server.ShaderboardNeoServer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Shaderboard.MOD_ID)
public final class ShaderboardNeoForge
{
	// ==================================================
	public ShaderboardNeoForge()
	{
		//create an instance of the mod's main class, depending on the dist
		switch(FMLEnvironment.getDist())
		{
			case CLIENT           -> new ShaderboardNeoClient();
			case DEDICATED_SERVER -> new ShaderboardNeoServer();
		}
	}
	// ==================================================
}
