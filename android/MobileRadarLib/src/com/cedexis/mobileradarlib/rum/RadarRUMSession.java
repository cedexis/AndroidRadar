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
import com.cedexis.mobileradarlib.InitHandler;
import com.cedexis.mobileradarlib.InitResult;
import com.cedexis.mobileradarlib.ReportHandler;

public class RadarRUMSession {
    
    private static final String TAG = "RadarRUMSession";
    private ExecutorService _threadPool;
    private int _zoneId;
    private int _customerId;
    private String _initHost;
    private String _reportHost;
    private Future<InitResult> _futureInit;
    private InitResult _initResult;
    private Queue<IRUMObject> _preInitQueue;
    private Application _app;
    private Timer _initTimer;
    
    public RadarRUMSession(Application app, long appStartTime, int zoneId,
            int customerId) {
        this(app, appStartTime, zoneId, customerId, "init.cedexis-radar.net",
                "report.init.cedexis-radar.net");
    }
    
    public RadarRUMSession(Application app, long appStartTime, int zoneId,
            int customerId, String initHost, String reportHost) {
        this._zoneId = zoneId;
        this._customerId = customerId;
        this._initHost = initHost;
        this._reportHost = reportHost;
        this._preInitQueue = new LinkedList<IRUMObject>();
        this._preInitQueue.add(new RUMEvent("appStart", 0, appStartTime));
        this._app = app;
    }
    
    public void reportSliceStart(String sliceName) {
        Log.d(TAG, String.format("Received request to start slice \"%s\"", sliceName));
        this.reportRUMObject(new RUMSlice(sliceName, new Date().getTime(), true));
    }
    
    public void reportSliceEnd(String sliceName) {
        Log.d(TAG, String.format("Received request to end slice \"%s\"", sliceName));
        this.reportRUMObject(new RUMSlice(sliceName, new Date().getTime(), false));
    }
    
    public void reportEvent(String eventName) {
        this.reportEvent(eventName, 0);
    }
    
    public void reportEvent(String eventName, long tag) {
        Log.d(TAG, String.format("Received request to report event \"%s\", tag %d", eventName, tag));
        this.reportRUMObject(new RUMEvent(eventName, tag, new Date().getTime()));
    }
    
    public void reportSetProperty(String name, String value) {
        Log.d(TAG, String.format("Received request to set property \"%s\" to \"%s\"", name, value));
        this.reportRUMObject(new RUMMetadata(name, value, new Date().getTime()));
    }
    
    private void reportRUMObject(IRUMObject rumObject) {
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
                this._threadPool.execute(
                    this._preInitQueue.remove()
                        .createReportHandler(
                            this._initResult.getRequestSignature()));
            }
            
