package com.cedexis.radar.android;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.cedexis.radar.java.CustomerData;
import com.cedexis.radar.java.DeviceType;
import com.cedexis.radar.java.Sampler;
import com.cedexis.radar.java.VersionedSampler;

@RunWith(RobolectricTestRunner.class)
public class RadarSessionServiceTests {

	@Test
	public void setupForStartCommandTest_withCustomerName_withImpact() {
		Intent intent = new Intent();
		intent.putExtra("zoneId", 111);
		intent.putExtra("customerId", 222222);
		intent.putExtra("customerName", "Some Customer Inc.");
		intent.putExtra("impact", "abcXYZ123");
		RadarSessionService service = new RadarSessionService();
		service.setupForStartCommand(intent);
		
		assertEquals("Unexpected zone id", 111, service.getZoneId());
		assertEquals("Unexpected customer id", 222222, service.getCustomerId());
		assertEquals("Unexpected customer name", "Some Customer Inc.", service.getCustomerName());
		assertEquals("Unexpected impact value", "abcXYZ123", service.getImpactValue());
	}
	
	@Test
	public void processRun() {
		RadarSessionService service = new RadarSessionService();
		
		CustomerData customer = new CustomerData(111, 222, "Company ABC");
		service.setCustomer(customer);
		ISystemServiceProvider serviceProvider = mock(ISystemServiceProvider.class);
		service.setSystemServiceProvider(serviceProvider);
		ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
		NetworkInfo networkInfo = mock(NetworkInfo.class);
		TelephonyManager telephonyManager = mock(TelephonyManager.class);
		IRadarServiceProvider radarServiceProvider = mock(IRadarServiceProvider.class);
		service.setRadarServiceProvider(radarServiceProvider);
		
		// Expectations
		when(serviceProvider.getConnectivityManager())
			.thenReturn(connectivityManager);
		when(connectivityManager.getActiveNetworkInfo())
			.thenReturn(networkInfo);
		when(networkInfo.getTypeName())
			.thenReturn("some human-readable network type name");
		when(serviceProvider.getTelephonyManager())
			.thenReturn(telephonyManager);
		when(telephonyManager.getNetworkType())
			.thenReturn(12345);
		
		service.doRun();
		
		verify(radarServiceProvider)
			.doRadarSession(
				same(customer),
				eq(new VersionedSampler(Sampler.ANDROID, 2, 0)),
				same(DeviceType.MOBILE),
				isNull(String.class),
				anyMapOf(String.class, String.class));
	}

}
