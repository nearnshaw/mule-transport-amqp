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

import org.junit.Test;

public class AmqpsEndpointUtilTestCase
{
    @Test
    public void testGetQueueName()
    {
        assertEquals("queue",
            AmqpEndpointUtil.getQueueName("amqps://exchange/amqp-queue.queue?connector=foo"));
        assertEquals("queue", AmqpEndpointUtil.getQueueName("amqps://exchange/amqp-queue.queue"));
        assertEquals("queue", AmqpEndpointUtil.getQueueName("amqps://amqp-queue.queue?connector=foo\""));
        assertEquals("queue", AmqpEndpointUtil.getQueueName("amqps://amqp-queue.queue"));
        assertEquals("", AmqpEndpointUtil.getQueueName("amqps://exchange?connector=foo\""));
        assertEquals("", AmqpEndpointUtil.getQueueName("amqps://exchange"));
    }

    @Test
    public void testGetExchangeName()
    {
        assertEquals("exchange", AmqpEndpointUtil.getExchangeName(
            "amqps://exchange/amqp-queue.queue?connector=foo", AmqpsConnector.AMQPS));
        assertEquals("exchange",
            AmqpEndpointUtil.getExchangeName("amqps://exchange/amqp-queue.queue", AmqpsConnector.AMQPS));
        assertEquals("",
            AmqpEndpointUtil.getExchangeName("amqps://amqp-queue.queue?connector=foo", AmqpsConnector.AMQPS));
        assertEquals("", AmqpEndpointUtil.getExchangeName("amqps://amqp-queue.queue", AmqpsConnector.AMQPS));
        assertEquals("exchange",
            AmqpEndpointUtil.getExchangeName("amqps://exchange?connector=foo", AmqpsConnector.AMQPS));
        assertEquals("exchange", AmqpEndpointUtil.getExchangeName("amqps://exchange", AmqpsConnector.AMQPS));
    }
}
