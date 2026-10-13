package com.ironpath.model;

public enum Truth
{
	TRUE, FALSE, UNKNOWN;

	public static Truth of(Observation<Boolean> observation)
	{
		return !observation.known() ? UNKNOWN : observation.value() ? TRUE : FALSE;
	}

	public static Truth all(Truth... values)
	{
		boolean unknown = false;
		for (Truth value : values)
		{
			if (value == FALSE) { return FALSE; }
			unknown |= value == UNKNOWN;
		}
		return unknown ? UNKNOWN : TRUE;
	}

	public static Truth any(Truth... values)
	{
		boolean unknown = false;
		for (Truth value : values)
		{
			if (value == TRUE) { return TRUE; }
			unknown |= value == UNKNOWN;
		}
		return unknown ? UNKNOWN : FALSE;
	}
}
