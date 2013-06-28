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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AmqpExceptionStrategyITCase extends AbstractAmqpInboundITCase
{
    public AmqpExceptionStrategyITCase() throws Exception
    {
        super();
        // create/delete the required pre-existing exchanges and queues
        setupExchangeAndQueue("amqpRejectingExceptionStrategy");
    }

    @Override
    protected String getConfigResources()
    {
        return "exception-strategy-tests-config.xml";
    }

    @Test
    public void testRejectingExceptionStrategy() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpRejectingExceptionStrategy");

        // check the message has been successfully pushed back to the queue
        assertNotNull(consumeMessageWithAmqp(getQueueName("amqpRejectingExceptionStrategy"),
            getTestTimeoutSecs()));
    }
}
