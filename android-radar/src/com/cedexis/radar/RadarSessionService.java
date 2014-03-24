package com.cedexis.radar;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cedexis.radar.service.CustomerData;
import com.cedexis.radar.service.data.DeviceType;
import com.cedexis.radar.service.data.Sampler;
import com.cedexis.radar.service.data.VersionedSampler;

public class RadarSessionService extends Service implements Runnable {

	private static final String TAG = "RadarSessionService";
	
	private CustomerData customer = null;
	private String impact = null;
	private ISystemServiceProvider systemServiceProvider = new SystemServiceProvider(this);
	private IRadarServiceProvider radarServiceProvider = new RadarServiceProvider();
	
	public void setCustomer(CustomerData value) {
		this.customer = value;
	}
	
	public void setSystemServiceProvider(ISystemServiceProvider value) {
		systemServiceProvider = value;
	}
	
	public int getZoneId() {
		return customer.getZoneId();
	}

	public int getCustomerId() {
		return customer.getCustomerId();
	}

	public String getCustomerName() {
		return customer.getCustomerName();
	}

	public String getImpactValue() {
		return impact;
	}
	
	public void setRadarServiceProvider(IRadarServiceProvider value) {
		this.radarServiceProvider = value;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setupForStartCommand(intent);
		
		// Do the Radar session on worker thread
		new Thread(this).start();
		return START_NOT_STICKY;
	}

	public void setupForStartCommand(Intent intent) {
		// Get customer data from the intent
		int zoneId = intent.getIntExtra("zoneId", -1);
		int customerId = intent.getIntExtra("customerId", -1);
		String customerName = null;
		if (intent.hasExtra("customerName")) {
			customerName = intent.getStringExtra("customerName");
		}
		if (intent.hasExtra("impact")) {
			impact = intent.getStringExtra("impact");
		}
		Log.d(TAG, String.format("Zone Id: %d", zoneId));
		Log.d(TAG, String.format("Customer Id: %d", customerId));
		Log.d(TAG, "Customer name: " + customerName);
		Log.d(TAG, String.format("Impact: %s", impact));
		customer = new CustomerData(zoneId, customerId, customerName);
	}

	@Override
	public void run() {
		doRun();
		stopSelf();
	}

	public void doRun() {
		// We're hard-coding the device type here, but really we'd like to
		// derive it from device properties and capabilities that can be
		// learned through the Android API.
		DeviceType deviceType = DeviceType.MOBILE;
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("x-radar-android-device", android.os.Build.DEVICE);
		headers.put("x-radar-android-model", android.os.Build.MODEL);
		headers.put("x-radar-android-product", android.os.Build.PRODUCT);
		headers.put("x-radar-android-os-version", System.getProperty("os.version"));
		headers.put("x-radar-android-os-api", String.valueOf(android.os.Build.VERSION.SDK_INT));
		
		// Try to determine the network type
		ConnectivityManager connectivityManager = systemServiceProvider.getConnectivityManager();
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (null != networkInfo) {
			headers.put("x-radar-android-network-type", networkInfo.getTypeName());
		}
		
		TelephonyManager telephonyManager = systemServiceProvider.getTelephonyManager();
		headers.put("x-radar-android-network-subtype", String.valueOf(telephonyManager.getNetworkType()));
		
		this.radarServiceProvider.doRadarSession(
			customer,
			new VersionedSampler(Sampler.ANDROID, 2, 0),
			deviceType,
			impact,
			headers);
	}

}
