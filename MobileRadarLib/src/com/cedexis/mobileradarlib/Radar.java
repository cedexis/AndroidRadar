package com.cedexis.mobileradarlib;

import java.util.ArrayList;
import java.util.Date;

import android.app.Application;

import com.cedexis.mobileradarlib.http.RadarHttpSessionManager;
import com.cedexis.mobileradarlib.rum.Queueing;
import com.cedexis.mobileradarlib.rum.RadarRUMSession;

public class Radar {
    
    private RadarRUMSession _rumSession;
    private RadarHttpSessionManager _httpSessionMgr;
    
    public Radar(RadarRUMSession rumSession, RadarHttpSessionManager httpSessionMgr) {
        this._rumSession = rumSession;
        this._httpSessionMgr = httpSessionMgr;
    }
    
    public static Radar createRadar(
            Application app,
            int zoneId,
            int customerId,
            String agentName,
            String agentVersion,
            long appStartTime) {
        
        Queueing queueing =
                new Queueing(
                    app,
                    zoneId,
                    customerId,
                    "init.cedexis-radar.net",
                    "report.init.cedexis-radar.net",
                    agentName,
                    agentVersion,
                    new ArrayList<IPostReportHandler>());
        RadarRUMSession rum = new RadarRUMSession(queueing);
        
        RadarHttpSessionManager http = new RadarHttpSessionManager(
                app,
                zoneId,
                customerId,
                "init.cedexis-radar.net",
                "report.init.cedexis-radar.net",
                "probes.cedexis.com",
                agentName,
                agentVersion);
        
        return new Radar(rum, http);
    }
    
    public void reportSliceStart(String sliceName) {
        this._rumSession.reportSliceStart(sliceName);
    }
    
    public void reportSliceEnd(String sliceName) {
        this._rumSession.reportSliceEnd(sliceName);
    }
    
    public int reportEvent(String eventName) {
        return this.reportEvent(eventName, 0);
    }
    
    public int reportEvent(String eventName, long tags) {
        return this.reportEvent(eventName, tags, new Date().getTime());
    }
    
    public int reportEvent(String eventName, long tags, long timestamp) {
        return this._rumSession.reportEvent(eventName, tags, timestamp);
    }
    
    public void reportSetProperty(String propertyName, String propertyValue) {
        this.reportSetProperty(propertyName, propertyValue, 0);
    }
    
    public void reportSetProperty(String propertyName, String propertyValue, int reportId) {
        this._rumSession.reportSetProperty(propertyName, propertyValue, reportId);
    }
    
    public int getLastRUMReportId() {
        return this._rumSession.getLastReportId();
    }
    
    public void addPostReportHandler(IPostReportHandler handler) {
        this._rumSession.addPostReportHandler(handler);
        this._httpSessionMgr.addPostReportHandler(handler);
    }
    
    public void removePostReportHandler(IPostReportHandler handler) {
        this._rumSession.removePostReportHandler(handler);
        this._httpSessionMgr.removePostReportHandler(handler);
    }
    
    public void queueHttpSession() {
        this._httpSessionMgr.queueSession();
    }
}
