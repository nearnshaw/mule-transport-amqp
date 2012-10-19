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

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleRuntimeException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.execution.ExecutionCallback;
import org.mule.api.execution.ExecutionTemplate;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.lifecycle.StartException;
import org.mule.api.transport.Connector;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.amqp.AmqpConnector.InboundConnection;
import org.mule.transport.amqp.AmqpConstants.AckMode;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * The <code>AmqpMessageReceiver</code> subscribes to a queue and dispatches received
 * messages to Mule.
 */
public class AmqpMessageReceiver extends AbstractMessageReceiver
{
    protected final AmqpConnector amqpConnector;
    protected InboundConnection inboundConnection;
    protected String consumerTag;

    public AmqpMessageReceiver(final Connector connector,
                               final FlowConstruct flowConstruct,
                               final InboundEndpoint endpoint) throws CreateException
    {
        super(connector, flowConstruct, endpoint);
        this.amqpConnector = (AmqpConnector) connector;
    }

    @Override
    public void doStart() throws MuleException
    {
        inboundConnection = amqpConnector.connect(this);

        if (logger.isDebugEnabled())
        {
            logger.debug("Connected queue: " + getQueueName() + " on channel: " + getChannel());
        }

        try
        {
            if (inboundConnection == null)
            {
                doConnect();
            }

            if (endpoint.getTransactionConfig().isTransacted())
            {
                getChannel().txSelect();
            }

            consumerTag = getChannel().basicConsume(getQueueName(), amqpConnector.getAckMode().isAutoAck(),
                getClientConsumerTag(), amqpConnector.isNoLocal(), amqpConnector.isExclusiveConsumers(),
                null, new AmqpConsumer(getChannel()));

            logger.info("Started subscription: " + consumerTag + " on channel: " + getChannel());
        }
        catch (final Exception e)
        {
            throw new StartException(MessageFactory.createStaticMessage("Error when subscribing to queue: "
                                                                        + getQueueName() + " on channel: "
                                                                        + getChannel()), e, this);
        }
    }

    @Override
    public void doStop()
    {
        Channel channel = null;
        try
        {
            channel = getChannel();
            if (channel == null)
            {
                return;
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Cancelling subscription of: " + consumerTag + " on channel: " + channel);
            }

            channel.basicCancel(consumerTag);

            logger.info("Cancelled subscription of: " + consumerTag + " on channel: " + channel);

            if (logger.isDebugEnabled())
            {
                logger.debug("Disconnecting: queue: " + getQueueName() + " from channel: " + channel);
            }

            amqpConnector.closeChannel(channel);
        }
        catch (final Exception e)
        {
            logger.warn(
                MessageFactory.createStaticMessage("Failed to cancel subscription: " + consumerTag
                                                   + " on channel: " + channel), e);
        }
        finally
        {
            inboundConnection = null;
        }
    }

    protected Channel getChannel()
    {
        return inboundConnection == null ? null : inboundConnection.getChannel();
    }

    protected String getQueueName()
    {
        return inboundConnection == null ? null : inboundConnection.getQueue();
    }

    protected String getClientConsumerTag()
    {
        return AmqpEndpointUtil.getConsumerTag(getEndpoint());
    }

    private void deliverAmqpMessage(final AmqpMessage amqpMessage)
    {
        final AmqpMessageRouterWork work = new AmqpMessageRouterWork(getChannel(), amqpMessage);

        try
        {
            // deliver message in a different thread to free the Amqp Connector's
            // thread
            getWorkManager().scheduleWork(work);
        }
        catch (final WorkException we)
        {
            throw new MuleRuntimeException(MessageFactory.createStaticMessage("Failed to deliver: "
                                                                              + amqpMessage), we);
        }
    }

    public final class AmqpConsumer extends DefaultConsumer
    {
        public AmqpConsumer(final Channel channel)
        {
            super(channel);
        }

        @Override
        public void handleDelivery(final String consumerTag,
                                   final Envelope envelope,
                                   final AMQP.BasicProperties properties,
                                   final byte[] body) throws IOException
        {
            final AmqpMessage amqpMessage = new AmqpMessage(consumerTag, envelope, properties, body);

            if (logger.isDebugEnabled())
            {
                logger.debug("Received: " + amqpMessage);
            }

            deliverAmqpMessage(amqpMessage);
        }
    }

    private final class AmqpMessageRouterWork implements Work
    {
        private final Log logger = LogFactory.getLog(AmqpMessageRouterWork.class);
        private final Channel channel;
        private final AmqpMessage amqpMessage;

        private AmqpMessageRouterWork(final Channel channel, final AmqpMessage amqpMessage)
        {
            this.channel = channel;
            this.amqpMessage = amqpMessage;
        }

        public void run()
        {
            try
            {
                final MuleMessage muleMessage = createMuleMessage(amqpMessage);

                if ((getEndpoint().getExchangePattern() == MessageExchangePattern.REQUEST_RESPONSE)
                    && (muleMessage.getReplyTo() == null))
                {
                    logger.warn(String.format(
                        "Impossible to honor the request-response exchange pattern of %s for AMQP message without reply to: %s",
                        getEndpoint(), muleMessage));
                }

                if (amqpConnector.getAckMode() == AckMode.MANUAL)
                {
                    // in manual AckMode, the channel will be needed to ack the
                    // message
                    muleMessage.setProperty(AmqpConstants.CHANNEL, channel, PropertyScope.INVOCATION);
                }

                if (endpoint.getTransactionConfig().isTransacted())
                {
                    final ExecutionTemplate<MuleEvent> executionTemplate = createExecutionTemplate();
                    final ExecutionCallback<MuleEvent> processingCallback = new ExecutionCallback<MuleEvent>()
                    {
                        public MuleEvent process() throws Exception
                        {
                            routeMessage(muleMessage);
                            return null;
                        }
                    };
                    executionTemplate.execute(processingCallback);
                }
                else
                {
                    try
                    {
                        routeMessage(muleMessage);
                    }
                    finally
                    {
                        amqpConnector.ackMessageIfNecessary(channel, amqpMessage);
                    }
                }
            }
            catch (final Exception e)
            {
                logger.error("Impossible to route: " + amqpMessage, e);
            }

        }

        public void release()
        {
            // NOOP
        }
    }
}
