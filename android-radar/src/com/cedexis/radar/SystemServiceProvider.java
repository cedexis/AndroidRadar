package com.cedexis.radar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

public class SystemServiceProvider implements ISystemServiceProvider {

	private Context context;
	
	public SystemServiceProvider(Context context) {
		this.context = context;
	}

	@Override
	public ConnectivityManager getConnectivityManager() {
		return (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Override
	public TelephonyManager getTelephonyManager() {
		return (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	}

}
