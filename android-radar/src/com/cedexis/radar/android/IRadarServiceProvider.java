package com.cedexis.radar.android;

import java.util.Map;

import com.cedexis.radar.java.CustomerData;
import com.cedexis.radar.java.DeviceType;
import com.cedexis.radar.java.VersionedSampler;

public interface IRadarServiceProvider {
	void doRadarSession(CustomerData customer,
			VersionedSampler versionedSampler, DeviceType deviceType,
			String impact, Map<String, String> initHeaders);
}
