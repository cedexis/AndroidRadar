package com.cedexis.mobileradarlib.rum;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cedexis.mobileradarlib.rum.RadarRUMSession.RUMEvent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    RadarRUMSession.class,
    Queueing.class,
    RUMEvent.class
})
public class RadarRUMSessionTests {
    
    private RadarRUMSession _sut;
    private Queueing _mockQueueing; 
    
    @Before
    public void setUp() {
        this._mockQueueing = PowerMock.createMock(Queueing.class);
        this._sut = new RadarRUMSession(this._mockQueueing);
    }
    
    @After
    public void tearDown() {
        this._mockQueueing = null;
        this._sut = null;
    }
    
    @Test
    public void testReportEvent() throws Exception {
        RUMEvent mockEvent = PowerMock.createMockAndExpectNew(
                RUMEvent.class,
                1,
                "some event",
                123L,
                456L);
        this._mockQueueing.reportRUMObject(mockEvent);
        PowerMock.replayAll();
        
        // Code under test
        assertEquals(1, this._sut.reportEvent("some event", 123, 456));
        
        // Assert
        PowerMock.verifyAll();
    }
}
