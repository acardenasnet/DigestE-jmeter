package net.acardenas.jmeter.plugins.auth.digeste;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * User: acardenas
 */
public class DigesteSchemaTest
{
    private DigesteSchema digesteSchema;

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
}