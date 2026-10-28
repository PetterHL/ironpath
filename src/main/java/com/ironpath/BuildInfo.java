package com.ironpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Bundled build metadata also works on the unpacked development run classpath. */
final class BuildInfo
{
	private static final Properties METADATA=read();
	static final String VERSION=value("version","[0-9A-Za-z.-]{1,40}","unknown");
	static final String REVISION=value("revision","[0-9a-f]{40}","unknown");
	static final String WORKTREE=value("worktree","CLEAN|DIRTY|UNKNOWN","UNKNOWN");
	private BuildInfo() { }
	private static String value(String key,String pattern,String fallback)
	{ String value=METADATA.getProperty(key,""); return value.matches(pattern)?value:fallback; }
	private static Properties read()
	{
		Properties properties=new Properties();
		try(InputStream input=BuildInfo.class.getResourceAsStream("/com/ironpath/build.properties"))
		{
			if(input!=null) { properties.load(input); }
		}
		catch(IOException ex) { /* Unknown provenance is explicit, never guessed. */ }
		return properties;
	}
}
