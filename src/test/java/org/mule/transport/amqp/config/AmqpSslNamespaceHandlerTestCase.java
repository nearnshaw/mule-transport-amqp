/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.amqp.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.amqp.AmqpConnector;

public class AmqpSslNamespaceHandlerTestCase extends FunctionalTestCase
{
    public AmqpSslNamespaceHandlerTestCase()
    {
        super();
        setStartContext(false);
        setDisposeContextPerClass(true);
    }

    @Override
    protected String getConfigResources()
    {
        return "amqp-ssl-namespace-config.xml";
    }

    @Test
    public void testDefaultSslProtocol() throws Exception
    {
        final AmqpConnector c = (AmqpConnector) muleContext.getRegistry().lookupConnector(
            "amqpDefaultSslConnector");

        assertEquals("SSLv3", c.getSslProtocol());
        assertNull(c.getSslTrustManager());
    }

    @Test
    public void testTlsConnector() throws Exception
    {
        final AmqpConnector c = (AmqpConnector) muleContext.getRegistry().lookupConnector("amqpTlsConnector");

        assertEquals("TLS", c.getSslProtocol());
        assertNull(c.getSslTrustManager());
    }

    @Test
    public void testTrustManagerConnector() throws Exception
    {
        final AmqpConnector c = (AmqpConnector) muleContext.getRegistry().lookupConnector(
            "amqpTrustManagerConnector");

        assertEquals("SSLv3", c.getSslProtocol());
        assertNotNull(c.getSslTrustManager());
    }

    @Test
    public void testTlsTrustManagerConnector() throws Exception
    {
        final AmqpConnector c = (AmqpConnector) muleContext.getRegistry().lookupConnector(
            "amqpTlsTrustManagerConnector");

        assertEquals("TLS", c.getSslProtocol());
        assertNotNull(c.getSslTrustManager());
    }
}
