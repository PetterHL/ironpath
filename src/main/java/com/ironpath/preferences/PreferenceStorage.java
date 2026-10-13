package com.ironpath.preferences;

import java.io.IOException;

public interface PreferenceStorage
{
	Preferences read(String key) throws IOException;
	void write(String key, Preferences preferences) throws IOException;
}
