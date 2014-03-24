package com.cedexis.radar;

import java.util.Map;

import com.cedexis.radar.service.CustomerData;
import com.cedexis.radar.service.data.DeviceType;
import com.cedexis.radar.service.data.VersionedSampler;

public interface IRadarServiceProvider {
	void doRadarSession(CustomerData customer,
			VersionedSampler versionedSampler, DeviceType deviceType,
			String impact, Map<String, String> headers);
}
