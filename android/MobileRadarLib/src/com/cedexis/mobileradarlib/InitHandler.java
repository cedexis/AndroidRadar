package com.cedexis.mobileradarlib;

import java.util.concurrent.Callable;

interface IInitHandler extends Callable<InitResult> {
}

public class InitHandler implements IInitHandler {
    
    private int _zoneId;
    private int _customerId;
    private String _initHost;
    
    public InitHandler(int zoneId, int customerId, String initHost) {
        this._zoneId = zoneId;
        this._customerId = customerId;
        this._initHost = initHost;
    }
    
    @Override
    public InitResult call() {
        return InitResult.doInit(this._zoneId, this._customerId, this._initHost);
    }
}