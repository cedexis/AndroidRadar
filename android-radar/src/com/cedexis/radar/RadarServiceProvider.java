package com.cedexis.radar;

import java.util.Map;

import com.cedexis.radar.service.CustomerData;
import com.cedexis.radar.service.RadarService;
import com.cedexis.radar.service.data.DeviceType;
import com.cedexis.radar.service.data.Sampler;
import com.cedexis.radar.service.data.VersionedSampler;

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
