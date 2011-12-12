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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.config.MuleProperties;
import org.mule.transport.AbstractMuleMessageFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;

public class AmqpMuleMessageFactory extends AbstractMuleMessageFactory
{
    public AmqpMuleMessageFactory(final MuleContext context)
    {
        super(context);
    }

    @Override
    protected Class<?>[] getSupportedTransportMessageTypes()
    {
        return new Class[]{AmqpMessage.class};
    }

    @Override
    protected Object extractPayload(final Object transportMessage, final String encoding) throws Exception
    {
        return transportMessage;
    }

    @Override
    protected void addProperties(final DefaultMuleMessage muleMessage, final Object transportMessage)
        throws Exception
    {
        final AmqpMessage amqpMessage = (AmqpMessage) transportMessage;

        final Map<String, Object> messageProperties = new HashMap<String, Object>();
        putIfNonNull(messageProperties, AmqpConstants.CONSUMER_TAG, amqpMessage.getConsumerTag());
        addEnvelopeProperties(messageProperties, amqpMessage.getEnvelope());
        addBasicProperties(muleMessage, messageProperties, amqpMessage.getProperties());
        addHeaders(messageProperties, amqpMessage.getProperties().getHeaders());

        muleMessage.addInboundProperties(messageProperties);
    }

    private void addEnvelopeProperties(final Map<String, Object> messageProperties, final Envelope envelope)
    {
        if (envelope == null) return;

        putIfNonNull(messageProperties, AmqpConstants.DELIVERY_TAG, envelope.getDeliveryTag());
        putIfNonNull(messageProperties, AmqpConstants.REDELIVER, envelope.isRedeliver());
        putIfNonNull(messageProperties, AmqpConstants.EXCHANGE, envelope.getExchange());
        putIfNonNull(messageProperties, AmqpConstants.ROUTING_KEY, envelope.getRoutingKey());
    }

    private void addBasicProperties(final DefaultMuleMessage muleMessage,
                                    final Map<String, Object> messageProperties,
                                    final BasicProperties amqpProperties)
    {
        if (amqpProperties == null) return;

        putIfNonNull(messageProperties, AmqpConstants.APP_ID, amqpProperties.getAppId());
        putIfNonNull(messageProperties, AmqpConstants.CONTENT_ENCODING, amqpProperties.getContentEncoding());
        putIfNonNull(messageProperties, AmqpConstants.CONTENT_TYPE, amqpProperties.getContentType());

        final String correlationId = amqpProperties.getCorrelationId();
        putIfNonNull(messageProperties, AmqpConstants.CORRELATION_ID, correlationId);
        putIfNonNull(messageProperties, MuleProperties.MULE_CORRELATION_ID_PROPERTY, correlationId);
        muleMessage.setCorrelationId(correlationId);

        putIfNonNull(messageProperties, AmqpConstants.DELIVERY_MODE, amqpProperties.getDeliveryMode());
        putIfNonNull(messageProperties, AmqpConstants.EXPIRATION, amqpProperties.getExpiration());

        final String messageId = amqpProperties.getMessageId();
        putIfNonNull(messageProperties, AmqpConstants.MESSAGE_ID, messageId);
        putIfNonNull(messageProperties, MuleProperties.MULE_MESSAGE_ID_PROPERTY, messageId);
        muleMessage.setUniqueId(messageId);

        putIfNonNull(messageProperties, AmqpConstants.PRIORITY, amqpProperties.getPriority());

        final String replyTo = amqpProperties.getReplyTo();
        putIfNonNull(messageProperties, AmqpConstants.REPLY_TO, replyTo);
        muleMessage.setReplyTo(replyTo);

        putIfNonNull(messageProperties, AmqpConstants.TIMESTAMP, amqpProperties.getTimestamp());
        putIfNonNull(messageProperties, AmqpConstants.TYPE, amqpProperties.getType());
        putIfNonNull(messageProperties, AmqpConstants.USER_ID, amqpProperties.getUserId());
    }

    private void addHeaders(final Map<String, Object> messageProperties, final Map<String, Object> headers)
    {
        for (final Entry<String, Object> header : headers.entrySet())
        {
            putIfNonNull(messageProperties, header.getKey(), header.getValue());
        }
    }

    private void putIfNonNull(final Map<String, Object> messageProperties,
                              final String key,
                              final Object value)
    {
        if (value == null)
        {
            return;
        }

        if (value instanceof LongString)
        {
            final String stringValue = new String(((LongString) value).getBytes(),
                AmqpConstants.LONG_STRING_CHARSET);
            messageProperties.put(key, stringValue);
        }
        else
        {
            messageProperties.put(key, value);
        }
    }

}
