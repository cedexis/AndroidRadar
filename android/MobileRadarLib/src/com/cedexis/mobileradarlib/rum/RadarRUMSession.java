package com.cedexis.mobileradarlib.rum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import com.cedexis.mobileradarlib.IPostReportHandler;

public class RadarRUMSession {
    
    private static String TAG = "RadarRUMSession";
    private int _lastReportId = 0;
    private Queueing _queueing;
    
    public RadarRUMSession(Queueing queueing) {
        this._queueing = queueing;
    }
    
    /**
     * The last used report id
     * @return
     */
    public int getLastReportId() {
        return this._lastReportId;
    }
    
    /**
     * Enable caller to register a function to be called after a report is sent.
     * 
     * @param handler
     */
    public void addPostReportHandler(IPostReportHandler handler) {
        List<IPostReportHandler> handlers = this._queueing.getPostReportHandlers();
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }
    
    /**
     * Remove the callback registered using RadarRUMSession.addPostReportHandler.
     * 
     * @param handler
     */
    public void removePostReportHandler(IPostReportHandler handler) {
        this._queueing.getPostReportHandlers().remove(handler);
    }
    
    /**
     * Start a new RUM slice.
     * 
     * @param sliceName
     */
    public void reportSliceStart(String sliceName) {
        this.reportSlice(sliceName, true);
    }
    
    /**
     * End the most recently started RUM slice by the same name.
     * 
     * @param sliceName
     */
    public void reportSliceEnd(String sliceName) {
        this.reportSlice(sliceName, false);
    }
    
    protected void reportSlice(String sliceName, boolean start) {
        Log.d(TAG, String.format(
                "Received request to %s slice \"%s\"",
                start ? "start" : "end",
                sliceName));
        RUMData data = new RUMSlice(
                sliceName,
                new Date().getTime(),
                start);
        this._queueing.reportRUMObject(data);
    }
    
    /**
     * 
     * @param eventName
     * @param tags
     * @param timestamp
     * @return
     */
    public int reportEvent(String eventName, long tags, long timestamp) {
        int reportId = ++this._lastReportId;
        RUMData data = new RUMEvent(
                reportId,
                eventName,
                tags,
                timestamp);
        this._queueing.reportRUMObject(data);
        return reportId;
    }
    
    public void reportSetProperty(String name, String value) {
        this.reportSetProperty(name, value, 0);
    }
    
    public void reportSetProperty(String name, String value, int reportId) {
        Log.d(TAG, String.format(
                "Received request to set property \"%s\" to \"%s\" for report id %d",
                name,
                value,
                reportId));
        RUMData data = new RUMMetadata(
                reportId,
                name,
                value,
                new Date().getTime()); 
        this._queueing.reportRUMObject(data);
    }
    
    static class RUMMetadata extends RUMReport {
        private String _name;
        private String _value;
        
        public RUMMetadata(int reportId, String name, String value, long timestamp) {
            super(reportId, timestamp);
            this._name = name;
            this._value = value;
        }
        
        public String getName() {
            return this._name;
        }
        
        public String getValue() {
            return this._value;
        }
        
        @Override
        public String toString() {
            return String.format(
                    Locale.getDefault(),
                    "RUMMetadata (%d, %s, %s, %d)",
                    this.getReportId(),
                    this._name,
                    this._value,
                    this.getTimestamp());
        }

        @Override
        public List<String> getReportElements(String requestSignature) {
            List<String> result = new ArrayList<String>();
            result.add("r2");
            result.add(String.format("%d", this.getReportId()));
            result.add(this.getName());
            result.add(this.getValue());
            result.add(String.format("%d", this.getTimestamp()));
            result.add(requestSignature);
            return result;
        }
    }
    
    static class RUMEvent extends RUMReport {
        private String _eventName;
        private long _tags;
        
        public RUMEvent(int reportId, String eventName, long tags, long timestamp) {
            super(reportId, timestamp);
            this._eventName = eventName;
            this._tags = tags;
        }
        
        @Override
        public String toString() {
            return String.format(
                    Locale.getDefault(),
                    "RUMEvent (%d, %s, %d, %d)",
                    this.getReportId(),
                    this._eventName,
                    this._tags,
                    this.getTimestamp());
        }

        @Override
        public List<String> getReportElements(String requestSignature) {
            List<String> result = new ArrayList<String>();
            result.add("r1");
            result.add(String.format("%d", this.getReportId()));
            result.add(this._eventName);
            result.add(String.format("%d", this._tags));
            result.add(String.format("%d", this.getTimestamp()));
            result.add(requestSignature);
            return result;
        }
    }
    
    class RUMSlice extends RUMData {
        private String _sliceName;
        private boolean _start;
        
        public RUMSlice(String sliceName, long timestamp, boolean isStartOfSlice) {
            super(timestamp);
            this._sliceName = sliceName;
            this._start = isStartOfSlice;
        }
        
        public String getSliceName() {
            return this._sliceName;
        }
        
        public boolean isStartOfSlice() {
            return this._start;
        }
        
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "RUMSlice (%s, %d, %b)",
                this._sliceName, this.getTimestamp(), this._start);
        }

        @Override
        public List<String> getReportElements(String requestSignature) {
            List<String> result = new ArrayList<String>();
            result.add("r3");
            result.add(this.getSliceName());
            result.add(String.format("%d", this.isStartOfSlice() ? 1 : 0));
            result.add(String.format("%d", this.getTimestamp()));
            result.add(requestSignature);
            return result;
        }
    }
}
