package net.acardenas.jmeter.plugins.auth.digeste;

import static junit.framework.Assert.assertEquals;

import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * User: acardenas
 */
public class DigesteSchemaTest
{
    private DigesteSchema digesteSchema;

    @Mock
    private HttpRequest httpRequest;

    @Before
    public void before()
    {
        digesteSchema = new DigesteSchema();
    }

    @Test
    public void getSchemeName()
    {
        String expectedSchemaName = "digeste";
        assertEquals(expectedSchemaName, digesteSchema.getSchemeName());
        return;
    }

    @Test
    public void authenticate() throws AuthenticationException
    {
        String expectedSchemaName = "digeste";
        UsernamePasswordCredentials myCredentials = new UsernamePasswordCredentials(
                "Username",
                "Secret");
        digesteSchema.authenticate(myCredentials, httpRequest);
        return;
    }

}