package com.cedexis.mobileradarlib.rum;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Application;
import android.util.Log;

import com.cedexis.mobileradarlib.DeviceStateChecker;
import com.cedexis.mobileradarlib.IPostReportHandler;
import com.cedexis.mobileradarlib.InitHandler;
import com.cedexis.mobileradarlib.InitResult;
import com.cedexis.mobileradarlib.ReportHandler;

public class RadarRUMSession {
    
    private static final String TAG = "RadarRUMSession";
    private int _currentReportId = 1;
    private Queueing _queueing;
    
    public RadarRUMSession(Queueing queueing) {
        this._queueing = queueing;
    }
    
    public static RadarRUMSession createSession(Application app,
            long appStartTime, int zoneId, int customerId) {
        return RadarRUMSession.createSession(
                app,
                appStartTime,
                zoneId,
                customerId,
                "init.cedexis-radar.net",
                "report.init.cedexis-radar.net",
                null,
                null);
    }
    
    public static RadarRUMSession createSession(Application app,
            long appStartTime, int zoneId, int customerId,
            String agentName,
            String agentVersion) {
        return RadarRUMSession.createSession(
                app,
                appStartTime,
                zoneId,
                customerId,
                "init.cedexis-radar.net",
                "report.init.cedexis-radar.net",
                agentName,
                agentVersion);
    }
    
    public static RadarRUMSession createSession(Application app,
            long appStartTime, int zoneId, int customerId,
            String initHost, String reportHost,
            String agentName,
            String agentVersion) {
        RadarRUMSession result = new RadarRUMSession(
                new Queueing(
                        app,
                        appStartTime,
                        zoneId,
                        customerId,
                        initHost,
                        reportHost,
                        agentName,
                        agentVersion,
                        new ArrayList<IPostReportHandler>()));
        
        result.reportEvent("appStart", 0, appStartTime);
        return result;
    }
    
    /**
     * The last used report id
     * @return
     */
    public int getCurrentReportId() {
        return this._currentReportId;
    }
    
