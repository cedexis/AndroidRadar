package com.cedexis.radar.android;

import java.util.Map;

import com.cedexis.radar.java.CustomerData;
import com.cedexis.radar.java.RadarService;
import com.cedexis.radar.java.DeviceType;
import com.cedexis.radar.java.Sampler;
import com.cedexis.radar.java.VersionedSampler;

public class RadarServiceProvider implements IRadarServiceProvider {

	private IDownloadersProvider downloadersProvider = new DownloadersProvider();
	
	@Override
	public void doRadarSession(CustomerData customer,
			VersionedSampler versionedSampler, DeviceType deviceType,
			String impact, Map<String, String> initHeaders) {
		RadarService
			.performRadarSession(
				customer,
				new VersionedSampler(
					Sampler.ANDROID,
					2,
					0),
				deviceType,
				impact,
				initHeaders,
				downloadersProvider.createSimpleDownloader(),
				downloadersProvider.createTimingDownloader(6000));
	}

}
