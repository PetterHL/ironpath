package com.ironpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Bundled build metadata also works on the unpacked development run classpath. */
final class BuildInfo
{
	static final String VERSION=readVersion();
	private BuildInfo() { }
	private static String readVersion()
	{
		try(InputStream input=BuildInfo.class.getResourceAsStream("/com/ironpath/build.properties"))
		{
			if(input==null) { return "unknown"; }
			Properties properties=new Properties(); properties.load(input);
			return properties.getProperty("version","unknown");
		}
		catch(IOException ex) { return "unknown"; }
	}
}