    /**
     * Enable caller to register a function to be called after a report is sent.
     * 
     * @param handler
     */
    public void addPostReportHandler(IPostReportHandler handler) {
        this._queueing.getPostReportHandlers().add(handler);
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
     * Queue a RUM event report to be sent (without tags)
     * 
     * @param eventName
     * @return The id that was generated to be sent with the report
     */
    public int reportEvent(String eventName) {
        return this.reportEvent(eventName, 0);
    }
    
    /**
     * Queue a RUM event report to eb sent
     * 
     * @param eventName
     * @param tag
     * @return The id that was generated to be sent with the report
     */
    public int reportEvent(String eventName, long tag) {
        return this.reportEvent(eventName, tag, new Date().getTime());
    }
    
    /**
     * 
     * @param eventName
     * @param tag
     * @param timestamp
     * @return
     */
    public int reportEvent(String eventName, long tag, long timestamp) {
        Log.d(TAG, String.format(
                "Received request to report event \"%s\", tag %d, timestamp %d",
                eventName,
                tag,
                timestamp));
        int reportId = this._currentReportId++;
        RUMData data = new RUMEvent(
                reportId,
                eventName,
                tag,
                timestamp);
        this._queueing.reportRUMObject(data);
        return reportId;
    }
    
    public void reportSetProperty(String name, String value) {
        this.reportSetProperty(0, name, value);
    }
    
    public void reportSetProperty(int reportId, String name, String value) {
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
                    "RUMMetadata (%s, %s, %d)",
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
        private long _tag;
        
        public RUMEvent(int reportId, String eventName, long tag, long timestamp) {
            super(reportId, timestamp);
            this._eventName = eventName;
            this._tag = tag;
        }
        
        public String getEventName() {
            return this._eventName;
        }
        
        public long getTag() {
            return this._tag;
        }
        
        @Override
        public String toString() {
            return String.format(
                    Locale.getDefault(),
                    "RUMEvent (%d, %s, %d, %d)",
                    this.getReportId(),
                    this._eventName,
                    this._tag,
                    this.getTimestamp());
        }

        @Override
        public List<String> getReportElements(String requestSignature) {
            List<String> result = new ArrayList<String>();
            result.add("r1");
            result.add(String.format("%d", this.getReportId()));
            result.add(this.getEventName());
            result.add(String.format("%d", this.getTag()));
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
    
    static class Queueing {

        private ExecutorService _threadPool;
        private Future<InitResult> _futureInit;
        private InitResult _initResult;
        private Queue<RUMData> _preInitQueue;
        private Timer _initTimer;
        private List<IPostReportHandler> _postReportHandlers;
        
        private int _zoneId;
        private int _customerId;
        private String _initHost;
        private String _reportHost;
        private String _agentName;
        private String _agentVersion;
        
        private Application _app;
        
        public Queueing(Application app, long appStartTime, int zoneId,
            int customerId, String initHost, String reportHost,
            String agentName,
            String agentVersion,
            List<IPostReportHandler> postReportHandlers) {
            this._app = app;
            this._zoneId = zoneId;
            this._customerId = customerId;
            this._initHost = initHost;
            this._reportHost = reportHost;
            this._agentName = agentName;
            this._agentVersion = agentVersion;
            this._preInitQueue = new LinkedList<RUMData>();
            this._postReportHandlers = postReportHandlers;
        }
        
        public List<IPostReportHandler> getPostReportHandlers() {
            return this._postReportHandlers;
        }
        
        public void reportRUMObject(RUMData rumObject) {
            if (null == this._futureInit) {
                // This is the first report. We initiate the init request here and
                // enqueue the report data
                this._preInitQueue.add(rumObject);
                this.beginInit();
            }
            else if (null == this._initResult) {
                // The init request has already been initiated, but hasn't
                // completed.
                Log.d(TAG, "Enqueueing report: " + rumObject);
                this._preInitQueue.add(rumObject);
            }
            else {
                // The init request has completed.
                // Just in case there are objects in the queue, we'll send their
                // reports now.
                while (!this._preInitQueue.isEmpty()) {
                    this.sendReport(this._preInitQueue.remove());
                }
                
                // Send the current event report.
                this.sendReport(rumObject);
            }
        }
        
        private void beginInit() {
            if (DeviceStateChecker.okToMeasure(this._app)) {
                Log.d(TAG, "Submitting InitHandler to the thread pool");
                this._futureInit = this.getThreadPool()
                    .submit(new InitHandler(
                        this._zoneId, this._customerId, this._initHost));
                this.waitForInit();
            }
            else {
                // This is normal when the app is first run because we don't know
                // the connectivity & battery state yet.
                this.rescheduleInit(10000);
            }
        }
        
        private void waitForInit() {
            try {
                this._initResult = this._futureInit.get(0, TimeUnit.MILLISECONDS);
                Log.d(TAG, "waitForInit got result: " + this._initResult);
                
                if (null != this._initTimer) {
                    this._initTimer.cancel();
                    this._initTimer.purge();
                    this._initTimer = null;
                }
                
                if (null == this._initResult) {
                    // There was a problem with the init.  We'll wait a moment and
                    // then try again
                    this.rescheduleInit(5000);
                }
                else {
                    // Send any enqueued reports
                    while (!this._preInitQueue.isEmpty()) {
                        this.sendReport(this._preInitQueue.remove());
                    }
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }
            catch (TimeoutException e) {
                Log.d(TAG, "Init timeout");
                // Schedule a task to wait before trying again
                if (null == this._initTimer) {
                    this._initTimer = new Timer();
                }
                this._initTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        waitForInit();
                    }
                }, 1000);
            }
        }

        private void sendReport(RUMData data) {
            Log.d(TAG, String.format("Sending %s", data.toString()));
            ReportHandler handler = new ReportHandler(
                    data,
                    this._reportHost,
                    this._agentName,
                    this._agentVersion,
                    this._initResult.getRequestSignature(),
                    this._postReportHandlers);
            this._threadPool.execute(handler);
        }
        
        private void rescheduleInit(int delay) {
            if (null != this._initTimer) {
                this._initTimer.cancel();
                this._initTimer.purge();
            }
            this._initTimer = new Timer();
            this._initTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    beginInit();
                }
            }, delay);
        }
        
        private ExecutorService getThreadPool() {
            if (null == this._threadPool) {
                this._threadPool = Executors.newSingleThreadExecutor();
            }
            return this._threadPool;
        }
    }
}
