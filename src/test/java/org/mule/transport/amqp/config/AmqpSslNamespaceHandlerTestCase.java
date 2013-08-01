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

import org.junit.Ignore;
import org.junit.Test;
import org.mule.transport.amqp.AmqpSslConnector;

@Ignore("reactivate and fix")
public class AmqpSslNamespaceHandlerTestCase extends AbstractAmqpNamespaceHandlerTestCase
{
    public AmqpSslNamespaceHandlerTestCase()
    {
        super();
    }

    @Override
    protected String getConfigResources()
    {
        return "amqp-ssl-namespace-config.xml";
    }

    @Override
    protected String getProtocol()
    {
        return AmqpSslConnector.AMQPS;
    }

    @Test
    public void testDefaultSslProtocol() throws Exception
    {
        final AmqpSslConnector c = (AmqpSslConnector) muleContext.getRegistry().lookupConnector(
            "amqpsDefaultSslConnector");

        assertEquals("SSLv3", c.getSslProtocol());
        assertNull(c.getSslTrustManager());
    }

    @Test
    public void testTlsConnector() throws Exception
    {
        final AmqpSslConnector c = (AmqpSslConnector) muleContext.getRegistry().lookupConnector(
            "amqpsTlsConnector");

        assertEquals("TLS", c.getSslProtocol());
        assertNull(c.getSslTrustManager());
    }

    @Test
    public void testTrustManagerConnector() throws Exception
    {
        final AmqpSslConnector c = (AmqpSslConnector) muleContext.getRegistry().lookupConnector(
            "amqpsTrustManagerConnector");

        assertEquals("SSLv3", c.getSslProtocol());
        assertNotNull(c.getSslTrustManager());
    }

    @Test
    public void testTlsTrustManagerConnector() throws Exception
    {
        final AmqpSslConnector c = (AmqpSslConnector) muleContext.getRegistry().lookupConnector(
            "amqpsTlsTrustManagerConnector");

        assertEquals("TLS", c.getSslProtocol());
        assertNotNull(c.getSslTrustManager());
    }
}
