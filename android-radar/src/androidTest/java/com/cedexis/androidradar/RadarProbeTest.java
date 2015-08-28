package com.cedexis.androidradar;

import android.test.AndroidTestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;

/**
 * Created by jrosas on 8/24/15.
 */
public class RadarProbeTest extends AndroidTestCase {

    public void testMakeUrl_SimpleValues_ReturnsCorrectUrl() throws JSONException, RadarClientException {
        String expectedUrlRegex = "http://global2\\.cmdolb\\.com/ops/akamai/images/r20.gif\\?rnd=1-1-10660-0-0-409-[\\d]+-A4KkaMfUeamyacifcaeqPfmOGjohHqqWT@RURGu4UoRURGvaYVQ1Gq9kfaGben8bgmf2iiQaGmaekiwaGkaeul2n6WDAeaGbejqbgk9TiaaO3yUaOarGagOtyNv0Dg9UmI5ZzweUAhyUChjVzaaa";
        RadarSessionProperties sessionProperties = new RadarSessionProperties(1, 10660);
        RadarSession session = new RadarSession(
                1 + new SecureRandom().nextInt(Integer.MAX_VALUE),
                System.currentTimeMillis() / 1000L,
                sessionProperties,
                null,
                null
        );
        session.setRequestSignature("A4KkaMfUeamyacifcaeqPfmOGjohHqqWT@RURGu4UoRURGvaYVQ1Gq9kfaGben8bgmf2iiQaGmaekiwaGkaeul2n6WDAeaGbejqbgk9TiaaO3yUaOarGagOtyNv0Dg9UmI5ZzweUAhyUChjVzaaa");
        JSONObject providerData = new JSONObject("{\"a\":true,\"b\":1,\"p\":{\"z\":0,\"c\":0,\"i\":409,\"p\":{\"a\":{\"a\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"b\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"c\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20-100KB.png\",\"t\":2}}}}}");
        RadarProvider provider = new RadarProvider(providerData, session);
        RadarProbe probe = provider.getProbes()[0];
        String url = probe.makeUrl();
        assertNotNull(url);
        assertTrue("Should match expected URL", url.matches(expectedUrlRegex));
    }

    public void testMakeReportUrl_SimpleValues_ReturnsCorrectUrl() throws JSONException, RadarClientException {
        String expectedUrl = "http://rpt.cedexis.com/f1/A4KkaMfUeamyacifcaeqPfmOGjohHqqWT@RURGu4UoRURGvaYVQ1Gq9kfaGben8bgmf2iiQaGmaekiwaGkaeul2n6WDAeaGbejqbgk9TiaaO3yUaOarGagOtyNv0Dg9UmI5ZzweUAhyUChjVzaaa/0/0/409/1/1/400";
        RadarSessionProperties sessionProperties = new RadarSessionProperties(1, 10660);
        RadarSession session = new RadarSession(
                1 + new SecureRandom().nextInt(Integer.MAX_VALUE),
                System.currentTimeMillis() / 1000L,
                sessionProperties,
                null,
                null
        );
        session.setRequestSignature("A4KkaMfUeamyacifcaeqPfmOGjohHqqWT@RURGu4UoRURGvaYVQ1Gq9kfaGben8bgmf2iiQaGmaekiwaGkaeul2n6WDAeaGbejqbgk9TiaaO3yUaOarGagOtyNv0Dg9UmI5ZzweUAhyUChjVzaaa");
        JSONObject providerData = new JSONObject("{\"a\":true,\"b\":1,\"p\":{\"z\":0,\"c\":0,\"i\":409,\"p\":{\"a\":{\"a\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"b\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"c\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20-100KB.png\",\"t\":2}}}}}");
        RadarProvider provider = new RadarProvider(providerData, session);
        RadarProbe probe = provider.getProbes()[0];
        String url = probe.makeReportUrl(400, 1);
        assertEquals("Should match expected URL", url, expectedUrl);
    }

    public void testCalculateThroughput_SimpleValues_ReturnsResult() throws JSONException {
        long expectedValue = 1600;
        RadarSessionProperties sessionProperties = new RadarSessionProperties(1, 10660);
        RadarSession session = new RadarSession(
                1 + new SecureRandom().nextInt(Integer.MAX_VALUE),
                System.currentTimeMillis() / 1000L,
                sessionProperties,
                null,
                null
        );
        JSONObject providerData = new JSONObject("{\"a\":true,\"b\":1,\"p\":{\"z\":0,\"c\":0,\"i\":409,\"p\":{\"a\":{\"a\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"b\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"c\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20-100KB.png\",\"t\":2}}}}}");
        RadarProvider provider = new RadarProvider(providerData, session);
        RadarProbe probe = provider.getProbes()[2];
        long throughput = probe.calculateThroughput(500);
        assertEquals("Calculated throughput should equals expected value", throughput, expectedValue);
    }

    public void testConstructor_SimpleValues_SetsFieldsWithValues() throws JSONException {
        RadarSessionProperties sessionProperties = new RadarSessionProperties(1, 10660);
        RadarSession session = new RadarSession(
                1 + new SecureRandom().nextInt(Integer.MAX_VALUE),
                System.currentTimeMillis() / 1000L,
                sessionProperties,
                null,
                null
        );
        session.setRequestSignature("A4KkaMfUeamyacifcaeqPfmOGjohHqqWT@RURGu4UoRURGvaYVQ1Gq9kfaGben8bgmf2iiQaGmaekiwaGkaeul2n6WDAeaGbejqbgk9TiaaO3yUaOarGagOtyNv0Dg9UmI5ZzweUAhyUChjVzaaa");
        JSONObject providerData = new JSONObject("{\"a\":true,\"b\":1,\"p\":{\"z\":0,\"c\":0,\"i\":409,\"p\":{\"a\":{\"a\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"b\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20.gif\",\"t\":2},\"c\":{\"u\":\"http://global2.cmdolb.com/ops/akamai/images/r20-100KB.png\",\"t\":2}}}}}");
        JSONObject probeData = providerData.getJSONObject("p").getJSONObject("p").getJSONObject("a").getJSONObject("c");
        RadarProvider provider = new RadarProvider(providerData, session);

        RadarProbe probe = new RadarProbe(session, probeData, ProbeType.THROUGHPUT, provider);
        assertSame(probe.getSession(), session);
        assertEquals(probe.getProbeType(), ProbeType.THROUGHPUT);
        assertEquals(probe.getProvider(), provider);
        assertEquals(probe.getBaseUrl(), "http://global2.cmdolb.com/ops/akamai/images/r20-100KB.png");
        assertEquals(probe.getObjectType(), 2);
    }
}
