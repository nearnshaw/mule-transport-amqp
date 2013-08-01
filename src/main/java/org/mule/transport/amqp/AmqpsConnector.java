/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.amqp;

import java.security.GeneralSecurityException;

import javax.net.ssl.TrustManager;

import org.mule.api.MuleContext;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.util.StringUtils;

import com.rabbitmq.client.ConnectionFactory;
import com.thoughtworks.xstream.InitializationException;

/**
 * Connects to a particular virtual host on a particular AMQP broker.
 */
public class AmqpsConnector extends AmqpConnector
{
    public static final String AMQPS = "amqps";

    // SSL support
    private String sslProtocol;
    private TrustManager sslTrustManager;

    public AmqpsConnector(final MuleContext muleContext)
    {
        super(muleContext);

        setPort(ConnectionFactory.DEFAULT_AMQP_OVER_SSL_PORT);
    }

    @Override
    public String getProtocol()
    {
        return AMQPS;
    }

    @Override
    public void doInitialise() throws InitialisationException
    {
        super.doInitialise();

        try
        {
            if (StringUtils.isNotBlank(sslProtocol))
            {
                if (sslTrustManager == null)
                {
                    getConnectionFactory().useSslProtocol(sslProtocol);
                }
                else
                {
                    getConnectionFactory().useSslProtocol(sslProtocol, sslTrustManager);
                }
            }
        }
        catch (final GeneralSecurityException gse)
        {
            throw new InitializationException("Failed to configure SSL", gse);
        }
    }

    public String getSslProtocol()
    {
        return sslProtocol;
    }

    public void setSslProtocol(final String sslProtocol)
    {
        this.sslProtocol = sslProtocol;
    }

    public TrustManager getSslTrustManager()
    {
        return sslTrustManager;
    }

    public void setSslTrustManager(final TrustManager sslTrustManager)
    {
        this.sslTrustManager = sslTrustManager;
    }
}
