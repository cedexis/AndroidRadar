package com.cedexis.mobileradarlib.rum;

import java.util.LinkedList;
import java.util.List;
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

import com.cedexis.mobileradarlib.DeviceStateChecker;
import com.cedexis.mobileradarlib.IPostReportHandler;
import com.cedexis.mobileradarlib.InitHandler;
import com.cedexis.mobileradarlib.InitResult;
import com.cedexis.mobileradarlib.ReportHandler;

public class Queueing {
    
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
    
    public Queueing(Application app,
            int zoneId,
            int customerId,
            String initHost,
            String reportHost,
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
            // The init request has already been initiated, but hasn't completed.
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