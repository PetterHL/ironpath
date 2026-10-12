package com.ironpath;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Local development launcher from the official RuneLite template; not a game test. */
public class IronpathPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(IronpathPlugin.class);
		RuneLite.main(args);
	}
}
