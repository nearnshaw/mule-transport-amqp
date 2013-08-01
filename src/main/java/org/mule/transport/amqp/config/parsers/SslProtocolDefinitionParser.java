/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.amqp.config.parsers;

import org.mule.config.spring.parsers.generic.ParentDefinitionParser;

/**
 * Simple configuration of the SSL protocol.
 */
public class SslProtocolDefinitionParser extends ParentDefinitionParser
{
    public SslProtocolDefinitionParser()
    {
        addAlias("protocol", "sslProtocol");
        addAlias("trustManager", "sslTrustManager");
    }
}
