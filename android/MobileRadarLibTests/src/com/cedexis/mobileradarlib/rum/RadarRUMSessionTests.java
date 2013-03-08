package com.cedexis.mobileradarlib.rum;

import static org.junit.Assert.fail;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cedexis.mobileradarlib.rum.RadarRUMSession.RUMEvent;

import android.app.Application;
import android.util.Log;

@PrepareForTest({
    Log.class,
    RadarRUMSession.Queueing.class
})
@RunWith(PowerMockRunner.class)
public class RadarRUMSessionTests {
    
    Application _mockApp;
    
    @Before
    public void setUp() {
        PowerMock.mockStatic(Log.class);
        this._mockApp = EasyMock.createMock(Application.class);
    }
    
    @Ignore
    @Test
    public void testRadarRUMSessionApplicationLongIntInt() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testRadarRUMSessionApplicationLongIntIntStringString() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testReportSliceStart() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testReportSliceEnd() {
        fail("Not yet implemented");
    }

    @Test
    public void testReportEventString() {
        EasyMock.expect(Log.d(
                EasyMock.eq("RadarRUMSession"),
                EasyMock.isA(String.class)))
                    .andReturn(0);
        RadarRUMSession.Queueing queueing =
                PowerMock.createMock(RadarRUMSession.Queueing.class);
        queueing.reportRUMObject(
                new RUMEvent(
                        2,
                        "some event",
                        0,
                        EasyMock.anyLong()));
        EasyMock.expectLastCall();
        PowerMock.replayAll();
        
        // Code under test
        RadarRUMSession sut = new RadarRUMSession(queueing);
        sut.reportEvent("some event");
        
        // Assertions
        PowerMock.verifyAll();
    }

    @Ignore
    @Test
    public void testReportEventStringLong() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testReportSetProperty() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testToString() {
        fail("Not yet implemented");
    }
}
