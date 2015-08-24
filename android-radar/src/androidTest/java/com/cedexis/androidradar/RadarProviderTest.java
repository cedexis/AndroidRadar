package com.cedexis.androidradar;

import android.test.AndroidTestCase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jrosas on 8/24/15.
 */
public class RadarProviderTest extends AndroidTestCase {
    public void testMakeProvidersRequestUrl_SimpleValues_ReturnsCorrectUrl() {
        String expectedPattern =
                "http://radar.cedexis.com/1/10660/radar/\\d+/[^/]+/providers.json\\?imagesok=1&t=1";
        String protocol = "http";
        RadarSessionProperties properties = new RadarSessionProperties(1, 10660);
        String url = RadarProvider.makeProvidersRequestUrl(protocol, properties);
        assertTrue("Should match the regex pattern", url.matches(expectedPattern));
    }

    public void testConstructor_WithInvalidJson_ThrowsExeption() throws JSONException {
        JSONObject invalidProviderObject = new JSONObject("{ \"not\": \"valid\" }");
        Boolean exceptionThrown = false;
        try {
            RadarProvider provider = new RadarProvider(invalidProviderObject, null);
        } catch (JSONException e) {
            assertNotNull(e);
            exceptionThrown = true;
        }
        assertTrue("Should throw an exception", exceptionThrown);
    }

    public void testConstructor_WithValidJson_ReturnsAnInstanceWithExpectedValues() throws JSONException {
        JSONObject validProviderObject = new JSONObject("{\"a\":true,\"b\":1,\"p\":{\"z\":3,\"c\":55,\"i\":418,\"p\":{\"a\":{\"a\":{\"u\":\"http:\\/\\/global2.cmdolb.com\\/ops\\/akamai\\/images\\/r20.gif\",\"t\":2},\"b\":{\"u\":\"http:\\/\\/global2.cmdolb.com\\/ops\\/akamai\\/images\\/r20.gif\",\"t\":2},\"c\":{\"u\":\"http:\\/\\/global2.cmdolb.com\\/ops\\/akamai\\/images\\/r20-100KB.png\",\"t\":2}}}}}");
        RadarProvider provider = null;
        try {
            provider = new RadarProvider(validProviderObject, null);
        } catch (JSONException e) {
            assertNull("Should not throw error", e);
        }
        assertNotNull("Should not return null", provider);
        assertEquals("Should initialize the ownerZoneId", provider.getOwnerZoneId(), 3);
        assertEquals("Should initialize the ownerCustomerId", provider.getOwnerCustomerId(), 55);
        assertEquals("Should initialize the providerId", provider.getProviderId(), 418);
        assertEquals("Should populate the probes", provider._probes.length, 3);
        assertSame("Should have a reference to the provider in the probe", provider._probes[0].getProvider(), provider);
    }
}
