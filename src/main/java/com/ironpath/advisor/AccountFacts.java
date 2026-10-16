package com.ironpath.advisor;

import com.ironpath.model.*;
import com.ironpath.preferences.Preferences;

/** Readable account facts take precedence over explicit planning corrections. */
public final class AccountFacts
{
	private AccountFacts() { }
	public static Truth planning(AccountSnapshot account, Preferences preferences, String key)
	{
		Observation<Boolean> live=account.fact(key);
		if(live.known()) { return Truth.of(live); }
		Preferences.Confirmation manual=preferences.confirmations.get(key);
		return manual==null?Truth.UNKNOWN:manual.value?Truth.TRUE:Truth.FALSE;
	}
}
