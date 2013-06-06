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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.model.SessionException;
import org.mule.api.processor.MessageProcessor;
import org.mule.config.i18n.MessageFactory;

import com.rabbitmq.client.Channel;

/**
 * Used to manually perform a basic recover of the current channel.
 */
public class AmqpRecover implements MessageProcessor
{
    private final static Log LOG = LogFactory.getLog(AmqpRecover.class);

    protected boolean requeue = false;

    public MuleEvent process(final MuleEvent event) throws MuleException
    {
        recover(event, requeue);
        return event;
    }

    public void setRequeue(final boolean requeue)
    {
        this.requeue = requeue;
    }

    private static void recover(final MuleEvent event, final boolean requeue) throws SessionException
    {
        final MuleMessage message = event.getMessage();
        final Channel channel = AmqpConnector.getChannelFromMessage(message);

        if (channel == null)
        {
            throw new SessionException(
                MessageFactory.createStaticMessage("No " + AmqpConstants.CHANNEL
                                                   + " session property found, impossible to recover message: "
                                                   + message));
        }

        try
        {
            channel.basicRecover(requeue);
        }
        catch (final IOException ioe)
        {
            throw new SessionException(MessageFactory.createStaticMessage("Failed to recover channel: "
                                                                          + channel), ioe);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Manually recovered channel: " + channel);
        }
    }
}
