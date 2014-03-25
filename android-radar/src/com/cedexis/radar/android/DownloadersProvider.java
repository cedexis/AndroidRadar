package com.cedexis.radar.android;

import com.cedexis.radar.java.HttpDownloader;
import com.cedexis.radar.java.HttpTimingDownloader;
import com.cedexis.radar.java.IHttpDownloader;
import com.cedexis.radar.java.IHttpTimingDownloader;

public class DownloadersProvider implements IDownloadersProvider {

	@Override
	public IHttpDownloader createSimpleDownloader() {
		return new HttpDownloader();
	}

	@Override
	public IHttpTimingDownloader createTimingDownloader(int readTimeout) {
		return new HttpTimingDownloader(readTimeout);
	}

}
