package com.cedexis.radar.android;

import com.cedexis.radar.java.IHttpDownloader;
import com.cedexis.radar.java.IHttpTimingDownloader;

public interface IDownloadersProvider {
	IHttpDownloader createSimpleDownloader();
	IHttpTimingDownloader createTimingDownloader(int readTimeout);
}
