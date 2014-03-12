package com.cedexis.mobileradarlib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cedexis.mobileradarlib.http.RadarHttpSessionManager;
import com.cedexis.mobileradarlib.rum.RadarRUMSession;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    Radar.class,
    RadarRUMSession.class,
    RadarHttpSessionManager.class
})
public class RadarTests {

    private Radar _sut;
    private RadarRUMSession _mockRumSession;
    private RadarHttpSessionManager _mockHttpSessionMgr;
    
    @Before
    public void setUp() {
        this._mockRumSession = PowerMock.createMock(RadarRUMSession.class);
        this._mockHttpSessionMgr = PowerMock.createMock(RadarHttpSessionManager.class);
        this._sut = new Radar(this._mockRumSession, this._mockHttpSessionMgr);
    }
    
    @After
    public void tearDown() {
        this._mockRumSession = null;
        this._mockHttpSessionMgr = null;
        this._sut = null;
    }
    
    @Test
    public void testReportSetPropertyStringString() {
        this._mockRumSession.reportSetProperty("some property", "some value", 0);
        PowerMock.expectLastCall();
        PowerMock.replayAll();
        
        // Code under test
        this._sut.reportSetProperty("some property", "some value");
        
        // Assert
        PowerMock.verifyAll();
    }

    @Test
    public void testReportSetPropertyStringStringInt() {
        this._mockRumSession.reportSetProperty("some property", "some value", 123);
        PowerMock.expectLastCall();
        PowerMock.replayAll();
        
        // Code under test
        this._sut.reportSetProperty("some property", "some value", 123);
        
        // Assert
        PowerMock.verifyAll();
    }

    @Test
    public void testQueueHttpSession() {
        this._mockHttpSessionMgr.queueSession();
        PowerMock.expectLastCall();
        PowerMock.replayAll();
        
        // Code under test
        this._sut.queueHttpSession();
        
        // Assert
        PowerMock.verifyAll();
    }
}
