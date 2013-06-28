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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.lang.RandomStringUtils;
import org.mule.module.client.MuleClient;
import org.mule.util.UUID;

import com.rabbitmq.client.QueueingConsumer.Delivery;

public abstract class AbstractAmqpOutboundITCase extends AbstractAmqpITCase
{
    public AbstractAmqpOutboundITCase() throws IOException
    {
        super();
    }

    protected void dispatchTestMessageAndAssertValidReceivedMessage(final String flowName) throws Exception
    {
        final String customHeaderValue = UUID.getUUID();
        final String payload = RandomStringUtils.randomAlphanumeric(20);
        new MuleClient(muleContext).dispatch("vm://" + flowName + ".in", payload,
            Collections.singletonMap("customHeader", customHeaderValue));

        final Delivery dispatchedMessage = consumeMessageWithAmqp(getQueueName(flowName),
            getTestTimeoutSecs() * 1000L);

        assertNotNull(dispatchedMessage);
        assertEquals(payload, new String(dispatchedMessage.getBody()));
        assertEquals(customHeaderValue, dispatchedMessage.getProperties()
            .getHeaders()
            .get("customHeader")
            .toString());
    }
}
