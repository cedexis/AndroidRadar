package com.cedexis.radar;

import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

public interface ISystemServiceProvider {
	ConnectivityManager getConnectivityManager();
	TelephonyManager getTelephonyManager();
}
