Mule AMQP Transport - Plug-in Mule Studio
=========================================

Getting the update site
-----------------------

To compile the plug-in for Mule Studio, first execute
 > mvn clean install
in the parent project (AMQP Transport).

Next execute
 > mvn clean install
under this project. The update site can be found in: org.mule.tooling.amqp.updatesite/target/repository

And for last, inside studio go to: help -> Install New Software -> Available Software Sites -> Add, and add the folder of the repository.
After this, you can install the plug-in in Studio in: help -> Install New Software, selecting the working site name used in the previus step.

Queues, Exchanges and bindings - Endpoint initialization
--------------------------------------------------------
 A. When you create an outboud-endpoint
  1. If you don't provide an exchange name it will use the default exchange name
  2. If you don't provide an exchange type it will only assure that the exchange exists
  2.b. If you provide an exchange, it will try to create the exchange

 B. When you create an inbound-endpoint
  1. It will execute all the steps from A
  2. If you provide a routingKey, an exchange name must be also provided
  3. If Queue name is empty, it will create a private queue and bind it to the exchange
  4. If some of the flags [ queue_durable | queue_auto_delete | queue_exclusive ] are true, it will create a Queue and bind it to the exchange
  4.b. If none of the flags are true, it will check if the queue exists

Consumers bind to a Queue
-------------------------
 Since AMQP is a transport and for that it sticks to the inbound/outbound endpoint rules of mule, it can't be two different consumers pointing to the same Queue. This is because when an endpoint is started, the endpointUri must be a Unique Key.
 The endpointUri of an AMQP Inbound-Endpoint is conformed as: amqp://{exchange-name}/amqp-queue.{queue-name}. So, using two different endpoints pointing to the same exchange and queue will result in a duplicate endpointUri.