            // Send the current event report.
            this._threadPool.execute(
                rumObject.createReportHandler(
                    this._initResult.getRequestSignature()));
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
                    this._threadPool.execute(
                        this._preInitQueue.remove()
                            .createReportHandler(
                                this._initResult.getRequestSignature()));
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
                    RadarRUMSession.this.waitForInit();
                }
            }, 1000);
        }
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
                RadarRUMSession.this.beginInit();
            }
        }, delay);
    }
    
    private ExecutorService getThreadPool() {
        if (null == this._threadPool) {
            this._threadPool = Executors.newSingleThreadExecutor();
        }
        return this._threadPool;
    }
    
    interface IRUMObject {
        ReportHandler createReportHandler(String requestSignature);
    }
    
    class RUMMetadata implements IRUMObject {
        private String _name;
        private String _value;
        private long _timestamp;
        
        public RUMMetadata(String name, String value, long timestamp) {
            this._name = name;
            this._value = value;
            this._timestamp = timestamp;
        }
        
        public String getName() {
            return this._name;
        }
        
        public String getValue() {
            return this._value;
        }
        
        public long getTimestamp() {
            return this._timestamp;
        }
        
        @Override
        public ReportHandler createReportHandler(String requestSignature) {
            return new RUMMetadataReportHandler(this, requestSignature);
        }
        
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "RUMMetadata (%s, %s, %d)",
                    this._name, this._value, this._timestamp);
        }
    }
    
    class RUMEvent implements IRUMObject {
        private String _eventName;
        private long _timestamp;
        private long _tag;
        
        public RUMEvent(String eventName, long tag, long timestamp) {
            this._eventName = eventName;
            this._timestamp = timestamp;
            this._tag = tag;
        }
        
        public String getEventName() {
            return this._eventName;
        }
        
        public long getTag() {
            return this._tag;
        }
        
        public long getTimestamp() {
            return this._timestamp;
        }
        
        @Override
        public ReportHandler createReportHandler(String requestSignature) {
            return new RUMEventReportHandler(this, requestSignature);
        }
        
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "RUMEvent (%s, %d, %d)",
                    this._eventName, this._tag, this._timestamp);
        }
    }
    
    abstract class RUMReportHandler<T> extends ReportHandler {
        private T _object;
        
        public RUMReportHandler(T object, String requestSignature) {
            super(_reportHost, requestSignature);
            this._object = object;
        }
        
        protected T getObject() {
            return this._object;
        }
    }
    
    class RUMEventReportHandler extends RUMReportHandler<RUMEvent> {
        
        public RUMEventReportHandler(RUMEvent event, String requestSignaure) {
            super(event, requestSignaure);
        }
        
        @Override
        public List<String> getReportElements() {
            List<String> result = new ArrayList<String>();
            result.add("r1");
            result.add(String.format("%d", 0)); // until we support report id
            result.add(this.getObject().getEventName());
            result.add(String.format("%d", this.getObject().getTag()));
            result.add(String.format("%d", this.getObject().getTimestamp()));
            result.add(this.getRequestSignature());
            return result;
        }
    }
    
    class RUMMetadataReportHandler extends RUMReportHandler<RUMMetadata> {
        
        public RUMMetadataReportHandler(RUMMetadata metadata, String requestSignature) {
            super(metadata, requestSignature);
        }
        
        @Override
        public List<String> getReportElements() {
            List<String> result = new ArrayList<String>();
            result.add("r2");
            result.add(String.format("%d", 0)); // until we support report id
            result.add(this.getObject().getName());
            result.add(this.getObject().getValue());
            result.add(String.format("%d", this.getObject().getTimestamp()));
            result.add(this.getRequestSignature());
            return result;
        }
    }
    
    class RUMSlice implements IRUMObject {
        private String _sliceName;
        private long _timestamp;
        private boolean _start;
        
        public RUMSlice(String sliceName, long timestamp, boolean isStartOfSlice) {
            this._sliceName = sliceName;
            this._timestamp = timestamp;
            this._start = isStartOfSlice;
        }
        
        public String getSliceName() {
            return this._sliceName;
        }
        
        public long getTimestamp() {
            return this._timestamp;
        }
        
        public boolean isStartOfSlice() {
            return this._start;
        }
        
        @Override
        public ReportHandler createReportHandler(String requestSignature) {
            return new RUMSliceReportHandler(this, requestSignature);
        }
        
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "RUMSlice (%s, %d, %b)",
                this._sliceName, this._timestamp, this._start);
        }
    }
    
    class RUMSliceReportHandler extends RUMReportHandler<RUMSlice> {
        
        public RUMSliceReportHandler(RUMSlice slice, String requestSignature) {
            super(slice, requestSignature);
        }
        
        @Override
        public List<String> getReportElements() {
            List<String> result = new ArrayList<String>();
            result.add("r3");
            result.add(this.getObject().getSliceName());
            result.add(String.format("%d", this.getObject().isStartOfSlice() ? 1 : 0));
            result.add(String.format("%d", this.getObject().getTimestamp()));
            result.add(this.getRequestSignature());
            return result;
        }
    }
}
