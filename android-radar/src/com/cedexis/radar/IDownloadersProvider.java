package com.cedexis.radar;

import com.cedexis.radar.service.IHttpDownloader;
import com.cedexis.radar.service.IHttpTimingDownloader;

public interface IDownloadersProvider {
	IHttpDownloader createSimpleDownloader();
	IHttpTimingDownloader createTimingDownloader(int readTimeout);
}
