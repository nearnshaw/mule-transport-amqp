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

import org.mule.api.MuleContext;
import org.mule.api.transaction.TransactionException;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.MessageFactory;
import org.mule.transaction.AbstractSingleResourceTransaction;
import org.mule.transaction.IllegalTransactionStateException;

import com.rabbitmq.client.Channel;

/**
 * {@link AmqpTransaction} is a wrapper for an AMQP local transaction. This object
 * holds the AMQP channel and controls when the transaction is committed or rolled
 * back.
 */
public class AmqpTransaction extends AbstractSingleResourceTransaction
{
    public AmqpTransaction(final MuleContext muleContext)
    {
        super(muleContext);
    }

    @Override
    public void bindResource(final Object key, final Object resource) throws TransactionException
    {
        if (!(resource instanceof Channel))
        {
            throw new IllegalTransactionStateException(
                CoreMessages.transactionCanOnlyBindToResources(Channel.class.getName()));
        }

        super.bindResource(key, resource);

        // now that we have a channel, we can actually begin the transaction
        begin();
    }

    @Override
    protected void doBegin() throws TransactionException
    {
        if (resource == null)
        {
            started.set(false);
            return;
        }

        try
        {
            ((Channel) resource).txSelect();
        }
        catch (final IOException ioe)
        {
            throw new TransactionException(MessageFactory.createStaticMessage("Failed to begin transaction"),
                ioe);
        }
    }

    @Override
    protected void doCommit() throws TransactionException
    {
        if (resource == null)
        {
            logger.warn(CoreMessages.commitTxButNoResource(this));
            return;
        }

        try
        {
            ((Channel) resource).txCommit();
        }
        catch (final IOException ioe)
        {
            throw new TransactionException(CoreMessages.transactionCommitFailed(), ioe);
        }
    }

    @Override
    protected void doRollback() throws TransactionException
    {
        if (resource == null)
        {
            logger.warn(CoreMessages.rollbackTxButNoResource(this));
            return;
        }

        try
        {
            ((Channel) resource).txRollback();
        }
        catch (final IOException ioe)
        {
            throw new TransactionException(CoreMessages.transactionRollbackFailed(), ioe);
        }
    }
}
