package net.acardenas.jmeter.plugins.auth.digeste;

import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.Entry;
import org.junit.Before;
import org.junit.Test;

/**
 * User: acardenas
 */
public class DigesteSamplerTest
{
    private HTTPSampler sampler;

    public DigesteSamplerTest()
    {
        // empty
    }

    @Before
    public void before()
    {
        sampler = new DigesteSampler();
    }

    @Test
    public void test()
    {
        sampler.setDomain("localhost");
        sampler.setPort(8088);
        sampler.setPath("/ws/v1.0/authenticate");
        sampler.sample();
    }
}