package com.cedexis.radar;

import com.cedexis.radar.service.HttpDownloader;
import com.cedexis.radar.service.HttpTimingDownloader;
import com.cedexis.radar.service.IHttpDownloader;
import com.cedexis.radar.service.IHttpTimingDownloader;

public class DownloadersProvider implements IDownloadersProvider {

	@Override
	public IHttpDownloader createSimpleDownloader() {
		return new HttpDownloader();
	}

	@Override
	public IHttpTimingDownloader createTimingDownloader() {
		return new HttpTimingDownloader();
	}

}
